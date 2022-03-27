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
package de.javagl.jsonmodelgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class summarizing the input for the {@link JsonModelGen} code generation
 * process.
 */
public class GeneratorInput
{
    /**
     * The base URL string for the schema
     */
    private String uriString;
    
    /**
     * The list of search URI strings
     */
    private final List<String> searchUriStrings;
    
    /**
     * The optional header code to be inserted into the classes
     */
    private String headerCode;
    
    /**
     * The package name to use for the generated classes
     */
    private String packageName;
    
    /**
     * Default constructor
     */
    public GeneratorInput()
    {
        this.searchUriStrings = new ArrayList<String>();
    }
    
    /**
     * Returns the base URI string for the schema
     *  
     * @return The base URI string
     */
    public String getUriString()
    {
        return uriString;
    }
    
    /**
     * Set the base URI string for the schema
     * 
     * @param uriString The base URI string for the schema
     */
    void setUriString(String uriString)
    {
        this.uriString = uriString;
    }
    
    /**
     * Add the given search URI string
     * 
     * @param searchUriString The search URI string
     */
    void addSearchUriString(String searchUriString)
    {
        if (!this.searchUriStrings.contains(searchUriString))
        {
            this.searchUriStrings.add(searchUriString);
        }
    }
    
    /**
     * Returns the search URI strings
     * 
     * @return The search URI strings
     */
    public List<String> getSearchUriStrings()
    {
        return Collections.unmodifiableList(searchUriStrings);
    }
    
    /**
     * Returns the optional header code to be inserted into the classes
     * 
     * @return The header code
     */
    public String getHeaderCode()
    {
        return headerCode;
    }
    
    /**
     * Set the optional header code to be inserted into the classes
     * 
     * @param headerCode The header code
     */
    void setHeaderCode(String headerCode)
    {
        this.headerCode = headerCode;
    }
    
    /**
     * Returns the package name to be used for the generated classes
     * 
     * @return The package name
     */
    public String getPackageName()
    {
        return packageName;
    }
    
    /**
     * Set the package name to be used for the generated classes
     * 
     * @param packageName The package name
     */
    void setPackageName(String packageName)
    {
        this.packageName = packageName;
    }
}

