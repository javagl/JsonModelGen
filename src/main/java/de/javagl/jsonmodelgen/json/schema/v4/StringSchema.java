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
package de.javagl.jsonmodelgen.json.schema.v4;

/**
 * Specialization of a {@link Schema} for string types. See
 * <a href="https://tools.ietf.org/html/draft-zyp-json-schema-03">
 * https://tools.ietf.org/html/draft-zyp-json-schema-03</a>
 */
public class StringSchema extends Schema
{
    /**
     * See {@link #getMaxLength()}
     */
    private Integer maxLength;

    /**
     * See {@link #getMinLength()}
     */
    private Integer minLength;

    /**
     * See {@link #getPattern()}
     */
    private String pattern;

    @Override
    public StringSchema asString()
    {
        return this;
    }

    /**
     * When the instance value is a string, this defines the maximum
     * length of the string.<br>
     * <br>
     * https://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.18
     *
     * @return The value
     */
    public Integer getMaxLength()
    {
        return maxLength;
    }

    /**
     * See {@link #getMaxLength()}
     *
     * @param maxLength The value
     */
    public void setMaxLength(Integer maxLength)
    {
        this.maxLength = maxLength;
    }

    /**
     * When the instance value is a string, this defines the minimum
     * length of the string.<br>
     * <br>
     * https://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.17
     *
     * @return The value
     */
    public Integer getMinLength()
    {
        return minLength;
    }

    /**
     * See {@link #getMinLength()}
     *
     * @param minLength The value
     */
    public void setMinLength(Integer minLength)
    {
        this.minLength = minLength;
    }

    /**
     * When the instance value is a string, this provides a regular expression
     * that a string instance MUST match in order to be valid.<br>
     * <br>
     * https://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.16
     *
     * @return The value
     */
    public String getPattern()
    {
        return pattern;
    }

    /**
     * See {@link #getPattern()}
     *
     * @param pattern The value
     */
    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }
}