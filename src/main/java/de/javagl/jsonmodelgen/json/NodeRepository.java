/*
 * JsonModelGen - Model Generation from JSON Schema 
 *
 * Copyright (c) 2015-2016 Marco Hutter - http://www.javagl.de
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package de.javagl.jsonmodelgen.json;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * A repository that maps URIs to JSON nodes. The repository processes
 * a URI and the JSON node that was parsed from this URI in parallel.
 * During this process, it builds a mapping from URIs to JSON nodes,
 * resolving references, so that multiple equivalent URIs are mapped 
 * to the same node instance. For example:
 * <pre><code>
 * Root URI:
 *     file:/C:/root.json    
 * Root node:
 *     root { "extends" : { "$ref" : "extended.json" } }
 * 
 * After processing, the URIs
 *     file:/C:/root.json#/extends
 *     file:/C:/extends.json
 * will both map to the same node, which was parsed from "extended.json"
 * </code></pre>
 */
public class NodeRepository
{
    /**
     * The logger used in this class
     */
    private static final Logger logger = 
        Logger.getLogger(NodeRepository.class.getName());
    
    /**
     * The debug log level
     */
    private static final Level level = Level.FINE;
    
    /**
     * Logging indentation level 
     */
    private static int logIndent = 0;

    /**
     * Logging utility method
     * 
     * @param s The log message
     */
    private static void log(String s)
    {
        if (logger.isLoggable(level))
        {
            String indent = "";
            for (int i=0; i<logIndent; i++)
            {
                indent += "  ";
            }
            logger.log(level, indent+s);
        }
    }
    
    
    
    /**
     * The mapping from URIs to JSON nodes
     */
    private final Map<URI, JsonNode> uriToNode;
    
    /**
     * The mapping from URIs to canonical URIs. This can be imagined
     * as the mapping from URIs with fragments to URIs without fragments
     * (if they exist). 
     */
    private final Map<URI, URI> uriToCanonicalUri;
    
    /**
     * The root URI
     */
    private final List<URI> rootUris;
    
    /**
     * The root nodes
     */
    private final List<JsonNode> rootNodes;
    
    /**
     * Create a new repository
     */
    public NodeRepository()
    {
        this.rootUris = new ArrayList<URI>();
        this.rootNodes = new ArrayList<JsonNode>();
        this.uriToNode = new LinkedHashMap<URI, JsonNode>();
        this.uriToCanonicalUri = new LinkedHashMap<URI, URI>();

    }
    
    /**
     * Generate the nodes, recursively, starting at the given root URI
     * 
     * @param newRootUri The root URI
     */
    public void generateNodes(URI newRootUri)
    {
        URI rootUri = newRootUri.normalize();
        JsonNode rootNode = JsonUtils.readNodeOptional(rootUri);
        if (rootNode == null)
        {
            throw new JsonException("Could not read node from "+rootUri); 
        }
        rootUris.add(rootUri);
        rootNodes.add(rootNode);
        generateNodes(rootUri, rootNode);
    }
    
    /**
     * Generate the nodes that start at the given URI, with the given node
     * that was parsed from the given URI
     * 
     * @param uri The current URI
     * @param node The current node
     */
    private void generateNodes(URI uri, JsonNode node)
    {
        uri = uri.normalize();
        
        logger.info("Generate nodes      for "+uri);
        
        if (uriToNode.containsKey(uri)) 
        {
            logger.info("Node already known for  "+uri);
            return;
        }
        
        log("generateNodes");
        log("    uri "+uri);
        log("    node "+node);
        
        put(uri, node);
        
        logIndent++;
        generateSubNodes(uri, node);
        logIndent--;
        
        logger.info("Generate nodes DONE for "+uri);
    }
    
    /**
     * Generate the sub-nodes ... TODO real comment
     * 
     * @param uri The current URI
     * @param node The current node
     */
    private void generateSubNodes(URI uri, JsonNode node)
    {
        log("generateSubNodes of "+uri);
        
        if (node.isArray())
        {
            for (int i=0; i<node.size(); i++)
            {
                JsonNode arrayItem = node.get(i);

                log("generateSubNodes for array item "+arrayItem);
                
                URI itemUri = URIs.appendToFragment(uri, String.valueOf(i));
                
                logIndent++;
                generateNodes(itemUri, arrayItem);
                logIndent--;
            }
        }
        else
        {
            Iterator<Entry<String, JsonNode>> iterator = node.fields();
            while (iterator.hasNext())
            {
                Entry<String, JsonNode> field = iterator.next();
                String fieldName = field.getKey();
                JsonNode fieldValue = field.getValue();
                
                log("generateSubNodes field '"+fieldName+"' value "+fieldValue);
                
                if (fieldName.equals("$ref"))
                {
                    String refString = fieldValue.asText();
                    processRef(uri, refString);
                }
                
                uri = getCanonicalUri(uri);
                URI propertyUri = URIs.appendToFragment(uri, fieldName);
                
                logIndent++;
                generateNodes(propertyUri, fieldValue);
                logIndent--;
            }
        }
    }
    
    /**
     * Resolve the given "$ref" string against the given URI. 
     * 
     * This is not supposed to be used by clients. It tries to resolve
     * the URI, and returns the resolved URI if it finds a node at the
     * resulting location. If it does not find a node, it will try
     * to resolve the reference string against other known root URIs
     * as a fallback, and return any URI where a node is found.
     * 
     * @param uri The base URI
     * @param refString The reference string
     * @return The normalized URI, or <code>null</code> 
     * if it cannot be resolved.
     */
    public URI resolveRefUri(URI uri, String refString) 
    {
        URI refUri = uri.resolve(refString).normalize();
        
        // If the URI and its node are already known, just return it
        if (containsUri(refUri)) 
        {
            return refUri;
        }
        
        // If a node exists at the resulting URI, return it as well
        JsonNode refNode = JsonUtils.readNodeOptional(refUri);
        if (refNode != null)
        {
            return refUri;
        }

        // Try to resolve the string against other root URIs as a fallback
        URI fallbackRefUri = fallbackResolveAgainstRoot(refString);
        if (fallbackRefUri != null)
        {
            return fallbackRefUri;
        }
        
        log("Could not resolve reference " + refString 
            + " against " + uri + " or any root URI");
        return null;
    }

    /**
     * Process a "$ref" (reference) that was parsed from a JSON field
     * 
     * @param uri The current URI
     * @param refString The string value of the "$ref" field 
     */
    private void processRef(URI uri, String refString)
    {
        if (refString.equals("#"))
        {
            // TODO Think about how to handle self-references with
            // multiple root nodes...
            URI refUri = uri.resolve(refString).normalize();
            logger.severe("Self-reference "+refUri);
            //put(refUri, rootNode);
            return;
        }

        URI refUri = resolveRefUri(uri, refString);
        
        // If the URI and its node are already known, just 
        // process the node
        if (containsUri(refUri))
        {
            log("generateSubNodes with known ref");
            log("   uri          "+uri);
            log("   canonicalUri "+refUri);

            JsonNode refNode = get(refUri);
            uriToCanonicalUri.put(uri, refUri);
            logIndent++;
            generateNodes(uri, refNode);
            logIndent--;
            return;
        }
            
        // Try to create the node by reading it from the 
        // given URI, and the process it
        JsonNode refNode = JsonUtils.readNodeOptional(refUri);
        if (refNode != null)
        {
            String fragment = refUri.getFragment();
            if (fragment != null)
            {
                refNode = resolveFragment(refNode, fragment);
            }
            
            log("generateSubNodes");
            log("   uri          "+uri);
            log("   canonicalUri "+refUri);

            uriToCanonicalUri.put(uri, refUri);

            logIndent++;
            generateNodes(uri, refNode);
            logIndent--;
            logIndent++;
            generateNodes(refUri, refNode);
            logIndent--;
            
            return;
        }
        
        // No node could be read from the given URI.
        log("WARNING: generateSubNodes: " + 
            "No node found for refUri "+refUri);
    }
    
    /**
     * Resolves a given fragment string against a given node.
     * 
     * For a node like <code>{ "foo": { "bar" : { "inner": ... } } }</code>
     * and a fragment <code>"/foo/bar"</code>, this will return the 
     * node <code>{ "inner": ... }</code>.
     * 
     * If the fragment cannot be resolved, then <code>null</code> is 
     * returned.
     * 
     * @param node The node
     * @param fragment The fragment
     * @return The resolved fragment node
     */
    private static JsonNode resolveFragment(JsonNode node, String fragment)
    {
        String f = fragment;
        if (f.startsWith("/")) 
        {
            f = f.substring(1);
        }
        String tokens[] = f.split("/");
        JsonNode current = node;
        for (String token : tokens)
        {
            current = current.get(token);
            if (current == null)
            {
                logger.warning("Could not resolve fragment " + fragment
                    + " against " + node);
                return null;
            }
        }
        return current;
    }

    /**
     * A standalone schema file may not define any URI to resolve
     * references against. Try to find the right URI by resolving 
     * the reference string against all root URIs, and return
     * the one where a node could be read.
     * 
     * @param refString The reference string
     * @return The resolved URI, or <code>null</code> if none was found
     */
    private URI fallbackResolveAgainstRoot(String refString)
    {
        logger.warning("Attempting to resolve " 
            + refString + " against known roots...");
        for (URI rootUri : rootUris)
        {
            URI refUri = rootUri.resolve(refString).normalize();
            JsonNode refNode = JsonUtils.readNodeOptional(refUri);
            if (refNode != null)
            {
                logger.warning("Attempting to resolve against "
                    + "known roots resulted in " + refUri);
                return refUri;
            }
        }
        logger.warning("Attempting to resolve against known roots failed");
        return null;
    }
    

    /**
     * Returns the canonical URI for the given URI. If there is a basic
     * URI (for example, one without fragments) that points to the same
     * node as the given URI, then this basic URI is returned. Otherwise,
     * the given URI is returned as it is
     * 
     * @param uri The URI
     * @return The canonical URI
     */
    public URI getCanonicalUri(URI uri)
    {
        uri = uri.normalize();
        URI result = uriToCanonicalUri.get(uri);
        if (result == null)
        {
            return uri;
        }
        return result;
    }
    
    /**
     * Computes an unmodifiable map from nodes to the (unmodifiable) lists 
     * of URIs that are mapped to the given node
     * 
     * @return The map
     */
    public Map<JsonNode, List<URI>> computeNodeToUrisMapping()
    {
        Map<JsonNode, List<URI>> nodeToUris = 
            new LinkedHashMap<JsonNode, List<URI>>();
        for (URI uri : getUris())
        {
            JsonNode node = get(uri);
            List<URI> uris = nodeToUris.get(node);
            if (uris == null)
            {
                uris = new ArrayList<URI>();
                nodeToUris.put(node, uris);
            }
            uris.add(uri);
        }
        return Maps.deepUnmodifiable(nodeToUris);
    }
    
    
    /**
     * Returns the root nodes that have been parsed from the URIs that 
     * have been given to {@link #generateNodes(URI)}.
     * 
     * @return The root nodes
     */
    public List<JsonNode> getRootNodes()
    {
        return Collections.unmodifiableList(rootNodes);
    }
    
    /**
     * Returns the root URIs that have been given to 
     * {@link #generateNodes(URI)}.
     * 
     * @return The root URIs
     */
    public List<URI> getRootUris()
    {
        return Collections.unmodifiableList(rootUris);
    }
    
    /**
     * Store the given mapping from a URI to a node
     * 
     * @param uri The URI
     * @param node The node
     */
    void put(URI uri, JsonNode node)
    {
        uriToNode.put(uri, node);
    }
    
    /**
     * Returns whether this repository contains the given URI
     * 
     * @param uri The URI
     * @return Whether this repository contains the given URI
     */
    boolean containsUri(URI uri)
    {
        return uriToNode.containsKey(uri);
    }
    
    /**
     * Returns an unmodifiable view on the set of URIs that are
     * contained in this repository
     * 
     * @return The URIs
     */
    public Set<URI> getUris()
    {
        return Collections.unmodifiableSet(uriToNode.keySet());
    }
    
    /**
     * Returns the node that was stored for the given URI, or <code>null</code>
     * if no such node can be found
     * 
     * @param uri The URI
     * @return The node
     */
    public JsonNode get(URI uri)
    {
        return uriToNode.get(uri);
    }
    
    
    /**
     * Create a debugging string. Do not use this method.
     * 
     * @return Nothing important. Go away.
     */
    public String createDebugString()
    {
        StringBuilder sb = new StringBuilder();
        Map<JsonNode, List<URI>> nodeToUris = computeNodeToUrisMapping();
        for (Entry<JsonNode, List<URI>> entry : nodeToUris.entrySet())
        {
            JsonNode node = entry.getKey();
            List<URI> uris = entry.getValue();
            sb.append("URIs:\n");
            for (URI uri : uris)
            {
                sb.append("    "+uri+"\n");
            }
            sb.append("Node:\n");
            try
            {
                ObjectMapper mapper = new ObjectMapper();
                Object json = mapper.readValue(node.toString(), Object.class);
                String indented = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(json);
                sb.append(indented+"\n\n");
            }
            catch (JsonParseException e)
            {
                sb.append("ERROR: "+e.getMessage()+"\n");
            }
            catch (JsonMappingException e)
            {
                sb.append("ERROR: "+e.getMessage()+"\n");
            }
            catch (IOException e)
            {
                sb.append("ERROR: "+e.getMessage()+"\n");
            }
        }
        return sb.toString();
    }
    
    
}