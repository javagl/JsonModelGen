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

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utility methods related to {@link Schema} instances, mainly intended
 * for debugging
 */
public class SchemaUtils
{
    /**
     * Create a short, unspecified debugging string for the given
     * {@link Schema}s, summarizing the most important information
     * about them
     *
     * @param schemas The {@link Schema}s
     * @return The debugging string
     */
    public static String createShortSchemaDebugString(
        Iterable<? extends Schema> schemas)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if (schemas != null)
        {
            int n = 0;
            for (Schema schema : schemas)
            {
                if (n > 0)
                {
                    sb.append(", ");
                }
                sb.append(createShortSchemaDebugString(schema));
                n++;
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Create a short, unspecified debugging string for the given
     * {@link Schema}, summarizing the most important information
     * about it
     *
     * @param schema The {@link Schema}
     * @return The debugging string
     */
    public static String createShortSchemaDebugString(Schema schema)
    {
        if (schema == null)
        {
            return "null";
        }
        return "Schema[" +
            "type="+schema.getClass().getSimpleName()+", " +
            "title="+schema.getTitle()+", " +
            "id="+schema.getId()+", " +
            "description="+schema.getDescription()+"]";
    }

    /**
     * Print the {@link #createSchemaDebugString(URI, Schema)} to the console
     *
     * @param uri The URI
     * @param schema The {@link Schema}
     */
    public static void printSchemaDebugString(URI uri, Schema schema)
    {
        System.out.println(createSchemaDebugString(uri, schema));
    }

    /**
     * Create an elaborate, unspecified debugging string representation of
     * the given {@link Schema}
     *
     * @param uri The {@link Schema} URI
     * @param schema The {@link Schema}
     * @return The string
     */
    public static String createSchemaDebugString(URI uri, Schema schema)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Schema for "+uri+" is "+
            createShortSchemaDebugString(schema)).append("\n");
        sb.append("{").append("\n");
        append(sb, "$schema", schema.getSchemaString(), ",\n");
        append(sb, "id", schema.getId(), ",\n");
        append(sb, "title", schema.getTitle(), ",\n");
        append(sb, "description", schema.getDescription(), ",\n");
        append(sb, "default", schema.getDefaultString(), ",\n");
        append(sb, "type", schema.getTypeStrings(), ",\n");
        append(sb, "enum", schema.getEnumStrings(), ",\n");
        append(sb, "$ref",
            createShortSchemaDebugString(schema.getRef()), ",\n");
        append(sb, "allOf",
            createShortSchemaDebugString(schema.getAllOf()), ",\n");
        append(sb, "anyOf",
            createShortSchemaDebugString(schema.getAnyOf()), ",\n");
        append(sb, "oneOf",
            createShortSchemaDebugString(schema.getOneOf()), ",\n");
        append(sb, "not",
            createShortSchemaDebugString(schema.getNot()), ",\n");
        append(sb, "format", schema.getFormat(), ",\n");

        if (schema.isString())
        {
            StringSchema stringSchema = schema.asString();
            append(sb, "maxLength", stringSchema.getMaxLength(), ",\n");
            append(sb, "minLength", stringSchema.getMinLength(), ",\n");
            append(sb, "pattern", stringSchema.getPattern(), ",\n");
        }
        if (schema.isArray())
        {
            ArraySchema arraySchema = schema.asArray();
            append(sb, "prefixItems",
                arraySchema.getPrefixItems(), ",\n");
            append(sb, "items", arraySchema.getItems(), ",\n");
            append(sb, "maxItems", arraySchema.getMaxItems(), ",\n");
            append(sb, "minItems", arraySchema.getMinItems(), ",\n");
            append(sb, "uniqueItems", arraySchema.getUniqueItems(), ",\n");
        }
        if (schema.isNumber())
        {
            NumberSchema numericSchema = schema.asNumber();
            append(sb, "multipleOf", numericSchema.getMultipleOf(), ",\n");
            append(sb, "maximum", numericSchema.getMaximum(), ",\n");
            append(sb, "exclusiveMaximum",
                numericSchema.getExclusiveMaximum(), ",\n");
            append(sb, "minimum", numericSchema.getMinimum(), ",\n");
            append(sb, "exclusiveMinimum",
                numericSchema.getExclusiveMinimum(), ",\n");
        }
        if (schema.isObject())
        {
            ObjectSchema objectSchema = schema.asObject();
            append(sb, "required", objectSchema.getRequired(), ",\n");
            append(sb, "additionalProperties",
                objectSchema.getAdditionalProperties(), ",\n");
            append(sb, "properties", objectSchema.getProperties(), ",\n");
            append(sb, "patternProperties",
                objectSchema.getPatternProperties(), ",\n");
            append(sb, "dependentRequired", 
                objectSchema.getDependentRequired(), ",\n");
        }

        sb = new StringBuilder(sb.subSequence(0, sb.length()-2));
        sb.append("\n");
        sb.append("}");
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Append a property description to the given string builder
     *
     * @param sb The string builder
     * @param propertyName The property name
     * @param value The property value
     * @param end The string that should be appended at the end
     */
    private static void append(
        StringBuilder sb, String propertyName, Object value, String end)
    {
        if (value == null)
        {
            return;
        }
        if (value instanceof String)
        {
            sb.append("    " + "\"" + propertyName + "\"" +
                ":\"" + value + "\"").append(end);
        }
        else if (value instanceof Collection<?>)
        {
            Collection<?> collection = (Collection<?>) value;
            sb.append("    " + "\"" + propertyName + "\"" + ":" +
                createString(collection)).append(end);
        }
        else if (value instanceof Map<?, ?>)
        {
            sb.append("    " + "\"" + propertyName + "\"" + ":").append("\n");
            sb.append("    " + "{").append("\n");
            Map<?, ?> map = (Map<?, ?>) value;
            for (Entry<?, ?> entry : map.entrySet())
            {
                Object entryValue = entry.getValue();
                if (entryValue instanceof Schema)
                {
                    Schema entryValueSchema = (Schema)entryValue;
                    sb.append("        " + "\"" + entry.getKey() + "\":" +
                        createShortSchemaDebugString(entryValueSchema) + "")
                        .append(",\n");
                }
                else
                {
                    sb.append("        " + "\"" + entry.getKey() + "\":" +
                        entryValue + "").append(",\n");
                }

            }
            sb.append("    " + "}").append(end);
        }
        else if (value instanceof Schema)
        {
            Schema schema = (Schema)value;
            sb.append(
                "    " + "\"" + propertyName + "\"" + ":" +
                createShortSchemaDebugString(schema)).append(end);
        }
        else
        {
            sb.append(
                "    " + "\"" + propertyName + "\"" + ":" + value).append(end);
        }
    }

    /**
     * Create a string representation of the given collection, enclosing
     * <code>String</code> elements in <code>"quotes"</code>
     *
     * @param collection The collection
     * @return The string
     */
    private static String createString(Collection<?> collection)
    {
        if (collection == null)
        {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        int n = 0;
        for (Object object : collection)
        {
            if (n > 0)
            {
                sb.append(",");
            }
            if (object instanceof String)
            {
                sb.append("\"").append(object).append("\"");
            }
            else
            {
                sb.append(object);
            }
            n++;
        }
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Returns whether the given property name is contained in the
     * {@link ObjectSchema#getRequired()} list (or <code>false</code> if
     * this list is <code>null</code>)
     * 
     * @param schema The {@link ObjectSchema}
     * @param propertyName The property name
     * @return Whether the property is required
     */
    public static boolean isRequired(ObjectSchema schema, String propertyName)
    {
        List<String> required = schema.getRequired();
        if (required == null)
        {
            return false;
        }
        return required.contains(propertyName);
    }
    

    /**
     * Private constructor to prevent instantiation
     */
    private SchemaUtils()
    {
        // Private constructor to prevent instantiation
    }


}
