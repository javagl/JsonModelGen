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

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JType;

import de.javagl.jsonmodelgen.json.schema.v202012.Schema;

/**
 * A package-private class summarizing information about a property
 * during code generation.
 */
class PropertyInfo
{
    /**
     * The target class
     */
    private final JDefinedClass definedClass;
    
    /**
     * The property name (will be the field name)
     */
    private final String propertyName;
    
    /**
     * The property type
     */
    private final JType propertyType;
    
    /**
     * The property schema
     */
    private final Schema propertySchema;
    
    /**
     * Whether the property is required
     */
    private final boolean isRequired;
    
    /**
     * Creates a new instance
     * 
     * @param definedClass The target class
     * @param propertyName The property name (will be the field name)
     * @param propertyType The property type
     * @param propertySchema The property schema
     * @param isRequired Whether the property is required
     */
    PropertyInfo(JDefinedClass definedClass, String propertyName,
        JType propertyType, Schema propertySchema, boolean isRequired)
    {
        this.definedClass = definedClass;
        this.propertyName = propertyName;
        this.propertyType = propertyType;
        this.propertySchema = propertySchema;
        this.isRequired = isRequired;
    }
    
    /**
     * Returns the target class
     * 
     * @return The target class
     */
    JDefinedClass getDefinedClass()
    {
        return definedClass;
    }
    
    /**
     * Returns the property name (will be the field name)
     * 
     * @return The property name (will be the field name)
     */
    String getPropertyName()
    {
        return propertyName;
    }
    
    /**
     * Returns the property type
     * 
     * @return The property type
     */
    JType getPropertyType()
    {
        return propertyType;
    }
    
    /**
     * Returns the property schema
     * 
     * @return The property schema
     */
    Schema getPropertySchema()
    {
        return propertySchema;
    }
    
    /**
     * Returns whether the property is required
     * 
     * @return Whether the property is required
     */
    boolean isRequired()
    {
        return isRequired;
    }
    
}
