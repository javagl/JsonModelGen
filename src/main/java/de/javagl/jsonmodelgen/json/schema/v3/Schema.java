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
package de.javagl.jsonmodelgen.json.schema.v3;

import java.util.Set;

/**
 * A class representing a JSON schema, version 03, according to the definitions 
 * at <a href="https://tools.ietf.org/html/draft-zyp-json-schema-03">
 * https://tools.ietf.org/html/draft-zyp-json-schema-03</a>
 */
public class Schema
{
    /**
     * TODO: Link descriptions are not implemented yet.<br>
     * <br> 
     * See https://tools.ietf.org/html/draft-zyp-json-schema-03#section-6.1
     */
    private enum LinkDescription{}
    
    /**
     * See {@link #getId()}
     */
    private String id;
    
    /**
     * See {@link #getSchemaString()}
     */
    private String schemaString;
    
    /**
     * See {@link #getTitle()}
     */
    private String title;

    /**
     * See {@link #getDescription()}
     */
    private String description;
    
    /**
     * See {@link #getDefaultString()}
     */
    private String defaultString;

    /**
     * See {@link #getTypeStrings()}
     */
    private Set<String> typeStrings;
    
    /**
     * See {@link #getEnumStrings()}
     */
    private Set<String> enumStrings;
    
    /**
     * See {@link #getExtendsSchema()}
     */
    private Schema extendsSchema;
    
    /**
     * See {@link #getDisallowStrings()}
     */
    private Set<String> disallowStrings;
    
    /**
     * See {@link #getLinks()}
     */
    private Set<LinkDescription> links;
    
    /**
     * See {@link #getFormat()}
     */
    private String format;
    
    /**
     * See {@link #isRequired()}
     */
    private Boolean required;
    
    
    /**
     * Returns whether this schema is a {@link NumberSchema}
     * 
     * @return Whether this schema is a {@link NumberSchema}
     */
    public final boolean isNumber()
    {
        return asNumber() != null;
    }

    /**
     * Returns whether this schema is an {@link IntegerSchema}
     * 
     * @return Whether this schema is an {@link IntegerSchema}
     */
    public final boolean isInteger()
    {
        return asInteger() != null;
    }

    /**
     * Returns whether this schema is a {@link StringSchema}
     * 
     * @return Whether this schema is a {@link StringSchema}
     */
    public final boolean isString()
    {
        return asString() != null;
    }

    /**
     * Returns whether this schema is an {@link ArraySchema}
     * 
     * @return Whether this schema is an {@link ArraySchema}
     */
    public final boolean isArray()
    {
        return asArray() != null;
    }

    /**
     * Returns whether this schema is an {@link ObjectSchema}
     * 
     * @return Whether this schema is an {@link ObjectSchema}
     */
    public final boolean isObject()
    {
        return asObject() != null;
    }

    /**
     * Returns whether this schema is an {@link BooleanSchema}
     * 
     * @return Whether this schema is an {@link BooleanSchema}
     */
    public final boolean isBoolean()
    {
        return asBoolean() != null;
    }
    
    /**
     * Returns this schema as a {@link NumberSchema}, or <code>null</code>
     * if this schema has a different type.
     * 
     * @return This schema with the appropriate type, or <code>null</code>
     */
    public NumberSchema asNumber()
    {
        return null;
    }
    
    /**
     * Returns this schema as an {@link IntegerSchema}, or <code>null</code>
     * if this schema has a different type.
     * 
     * @return This schema with the appropriate type, or <code>null</code>
     */
    public IntegerSchema asInteger()
    {
        return null;
    }

    /**
     * Returns this schema as a {@link StringSchema}, or <code>null</code>
     * if this schema has a different type.
     * 
     * @return This schema with the appropriate type, or <code>null</code>
     */
    public StringSchema asString()
    {
        return null;
    }
    
    /**
     * Returns this schema as an {@link ArraySchema}, or <code>null</code>
     * if this schema has a different type.
     * 
     * @return This schema with the appropriate type, or <code>null</code>
     */
    public ArraySchema asArray()
    {
        return null;
    }

    /**
     * Returns this schema as an {@link ObjectSchema}, or <code>null</code>
     * if this schema has a different type.
     * 
     * @return This schema with the appropriate type, or <code>null</code>
     */
    public ObjectSchema asObject()
    {
        return null;
    }

    /**
     * Returns this schema as a {@link BooleanSchema}, or <code>null</code>
     * if this schema has a different type.
     * 
     * @return This schema with the appropriate type, or <code>null</code>
     */
    public BooleanSchema asBoolean()
    {
        return null;
    }

    
    /**
     * This attribute defines a URI of a JSON Schema that is the schema of
     * the current schema. (The value of the "$schema" property)<br>
     * <br>
     * http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.29
     * 
     * @return The value
     */
    public String getSchemaString()
    {
        return schemaString;
    }

    /**
     * See {@link #getSchemaString()}
     * 
     * @param schemaString The value
     */
    public void setSchemaString(String schemaString)
    {
        this.schemaString = schemaString;
    }
    
    /**
     * This attribute is a string that provides a short description of the
     * instance property.<br>
     * <br>
     * http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.21
     * 
     * @return The value
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * See {@link #getTitle()}
     * 
     * @param title The value
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * This attribute is a string that provides a full description of the of
     * purpose the instance property.<br>
     * <br>
     * http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.22
     * 
     * @return The value
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * See {@link #getDescription()}
     * 
     * @param description The value
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * This attribute defines the default value of the instance when the
     * instance is undefined.<br>
     * <br>
     * http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.20
     * 
     * @return The value
     */
    public String getDefaultString()
    {
        return defaultString;
    }

    /**
     * See {@link #getDefaultString()}
     * 
     * @param defaultString The value
     */
    public void setDefaultString(String defaultString)
    {
        this.defaultString = defaultString;
    }

    /**
     * This attribute defines what the primitive type or the schema of the
     * instance MUST be in order to validate.<br>
     * <br>
     * http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.1<br>
     * <br>
     * The value of this keyword MUST be either a string or an array. If it 
     * is an array, elements of the array MUST be strings and MUST be unique.
     * String values MUST be one of the seven primitive types defined by the 
     * core specification. 
     * 
     * @return The value
     */
    public Set<String> getTypeStrings()
    {
        return typeStrings;
    }

    /**
     * See {@link #getTypeStrings()}
     * 
     * @param typeStrings The value
     */
    public void setTypeStrings(Set<String> typeStrings)
    {
        this.typeStrings = typeStrings;
    }

    
    /**
     * This provides an enumeration of all possible values that are valid
     * for the instance property.<br>
     * <br>  
     * http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.19<br>
     * <br>
     * The value of this keyword MUST be an array. This array MUST have at 
     * least one element. Elements in the array MUST be unique. Elements 
     * in the array MAY be of any type, including null.
     *  
     * @return The value
     */
    public Set<String> getEnumStrings()
    {
        return enumStrings;
    }
    
    /**
     * See {@link #getEnumStrings()}
     * 
     * @param enumStrings The value
     */
    public void setEnumStrings(Set<String> enumStrings)
    {
        this.enumStrings = enumStrings;
    }
    
    /**
     * The value of this property MUST be another schema which will provide
     * a base schema which the current schema will inherit from.<br>
     * <br>
     * https://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.26
     * 
     * @return The value
     */
    public Schema getExtendsSchema()
    {
        return extendsSchema;
    }
    
    /**
     * See {@link #setExtendsSchema(Schema)} 
     * 
     * @param extendsSchema The value
     */
    public void setExtendsSchema(Schema extendsSchema)
    {
        this.extendsSchema = extendsSchema;
    }
    
    /**
     * This attribute takes the same values as the "type" attribute, however
     * if the instance matches the type or if this value is an array and the
     * instance matches any type or schema in the array, then this instance
     * is not valid.<br>
     * <br>
     * https://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.25
     * 
     * @return The value
     */
    public Set<String> getDisallowStrings()
    {
        return disallowStrings;
    }
    
    /**
     * See {@link #getDisallowStrings()}
     * 
     * @param disallow The value
     */
    public void setDisallowStrings(Set<String> disallow)
    {
        this.disallowStrings = disallow;
    }
    
    /**
     * This attribute defines the current URI of this schema (this attribute
     * is effectively a "self" link).<br>
     * <br>
     * https://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.27
     * 
     * @return The value
     */
    public String getId()
    {
        return id;
    }
    
    /**
     * See {@link #getId()}
     *  
     * @param id The value
     */
    public void setId(String id)
    {
        this.id = id;
    }
    
    /**
     * The value of the links property MUST be an array, where each item in
     * the array is a link description object which describes the link
     * relations of the instances.<br>
     * <br>
     * https://tools.ietf.org/html/draft-zyp-json-schema-03#section-6.1
     * 
     * @return The value
     */
    public Set<LinkDescription> getLinks()
    {
        return links;
    }
    
    /**
     * See {@link #getLinks()}
     * 
     * @param links The value
     */
    public void setLinks(Set<LinkDescription> links)
    {
        this.links = links;
    }
    
    /**
     * This property defines the type of data, content type, or microformat
     * to be expected in the instance property values.<br>
     * <br>
     * https://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.23
     * 
     * @return The value
     */
    public String getFormat()
    {
        return format;
    }
    
    /**
     * See {@link #getFormat()}
     * 
     * @param format The value
     */
    public void setFormat(String format)
    {
        this.format = format;
    }

    /**
     * This attribute indicates if the instance must have a value, and not
     * be undefined. This is false by default, making the instance optional.<br>
     * <br>
     * https://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.7<br>
     * <br>
     * TODO: It is not clear whether this belongs here. This is a part that
     * has been refactored between v03 and v04: In v04, it is an array of 
     * property names, and NOT stored for each property individually. 
     * 
     * @return The value
     */
    public Boolean isRequired()
    {
        return required;
    }
    
    /**
     * See {@link #isRequired()}
     * 
     * @param required The value
     */
    public void setRequired(Boolean required)
    {
        this.required = required;
    }
 
}