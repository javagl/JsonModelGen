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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Specialization of a {@link Schema} for object types. 
 */
public class ObjectSchema extends Schema
{
    /**
     * See {@link #getMaxProperties()}
     */
    private Integer maxProperties;
    
    /**
     * See {@link #getMinProperties()}
     */
    private Integer minProperties;

    /**
     * See {@link #getRequired()}
     */
    private List<String> required;
    
    /**
     * See {@link #getAdditionalProperties()}
     */
    private Schema additionalProperties;

    /**
     * See {@link #getProperties()}
     */
    private Map<String, Schema> properties;

    /**
     * See {@link #getPatternProperties()}
     */
    private Map<String, Schema> patternProperties;

    /**
     * See {@link #getDependentRequired()}
     */
    private Map<String, Set<String>> dependentRequired;

    /**
     * Whether this object represents an "any" type
     */
    private final boolean any;

    /**
     * Default constructor
     */
    public ObjectSchema()
    {
        this(false);
    }

    /**
     * Creates a new instance
     *
     * @param any Whether this instance should represent an "any" type
     */
    public ObjectSchema(boolean any)
    {
        this.any = any;
    }

    /**
     * Whether this instance represents an "any" type
     *
     * @return Whether this instance represents an "any" type
     */
    public boolean isAny()
    {
        return any;
    }

    @Override
    public ObjectSchema asObject()
    {
        return this;
    }
    
    /**
     * An object instance is valid against "maxProperties" if its number of
     * properties is less than, or equal to, the value of this keyword.
     * If this keyword is not present, it may be considered present with a 
     * value of 0.
     * 
     * @return The value
     */
    public Integer getMaxProperties()
    {
        return maxProperties;
    }
    
    /**
     * See {@link #getMaxProperties()}
     * 
     * @param maxProperties The value
     */
    public void setMaxProperties(Integer maxProperties)
    {
        this.maxProperties = maxProperties;
    }

    /**
     * An object instance is valid against "minProperties" if its number of
     * properties is greater than, or equal to, the value of this keyword.
     * If this keyword is not present, it may be considered present with a 
     * value of 0.
     * 
     * @return The value
     */
    public Integer getMinProperties()
    {
        return minProperties;
    }
    
    /**
     * See {@link #getMinProperties()}
     * 
     * @param minProperties The value
     */
    public void setMinProperties(Integer minProperties)
    {
        this.minProperties = minProperties;
    }
    
    /**
     * An object instance is valid against this keyword if its property set
     * contains all elements in this keyword's array value.
     * <br>
     * https://json-schema.org/draft/2020-12/json-schema-validation.html#rfc.section.6.5.3
     *
     * @return The value
     */
    public List<String> getRequired()
    {
        return required;
    }

    /**
     * See {@link #getRequired()}
     *
     * @param required The value
     */
    public void setRequired(List<String> required)
    {
        this.required = required;
    }
    
    /**
     * This attribute defines a schema for all properties that are not
     * explicitly defined in an object type definition.<br>
     * <br>
     * https://json-schema.org/draft/2020-12/json-schema-core.html#additionalProperties
     *
     * @return The value
     */
    public Schema getAdditionalProperties()
    {
        return additionalProperties;
    }

    /**
     * See {@link #getAdditionalProperties()}
     *
     * @param additionalProperties The value
     */
    public void setAdditionalProperties(Schema additionalProperties)
    {
        this.additionalProperties = additionalProperties;
    }

    /**
     * This attribute is an object with property definitions that define the
     * valid values of instance object property values.<br>
     * <br>
     * https://json-schema.org/draft/2020-12/json-schema-core.html#rfc.section.10.3.2.1
     *
     * @return The value
     */
    public Map<String, Schema> getProperties()
    {
        return properties;
    }

    /**
     * See {@link #getProperties()}
     *
     * @param properties The value
     */
    public void setProperties(Map<String, Schema> properties)
    {
        this.properties = properties;
    }

    /**
     * This attribute is an object that defines the schema for a set of
     * property names of an object instance.<br>
     * <br>
     * https://json-schema.org/draft/2020-12/json-schema-core.html#rfc.section.10.3.2.2
     *
     * @return The value
     */
    public Map<String, Schema> getPatternProperties()
    {
        return patternProperties;
    }

    /**
     * See {@link #getPatternProperties()}
     *
     * @param patternProperties The value
     */
    public void setPatternProperties(Map<String, Schema> patternProperties)
    {
        this.patternProperties = patternProperties;
    }

    /**
     * This attribute is an object that defines the requirements of a
     * property on an instance object.<br>
     * <br>
     * https://json-schema.org/draft/2020-12/json-schema-validation.html#rfc.section.6.5.4
     *
     * @return The value
     */
    public Map<String, Set<String>> getDependentRequired()
    {
        return dependentRequired;
    }

    /**
     * See {@link #getDependentRequired()}
     *
     * @param dependentRequired The value
     */
    public void setDependentRequired(Map<String, Set<String>> dependentRequired)
    {
        this.dependentRequired = dependentRequired;
    }
}