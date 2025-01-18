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

/**
 * Specialization of a {@link Schema} for number types.
 */
public class NumberSchema extends Schema
{
    /**
     * See {@link #getMultipleOf()}
     */
    private Number multipleOf;

    /**
     * See {@link #getMinimum()}
     */
    private Number maximum;

    /**
     * See {@link #getExclusiveMaximum()}
     */
    private Number exclusiveMaximum;

    /**
     * See {@link #getMinimum()}
     */
    private Number minimum;

    /**
     * See {@link #getExclusiveMinimum()}
     */
    private Number exclusiveMinimum;

    @Override
    public NumberSchema asNumber()
    {
        return this;
    }

    /**
     * A numeric instance is valid against "multipleOf" if the result of the
     * division of the instance by this keyword's value is an integer.<br>
     * <br>
     * https://json-schema.org/draft/2020-12/json-schema-validation.html#rfc.section.6.2.1
     *
     * @return The value
     */
    public Number getMultipleOf()
    {
        return multipleOf;
    }

    /**
     * See {@link #getMultipleOf()}
     *
     * @param multipleOf The value
     */
    public void setMultipleOf(Number multipleOf)
    {
        this.multipleOf = multipleOf;
    }

    /**
     * This attribute defines the maximum value of the instance property
     * when the type of the instance value is a number.<br>
     * <br>
     * https://json-schema.org/draft/2020-12/json-schema-validation.html#rfc.section.6.2.2
     *
     * @return The value
     */
    public Number getMaximum()
    {
        return maximum;
    }

    /**
     * See {@link #getMaximum()}
     *
     * @param maximum The value
     */
    public void setMaximum(Number maximum)
    {
        this.maximum = maximum;
    }

    /**
     * The value of "exclusiveMaximum" MUST be a number, representing an 
     * exclusive upper limit for a numeric instance.
     * 
     * If the instance is a number, then the instance is valid only if 
     * it has a value strictly less than (not equal to) "exclusiveMaximum".
     * 
     * https://json-schema.org/draft/2020-12/json-schema-validation.html#rfc.section.6.2.3
     *
     * @return The value
     */
    public Number getExclusiveMaximum()
    {
        return exclusiveMaximum;
    }

    /**
     * See {@link #getExclusiveMaximum()}
     *
     * @param exclusiveMaximum The value
     */
    public void setExclusiveMaximum(Number exclusiveMaximum)
    {
        this.exclusiveMaximum = exclusiveMaximum;
    }

    /**
     * This attribute defines the minimum value of the instance property
     * when the type of the instance value is a number.<br>
     * <br>
     * https://json-schema.org/draft/2020-12/json-schema-validation.html#rfc.section.6.2.4
     *
     * @return The value
     */
    public Number getMinimum()
    {
        return minimum;
    }

    /**
     * See {@link #getMinimum()}
     *
     * @param minimum The value
     */
    public void setMinimum(Number minimum)
    {
        this.minimum = minimum;
    }
    /**
     * The value of "exclusiveMinimum" MUST be a number, representing an 
     * exclusive lower limit for a numeric instance.
     * 
     * If the instance is a number, then the instance is valid only if it 
     * has a value strictly greater than (not equal to) "exclusiveMinimum".
     * 
     * https://json-schema.org/draft/2020-12/json-schema-validation.html#rfc.section.6.2.5
     *
     * @return The value
     */
    public Number getExclusiveMinimum()
    {
        return exclusiveMinimum;
    }

    /**
     * See {@link #getExclusiveMinimum()}
     *
     * @param exclusiveMinimum The value
     */
    public void setExclusiveMinimum(Number exclusiveMinimum)
    {
        this.exclusiveMinimum = exclusiveMinimum;
    }
}