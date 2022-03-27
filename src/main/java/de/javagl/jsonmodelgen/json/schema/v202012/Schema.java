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
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class representing a JSON schema, version 2020-12, according to the 
 * definitions https://json-schema.org/draft/2020-12/json-schema-core.html
 */
public class Schema
{
    /**
     * The URI
     */
    private URI uri;
    
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
     * See {@link #getRef()}
     */
    private Schema ref;
    
    /**
     * See {@link #getAllOf()}
     */
    private List<Schema> allOf;

    /**
     * See {@link #getAnyOf()}
     */
    private List<Schema> anyOf;

    /**
     * See {@link #getOneOf()}
     */
    private List<Schema> oneOf;

    /**
     * See {@link #getNot()}
     */
    private Schema not;

    /**
     * See {@link #getDefinitions()}
     */
    private Map<String, Schema> definitions;
    
    /**
     * See {@link #getFormat()}
     */
    private String format;

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
     * Returns the URI
     * 
     * @return The URI
     */
    public URI getUri()
    {
        return uri;
    }
    
    /**
     * See {@link #getUri()}
     * 
     * @param uri The value
     */
    public void setUri(URI uri)
    {
        this.uri = uri;
    }

    /**
     * The "$schema" keyword is both used as a JSON Schema version
     * identifier and the location of a resource which is itself a JSON
     * Schema, which describes any schema written for this particular 
     * version. (The value of the "$schema" property)<br>
     * <br>
     * https://json-schema.org/draft/2020-12/json-schema-core.html#rfc.section.8.1.1
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
     * This keyword can be used to supply a default JSON value associated
     * with a particular schema. <br>
     * <br>
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
     * Returns the reference
     * 
     * @return The reference
     */
    public Schema getRef()
    {
        return ref;
    }
    
    /**
     * See {@link #getRef()}
     * 
     * @param ref The value
     */
    public void setRef(Schema ref)
    {
        this.ref = ref;
    }
    
    /**
     * An instance validates successfully against this keyword if it
     * validates successfully against all schemas defined by this keyword's
     * value.
     * <br>
     * @return The value
     */
    public List<Schema> getAllOf()
    {
        return allOf;
    }

    /**
     * See {@link #getAllOf()}
     *
     * @param allOf The value
     */
    public void setAllOf(List<Schema> allOf)
    {
        this.allOf = allOf;
    }
    
    /**
     * An instance validates successfully against this keyword if it
     * validates successfully against at least one schema defined by this 
     * keyword's value.
     * <br>
     * @return The value
     */
    public List<Schema> getAnyOf()
    {
        return anyOf;
    }

    /**
     * See {@link #getAnyOf()}
     *
     * @param anyOf The value
     */
    public void setAnyOf(List<Schema> anyOf)
    {
        this.anyOf = anyOf;
    }

    /**
     * An instance validates successfully against this keyword if it
     * validates successfully against exactly one schema defined by this
     * keyword's value.
     * <br>
     * @return The value
     */
    public List<Schema> getOneOf()
    {
        return oneOf;
    }

    /**
     * See {@link #getOneOf()}
     *
     * @param oneOf The value
     */
    public void setOneOf(List<Schema> oneOf)
    {
        this.oneOf = oneOf;
    }

    /**
     * An instance is valid against this keyword if it fails to validate
     * successfully against the schema defined by this keyword.
     * <br>
     * @return The value
     */
    public Schema getNot()
    {
        return not;
    }

    /**
     * See {@link #getNot()}
     *
     * @param not The value
     */
    public void setNot(Schema not)
    {
        this.not = not;
    }
    
    /**
     * This keyword plays no role in validation per se.  Its role is to
     * provide a standardized location for schema authors to inline JSON
     * Schemas into a more general schema.
     * <br>
     * @return The value
     */
    public Map<String, Schema> getDefinitions()
    {
        return definitions;
    }

    /**
     * See {@link #getDefinitions()}
     *
     * @param definitions The value
     */
    public void setDefinitions(Map<String, Schema> definitions)
    {
        this.definitions = definitions;
    }
    
    /**
     * This attribute defines the current URI of this schema.<br>
     * 
     * https://json-schema.org/draft/2020-12/json-schema-core.html#rfc.section.8.2.1
     * <br>
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
     * The "format" keyword is defined to allow interoperable semantic 
     * validation for a fixed subset of values which are accurately described
     * by authoritative resources
     * <br>
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

}