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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import de.javagl.jsonmodelgen.json.URIs;

/**
 * Utility methods for schema generators 
 */
public class SchemaGeneratorUtils
{
    /**
     * The logger used in this class
     */
    private static final Logger logger = 
        Logger.getLogger(SchemaGeneratorUtils.class.getName());
    
    /**
     * Returns the schemas that are referred to from the specified
     * property of the given node. This assumes that the given node contains
     * a property with the given name that refers to an array of nodes.
     * These nodes must either contain <code>"$ref"</code> properties that 
     * contain (relative) URIs, or standalone schema definitions that 
     * contain an explicit <code>"type" : [ ... ]</code> property with a list 
     * of type strings.<br>  
     * <br>
     * If a reference or is present, it is resolved against the given base 
     * URI and passed to the given schema resolver for resolution.<br>
     * <br> 
     * If a standalone schema is present, then the fragment of the given
     * URI is extended with a fragment that is derived from the given 
     * propertyName, and the resulting URI is passed to the given schema 
     * resolver for resolution.<br>
     * <br>
     * Otherwise, a warning is printed and <code>null</code> is returned.
     * 
     * @param uri The base URI of the schema
     * @param node The node
     * @param propertyName The name of the property that contains an array
     * of references
     * @param schemaResolver The function that can resolve schemas for a
     * given URI
     * @return The resolved schemas
     */
    public static <S> List<S> getSubSchemasArray(
        URI uri, JsonNode node, String propertyName, 
        Function<URI, S> schemaResolver)
    {
        JsonNode propertyNode = node.get(propertyName);
        if (propertyNode == null)
        {
            logger.warning("getSubSchemas: Found no "+propertyName+" node");
            logger.warning("    node         "+node);
            return null;
        }
        
        if (!propertyNode.isArray())
        {
            logger.warning("getSubSchemas: The "+propertyName+" node is no array");
            logger.warning("    node         "+node);
            logger.warning("    propertyNode "+propertyNode);
            return null;
        }
        
        List<S> subSchemas = new ArrayList<S>();
        for (int i=0; i<propertyNode.size(); i++)
        {
            String propertyNodeItemName = propertyName+"/"+i;
            JsonNode propertyNodeItem = propertyNode.get(i);
            S subSchema = getSubSchema(uri, propertyNodeItem, 
                propertyNodeItemName, schemaResolver);
            if (subSchema == null)
            {
                logger.warning("getSubSchemas: The " + propertyName + " array element " + 
                    propertyNodeItemName + " did not define a schema");
                logger.warning("    node             "+node);
                logger.warning("    propertyNode     "+propertyNode);
                logger.warning("    propertyNodeItem "+propertyNode);
            }
            else
            {
                subSchemas.add(subSchema);
            }
        }
        return subSchemas;
    }

    /**
     * Returns the schemas that are referred to from the specified
     * property of the given node. This assumes that the given node contains
     * a property with the given name that refers to an dictionary of nodes.
     * These nodes must either contain <code>"$ref"</code> properties that 
     * contain (relative) URIs, or standalone schema definitions that 
     * contain an explicit <code>"type" : [ ... ]</code> property with a list 
     * of type strings.<br>  
     * <br>
     * If a reference or is present, it is resolved against the given base 
     * URI and passed to the given schema resolver for resolution.<br>
     * <br> 
     * If a standalone schema is present, then the fragment of the given
     * URI is extended with a fragment that is derived from the given 
     * propertyName, and the resulting URI is passed to the given schema 
     * resolver for resolution.<br>
     * <br>
     * Otherwise, a warning is printed and <code>null</code> is returned.
     * 
     * @param uri The base URI of the schema
     * @param node The node
     * @param propertyName The name of the property that contains a dictionary
     * of references
     * @param schemaResolver The function that can resolve schemas for a
     * given URI
     * @return The resolved schemas
     */
    public static <S> Map<String, S> getSubSchemasMap(
        URI uri, JsonNode node, String propertyName, 
        Function<URI, S> schemaResolver)
    {
        JsonNode propertyNode = node.get(propertyName);
        if (propertyNode == null)
        {
            logger.warning("getSubSchemas: Found no "+propertyName+" node");
            logger.warning("    node         "+node);
            return null;
        }
        
        if (!propertyNode.isObject())
        {
            logger.warning("getSubSchemas: The "+propertyName+" node is no object");
            logger.warning("    node         "+node);
            logger.warning("    propertyNode "+propertyNode);
            return null;
        }
        
        
        Map<String, S> subSchemas = new LinkedHashMap<String, S>();
        Iterator<String> fieldNames = propertyNode.fieldNames();
        while (fieldNames.hasNext())
        {
            String fieldName = fieldNames.next();
            String propertyNodeItemName = propertyName+"/"+fieldName;
            JsonNode propertyNodeItem = propertyNode.get(fieldName);
            S subSchema = getSubSchema(uri, propertyNodeItem, 
                propertyNodeItemName, schemaResolver);
            if (subSchema == null)
            {
                logger.warning("getSubSchemas: The " + propertyName + " element " + 
                    propertyNodeItemName + " did not define a schema");
                logger.warning("    node             "+node);
                logger.warning("    propertyNode     "+propertyNode);
                logger.warning("    propertyNodeItem "+propertyNode);
            }
            else
            {
                subSchemas.put(fieldName, subSchema);
            }
        }
        return subSchemas;
    }
    
    /**
     * Returns the schema that is referred to from the given node. This assumes 
     * that the given node either contains a <code>"$ref"</code> property that 
     * contains a (relative) URI, or a standalone schema definition that 
     * contains an explicit <code>"type" : [ ... ]</code> property with a list 
     * of type strings.<br>  
     * <br>
     * If a reference or is present, it is resolved against the given base 
     * URI and passed to the given schema resolver for resolution.<br>
     * <br> 
     * If a standalone schema is present, then the fragment of the given
     * URI is extended with the given subSchemaFragment, and the resulting
     * URI is passed to the given schema resolver for resolution.<br>
     * <br>
     * Otherwise, a warning is printed and <code>null</code> is returned.
     * 
     * @param uri The base URI of the schema
     * @param node The node
     * @param subSchemaFragment The sub-schema fragment 
     * @param schemaResolver The function that can resolve schemas for a
     * given URI
     * @return The resolved schema
     */
    public static <S> S getSubSchema(
        URI uri, JsonNode node, String subSchemaFragment, 
        Function<URI, S> schemaResolver)
    {
        URI refUri = getRefUriOptional(uri, node);
        if (refUri == null)
        {
            //logger.warning("getSubSchema: Found no refUri in node");
            //logger.warning("    node  "+node);
            //logger.warning("    Assuming type information to be present, resolving...");
            
            URI subUri = URIs.appendToFragment(uri, subSchemaFragment);
            return schemaResolver.apply(subUri);
        }
        S subSchema = schemaResolver.apply(refUri);
        if (subSchema == null)
        {
            logger.warning("getSubSchema: " + 
                "Could not resolve schema of node ref");
            logger.warning("    uri              "+uri);
            logger.warning("    node              "+node);
            logger.warning("    refUri           "+refUri);
            return null;
        }
        return subSchema;
    }
    
    
    /**
     * Tries to obtain the value of the <code>"$ref"</code> field from the 
     * given node, and resolve it against the given URI. If the given node 
     * has no <code>"$ref"</code> field, then <code>null</code> is returned.
     * 
     * @param uri The base URI
     * @param node The node
     * @return The reference URI, or <code>null</code>
     */
    private static URI getRefUriOptional(URI uri, JsonNode node)
    {
        if (!node.has("$ref"))
        {
            return null;
        }
        JsonNode refNode = node.get("$ref");
        String refString = refNode.asText();
        URI refUri = uri.resolve(refString);
        return refUri.normalize();
    }

    /**
     * Private constructor to prevent instantiation
     */
    private SchemaGeneratorUtils()
    {
        // Private constructor to prevent instantiation
    }
}
