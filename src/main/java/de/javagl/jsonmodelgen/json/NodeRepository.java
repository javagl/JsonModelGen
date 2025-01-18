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
 * A repository that maps URIs to JSON nodes.
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
    private static final Level level = Level.FINEST;
    
    /**
     * Logging utility method
     * 
     * @param s The log message
     */
    private static void log(String s)
    {
        if (logger.isLoggable(level))
        {
            logger.log(level, s);
        }
    }
    
    /**
     * The root URIs
     */
    private final List<URI> rootUris;
    
    /**
     * The root nodes
     */
    private final List<JsonNode> rootNodes;

    /**
     * The search URIs
     */
    private final List<URI> searchUris;
    
    /**
     * The mapping from URIs to JSON nodes
     */
    private final Map<URI, JsonNode> uriToNode;
    
    /**
     * The mapping from nodes to full URIs
     */
    private final Map<JsonNode, URI> nodeToFullUri;
    
    /**
     * Create a new repository
     */
    public NodeRepository()
    {
        this.rootUris = new ArrayList<URI>();
        this.rootNodes = new ArrayList<JsonNode>();
        this.searchUris = new ArrayList<URI>();
        this.uriToNode = new LinkedHashMap<URI, JsonNode>();
        this.nodeToFullUri = new LinkedHashMap<JsonNode, URI>();
    }
    
    /**
     * Add the given URI to resolve relative URIs against while trying to
     * find a node
     * 
     * @param searchUri The search URI
     */
    public void addSearchUri(URI searchUri)
    {
        searchUris.add(searchUri);
    }
    
    /**
     * Add the given root RUI
     * 
     * @param newRootUri The root URI
     */
    public void addRootUri(URI newRootUri)
    {
        URI rootUri = newRootUri.normalize();
        JsonNode rootNode = JsonUtils.readNodeOptional(rootUri);
        if (rootNode == null)
        {
            throw new JsonException("Could not read node from "+rootUri); 
        }
        rootUris.add(rootUri);
        rootNodes.add(rootNode);
        uriToNode.put(rootUri, rootNode);
        nodeToFullUri.put(rootNode, rootUri);
        searchUris.add(rootUri);
    }
    
    /**
     * Resolve the JSON node for the given input URI
     * 
     * @param inputUri The input URI
     * @return The JSON node
     */
    public JsonNode resolveNode(URI inputUri)
    {
        log("resolveNode for " + inputUri);

        URI uri = inputUri.normalize();
        JsonNode node = uriToNode.get(uri);
        if (node != null)
        {
            //logger.fine("Using known node for " + uri + ": " + node);
            return node;
        }
        
        ResolveResult resolveResult = resolveBaseNode(uri);
        if (resolveResult == null)
        {
            return null;
        }
        JsonNode baseNode = resolveResult.node;        
        JsonNode resultNode = resolveFragment(
            baseNode, inputUri.getFragment());
        
        put(uri, resultNode, resolveResult.fullUri);;
        return resultNode;
    }
    
    /**
     * Return structure for {@link NodeRepository#resolveBaseNode(URI)}
     */
    static class ResolveResult
    {
        /**
         * The node
         */
        JsonNode node;
        
        /**
         * The full URI
         */
        URI fullUri;
    }
    
    /**
     * Resolve the given input URI against a node. If it is absolute,
     * it will be read directly. Otherwise, it will be attempted to
     * resolve it against the search URIs.
     * 
     * If the node cannot be resolved, a warning is printed and
     * <code>null</code> is returned.
     * 
     * @param inputUri The input URI
     * @return The resolved node and its full URI
     */
    private ResolveResult resolveBaseNode(URI inputUri) 
    {
        URI uri = inputUri.normalize();
        if (uri.isAbsolute())
        {
            JsonNode node = JsonUtils.readNodeOptional(uri);
            if (node != null)
            {
                ResolveResult resolveResult = new ResolveResult();
                resolveResult.node = node;
                resolveResult.fullUri = uri;
                return resolveResult;
            }
        }
        for (URI searchUri : searchUris)
        {
            URI resolved = searchUri.resolve(uri);
            JsonNode node = JsonUtils.readNodeOptional(resolved);
            if (node != null)
            {
                logger.info("Resolved " + uri + " against search URI "
                    + searchUri + " to " + resolved);
                
                ResolveResult resolveResult = new ResolveResult();
                resolveResult.node = node;
                resolveResult.fullUri = resolved;
                return resolveResult;
            }
        }
        logger.warning("Could not read node from URI " + uri);
        return null;
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
        if (fragment == null)
        {
            return node;
        }
        String f = fragment;
        if (f.startsWith("/")) 
        {
            f = f.substring(1);
        }
        String tokens[] = f.split("/");
        JsonNode current = node;
        for (String token : tokens)
        {
            if (current.isArray())
            {
                int index = 0;
                try
                {
                    index = Integer.parseInt(token);                    
                }
                catch (NumberFormatException e)
                {
                    logger.severe("Expected index, found "+token);
                    return null;
                }
                current = current.get(index);
            }
            else
            {
                current = current.get(token);
            }
            if (current == null)
            {
                logger.severe("Could not resolve fragment " + fragment
                    + " against " + node);
                return null;
            }
        }
        return current;
    }

    /**
     * Returns the full URI that the given node was obtained from
     * 
     * @param node The node
     * @return The full URI
     */
    public URI getFullUri(JsonNode node)
    {
        return nodeToFullUri.get(node);
    }
    
    /**
     * Returns the root nodes that have been parsed from the URIs that 
     * have been given to {@link #addRootUri(URI)}
     * 
     * @return The root nodes
     */
    public List<JsonNode> getRootNodes()
    {
        return Collections.unmodifiableList(rootNodes);
    }
    
    /**
     * Returns the root URIs that have been given to 
     * {@link #addRootUri(URI)}
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
     * @param fullUri The full URI
     */
    private void put(URI uri, JsonNode node, URI fullUri)
    {
        log("Storing node");
        log("    uri     " + uri);
        log("    node    " + node);
        log("    fullUri " + fullUri);
        uriToNode.put(uri, node);
        nodeToFullUri.put(node, fullUri);
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
            JsonNode node = resolveNode(uri);
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
  
}