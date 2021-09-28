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

import java.util.Collection;

/**
 * Specialization of a {@link Schema} for array types.
 *  
 * https://json-schema.org/draft/2020-12/json-schema-core.html#rfc.section.10.3.1
 */
public class ArraySchema extends Schema
{
    /**
     * See {@link #getPrefixItems()}
     */
    private Collection<Schema> prefixItems;

    /**
     * See {@link #getItems()}
     */
    private Schema items;

    /**
     * See {@link #getMaxItems()}
     */
    private Integer maxItems;

    /**
     * See {@link #getMinItems()}
     */
    private Integer minItems;

    /**
     * See {@link #getUniqueItems()}
     */
    private Boolean uniqueItems;

    @Override
    public ArraySchema asArray()
    {
        return this;
    }

    /**
     * An array where each item is a schema that corresponds 
     * to each index of the document’s array. 
     * 
     * https://json-schema.org/draft/2020-12/json-schema-core.html#rfc.section.10.3.1.1
     *
     * @return The value
     */
    public Collection<Schema> getPrefixItems()
    {
        return prefixItems;
    }

    /**
     * See {@link #getPrefixItems()}
     *
     * @param prefixItems The value
     */
    public void setPrefixItems(Collection<Schema> prefixItems)
    {
        this.prefixItems = prefixItems;
    }

    /**
     * This attribute defines the allowed items in an instance array.<br>
     * <br>
     * https://json-schema.org/draft/2020-12/json-schema-core.html#rfc.section.10.3.1.2
     *
     * @return The value
     */
    public Schema getItems()
    {
        return items;
    }

    /**
     * See {@link #getItems()}
     *
     * @param items The value
     */
    public void setItems(Schema items)
    {
        this.items = items;
    }

    /**
     * This attribute defines the maximum number of values in an array when
     * the array is the instance value.<br>
     * <br>
     * https://json-schema.org/draft/2020-12/json-schema-validation.html#rfc.section.6.4.1
     *
     * @return The value
     */
    public Integer getMaxItems()
    {
        return maxItems;
    }

    /**
     * See {@link #getMaxItems()}
     *
     * @param maxItems The value
     */
    public void setMaxItems(Integer maxItems)
    {
        this.maxItems = maxItems;
    }

    /**
     * This attribute defines the minimum number of values in an array when
     * the array is the instance value.<br>
     * <br>
     * https://json-schema.org/draft/2020-12/json-schema-validation.html#rfc.section.6.4.2
     *
     * @return The value
     */
    public Integer getMinItems()
    {
        return minItems;
    }

    /**
     * See {@link #getMinItems()}
     *
     * @param minItems The value
     */
    public void setMinItems(Integer minItems)
    {
        this.minItems = minItems;
    }

    /**
     * This attribute indicates that all items in an array instance MUST be
     * unique (contains no two identical values).<br>
     * <br>
     * https://json-schema.org/understanding-json-schema/reference/array.html#uniqueness
     *
     * @return The value
     */
    public Boolean getUniqueItems()
    {
        return uniqueItems;
    }

    /**
     * See {@link #getUniqueItems()}
     *
     * @param uniqueItems The value
     */
    public void setUniqueItems(Boolean uniqueItems)
    {
        this.uniqueItems = uniqueItems;
    }
}