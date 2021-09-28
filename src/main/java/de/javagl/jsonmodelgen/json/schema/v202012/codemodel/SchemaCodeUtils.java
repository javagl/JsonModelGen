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
package de.javagl.jsonmodelgen.json.schema.v202012.codemodel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import de.javagl.jsonmodelgen.json.schema.v202012.ObjectSchema;
import de.javagl.jsonmodelgen.json.schema.v202012.Schema;
import de.javagl.jsonmodelgen.json.schema.v202012.SchemaUtils;

/**
 * Utility methods related to {@link Schema} instances, mainly intended
 * for code generation
 */
class SchemaCodeUtils
{
    /**
     * The logger used in this class
     */
    private static final Logger logger =
        Logger.getLogger(SchemaCodeUtils.class.getName());
    
    /**
     * If the given {@link Schema} contains 
     * {@link Schema#getAnyOf() anyOf} information, then this method
     * will check the {@link Schema#getTypeStrings()} of all elements. If 
     * there is a common type, then the first element of the <code>anyOf</code>
     * list will be returned. Otherwise, <code>null</code> will be returned.
     * 
     * @param schema The {@link Schema}
     * @return The first of the anyOf-Schemas if they have a common type,
     * or <code>null</code> otherwise.
     */
    static Schema determineCommonTypeFromAnyOf(Schema schema)
    {
        List<Schema> anyOf = schema.getAnyOf();
        if (anyOf == null)
        {
            return null;
        }
        Set<String> typeStrings = new LinkedHashSet<String>();
        for (Schema anyOfSchema : anyOf)
        {
            typeStrings.addAll(anyOfSchema.getTypeStrings());
        }
        if (typeStrings.size() != 1)
        {
            logger.warning("Multiple types in anyOf: " + typeStrings
                + " for " + SchemaUtils.createShortSchemaDebugString(schema));
            return null;
        }
        return anyOf.get(0);
    }
    
    
    /**
     * If the given {@link Schema} contains 
     * {@link Schema#getAnyOf() anyOf} information, then this method
     * will fetch the one element of the anyOf-Schemas that is NOT an
     * {@link ObjectSchema}. If there are none or multiple of such elements,
     * then <code>null</code> is returned.
     * 
     * @param schema The {@link Schema}
     * @return The element of the anyOf-Schemas that is NOT an 
     * {@link ObjectSchema}
     */
    static Schema determineTypeFromUntypedAnyOf(Schema schema)
    {
        List<Schema> anyOf = schema.getAnyOf();
        if (anyOf == null)
        {
            return null;
        }
        Schema nonObjectSchema = null;
        for (Schema anyOfSchema : anyOf)
        {
            if (!anyOfSchema.isObject())
            {
                if (nonObjectSchema != null)
                {
                    logger.warning("Multiple non-object types in anyOf for "
                        + SchemaUtils.createShortSchemaDebugString(schema));
                    return null;
                }
                nonObjectSchema = anyOfSchema;
            }
        }
        if (nonObjectSchema == null)
        {
            return null;
        }
        return nonObjectSchema;
    }
    
    
    /**
     * Returns the list containing the (unique) 
     * {@link Schema#getEnumStrings() enum strings} that appear in 
     * all the {@link Schema#getAnyOf() anyOf} schemas of the 
     * given schema, or <code>null</code> if there are no anyOf schemas.
     *  
     * @param schema The {@link Schema}
     * @return The enum strings
     */
    static List<String> determineEnumStringsFromAnyOf(Schema schema)
    {
        List<Schema> anyOf = schema.getAnyOf();
        if (anyOf == null)
        {
            return null;
        }
        Set<String> allEnumStrings = new LinkedHashSet<String>();
        for (Schema anyOfSchema : anyOf)
        {
            Set<String> enumStrings = anyOfSchema.getEnumStrings();
            if (enumStrings != null)
            {
                allEnumStrings.addAll(enumStrings);
            }
        }
        return new ArrayList<String>(allEnumStrings);
    }
    
    
    
    /**
     * Private constructor to prevent instantiation
     */
    private SchemaCodeUtils()
    {
        // Private constructor to prevent instantiation
    }

}
