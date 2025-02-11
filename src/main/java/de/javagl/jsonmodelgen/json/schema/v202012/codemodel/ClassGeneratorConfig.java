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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A class storing different configuration flags that determine the structure
 * of the classes that are created with a {@link ClassGenerator}.
 */
public class ClassGeneratorConfig
{
    /**
     * When a property with the name <i>PropertyName</i> is an array 
     * (implemented as a collection), then code generator will by
     * default generate a Getter and a Setter for the collection type. 
     * When this flag is <code>true</code>, it will cause to additionally 
     * create methods 
     * <code>add</code><i><code>PropertyName</code></i><code>(...)</code> and
     * <code>remove</code><i><code>PropertyName</code></i><code>(...)</code> 
     * to add or remove individual elements.
     */
    public static final String CREATE_ADDERS_AND_REMOVERS = 
        "CREATE_ADDERS_AND_REMOVERS";
    
    /**
     * When a property with the name <i>PropertyName</i> has a "default" 
     * value in the schema, then this flag will cause a method to be 
     * generated that has the name 
     * <code>default</code><i><code>PropertyName</code></i><code>()</code>
     * and returns this default value.
     */
    public static final String CREATE_GETTERS_WITH_DEFAULT = 
        "CREATE_GETTERS_WITH_DEFAULT";
    
    /**
     * The set of flags that are currently set
     */
    private final Set<String> flags;
    
    /**
     * The mapping from regular expressions (that are applied to schema
     * URIs) to fixed types that should be used for the respective code
     * element.
     * 
     * @see #addTypeOverride(String, Class)
     */
    private final Map<String, Class<?>> typeOverrides;

    /**
     * The mapping from class names that have been determined from the
     * schema, to names that should be used instead.
     * 
     * @see #addClassNameOverride(String, String)
     */
    private final Map<String, String> classNameOverrides;
    
    /**
     * The set of full property names for which validation should be skipped.
     * 
     * @see #setSkippingValidation(String, boolean)
     */
    private final Set<String> skippingValidationFullPropertyNames;
    
    /**
     * The default number type (Float or Double)
     */
    private Class<?> numberType;
    
    /**
     * Creates a new instance with all flags being <code>false</code>
     */
    public ClassGeneratorConfig()
    {
        this.flags = new HashSet<String>();
        this.typeOverrides = new LinkedHashMap<String, Class<?>>();
        this.classNameOverrides = new LinkedHashMap<String, String>();
        this.skippingValidationFullPropertyNames = new HashSet<String>();
        this.numberType = Float.class;
    }
    
    /**
     * Returns whether the specified flag is set
     * 
     * @param flag The flag
     * @return Whether the flag is set
     */
    public boolean is(String flag) 
    {
        return flags.contains(flag);
    }
    
    /**
     * Set the specified flag to have the given value
     * 
     * @param flag The flag
     * @param value The value
     * @return The value that the flag had before this call
     */
    public boolean set(String flag, boolean value)
    {
        if (value)
        {
            return flags.add(flag);
        }
        return flags.remove(flag);
    }
    
    /**
     * Add the given override for a type, based on a regular expression
     * that is matched against the schema URI string.
     * 
     * This is a clumsy workaround for the glTF accessor.min/max properties
     * that are translated to Number[] arrays, to make sure that they can 
     * also contain integer values without losing precision, and still allow 
     * them to be written without trailing ".0" decimals when they are 
     * serialized to JSON.
     * 
     * See https://github.com/KhronosGroup/glTF-Validator/issues/8
     * 
     * @param schemaUriRegex The regular expression for the schema URI
     * @param type The type
     */
    public void addTypeOverride(String schemaUriRegex, Class<?> type)
    {
        typeOverrides.put(schemaUriRegex, type);
    }
    
    /**
     * Returns the type override for the given schema URI, or <code>null</code>
     * if no type override was defined.
     * 
     * @see #addTypeOverride(String, Class)
     * 
     * @param schemaUri The schema URI
     * @return The type override
     */
    public Class<?> getTypeOverride(String schemaUri)
    {
        if (schemaUri == null)
        {
            return null;
        }
        for (Entry<String, Class<?>> entry : typeOverrides.entrySet())
        {
            String key = entry.getKey();
            if (schemaUri.matches(key))
            {
                return entry.getValue();
            }
        }
        return null;
    }
    
    /**
     * Add the given override for a class name.
     * 
     * Certain Java language core class names (like <code>"Class"</code>
     * or <code>"Enum"</code>) should not be used, so they may be overridden
     * here.
     * 
     * @param oldClassName The old class name
     * @param newClassName The new class name
     */
    public void addClassNameOverride(String oldClassName, String newClassName)
    {
        classNameOverrides.put(oldClassName, newClassName);
    }
    
    /**
     * Returns the override for the given class name, or <code>null</code> 
     * if no override was defined.
     * 
     * @see #addClassNameOverride(String, String)
     * 
     * @param oldClassName The old class name
     * @return The new class name, or <code>null</code>
     */
    public String getClassNameOverride(String oldClassName)
    {
        return classNameOverrides.get(oldClassName);
    }
    
    /**
     * Set whether validation should be skipped for a certain property.
     * 
     * The full property name is given as the fully qualified class name,
     * combined with <code>#</code> and the property name. For example,
     * <code>de.javagl.jgltf.impl.v2.Image#mimeType</code>.
     * 
     * Guess why I used this as an example.
     * 
     * @param fullPropertyName The full property name
     * @param skipping Whether validation should be skipped
     * @return The value that the flag had previously
     */
    public boolean setSkippingValidation(
        String fullPropertyName, boolean skipping)
    {
        if (skipping)
        {
            return skippingValidationFullPropertyNames.add(fullPropertyName);
        }
        return skippingValidationFullPropertyNames.remove(fullPropertyName);
        
    }

    /**
     * Returns whether a validation should be skipped for the specified
     * property.
     * 
     * @see #setSkippingValidation(String, boolean)
     * 
     * @param fullPropertyName The full property name
     * @return Whether validation should be skipped
     */
    public boolean isSkippingValidation(String fullPropertyName)
    {
        return skippingValidationFullPropertyNames.contains(fullPropertyName);
    }
    
    /**
     * Set the default number type (Float or Double)
     * 
     * @param numberType The number type
     */
    public void setNumberType(Class<?> numberType)
    {
        this.numberType = numberType;
    }
    
    /**
     * Returns the default number type (Float or Double)
     * 
     * @return The number type
     */
    public Class<?> getNumberType()
    {
        return numberType;
    }
    
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("ClassGeneratorConfig[");
        boolean first = true;
        for (String flag : flags)
        {
            if (!first)
            {
                sb.append(",");
            }
            first = false;
            sb.append(flag);
        }
        sb.append("]");
        return sb.toString();
    }

    
}

