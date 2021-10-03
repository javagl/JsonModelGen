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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JStatement;
import com.sun.codemodel.JType;

import de.javagl.jsonmodelgen.json.schema.codemodel.CodeModelInitializers;
import de.javagl.jsonmodelgen.json.schema.codemodel.CodeModels;
import de.javagl.jsonmodelgen.json.schema.codemodel.NameUtils;
import de.javagl.jsonmodelgen.json.schema.codemodel.StringUtils;
import de.javagl.jsonmodelgen.json.schema.v202012.Schema;

/**
 * Methods to create methods in a code model
 */
class CodeModelMethods
{
    
    /**
     * Add a "setter" for the specified property in the given class
     *
     * @param propertyInfo The {@link PropertyInfo}
     * @param performValidation Whether validation statements should be added
     * @return The setter method
     */
    static JMethod addSetter(
        PropertyInfo propertyInfo, boolean performValidation)
    {
        JDefinedClass definedClass = propertyInfo.getDefinedClass();
        String propertyName = propertyInfo.getPropertyName();
        JType propertyType = propertyInfo.getPropertyType();
        Schema propertySchema = propertyInfo.getPropertySchema();
        boolean isRequired = propertyInfo.isRequired();

        String sanitizedPropertyName = 
            NameUtils.makeValidJavaIdentifier(propertyName);
        
        JCodeModel codeModel = definedClass.owner();
        String methodName = 
            "set" + StringUtils.capitalize(sanitizedPropertyName);
        JMethod method = definedClass.method(
            JMod.PUBLIC, definedClass.owner().VOID, methodName);
        method.param(propertyType, sanitizedPropertyName);
        JBlock block = method.body();

        JBlock nullHandlingStatements = new JBlock();
        CodeModelValidations.createNullHandlingStatements(
            nullHandlingStatements, codeModel, 
            sanitizedPropertyName, propertyType, propertySchema, isRequired);
        addAllStatements(block, nullHandlingStatements);
        
        JBlock validationStatements = new JBlock();
        if (performValidation)
        {
            CodeModelValidations.createValidationStatements(
                validationStatements, codeModel, 
                sanitizedPropertyName, propertyType, propertySchema);
            addAllStatements(block, validationStatements);
        }

        block.assign(
            JExpr._this().ref(sanitizedPropertyName), 
            JExpr.ref(sanitizedPropertyName));

        
        JDocComment docComment = method.javadoc();
        StringBuilder sb = new StringBuilder();
        String description = CodeModelDocs.createJavaDocDescription(
            definedClass.name(), sanitizedPropertyName, 
            propertySchema, isRequired);
        sb.append(StringUtils.format(description, 
            CodeModelDocs.MAX_COMMENT_LINE_LENGTH)+"\n");
        sb.append("\n");
        sb.append("@param "+sanitizedPropertyName
            +" The "+sanitizedPropertyName+" to set");
        if (isRequired)
        {
            sb.append("\n");
            sb.append("@throws NullPointerException If the given value " + 
                "is <code>null</code>");
        }
        if (!validationStatements.getContents().isEmpty())
        {
            sb.append("\n");
            sb.append("@throws IllegalArgumentException If the given value " + 
                "does not meet\nthe given constraints");
        }
        docComment.append(sb.toString());
        
        return method;
    }
    
    /**
     * Add a "getter" for the specified property in the given class
     * 
     * @param propertyInfo The {@link PropertyInfo}
     * @return The getter method
     */
    static JMethod addGetter(PropertyInfo propertyInfo)
    {
        JDefinedClass definedClass = propertyInfo.getDefinedClass();
        String propertyName = propertyInfo.getPropertyName();
        JType propertyType = propertyInfo.getPropertyType();
        Schema propertySchema = propertyInfo.getPropertySchema();
        boolean isRequired = propertyInfo.isRequired();
        
        String sanitizedPropertyName = 
            NameUtils.makeValidJavaIdentifier(propertyName);
        
        JCodeModel codeModel = definedClass.owner();
        String methodName = getGetterMethodName(
            codeModel, sanitizedPropertyName, propertyType);
        JMethod method = definedClass.method(
            JMod.PUBLIC, propertyType, methodName);
        JBlock block = method.body();
        block._return(JExpr._this().ref(sanitizedPropertyName));
        
        JDocComment docComment = method.javadoc();
        StringBuilder sb = new StringBuilder();
        String description = CodeModelDocs.createJavaDocDescription(
            definedClass.name(), sanitizedPropertyName, propertySchema, isRequired);
        sb.append(StringUtils.format(description, 
            CodeModelDocs.MAX_COMMENT_LINE_LENGTH)+"\n");
        sb.append("\n");
        sb.append("@return The "+sanitizedPropertyName);
        docComment.append(sb.toString());
        
        return method;
    }
    
    /**
     * Returns the "getter" method name for the given property. 
     * 
     * @param codeModel The code model
     * @param propertyName The property name
     * @param propertyType The property type
     * @return The getter method name
     */
    private static String getGetterMethodName(
        JCodeModel codeModel, String propertyName, JType propertyType)
    {
        if (propertyType.equals(codeModel.BOOLEAN) ||
            propertyType.unboxify().equals(codeModel.BOOLEAN))
        {
            return "is" + StringUtils.capitalize(propertyName);
        }
        return "get" + StringUtils.capitalize(propertyName);
    }
    

    /**
     * Add a method for the specified property in the given class,
     * that returns the default value
     * 
     * @param propertyInfo The {@link PropertyInfo} 
     * @return The method
     */
    static JMethod addDefaultGetter(PropertyInfo propertyInfo)
    {
        JDefinedClass definedClass = propertyInfo.getDefinedClass();
        String propertyName = propertyInfo.getPropertyName();
        JType propertyType = propertyInfo.getPropertyType();
        Schema propertySchema = propertyInfo.getPropertySchema();

        String sanitizedPropertyName = 
            NameUtils.makeValidJavaIdentifier(propertyName);
        
        JCodeModel codeModel = definedClass.owner();
        String methodName =
            "default" + StringUtils.capitalize(sanitizedPropertyName);
        JMethod method = definedClass.method(
            JMod.PUBLIC, propertyType, methodName);
        JBlock block = method.body();

        JExpression defaultValueExpression = 
            CodeModelInitializers.createInitializer(
                codeModel, propertySchema.getDefaultString(), propertyType);
        if (defaultValueExpression == null)
        {
            block._return(JExpr._this().ref("null"));
        }
        else
        {
            block._return(defaultValueExpression);
        }
        
        JDocComment docComment = method.javadoc();
        StringBuilder sb = new StringBuilder();
        String getterMethodName = getGetterMethodName(
            codeModel, sanitizedPropertyName, propertyType);
        String description = CodeModelDocs.createJavaDoc(Arrays.asList(
            "Returns the default value of the " + sanitizedPropertyName,
            "@see #"+getterMethodName));
        sb.append(StringUtils.format(description, 
            CodeModelDocs.MAX_COMMENT_LINE_LENGTH)+"\n");
        sb.append("\n");
        sb.append("@return The default " + sanitizedPropertyName);
        docComment.append(sb.toString());
        
        return method;
    }
    
    
    /**
     * Add an "adder" for the specified property in the given class, 
     * whose type must be a subtype of "Map"
     *
     * @param propertyInfo The {@link PropertyInfo}
     * @return The adder method
     * @throws IllegalArgumentException If the given property type is not 
     * a subtype of "Map"
     */
    static JMethod addAdderForMap(PropertyInfo propertyInfo)
    {
        JDefinedClass definedClass = propertyInfo.getDefinedClass();
        String propertyName = propertyInfo.getPropertyName();
        JType propertyType = propertyInfo.getPropertyType();
        
        if (!CodeModels.isSubtypeOf(propertyType, Map.class))
        {
            throw new IllegalArgumentException(
                "Property type must be a subtype of Map, but is " + 
                propertyType);
        }
        JCodeModel codeModel = definedClass.owner();
        
        String methodName = 
            "add" + StringUtils.capitalize(propertyName);
        JMethod method = definedClass.method(
            JMod.PUBLIC, definedClass.owner().VOID, methodName);
        
        JClass propertyClass = (JClass)propertyType;
        List<JClass> typeParameters = 
            propertyClass.getTypeParameters();
        JClass keyType = typeParameters.get(0);
        JClass valueType = typeParameters.get(1);
        
        method.param(keyType, "key");
        method.param(valueType, "value");
        JBlock block = method.body();

        JBlock nullCheckStatements = new JBlock();
        CodeModelValidations.createNullCheckStatement(
            nullCheckStatements, codeModel, "key");
        CodeModelValidations.createNullCheckStatement(
            nullCheckStatements, codeModel, "value");
        addAllStatements(block, nullCheckStatements);

        block.decl(propertyType, "oldMap", JExpr._this().ref(propertyName));
        JClass mapType = codeModel.ref(LinkedHashMap.class);
        JClass typedMapType = mapType.narrow(keyType, valueType);
        block.decl(propertyType, "newMap", JExpr._new(typedMapType));
        
        JExpression conditionExpression = 
            JExpr.ref("oldMap").ne(JExpr.ref("null"));
        block._if(conditionExpression)._then().invoke(
            JExpr.ref("newMap"), "putAll").arg(JExpr.ref("oldMap"));
        block.invoke(JExpr.ref("newMap"), "put")
            .arg(JExpr.ref("key")).arg(JExpr.ref("value"));
        block.assign(JExpr._this().ref(propertyName), JExpr.ref("newMap"));
        
        JDocComment docComment = method.javadoc();
        StringBuilder sb = new StringBuilder();
        String description = CodeModelDocs.createJavaDoc(Arrays.asList(
            "Add the given " + propertyName + ". The " + propertyName +
            " of this instance will be replaced with a map that contains " +
            " all previous mappings, and additionally the new mapping. "));
        sb.append(StringUtils.format(description, 
            CodeModelDocs.MAX_COMMENT_LINE_LENGTH)+"\n");
        sb.append("\n");
        sb.append("@param key The key").append("\n");;
        sb.append("@param value The value").append("\n");
        sb.append("@throws NullPointerException If the given key " + 
            "or value is <code>null</code>");
        docComment.append(sb.toString());

        return method;
    }
    
    /**
     * Add a "remover" for the specified property in the given class, 
     * whose type must be a subtype of "Map"
     *
     * @param propertyInfo The {@link PropertyInfo}
     * @return The adder method
     * @throws IllegalArgumentException If the given property type is not 
     * a subtype of "Map"
     */
    static JMethod addRemoverForMap(PropertyInfo propertyInfo)
    {
        JDefinedClass definedClass = propertyInfo.getDefinedClass();
        String propertyName = propertyInfo.getPropertyName();
        JType propertyType = propertyInfo.getPropertyType();
        boolean isRequired = propertyInfo.isRequired();
        
        if (!CodeModels.isSubtypeOf(propertyType, Map.class))
        {
            throw new IllegalArgumentException(
                "Property type must be a subtype of Map, but is " + 
                propertyType);
        }
        JCodeModel codeModel = definedClass.owner();

        String methodName = 
            "remove" + StringUtils.capitalize(propertyName);
        JMethod method = definedClass.method(
            JMod.PUBLIC, definedClass.owner().VOID, methodName);
        
        JClass propertyClass = (JClass)propertyType;
        List<JClass> typeParameters = 
            propertyClass.getTypeParameters();
        JClass keyType = typeParameters.get(0);
        JClass valueType = typeParameters.get(1);
        
        method.param(keyType, "key");
        JBlock block = method.body();

        JBlock nullCheckStatements = new JBlock();
        CodeModelValidations.createNullCheckStatement(
            nullCheckStatements, codeModel, "key");
        addAllStatements(block, nullCheckStatements);

        block.decl(propertyType, "oldMap", JExpr._this().ref(propertyName));
        JClass mapType = codeModel.ref(LinkedHashMap.class);
        JClass typedMapType = mapType.narrow(keyType, valueType);
        block.decl(propertyType, "newMap", JExpr._new(typedMapType));
        
        JExpression conditionExpression = 
            JExpr.ref("oldMap").ne(JExpr.ref("null"));
        block._if(conditionExpression)._then().invoke(
            JExpr.ref("newMap"), "putAll").arg(JExpr.ref("oldMap"));
        block.invoke(JExpr.ref("newMap"), "remove")
            .arg(JExpr.ref("key"));
        
        if (!isRequired)
        {
            JExpression emptyConditionExpression = 
                JExpr.ref("newMap").invoke("isEmpty");
            JConditional ifEmpty = block._if(emptyConditionExpression);
            ifEmpty._then().assign(
                JExpr._this().ref(propertyName), JExpr.ref("null"));
            ifEmpty._else().assign(
                JExpr._this().ref(propertyName), JExpr.ref("newMap"));
        }
        else
        {
            block.assign(JExpr._this().ref(propertyName), JExpr.ref("newMap"));
        }
        
        JDocComment docComment = method.javadoc();
        StringBuilder sb = new StringBuilder();
        
        List<String> javaDocLines = new ArrayList<String>(Arrays.asList(
            "Remove the given " + propertyName + ". The " + 
            propertyName + " of this instance will be replaced with a map" + 
            " that contains all previous mappings, except for the one" + 
            " with the given key."));
        if (!isRequired)
        {
            javaDocLines.add("If this new map would be empty, " + 
                "then it will be set to <code>null</code>.");
        }
        String description = CodeModelDocs.createJavaDoc(javaDocLines);
        sb.append(StringUtils.format(description, 
            CodeModelDocs.MAX_COMMENT_LINE_LENGTH)+"\n");
        sb.append("\n");
        sb.append("@param key The key").append("\n");
        sb.append("@throws NullPointerException If the given key " + 
            "is <code>null</code>");
        docComment.append(sb.toString());
        return method;
    }
    

    /**
     * Add an "adder" for the specified property in the given class, 
     * whose type must be a subtype of "List"
     *
     * @param propertyInfo The {@link PropertyInfo}
     * @return The adder method
     * @throws IllegalArgumentException If the given property type is not 
     * a subtype of "List"
     */
    static JMethod addAdderForList(PropertyInfo propertyInfo)
    {
        JDefinedClass definedClass = propertyInfo.getDefinedClass();
        String propertyName = propertyInfo.getPropertyName();
        JType propertyType = propertyInfo.getPropertyType();

        if (!CodeModels.isSubtypeOf(propertyType, List.class))
        {
            throw new IllegalArgumentException(
                "Property type must be a subtype of List, but is " + 
                propertyType);
        }
        JCodeModel codeModel = definedClass.owner();
        
        String methodName = 
            "add" + StringUtils.capitalize(propertyName);
        JMethod method = definedClass.method(
            JMod.PUBLIC, definedClass.owner().VOID, methodName);
        
        JClass propertyClass = (JClass)propertyType;
        List<JClass> typeParameters = 
            propertyClass.getTypeParameters();
        JClass elementType = typeParameters.get(0);
        
        method.param(elementType, "element");
        JBlock block = method.body();

        JBlock nullCheckStatements = new JBlock();
        CodeModelValidations.createNullCheckStatement(
            nullCheckStatements, codeModel, "element");
        addAllStatements(block, nullCheckStatements);

        block.decl(propertyType, "oldList", JExpr._this().ref(propertyName));
        JClass listType = codeModel.ref(ArrayList.class);
        JClass typedListType = listType.narrow(elementType);
        block.decl(propertyType, "newList", JExpr._new(typedListType));
        
        JExpression conditionExpression = 
            JExpr.ref("oldList").ne(JExpr.ref("null"));
        block._if(conditionExpression)._then().invoke(
            JExpr.ref("newList"), "addAll").arg(JExpr.ref("oldList"));
        block.invoke(JExpr.ref("newList"), "add")
            .arg(JExpr.ref("element"));
        block.assign(JExpr._this().ref(propertyName), JExpr.ref("newList"));
        
        JDocComment docComment = method.javadoc();
        StringBuilder sb = new StringBuilder();
        String description = CodeModelDocs.createJavaDoc(Arrays.asList(
            "Add the given " + propertyName + ". The " + propertyName +
            " of this instance will be replaced with a list that contains " +
            " all previous elements, and additionally the new element."));
        sb.append(StringUtils.format(description, 
            CodeModelDocs.MAX_COMMENT_LINE_LENGTH)+"\n");
        sb.append("\n");
        sb.append("@param element The element").append("\n");
        sb.append("@throws NullPointerException If the given " + 
            "element is <code>null</code>");
        docComment.append(sb.toString());

        return method;
    }
    
    /**
     * Add a "remover" for the specified property in the given class, 
     * whose type must be a subtype of "List"
     * 
     * @param propertyInfo The {@link PropertyInfo}
     * @return The adder method
     * @throws IllegalArgumentException If the given property type is not 
     * a subtype of "List"
     */
    static JMethod addRemoverForList(PropertyInfo propertyInfo)
    {
        JDefinedClass definedClass = propertyInfo.getDefinedClass();
        String propertyName = propertyInfo.getPropertyName();
        JType propertyType = propertyInfo.getPropertyType();
        boolean isRequired = propertyInfo.isRequired();
        
        if (!CodeModels.isSubtypeOf(propertyType, List.class))
        {
            throw new IllegalArgumentException(
                "Property type must be a subtype of List, but is " + 
                propertyType);
        }
        JCodeModel codeModel = definedClass.owner();
        
        String methodName = 
            "remove" + StringUtils.capitalize(propertyName);
        JMethod method = definedClass.method(
            JMod.PUBLIC, definedClass.owner().VOID, methodName);
        
        JClass propertyClass = (JClass)propertyType;
        List<JClass> typeParameters = 
            propertyClass.getTypeParameters();
        JClass elementType = typeParameters.get(0);
        
        method.param(elementType, "element");
        JBlock block = method.body();

        JBlock nullCheckStatements = new JBlock();
        CodeModelValidations.createNullCheckStatement(
            nullCheckStatements, codeModel, "element");
        addAllStatements(block, nullCheckStatements);

        block.decl(propertyType, "oldList", JExpr._this().ref(propertyName));
        JClass listType = codeModel.ref(ArrayList.class);
        JClass typedListType = listType.narrow(elementType);
        block.decl(propertyType, "newList", JExpr._new(typedListType));
        
        JExpression conditionExpression = 
            JExpr.ref("oldList").ne(JExpr.ref("null"));
        block._if(conditionExpression)._then().invoke(
            JExpr.ref("newList"), "addAll").arg(JExpr.ref("oldList"));
        block.invoke(JExpr.ref("newList"), "remove")
            .arg(JExpr.ref("element"));
        
        if (!isRequired)
        {
            JExpression emptyConditionExpression = 
                JExpr.ref("newList").invoke("isEmpty");
            JConditional ifEmpty = block._if(emptyConditionExpression);
            ifEmpty._then().assign(
                JExpr._this().ref(propertyName), JExpr.ref("null"));
            ifEmpty._else().assign(
                JExpr._this().ref(propertyName), JExpr.ref("newList"));
        }
        else
        {
            block.assign(JExpr._this().ref(propertyName), JExpr.ref("newList"));
        }
        
        JDocComment docComment = method.javadoc();
        StringBuilder sb = new StringBuilder();

        List<String> javaDocLines = new ArrayList<String>(Arrays.asList(
            "Remove the given " + propertyName + ". The " + 
            propertyName + " of this instance will be replaced with a list" + 
            " that contains all previous elements, except for the removed" + 
            " one."));
        if (!isRequired)
        {
            javaDocLines.add("If this new list would be empty, " + 
                "then it will be set to <code>null</code>.");
        }
        String description = CodeModelDocs.createJavaDoc(javaDocLines);
        sb.append(StringUtils.format(description, 
            CodeModelDocs.MAX_COMMENT_LINE_LENGTH)+"\n");
        sb.append("\n");
        sb.append("@param element The element").append("\n");
        sb.append("@throws NullPointerException If the given element " + 
            "is <code>null</code>");
        docComment.append(sb.toString());
        return method;
    }

    /**
     * Add all JStatement instances from the given source block to the
     * given target block
     * 
     * @param target The target
     * @param source The source
     */
    private static void addAllStatements(JBlock target, JBlock source)
    {
        for (Object object : source.getContents())
        {
            if (object instanceof JStatement)
            {
                JStatement statement = (JStatement)object;
                target.add(statement);
            }
        }
    }

    /**
     * Private constructor to prevent instantiation
     */
    private CodeModelMethods()
    {
        // Private constructor to prevent instantiation
    }
    
}