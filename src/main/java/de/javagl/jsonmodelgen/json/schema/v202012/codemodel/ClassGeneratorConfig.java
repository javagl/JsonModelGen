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
     * Creates a new instance with all flags being <code>false</code>
     */
    public ClassGeneratorConfig()
    {
        this.flags = new HashSet<String>();
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

