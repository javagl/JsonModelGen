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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility methods related to names. Specifically, property names that
 * are used in the code generation process.
 */
public class NameUtils
{
    /**
     * The set of reserved Java keywords
     */
    private static final Set<String> JAVA_KEYWORDS;
    
    /**
     * The set of reserved Java literals
     */
    private static final Set<String> JAVA_LITERALS = 
        new LinkedHashSet<String>(Arrays.asList("false", "null", "true"));
    
    // Initialize the JAVA_KEYWORDS
    static
    {
        List<String> keywords = Arrays.asList(
            "abstract", 
            "assert", 
            "boolean", 
            "break", 
            "byte", 
            "case", 
            "catch", 
            "char", 
            "class", 
            "const", 
            "continue", 
            "default", 
            "do", 
            "double", 
            "else", 
            "enum", 
            "extends", 
            "final", 
            "finally", 
            "float", 
            "for", 
            "goto", 
            "if", 
            "implements", 
            "import", 
            "instanceof", 
            "int", 
            "interface", 
            "long", 
            "native", 
            "new", 
            "package", 
            "private", 
            "protected", 
            "public", 
            "return", 
            "short", 
            "static", 
            "strictfp", 
            "super", 
            "switch", 
            "synchronized", 
            "this", 
            "throw", 
            "throws", 
            "transient", 
            "try", 
            "void", 
            "volatile", 
            "while"
        );
        JAVA_KEYWORDS = new LinkedHashSet<String>(keywords);
    }
    
    /**
     * Returns whether the given string is <i>syntactically</i> a valid Java
     * identifier. This will also return <code>true</code> for strings that
     * are reserved keywords or literals.
     * 
     * @param string The string
     * @return Whether the name is a syntactically valid Java identifier
     */
    private static boolean isSyntacticallyValidIdentifier(String string)
    {
        if (string.length() == 0)
        {
            return false;
        }
        for (int i = 0; i < string.length(); i++)
        {
            char c = string.charAt(i);
            if (i == 0)
            {
                if (!Character.isJavaIdentifierStart(c))
                {
                    return false;
                }
            }
            else
            {
                if (!Character.isJavaIdentifierPart(c))
                {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Returns whether the given string is a reserved Java keyword.
     * 
     * @param string The string
     * @return Whether the string is a keyword
     */
    private static boolean isJavaKeyword(String string)
    {
        return JAVA_KEYWORDS.contains(string);
    }
    
    /**
     * Returns whether the given string is a reserved Java literal.
     * 
     * @param string The string
     * @return Whether the string is a literal
     */
    private static boolean isJavaLiteral(String string)
    {
        return JAVA_LITERALS.contains(string);
    }
    
    /**
     * Returns whether the given string is a valid Java identifier.
     * 
     * @param string The string
     * @return Whether the string is an identifier
     */
    private static boolean isValidJavaIdentifier(String string)
    {
        if (isJavaKeyword(string))
        {
            return false;
        }
        if (isJavaLiteral(string))
        {
            return false;
        }
        return isSyntacticallyValidIdentifier(string);
    }
    
    /**
     * Convert the given string into a syntactically valid Java identifier,
     * by omitting all characters at the beginning that are not valid Java
     * identifier starts, and all characters in the remaining string that
     * are not valid Java identifier parts.
     * 
     * @param string The string
     * @return The resulting string
     */
    private static String makeSyntacticallyValidJavaIdentifier(String string)
    {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<string.length(); i++)
        {
            char c = string.charAt(i);
            if (sb.length() == 0)
            {
                if (Character.isJavaIdentifierStart(c))
                {
                    sb.append(c);
                }
            }
            else
            {
                if (Character.isJavaIdentifierPart(c))
                {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }
    
    /**
     * Make sure that the given string is a valid Java identifier. The exact
     * way how this is achieved is not specified.
     * 
     * @param string The string
     * @return The string as a valid identifier
     * @throws IllegalArgumentException If the string cannot be converted into
     * a valid Java identifier
     */
    public static String makeValidJavaIdentifier(String string)
    {
        String result = makeValidJavaIdentifierUnchecked(string);
        if (!isValidJavaIdentifier(result))
        {
            throw new IllegalArgumentException(
                "Could not convert into an idenfifier: " + string);
        }
        return result;
    }
    
    /**
     * Make sure that the given string is a valid Java identifier. The exact
     * way how this is achieved is not specified.
     * 
     * @param string The string
     * @return The string as a valid identifier
     */
    public static String makeValidJavaIdentifierUnchecked(String string)
    {
       if (isSyntacticallyValidIdentifier(string))
       {
           if (isJavaKeyword(string) || isJavaLiteral(string))
           {
               return string + "_";
           }
           return string;
       }
       String s = makeSyntacticallyValidJavaIdentifier(string);
       if (isJavaKeyword(s) || isJavaLiteral(s))
       {
           return s + "_";
       }
       return s;
    }
    

    /**
     * Private constructor to prevent instantiation
     */
    private NameUtils()
    {
        // Private constructor to prevent instantiation
    }
    
    /**
     * A basic test
     * 
     * @param args not used
     */
    public static void main(String[] args)
    {
        test("example");
        test("_example");
        test("123example");
        test("12!example!!extended");
        test("12_example:extended");
        try 
        {
            test("1234"); // Well...
        }
        catch (IllegalArgumentException e)
        {
            // Expected that
            System.out.println(e.getMessage());
        }
    }

    /**
     * Only for testing
     * 
     * @param string The input
     */
    private static void test(String string)
    {
        System.out.printf("%30s becomes %30s\n", 
            "\"" + string + "\"", 
            "\"" + makeValidJavaIdentifier(string) + "\"");
    }
    
    
}
