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
package de.javagl.jsonmodelgen.json.schema.codemodel;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import de.javagl.jsonmodelgen.json.URIs;

/**
 * Utility methods for schema generators.
 */
public class SchemaGeneratorUtils
{
    // TODO More sensible comments here...
    
    /**
     * The logger used in this class
     */
    private static final Logger logger = 
        Logger.getLogger(SchemaGeneratorUtils.class.getName());
    
    /**
     * Returns a list of all URIs that are created by iterating over
     * the elements of the specified (array) property of the given 
     * node.
     * 
     * This is done by either extracting their "$ref" property, or 
     * interpreting their name as a fragment. 
     * 
     * @param uri The URI
     * @param node The node
     * @param propertyName The property name
     * @return The URIs
     */
    public static List<URI> getSubUrisArray(
        URI uri, JsonNode node, String propertyName)
    {
        JsonNode propertyNode = node.get(propertyName);
        if (propertyNode == null)
        {
            logger.warning("getSubUris: Found no "+propertyName+" node");
            logger.warning("    node         "+node);
            return Collections.emptyList();
        }
        
        if (!propertyNode.isArray())
        {
            logger.warning("getSubUris: The "+propertyName+" node is no array");
            logger.warning("    node         "+node);
            logger.warning("    propertyNode "+propertyNode);
            return Collections.emptyList();
        }
        
        List<URI> subUris = new ArrayList<URI>();
        for (int i=0; i<propertyNode.size(); i++)
        {
            String propertyNodeItemName = propertyName+"/"+i;
            URI subUri = URIs.appendToFragment(uri, propertyNodeItemName);
            subUris.add(subUri);
        }
        return subUris;
    }
    
    /**
     * Returns a map of all URIs that are created by iterating over
     * the elements of the specified (object) property of the given 
     * node, mapping the property name to the URI.
     * 
     * This is done by either extracting their "$ref" property, or 
     * interpreting their name as a fragment. 
     * 
     * @param uri The URI
     * @param node The node
     * @param propertyName The property name
     * @return The URIs
     */
    public static Map<String, URI> getSubUrisMap(
        URI uri, JsonNode node, String propertyName)
    {
        JsonNode propertyNode = node.get(propertyName);
        if (propertyNode == null)
        {
            logger.warning("getSubUris: Found no "+propertyName+" node");
            logger.warning("    node         "+node);
            return Collections.emptyMap();
        }
        
        if (!propertyNode.isObject())
        {
            logger.warning("getSubUris: The "+propertyName+" node is no object");
            logger.warning("    node         "+node);
            logger.warning("    propertyNode "+propertyNode);
            return Collections.emptyMap();
        }
        
        Map<String, URI> subUris = new LinkedHashMap<String, URI>();
        Iterator<String> fieldNames = propertyNode.fieldNames();
        while (fieldNames.hasNext())
        {
            String fieldName = fieldNames.next();
            String propertyNodeItemName = propertyName+"/"+fieldName;
            URI subUri = URIs.appendToFragment(uri, propertyNodeItemName);
            subUris.put(fieldName, subUri);
        }
        return subUris;
    }
    
    /**
     * Creates schemas from the URIs that are created by
     * {@link #getSubUrisArray(URI, JsonNode, String)}.
     * 
     * @param <S> The schema type
     * @param uri The URI
     * @param node The node
     * @param propertyName The property name
     * @param schemaResolver The schema resolver
     * @return The schemas
     */
    public static <S> List<S> getSubSchemasArray(
        URI uri, JsonNode node, String propertyName,
        Function<URI, S> schemaResolver)
    {
        List<URI> subUris = getSubUrisArray(
            uri, node, propertyName);
        List<S> subSchemas = new ArrayList<S>();
        for (int i=0; i<subUris.size(); i++)
        {
            URI subUri = subUris.get(i);
            S subSchema = schemaResolver.apply(subUri);
            subSchemas.add(subSchema);
        }
        return subSchemas;
    }
    
    /**
     * Creates schemas from the URIs that are created by
     * {@link #getSubUrisMap(URI, JsonNode, String)}.
     * 
     * @param <S> The schema type
     * @param uri The URI
     * @param node The node
     * @param propertyName The property name
     * @param schemaResolver The schema resolver
     * @return The schemas
     */
    public static <S> Map<String, S> getSubSchemasMap(
        URI uri, JsonNode node, String propertyName,
        Function<URI, S> schemaResolver)
    {
        Map<String, URI> subUris = getSubUrisMap(
            uri, node, propertyName);
        Map<String, S> subSchemas = new LinkedHashMap<String, S>();
        for (String key : subUris.keySet())
        {
            URI subUri = subUris.get(key);
            S subSchema = schemaResolver.apply(subUri);
            subSchemas.put(key, subSchema);
        }
        return subSchemas;
    }
    
    /**
     * Creates a schema from the URI that is created by 
     * {@link #getSubUri(URI, String)}.
     * 
     * @param <S> The schema type
     * @param uri The URI
     * @param node The node
     * @param subSchemaFragment The schema fragment
     * @param schemaResolver The schema resolver
     * @return The schemas
     */
    public static <S> S getSubSchema(
        URI uri, JsonNode node, String subSchemaFragment,
        Function<URI, S> schemaResolver)
    {
        URI subUri = getSubUri(uri, subSchemaFragment);
        S subSchema = schemaResolver.apply(subUri);
        return subSchema;
    }
    
    /**
     * Returns a URI that is created by interpreting the name as a fragment. 
     * 
     * @param uri The URI
     * @param subSchemaFragment The fragment
     * @return The URIs
     */
    public static URI getSubUri(
        URI uri, String subSchemaFragment)
    {
        URI subUri = URIs.appendToFragment(uri, subSchemaFragment);
        return subUri;
    }
    
    
    
    
    
    /**
     * Private constructor to prevent instantiation
     */
    private SchemaGeneratorUtils()
    {
        // Private constructor to prevent instantiation
    }
}
