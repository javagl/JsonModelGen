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
import java.util.LinkedHashSet;
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
    private final URI rootUri;
    
    /**
     * The root node
     */
    private final JsonNode rootNode;
    
    /**
     * Create a new repository by parsing the given URI
     * 
     * @param rootUri The root URI
     */
    public NodeRepository(URI rootUri)
    {
        rootUri = rootUri.normalize();
        this.rootUri = rootUri;
        this.rootNode = JsonUtils.readNodeOptional(rootUri);
        if (rootNode == null)
        {
            throw new JsonException("Could not read node from "+rootUri); 
        }
        this.uriToNode = new LinkedHashMap<URI, JsonNode>();
        this.uriToCanonicalUri = new LinkedHashMap<URI, URI>();

        generateNodes(rootUri, rootNode);
    }
    
    Set<URI> xxx = new LinkedHashSet<URI>();
    
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
                    processRef(uri, node, refString);
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
     * Process a "$ref" (reference) that was parsed from a JSON field
     * 
     * @param uri The current URI
     * @param node The current node
     * @param refString The string value of the "$ref" field 
     */
    private void processRef(URI uri, JsonNode node, String refString)
    {
        if (refString.equals("#"))
        {
            URI refUri = uri.resolve(refString).normalize();
            System.out.println("Self-reference "+refUri+" to "+rootNode);
            put(refUri, rootNode);
        }
        else
        {
            URI refUri = uri.resolve(refString).normalize();
            if (!containsUri(refUri))
            {
                JsonNode refNode = JsonUtils.readNodeOptional(refUri);
                if (refNode != null)
                {
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
                }
                else
                {
                    log("WARNING: generateSubNodes: " + 
                        "No node found for refUri "+refUri);
                    log("WARNING:    uri          "+uri);
                    
                    logger.warning("No node found for refUri "+refUri);
                }
            }
            else
            {
                log("generateSubNodes with known ref");
                log("   uri          "+uri);
                log("   canonicalUri "+refUri);

                JsonNode refNode = get(refUri);
                uriToCanonicalUri.put(uri, refUri);
                logIndent++;
                generateNodes(uri, refNode);
                logIndent--;
            }
        }
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
     * Returns the root node that was parsed from the URI that was 
     * given in the constructor
     * 
     * @return The root node
     */
    public JsonNode getRootNode()
    {
        return rootNode;
    }
    
    /**
     * Returns the root URI that was given in the constructor
     * 
     * @return The root URI
     */
    public URI getRootUri()
    {
        return rootUri;
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