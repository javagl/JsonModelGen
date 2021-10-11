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
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;


/**
 * Utility methods for deriving class names from a collection of URIs.
 */
public class ClassNameUtils
{
    /**
     * The logger used in this class
     */
    private static final Logger logger = 
        Logger.getLogger(ClassNameUtils.class.toString());
    
    /**
     * Derive a class name from the given collection of URIs. The process
     * of how the class name is derived is unspecified.
     *  
     * @param uris The URIs
     * @return The class name
     * @throws IllegalArgumentException If the given collection is empty
     */
    public static String deriveClassName(Collection<URI> uris)
    {
        if (uris.isEmpty())
        {
            throw new IllegalArgumentException(
                "The collection of URIs is empty");
        }
        if (uris.size() == 1)
        {
            URI uri = uris.iterator().next();
            return deriveClassName(uri);
        }
        
        Set<URI> urisWithoutFragment = new LinkedHashSet<URI>();
        for (URI uri : uris)
        {
            if (uri.getFragment() == null)
            {
                urisWithoutFragment.add(uri);
            }
        }
        if (urisWithoutFragment.size() > 1)
        {
            logger.warning("Multiple URIs without fragment:");
            for (URI uri : urisWithoutFragment)
            {
                logger.warning("    "+uri);
            }
        }
        if (urisWithoutFragment.size() == 0)
        {
            logger.warning("No URIs without fragment:");
            for (URI uri : uris)
            {
                logger.warning("    "+uri);
            }
            URI uri = shortest(uris);
            return deriveClassName(uri);
        }
        URI uri = urisWithoutFragment.iterator().next();
        return deriveClassName(uri);
    }
    
    
    /**
     * Returns the URI from the given sequence that has the shortest
     * string representation (or <code>null</code> if the given sequence
     * is empty)
     * 
     * @param uris The URIs
     * @return The shortest URI
     */
    private static URI shortest(Iterable<? extends URI> uris)
    {
        int minLength = Integer.MAX_VALUE;
        URI shortestUri = null;
        for (URI uri : uris)
        {
            int length = uri.toString().length();
            if (length < minLength)
            {
                minLength = length;
                shortestUri = uri;
            }
        }
        return shortestUri;
    }
    
    /**
     * Derive a class name from the given URI. The process
     * of how the class name is derived is unspecified.
     *  
     * @param uri The URI
     * @return The class name
     */
    private static String deriveClassName(URI uri)
    {
        String uriString = uri.toString();
        String className = null;
        
        // If the URI is part of JSON Schema "definitions", then just
        // derive the class name from the respective name
        if (uriString.contains("#/definitions"))
        {
            int definitionsIndex = uriString.indexOf("#/definitions");
            String definitionName = uriString.substring(definitionsIndex + 13);
            className = StringUtils.capitalize(definitionName);
        }
        else
        {
            String fileName = Paths.get(uri.getPath()).getFileName().toString();
            fileName = stripSuffixIfPresent(fileName, ".json");
            fileName = stripSuffixIfPresent(fileName, ".schema");
            className = StringUtils.capitalize(fileName);

            String fragment = uri.getFragment();
            if (fragment != null)
            {
                className += fragment;
            }
        }
        
        className = cleanUpClassName(className);
        className = beautifyClassName(className);
        return className;
    }
    
    /**
     * Beautify the given class name. The details are not specified, but...
     * it may, for example, change names like 
     * <code>"NodeEXT_example_extension"</code> to
     * <code>"NodeExampleExtension"</code>...
     * 
     * @param className The class name
     * @return The beautified class name
     */
    private static String beautifyClassName(String className)
    {
        String preparedClassName = className;
        List<String> knownPrefixes = Arrays.asList(
            "KHR",
            "EXT",
            "3DTILES",
            "ADOBE",
            "AGI",
            "AGT",
            "ALCM",
            "ALI",
            "AMZN",
            "ANIMECH",
            "ASOBO",
            "AVR",
            "BLENDER",
            "CAPTURE",
            "CESIUM",
            "CITRUS",
            "CLO",
            "CVTOOLS",
            "EPIC",
            "FB",
            "FOXIT",
            "GOOGLE",
            "GRIFFEL",
            "KDAB",
            "LLQ",
            "MAXAR",
            "MESHOPT",
            "MOZ",
            "MPEG",
            "MSFT",
            "NV",
            "OFT",
            "OMI",
            "OWLII",
            "PANDA3D",
            "POLUTROPON",
            "PTC",
            "S8S",
            "SEIN",
            "SI",
            "SKFB",
            "SKYLINE",
            "SPECTRUM",
            "TRYON",
            "UX3D",
            "VRMC",
            "WEB3D"           
        );
        for (String prefix : knownPrefixes)
        {
            preparedClassName = preparedClassName.replaceAll(prefix + "_", "_");
        }
        
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = false;
        for (int i=0; i<preparedClassName.length(); i++)
        {
            char c = preparedClassName.charAt(i);
            if (c != '_')
            {
                if (capitalizeNext)
                {
                    c = Character.toUpperCase(c);
                }
                sb.append(c);
                capitalizeNext = false;
            }
            else
            {
                capitalizeNext = true;
            }
        }
        return sb.toString();
        
    }
    
    /**
     * Clean up the given class name string to become a "proper" Java class
     * name. This will omit all characters that are not Java identifier parts
     * (as reported by <code>Character.isJavaIdentifierPart</code>), and 
     * capitalize all characters that come after omitted characters. 
     * For example, a string like <code>"Animation.channel.target"</code>
     * will be converted to <code>"AnimationChannelTarget"</code>.
     *  
     * @param className The class name
     * @return The cleaned up class name
     */
    private static String cleanUpClassName(String className)
    {
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = false;
        for (int i=0; i<className.length(); i++)
        {
            char c = className.charAt(i);
            if (Character.isJavaIdentifierPart(c))
            {
                if (capitalizeNext)
                {
                    c = Character.toUpperCase(c);
                }
                sb.append(c);
                capitalizeNext = false;
            }
            else
            {
                capitalizeNext = true;
            }
        }
        return sb.toString();
    }
    
    /**
     * If the given string ends with the given suffix, then the string without
     * the suffix is returned. Otherwise, the string is returned as it is.
     * 
     * @param s The string
     * @param suffix The suffix
     * @return The result
     */
    private static String stripSuffixIfPresent(String s, String suffix)
    {
        if (s.endsWith(suffix))
        {
            return s.substring(0, s.length()-suffix.length());
        }
        return s;
    }
    
    /**
     * Private constructor to prevent instantiation
     */
    private ClassNameUtils()
    {
        // Private constructor to prevent instantiation
    }
}
