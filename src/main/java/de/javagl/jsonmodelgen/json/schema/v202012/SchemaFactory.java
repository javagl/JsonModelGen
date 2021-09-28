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
package de.javagl.jsonmodelgen.json.schema.v202012;

import java.util.logging.Logger;

/**
 * Methods to create {@link Schema} instances
 */
public class SchemaFactory
{
    /**
     * The logger used in this class
     */
    private static final Logger logger =
        Logger.getLogger(SchemaFactory.class.getName());

    /**
     * Create a {@link Schema} instance with the appropriate subtype, based
     * on the given type string.<br>
     *
     * @param typeString The type string
     * @return The {@link Schema} instance
     */
    public static Schema createSchema(String typeString)
    {
        if (typeString.equals("string"))
        {
            return new StringSchema();
        }
        if (typeString.equals("number"))
        {
            return new NumberSchema();
        }
        if (typeString.equals("integer"))
        {
            return new IntegerSchema();
        }

        if (typeString.equals("array"))
        {
            return new ArraySchema();
        }

        if (typeString.equals("boolean"))
        {
            return new BooleanSchema();
        }

        if (typeString.equals("null"))
        {
            return new ObjectSchema();
        }
        if (typeString.equals("object"))
        {
            return new ObjectSchema();
        }
        if (typeString.equals("any"))
        {
            return new ObjectSchema(true);
        }

        logger.warning("Unhandled type: "+typeString+", using ObjectSchema");
        return new ObjectSchema();
    }

    /**
     * Private constructor to prevent instantiation
     */
    private SchemaFactory()
    {
        // Private constructor to prevent instantiation
    }

}
