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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;

/**
 * Methods to create initializer expressions for values with different 
 * types, based on a string that contains a "default" value.
 */
public class CodeModelInitializers
{
    /**
     * The logger used in this class
     */
    private static final Logger logger = 
        Logger.getLogger(CodeModelInitializers.class.toString());
    
    /**
     * Create an expression to initialize a variable with the given type,
     * based on the given string representation of the value.<br>
     * <br>
     * If the given string is <code>null</code>, then <code>null</code>
     * is returned.<br>
     * <br>
     * @param codeModel The code model
     * @param initialValueString The string representation of the initial
     * value. This will usually be the "default" value for required fields.
     * @param type The type of the variable to initialize
     * @return The initializer expression
     */
    public static JExpression createInitializer(
        JCodeModel codeModel, String initialValueString, JType type)
    {
        if (initialValueString == null)
        {
            return null;
        }
        if (type.isPrimitive() || type.unboxify().isPrimitive())
        {
            JPrimitiveType primitiveType = (JPrimitiveType) type.unboxify();
            return CodeModels.createPrimitiveLiteralExpression(
                initialValueString, primitiveType);
        }
        if (type.binaryName().equals("java.lang.String"))
        {
            String s = initialValueString.trim();
            return JExpr.lit(s.substring(1, s.length()-1));
        }
        
        if (type.erasure().equals(codeModel._ref(List.class)))
        {
            JClass instanceClass = 
                createInstanceClass(codeModel, type, ArrayList.class);
            if (isEmptyBrackets(initialValueString))
            {
                return JExpr._new(instanceClass);
            }
            JExpression asListInvocation = 
                createAsListInvocation(codeModel, initialValueString, type);
            return JExpr._new(instanceClass).arg(asListInvocation);
        }
        if (type.erasure().equals(codeModel._ref(Set.class)))
        {
            JClass instanceClass = 
                createInstanceClass(codeModel, type, LinkedHashSet.class);
            if (isEmptyBrackets(initialValueString))
            {
                return JExpr._new(instanceClass);
            }
            JExpression asListInvocation = 
                createAsListInvocation(codeModel, initialValueString, type);
            return JExpr._new(instanceClass).arg(asListInvocation);
        }
        if (type.isArray())
        {
            JInvocation newArrayExpression = JExpr._new(type);

            String s = initialValueString.trim();
            String elementsString = s.substring(1, s.length()-1);
            StringTokenizer st = new StringTokenizer(elementsString, ",");
            while (st.hasMoreTokens())
            {
                String token = st.nextToken().trim();
                newArrayExpression.arg(
                    createInitializer(codeModel, token, type.elementType()));
            }
            return newArrayExpression;
        }
        if (type.erasure().equals(codeModel._ref(Map.class)))
        {
            if (initialValueString.equals("{}"))
            {
                JClass instanceClass =
                    createInstanceClass(codeModel, type, LinkedHashMap.class);
                return JExpr._new(instanceClass);
            }
            else
            {
                // TODO: Create initializer of map
                logger.warning("Initializer of map is not implemented " + 
                    "for this default string: "+initialValueString);
            }
        }
        logger.info("Using default initializer of " + type + 
            "for this default string: " + initialValueString);
        return JExpr._new(type);
    }
    
    /**
     * Returns whether the given string is "[]", ignoring any whitespace
     * that may appear before, in, or after the brackets
     *  
     * @param string The string
     * @return Whether the string describes empty brackets
     */
    private static boolean isEmptyBrackets(String string)
    {
        return string.replaceAll("\\s","").equals("[]");
    }
    
    /**
     * Create a class that is a parameterized version of the given class, 
     * where the type parameter is derived from the given type.
     *  
     * @param codeModel The code model
     * @param type The type
     * @param c The (raw) class
     * @return The class with the type parameter
     */
    private static JClass createInstanceClass(
        JCodeModel codeModel, JType type, Class<?> c)
    {
        JClass typeClass = (JClass)type;
        List<JClass> typeParameters = typeClass.getTypeParameters();
        JClass[] typeParametersArray = 
            typeParameters.toArray(new JClass[0]);
        JClass jc = (JClass) codeModel._ref(c);
        return jc.narrow(typeParametersArray);
    }

    /**
     * Creates an expression that performs an <code>Arrays.asList(...)</code>
     * invocation. The parameters of this call will be initializer expressions
     * that are created by calling {@link #createInitializer(
     * JCodeModel, String, JType)}. Each parameter will be created from one
     * of the tokens that are extracted from the given value string. This
     * string is therefore assumed to be of the form 
     * <code>[value0, value1, ..., valueN]</code>.  
     * 
     * @param codeModel The code model
     * @param string The value string
     * @param type The type
     * @return The expression
     */
    private static JExpression createAsListInvocation(
        JCodeModel codeModel, String string, JType type)
    {
        JClass c = (JClass)type;
        List<JClass> typeParameters = c.getTypeParameters();
        JType elementType = typeParameters.get(0);

        String s = string.trim();
        String elementsString = s.substring(1, s.length()-1);
        StringTokenizer st = new StringTokenizer(elementsString, ",");

        JInvocation asListInvocation = 
            codeModel.directClass("java.util.Arrays").staticInvoke("asList");
        while (st.hasMoreTokens())
        {
            String token = st.nextToken().trim();
            asListInvocation.arg(
                createInitializer(codeModel, token, elementType));
        }
        return asListInvocation;
    }

    
    /**
     * Private constructor to prevent instantiation
     */
    private CodeModelInitializers()
    {
        // Private constructor to prevent instantiation
    }
}
