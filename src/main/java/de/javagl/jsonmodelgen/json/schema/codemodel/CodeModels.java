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

import java.util.function.Function;
import java.util.logging.Logger;

import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JPrimitiveType;

/**
 * Utility methods to build parts of a code model 
 */
public class CodeModels
{
    /**
     * The logger used in this class
     */
    private static final Logger logger = 
        Logger.getLogger(CodeModels.class.getName());
    
    /**
     * Create a literal expression for the given primitive type, based on
     * the given string.
     * 
     * @param string The string
     * @param type The type
     * @return The expression
     * @throws IllegalArgumentException If the given type is not a primitive
     * type 
     */
    public static JExpression createPrimitiveLiteralExpression(
        String string, JPrimitiveType type)
    {
        if (string == null)
        {
            return null;
        }
        if (type.binaryName().equals("boolean"))
        {
            boolean value = Boolean.parseBoolean(string);
            return JExpr.lit(value);
        }
        if (type.binaryName().equals("char"))
        {
            char value = 0;
            value = string.charAt(0);
            return JExpr.lit(value);
        }

        if (type.binaryName().equals("byte"))
        {
            byte value = parse(string, type, Byte::parseByte, (byte)0);
            return JExpr.lit(value);
        }
        if (type.binaryName().equals("short"))
        {
            short value = parse(string, type, Short::parseShort, (short)0);
            return JExpr.lit(value);
        }
        if (type.binaryName().equals("int"))
        {
            int value = parse(string, type, Integer::parseInt, 0);
            return JExpr.lit(value);
        }
        if (type.binaryName().equals("long"))
        {
            long value = parse(string, type, Long::parseLong, 0L);
            return JExpr.lit(value);
        }
        if (type.binaryName().equals("float"))
        {
            float value = parse(string, type, Float::parseFloat, 0.0f);
            return JExpr.lit(value);
        }
        if (type.binaryName().equals("double"))
        {
            double value = parse(string, type, Double::parseDouble, 0.0);
            return JExpr.lit(value);
        }
        throw new IllegalArgumentException(
            "Unhandled type: "+type);
    }
    
    /**
     * Tries to parse a value of type "T" from the given string, using the
     * given parser function. If this is not possible, then a warning will
     * be printed and the given default value will be returned.
     * 
     * @param string The string
     * @param type The type
     * @param parser The parser function
     * @param defaultValue The default value
     * @return The parsed value or the default value
     */
    private static <T> T parse(String string, JPrimitiveType type, 
        Function<String, T> parser, T defaultValue)
    {
        try
        {
            return parser.apply(string);
        }
        catch (NumberFormatException e)
        {
            logger.warning("createPrimitiveLiteralExpression(" + 
                string + ", " + type + "): " + 
                "Could not parse string " + string);
        }
        return defaultValue;
    }
    
    /**
     * Create a literal expression for the given number
     * 
     * @param number The number
     * @return The expression
     * @throws IllegalArgumentException if the given number is not a boxed
     * version of the primitive number types.
     */
    public static JExpression createPrimitiveLiteralExpression(Number number)
    {
        if (number instanceof Byte)
        {
            return JExpr.lit(number.byteValue());
        }
        if (number instanceof Short)
        {
            return JExpr.lit(number.shortValue());
        }
        if (number instanceof Integer)
        {
            return JExpr.lit(number.intValue());
        }
        if (number instanceof Long)
        {
            return JExpr.lit(number.longValue());
        }
        if (number instanceof Float)
        {
            return JExpr.lit(number.floatValue());
        }
        if (number instanceof Double)
        {
            return JExpr.lit(number.doubleValue());
        }
        throw new IllegalArgumentException(
            "Unhandled number type: "+number.getClass());
    }
    

    /**
     * Private constructor to prevent instantiation
     */
    private CodeModels()
    {
        // Private constructor to prevent instantiation
    }
}
