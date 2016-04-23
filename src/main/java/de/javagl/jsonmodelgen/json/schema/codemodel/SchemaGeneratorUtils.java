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
import java.util.function.Function;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

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
     * Returns the schema that is referred to from the <code>"extends"</code>
     * property of the given node. This assumes that the given node contains
     * an <code>"extends"</code> property that refers to a node which contains
     * a <code>"$ref"</code> property that contains a (relative) URI. If such
     * a reference is present, it is resolved against the given base URI and
     * passed to the given schema resolver for resolution. Otherwise, a warning
     * is printed and <code>null</code> is returned.
     * 
     * @param uri The base URI of the schema
     * @param node The node
     * @param schemaResolver The function that can resolve schemas for a
     * given URI
     * @return The resolved schema
     */
    public static <S> S getExtendsSchema(
        URI uri, JsonNode node, Function<URI, S> schemaResolver)
    {
        JsonNode extendsNode = node.get("extends");
        if (extendsNode == null)
        {
            logger.warning("getExtendsSchema: Found no extends node");
            logger.warning("    node         "+node);
            return null;
        }
        URI refUri = getRefUriOptional(uri, extendsNode);
        if (refUri == null)
        {
            logger.warning("getExtendsSchema: Found no refUri in extends node");
            logger.warning("    extendsNode  "+extendsNode);
            return null;
        }
        S extended = schemaResolver.apply(refUri);
        if (extended == null)
        {
            logger.warning("getExtendsSchema: " + 
                "Could not resolve schema of extends ref");
            logger.warning("    uri          "+uri);
            logger.warning("    extendsNode  "+extendsNode);
            logger.warning("    refUri       "+refUri);
            return null;
        }
        return extended;
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
