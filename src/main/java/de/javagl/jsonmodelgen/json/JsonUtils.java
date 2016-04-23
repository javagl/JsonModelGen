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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility methods related to JSON parsing 
 */
public class JsonUtils
{   
    /**
     * The logger used in this class
     */
    private static final Logger logger = 
        Logger.getLogger(JsonUtils.class.getName());
    
    /**
     * The set of valid JSON type strings
     */
    private static final Set<String> VALID_TYPE_STRINGS;
    
    // Initialize the set of valid JSON type strings
    static
    {
        VALID_TYPE_STRINGS = new LinkedHashSet<String>();
        VALID_TYPE_STRINGS.add("null");
        VALID_TYPE_STRINGS.add("any");
        VALID_TYPE_STRINGS.add("object");
        VALID_TYPE_STRINGS.add("array");
        VALID_TYPE_STRINGS.add("boolean");
        VALID_TYPE_STRINGS.add("string");
        VALID_TYPE_STRINGS.add("number");
        VALID_TYPE_STRINGS.add("integer");
    }

    /**
     * Returns the string or string array with the given tag name from the 
     * given node, or the given fallback value if no such element could be 
     * found.
     * 
     * @param node The node
     * @param name The name of the value
     * @param fallback The fallback value
     * @return The value, or the fallback value
     */
    public static Set<String> getStringOrStringArrayAsSetOptional(
        JsonNode node, String name, Set<String> fallback)
    {
        List<String> list = getArrayAsStringsOptional(node, name, null);
        if (list == null)
        {
            return fallback;
        }
        return new LinkedHashSet<String>(list);
    }

    
    /**
     * Returns the string or string array with the given tag name from the 
     * given node, or the given fallback value if no such element could be 
     * found. If the specified child node is a textual node, then a list
     * containing only this text element will be returned. If the specified
     * child node is an array, then the elements of this array will be
     * returned as strings created with <code>JsonNode#toString</code>}
     * 
     * @param node The node
     * @param name The name of the value
     * @param fallback The fallback value
     * @return The value, or the fallback value
     */
    public static List<String> getStringOrStringArrayOptional(
        JsonNode node, String name, List<String> fallback)
    {
        List<String> result = getArrayAsStringsOptional(node, name, null);
        if (result != null)
        {
            return result;
        }
        String string = getStringOptional(node, name, null);
        if (string != null)
        {
            return Collections.singletonList(string);
        }
        return fallback;
    }
    
    /**
     * Returns the string array with the given tag name from the given node,
     * or the given fallback value if no such element could be found.
     * If the specified child node is an array, then the elements 
     * of this array will be returned as a list of strings, converted
     * with <code>JsonNode#toString</code>. 
     * 
     * @param node The node
     * @param name The name of the value
     * @param fallback The fallback value
     * @return The value, or the fallback value
     */
    public static List<String> getArrayAsStringsOptional(
        JsonNode node, String name, List<String> fallback)
    {
        List<JsonNode> list = getArrayElementsOptional(node, name, null);
        if (list == null)
        {
            return fallback;
        }
        return getElementsAsStrings(list);
    }
    
    /**
     * Returns the nodes of the JSON array with the given name in the given
     * node, or the given fallback if no such array exists.
     * 
     * @param node The node
     * @param name The name of the array
     * @param fallback The fallback value
     * @return The array elements, or the fallback value
     */
    public static List<JsonNode> getArrayElementsOptional(
        JsonNode node, String name, List<JsonNode> fallback)
    {
        if (node.has(name))
        {
            JsonNode n = node.get(name);
            if (n.isArray())
            {
                List<JsonNode> result = new ArrayList<JsonNode>();
                for (int i=0; i<n.size(); i++)
                {
                    JsonNode child = n.get(i);
                    result.add(child);
                }
                return result;
            }
        }
        return fallback;
    }
    
    /**
     * Returns a list containing one string for each node of the given 
     * collection, created with <code>JsonNode#toString</code>}
     * 
     * @param nodes The collection
     * @return The strings
     */
    private static List<String> getElementsAsStrings(
        Collection<? extends JsonNode> nodes)
    {
        List<String> result = new ArrayList<String>();
        for (JsonNode n : nodes)
        {
            result.add(n.toString());
        }
        return result;
    }
    
    /**
     * Returns the strings that are contained in the "type" tag of the given
     * node. These value of the type tag may either be a single value or an
     * array. If no type tag is found, then the given fallback is returned.
     * If one of the strings in the returned set is not a valid type, then
     * a warning will be printed.
     * 
     * @param node The node
     * @param fallback The fallback value
     * @return The type strings
     */
    public static Set<String> getTypeStringsOptional(
        JsonNode node, Set<String> fallback)
    {
        List<String> list = getStringOrStringArrayOptional(node, "type", null);
        if (list == null)
        {
            return fallback;
        }
        Set<String> set = new LinkedHashSet<String>();
        for (String s : list)
        {
            String t = dequote(s);
            if (!isValidTypeString(t))
            {
                logger.warning("WARNING: Not a valid type: "+t);
            }
            set.add(t);
        }
        return set;
    }
    
    /**
     * If the given string starts and ends with <code>"</code> quotes, then
     * they are removed and the result is returned. Otherwise, the string
     * itself is returned. This method will also remove potential leading
     * or trailing whitespace of the given string.
     * 
     * @param s The string
     * @return The result
     */
    private static String dequote(String s)
    {
        String t = s.trim();
        if (t.startsWith("\"") && t.endsWith("\""))
        {
            return t.substring(1, t.length()-1);
        }
        return t;
    }
    
    /**
     * Returns whether the given string is a valid JSON type string.
     * That is, whether it is "null", "any", "object", "array", "boolean", 
     * "string", "number" or "integer" 
     * 
     * @param string The string
     * @return Whether the string is a valid JSON type string
     */
    private static boolean isValidTypeString(String string)
    {
        return VALID_TYPE_STRINGS.contains(string);
    }

    /**
     * Returns a Number with the given name from the given node.
     * <p>
     * Returns the given fallback value if there is no such value in the
     * given node.
     * <p>
     * 
     * @param node The node to read the value from
     * @param name The name of the value
     * @param fallback The fallback value
     * @return The specified value, or the fallback value
     */
    public static Number getNumberOptional(
        JsonNode node, String name, Number fallback)
    {
        return getNumberOptional(
            node, name, null, false, null, false, fallback);
    }


    /**
     * Returns a Number with the given name from the given node, validated
     * against the given constraints.
     * <p>
     * Returns the given fallback value if there is no such value in the
     * given node.
     * <p>
     * Returns the given fallback value if the validation fails. 
     * <p>
     * If the <code>min</code> value is <code>null</code>,
     * then the respective validation will be skipped. 
     * 
     * @param node The node to read the value from
     * @param name The name of the value
     * @param min The minimum value, <b>inclusive</b>
     * @param fallback The fallback value
     * @return The specified value, or the fallback value
     */
    public static Number getNumberMinOptional(
        JsonNode node, String name, Number min, Number fallback)
    {
        return getNumberOptional(
            node, name, min, false, null, false, fallback);
    }
    
    /**
     * Returns a Number with the given name from the given node, validated
     * against the given constraints.
     * <p>
     * Returns the given fallback value if there is no such value in the
     * given node.
     * <p>
     * Returns the given fallback value if the validation fails. 
     * <p>
     * If the <code>min</code> or <code>max</code> value are <code>null</code>,
     * then the respective validation will be skipped. 
     * 
     * @param node The node to read the value from
     * @param name The name of the value
     * @param min The minimum value
     * @param minExclusive Whether the minimum is exclusive
     * @param max The maximum value
     * @param maxExclusive Whether the maximum is exclusive
     * @param fallback The fallback value
     * @return The specified value, or the fallback value
     */
    public static Number getNumberOptional(
        JsonNode node, String name, 
        Number min, boolean minExclusive, 
        Number max, boolean maxExclusive, 
        Number fallback)
    {
        if (node.has(name))
        {
            JsonNode n = node.get(name);
            Number result = null;
            if (n.isIntegralNumber())
            {
                result = n.asInt();
            }
            else
            {
                result = n.asDouble();
            }
            return validated(result, 
                min, minExclusive, 
                max, maxExclusive, fallback);
        }
        return fallback;
    }

    /**
     * Returns an Integer with the given name from the given node.
     * <p>
     * Returns the given fallback value if there is no such value in the
     * given node.
     * <p>
     * 
     * @param node The node to read the value from
     * @param name The name of the value
     * @param fallback The fallback value
     * @return The specified value, or the fallback value
     */
    public static Integer getIntegerOptional(
        JsonNode node, String name, Integer fallback)
    {
        return getIntegerOptional(
            node, name, null, false, null, false, fallback);
    }

    /**
     * Returns an Integer with the given name from the given node, validated
     * against the given constraints.
     * <p>
     * Returns the given fallback value if there is no such value in the
     * given node.
     * <p>
     * Returns the given fallback value if the validation fails. 
     * <p>
     * If the <code>min</code> value is <code>null</code>,
     * then the respective validation will be skipped. 
     * 
     * @param node The node to read the value from
     * @param name The name of the value
     * @param min The minimum value, <b>inclusive</b>
     * @param fallback The fallback value
     * @return The specified value, or the fallback value
     */
    public static Integer getIntegerMinOptional(
        JsonNode node, String name, Integer min, Integer fallback)
    {
        return getIntegerOptional(
            node, name, min, false, null, false, fallback);
    }

    /**
     * Returns an Integer with the given name from the given node, validated
     * against the given constraints.
     * <p>
     * Returns the given fallback value if there is no such value in the
     * given node.
     * <p>
     * Returns the given fallback value if the validation fails. 
     * <p>
     * If the <code>min</code> or <code>max</code> value are <code>null</code>,
     * then the respective validation will be skipped. 
     * 
     * @param node The node to read the value from
     * @param name The name of the value
     * @param min The minimum value
     * @param minExclusive Whether the minimum is exclusive
     * @param max The maximum value
     * @param maxExclusive Whether the maximum is exclusive
     * @param fallback The fallback value
     * @return The specified value, or the fallback value
     */
    static Integer getIntegerOptional(
        JsonNode node, String name, 
        Number min, boolean minExclusive, 
        Number max, boolean maxExclusive, 
        Integer fallback)
    {
        if (node.has(name))
        {
            JsonNode n = node.get(name);
            if (n.isIntegralNumber())
            {
                Integer result = n.asInt();
                return validated(result, 
                    min, minExclusive, 
                    max, maxExclusive, fallback);
            }
        }
        return fallback;
    }

    /**
     * Returns the given number, validated according to the given constraints,
     * or the given fallback value if the number is not valid.
     * <p>
     * If the <code>min</code> or <code>max</code> value are <code>null</code>,
     * then the respective validation will be skipped. 
     * <p>
     * The number will be checked against the constraints by converting it to  
     * a <code>double</code> value. 
     * <p>
     * If the validation fails, a warning will be printed.
     * 
     * @param number The input number
     * @param min The minimum value
     * @param minExclusive Whether the minimum is exclusive
     * @param max The maximum value
     * @param maxExclusive Whether the maximum is exclusive
     * @param fallback The fallback value
     * @return The validated input number, or the fallback value
     */
    static <T extends Number> T validated(
        T number, 
        Number min, boolean minExclusive, 
        Number max, boolean maxExclusive, 
        T fallback)
    {
        if (number == null)
        {
            return fallback;
        }
        if (min != null)
        {
            if (minExclusive)
            {
                if (number.doubleValue() <= min.doubleValue())
                {
                    logger.warning(
                        "Value is "+number+", " + 
                        "minimum (exclusive?"+minExclusive+") " +
                        "is "+min+", returning "+fallback);
                    return fallback;
                }
            }
            else
            {
                if (number.doubleValue() < min.doubleValue())
                {
                    logger.warning(
                        "Value is "+number+", " +
                        "minimum (exclusive?"+minExclusive+") "+
                        "is "+min+", returning "+fallback);
                    return fallback;
                }
            }
        }
        if (max != null)
        {
            if (maxExclusive)
            {
                if (number.doubleValue() >= max.doubleValue())
                {
                    logger.warning(
                        "Value is "+number+", "+
                        "maximum (exclusive?"+maxExclusive+") "+
                        "is "+max+", returning "+fallback);
                    return fallback;
                }
            }
            else
            {
                if (number.doubleValue() > max.doubleValue())
                {
                    logger.warning(
                        "Value is "+number+", "+
                        "maximum (exclusive?"+maxExclusive+") "+
                        "is "+max+", returning "+fallback);
                    return fallback;
                }
            }
        }
        return number;
    }

    /**
     * Returns a String with the given name from the given node, or 
     * the given fallback if no such element is contained in the node
     *  
     * @param node The node
     * @param name The name of the element
     * @param fallback The fallback value
     * @return The value, or the fallback value
     */
    public static String getStringOptional(
        JsonNode node, String name, String fallback)
    {
        if (node.has(name))
        {
            JsonNode n = node.get(name);
            return n.asText();
        }
        return fallback;
    }

    /**
     * Returns a Boolean with the given name from the given node, or 
     * the given fallback if no such element is contained in the node
     *  
     * @param node The node
     * @param name The name of the element
     * @param fallback The fallback value
     * @return The value, or the fallback value
     */
    public static Boolean getBooleanOptional(
        JsonNode node, String name, Boolean fallback)
    {
        if (node.has(name))
        {
            JsonNode n = node.get(name);
            return n.asBoolean();
        }
        return fallback;
    }
    
    /**
     * Read the JSON node from the given URI. Returns <code>null</code> if
     * the node could not be read for any reason. Will print a warning if
     * no node could be read.
     *  
     * @param uri The URI to read from
     * @return The JSON node, or <code>null</code>
     */
    public static JsonNode readNodeOptional(URI uri)
    {
        Exception exception = null;
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode refNode = mapper.readValue(uri.toURL(), JsonNode.class);
            return refNode;
        }
        catch (JsonParseException e)
        {
            exception = e;
        }
        catch (JsonMappingException e)
        {
            exception = e;
        }
        catch (IOException e)
        {
            exception = e;
        }
        logger.warning(
            "Could not read schema from "+uri+
            " - skipping ("+exception.getMessage()+")");
        return null;
    }


    /**
     * Private constructor to prevent instantiation
     */
    private JsonUtils()
    {
        // Private constructor to prevent instantiation
    }
}
