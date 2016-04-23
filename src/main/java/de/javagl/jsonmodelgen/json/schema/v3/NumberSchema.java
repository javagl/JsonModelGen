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

/**
 * Specialization of a {@link Schema} for number types. See 
 * <a href="https://tools.ietf.org/html/draft-zyp-json-schema-03">
 * https://tools.ietf.org/html/draft-zyp-json-schema-03</a>
 */
public class NumberSchema extends Schema
{
    /**
     * See {@link #getDivisibleBy()}
     */
    private Number divisibleBy;

    /**
     * See {@link #getMinimum()}
     */
    private Number maximum;

    /**
     * See {@link #isExclusiveMaximum()}
     */
    private Boolean exclusiveMaximum;

    /**
     * See {@link #getMinimum()}
     */
    private Number minimum;
    
    /**
     * See {@link #isExclusiveMinimum()}
     */
    private Boolean exclusiveMinimum;

    @Override
    public NumberSchema asNumber()
    {
        return this;
    }
    
    /**
     * This attribute defines what value the number instance must be
     * divisible by with no remainder (the result of the division must be an
     * integer.)<br>
     * <br>
     * https://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.24
     * 
     * @return The value
     */
    public Number getDivisibleBy()
    {
        return divisibleBy;
    }
    
    /**
     * See {@link #getDivisibleBy()}
     * 
     * @param divisibleBy The value
     */
    public void setDivisibleBy(Number divisibleBy)
    {
        this.divisibleBy = divisibleBy;
    }
    
    /**
     * This attribute defines the maximum value of the instance property
     * when the type of the instance value is a number.<br>
     * <br>
     * https://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.10
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
     * This attribute indicates if the value of the instance (if the
     * instance is a number) can not equal the number defined by the
     * "minimum" attribute.  This is false by default, meaning the instance
     * value can be greater then or equal to the minimum value.<br>
     * <br>
     * https://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.11
     * 
     * @return The value
     */
    public Boolean isExclusiveMaximum()
    {
        return exclusiveMaximum;
    }
    
    /**
     * See {@link #isExclusiveMaximum()}
     * 
     * @param exclusiveMaximum The value
     */
    public void setExclusiveMaximum(Boolean exclusiveMaximum)
    {
        this.exclusiveMaximum = exclusiveMaximum;
    }
    
    /**
     * This attribute defines the minimum value of the instance property
     * when the type of the instance value is a number.<br>
     * <br>
     * https://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.9
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
     * This attribute indicates if the value of the instance (if the
     * instance is a number) can not equal the number defined by the
     * "maximum" attribute.  This is false by default, meaning the instance
     * value can be less then or equal to the minimum value.<br>
     * <br>
     * https://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.12
     * 
     * @return The value
     */
    public Boolean isExclusiveMinimum()
    {
        return exclusiveMinimum;
    }
    
    /**
     * See {@link #isExclusiveMinimum()}
     * 
     * @param exclusiveMinimum The value
     */
    public void setExclusiveMinimum(Boolean exclusiveMinimum)
    {
        this.exclusiveMinimum = exclusiveMinimum;
    }
}