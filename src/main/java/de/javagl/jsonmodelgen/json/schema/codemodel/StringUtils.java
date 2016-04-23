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
package de.javagl.jsonmodelgen.json.schema.codemodel;

import java.net.URI;
import java.util.StringTokenizer;

/**
 * Utility methods for strings
 */
public class StringUtils
{
    /**
     * Tries to format the given string so that it does not contain lines
     * that are longer than the specified lengths. This is done by breaking
     * the given string at default delimiting characters, and re-assembling
     * the resulting tokens to a (possibly multi-line) string. Existing line
     * breaks will be preserved. 
     *  
     * @param s The input string
     * @param maxLineLength The maximum line length
     * @return The formatted string
     */
    public static String format(String s, int maxLineLength)
    {
        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(s, " \t\n\r\f", true);
        int currentLineLength = 0;
        while (st.hasMoreTokens())
        {
            String token = st.nextToken();
            if (token.equals("\n"))
            {
                sb.append("\n");
                currentLineLength = 0;
                continue;
            }
            if (token.trim().isEmpty())
            {
                continue;
            }
            if (currentLineLength + token.length() > maxLineLength)
            {
                sb.append("\n");
                currentLineLength = 0;
            }
            sb.append(token+" ");
            currentLineLength += token.length() + 1;
        }
        return sb.toString();
    }

    /**
     * Capitalize the first letter in the given string
     * 
     * @param string The string
     * @return The result
     */
    public static String capitalize(String string)
    {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }

    /**
     * Decapitalize the first letter in the given string
     * 
     * @param string The string
     * @return The result
     */
    static String decapitalize(String string)
    {
        return Character.toLowerCase(string.charAt(0)) + string.substring(1);
    }

    /**
     * Try to extract the "relevant" part that identifies the schema that 
     * is referred to by the given URI. This will usually be the "file name".
     * That is, the part behind the last slash "/" that precedes any 
     * fragment hash character "#". For example, from an URI like
     * <code>file://C:/directory/some.schema.json#/fragment/part</code>,
     * this method will return
     * <code>some.schema.json</code>
     * 
     * @param uri The URI
     * @return The schema name
     */
    public static String extractSchemaName(URI uri)
    {
        String s = uri.toString();
        int hashIndex = s.indexOf('#');
        if (hashIndex == -1)
        {
            hashIndex = s.length();
        }
        int lastSlashIndexBeforeHash = s.lastIndexOf('/', hashIndex);
        if (lastSlashIndexBeforeHash == -1)
        {
            return s;
        }
        String result = s.substring(lastSlashIndexBeforeHash+1);
        //System.out.println("Relevant for "+uri);
        //System.out.println("          is "+result);
        return result;
    }
    
    /**
     * Private constructor to prevent instantiation
     */
    private StringUtils()
    {
        // Private constructor to prevent instantiation
    }
}
