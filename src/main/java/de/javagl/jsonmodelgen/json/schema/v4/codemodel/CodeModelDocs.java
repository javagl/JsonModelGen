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
package de.javagl.jsonmodelgen.json.schema.v4.codemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import de.javagl.jsonmodelgen.json.schema.v4.ArraySchema;
import de.javagl.jsonmodelgen.json.schema.v4.NumberSchema;
import de.javagl.jsonmodelgen.json.schema.v4.Schema;

/**
 * Methods to create JavaDocs based on a {@link Schema} 
 */
class CodeModelDocs
{
    /**
     * The logger used in this class
     */
    private static final Logger logger = 
        Logger.getLogger(ClassGenerator.class.getName());
    
    /**
     * The maximum length for a line in a JavaDoc comment
     */
    static final int MAX_COMMENT_LINE_LENGTH = 70;
    
    /**
     * Create the JavaDoc-description for the specified property in the
     * given class
     * 
     * @param className The target class
     * @param propertyName The property name
     * @param propertySchema The property schema
     * @param isRequired Whether the property is required
     * @return The string
     */
    static String createJavaDocDescription(
        String className, String propertyName, 
        Schema propertySchema, boolean isRequired)
    {
        return createJavaDoc(createDescriptionLines(
            className, propertyName, propertySchema, isRequired));
    }
    
    /**
     * Create a JavaDoc description from the given lines. The result will
     * be a single string, where each line is separated by a HTML
     * line break: <code>&lt;br></code>
     *  
     * @param lines The input lines
     * @return The result
     */
    static String createJavaDoc(List<String> lines)
    {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<lines.size(); i++)
        {
            if (i > 0)
            {
                sb.append("<br>\n");
            }
            sb.append(lines.get(i));
        }
        return sb.toString();
    }
    
    /**
     * Perform a basic sanitation of the given line for the use in JavaDoc.
     * This mainly replaces HTML characters with their HTML equivalent.
     * 
     * @param line The input line
     * @return The result
     */
    private static String sanitizeJavaDoc(String line)
    {
        String result = line;
        result = result.replaceAll("<", "&lt;");
        result = result.replaceAll(">", "&gt;");
        return result;
    }
    
    /**
     * Create a list of strings, each being one line of the description of
     * the specified property for the given class. These lines will summarize
     * the constraints that are derived from the given {@link Schema}, e.g.
     * the valid values for the fields and other constraints.
     * 
     * @param className The target class
     * @param propertyName The property name
     * @param propertySchema The property schema
     * @param isRequired Whether the property is required
     * @return The strings
     */
    private static List<String> createDescriptionLines(
        String className, String propertyName, Schema propertySchema,
        boolean isRequired)
    {
        List<String> descriptionLines = new ArrayList<String>();
        
        descriptionLines.addAll(createBasicDescription(
            className, propertyName, propertySchema, isRequired));
        
        if (propertySchema.isNumber())
        {
            NumberSchema numberPropertySchema = propertySchema.asNumber();
            descriptionLines.addAll(
                createNumberPropertyDescription(numberPropertySchema));
        }
        if (propertySchema.isArray())
        {
            ArraySchema arrayPropertySchema = propertySchema.asArray();
            descriptionLines.addAll(
                createArrayPropertyDescription(arrayPropertySchema));
        }
        return descriptionLines;
    }

    /**
     * Creates a list of strings containing the basic description of the 
     * given property in the specified class.<br>
     * <br>
     * These lines will include information about the property name, the
     * owning class, whether the property is "required" or "optional", 
     * possible default values, and (for enum typed properties), the
     * set of valid values. 
     * 
     * @param className The target class
     * @param propertyName The property name
     * @param propertySchema The property schema
     * @param isRequired Whether the property is required
     * @return The strings
     */
    private static List<String> createBasicDescription(
        String className, String propertyName, Schema propertySchema, 
        boolean isRequired)
    {
        List<String> descriptionLines = new ArrayList<String>();
        String propertyDescription = propertySchema.getDescription();
        if (propertyDescription == null)
        {
            propertyDescription = "The "+propertyName+" of this "+className;
        }
        propertyDescription = sanitizeJavaDoc(propertyDescription);
        if (isRequired)
        {
            propertyDescription += " (required)";
        }
        else
        {
            propertyDescription += " (optional)";
        }
        descriptionLines.add(propertyDescription);
        
        if (propertySchema.getDefaultString() != null)
        {
            descriptionLines.add("Default: " + 
                propertySchema.getDefaultString());
        }
        if (propertySchema.getEnumStrings() != null)
        {
            descriptionLines
                .add("Valid values: " + propertySchema.getEnumStrings());
        }
        else
        {
            List<String> nonObjectEnumStrings =
                SchemaCodeUtils.determineEnumStringsFromAnyOf(propertySchema);
            if (nonObjectEnumStrings != null && !nonObjectEnumStrings.isEmpty())
            {
                descriptionLines.add("Valid values: " + nonObjectEnumStrings);
            }
        }
        
        return descriptionLines;
    }
    
    /**
     * Creates a list of strings containing the description of the 
     * given {@link NumberSchema}<br>
     * <br>
     * These lines will include information about the possible minimum
     * and maximum values, and whether the minimum and maximum values
     * are inclusive or exclusive.
     * 
     * @param numberPropertySchema The {@link NumberSchema}
     * @return The strings
     */
    private static List<String> createNumberPropertyDescription(
        NumberSchema numberPropertySchema)
    {
        List<String> descriptionLines = new ArrayList<String>();
        if (numberPropertySchema.getMinimum() != null)
        {
            String minimumDescription = 
                "Minimum: " + numberPropertySchema.getMinimum();
            if (numberPropertySchema.isExclusiveMinimum() == Boolean.TRUE)
            {
                minimumDescription += " (exclusive)";
            }
            else
            {
                minimumDescription += " (inclusive)";
            }
            descriptionLines.add(minimumDescription);
        }
        if (numberPropertySchema.getMaximum() != null)
        {
            String maximumDescription = "Maximum: " + 
                numberPropertySchema.getMaximum();
            if (numberPropertySchema.isExclusiveMaximum() == Boolean.TRUE)
            {
                maximumDescription += " (exclusive)";
            }
            else
            {
                maximumDescription += " (inclusive)";
            }
            descriptionLines.add(maximumDescription);
        }
        return descriptionLines; 
    }
    
    /**
     * Creates a list of strings containing the description of the
     * given {@link ArraySchema}<br>
     * <br>
     * These lines will include information about the minimum and maximum 
     * number of items, as well as descriptions of the types of the
     * array elements.
     * 
     * @param arrayPropertySchema The {@link ArraySchema}
     * @return The strings
     */
    private static List<String> createArrayPropertyDescription(
        ArraySchema arrayPropertySchema)
    {
        List<String> descriptionLines = new ArrayList<String>();
        
        Integer minItems = arrayPropertySchema.getMinItems();
        Integer maxItems = arrayPropertySchema.getMaxItems();
        if (minItems != null && maxItems != null &&
            minItems.equals(maxItems))
        {
            descriptionLines.add("Number of items: " + minItems);
        }
        else
        {
            if (minItems != null)
            {
                descriptionLines.add("Minimum number of items: " + minItems);
            }
            if (maxItems != null)
            {
                descriptionLines.add("Maximum number of items: " + maxItems);
            }
        }
        Collection<Schema> itemSchemas = arrayPropertySchema.getItems();
        if (itemSchemas.size() != 1)
        {
            logger.warning("Found "+itemSchemas.size()+
                " item schemas. Only 1 is supported.");
        }
        if (!itemSchemas.isEmpty())
        {
            descriptionLines.add("Array elements:");
            Schema itemSchema = itemSchemas.iterator().next();
            List<String> itemDescriptionLines = 
                createDescriptionLines(
                    "array", "elements", itemSchema, false);
            for (String itemDescription : itemDescriptionLines)
            {
                descriptionLines.add("&nbsp;&nbsp;"+itemDescription);
            }
        }
        return descriptionLines;
    }

    /**
     * Private constructor to prevent instantiation
     */
    private CodeModelDocs()
    {
        // Private constructor to prevent instantiation
    }
}
