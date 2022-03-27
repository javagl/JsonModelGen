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
package de.javagl.jsonmodelgen.json;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utility methods related to URIs
 */
public class URIs
{
    /**
     * Returns the given URI without a fragment
     * 
     * @param uri The URI
     * @return The result 
     */
    static URI withoutFragment(URI uri)
    {
        if (uri.getFragment() == null)
        {
            return uri;
        }
        try
        {
            return new URI(uri.getScheme(), uri.getSchemeSpecificPart(), null);
        }
        catch (URISyntaxException e)
        {
            // Should never happen here
            throw new IllegalArgumentException(e);
        }
    }
    
    /**
     * Creates a URI from the given string, wrapping possible exceptions
     * into an IO Exception
     * 
     * @param string The string
     * @return The URI
     * @throws IOException If there was a URI syntax error
     */
    public static URI create(String string) throws IOException
    {
        try 
        {
            return new URI(string);
        }
        catch (URISyntaxException e)
        {
            throw new IOException(e);
        }
    }
    
    
    /**
     * Appends the given string as a fragment to the given URI. 
     * The given string will be after a "/" slash. Examples:
     * <pre><code>
     * appendToFragment("uri", "newFragment") = "uri#/newFragment"
     * appendToFragment("uri#xyz", "newFragment") = "uri#xyz/newFragment"
     * </code></pre>
     * 
     * @param uri The URI
     * @param string The string to append
     * @return The new URI
     * @throws IllegalArgumentException If appending the given string as
     * a fragment causes an invalid URI syntax
     */
    public static URI appendToFragment(URI uri, String string)
    {
        try
        {
            String uriString = uri.toString();
            if (uri.getFragment() == null)
            {
                uriString += "#";
            }
            return new URI(uriString+"/"+string).normalize();
        }
        catch (URISyntaxException e)
        {
            throw new IllegalArgumentException(
                "Could not append fragment "+string+" to "+uri, e);
        }
    }
    
    /**
     * Private constructor to prevent instantiation
     */
    private URIs()
    {
        // Private constructor to prevent instantiation
    }

}
