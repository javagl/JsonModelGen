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
package de.javagl.jsonmodelgen.json.schema.v3.codemodel;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.writer.FileCodeWriter;

import de.javagl.jsonmodelgen.GlTFConfig;
import de.javagl.jsonmodelgen.json.schema.codemodel.ClassNameGenerator;
import de.javagl.jsonmodelgen.json.schema.codemodel.CodeModelInitializers;
import de.javagl.jsonmodelgen.json.schema.codemodel.CodeModels;
import de.javagl.jsonmodelgen.json.schema.codemodel.StringUtils;
import de.javagl.jsonmodelgen.json.schema.v3.ArraySchema;
import de.javagl.jsonmodelgen.json.schema.v3.BooleanSchema;
import de.javagl.jsonmodelgen.json.schema.v3.IntegerSchema;
import de.javagl.jsonmodelgen.json.schema.v3.NumberSchema;
import de.javagl.jsonmodelgen.json.schema.v3.ObjectSchema;
import de.javagl.jsonmodelgen.json.schema.v3.Schema;
import de.javagl.jsonmodelgen.json.schema.v3.SchemaGenerator;
import de.javagl.jsonmodelgen.json.schema.v3.SchemaUtils;
import de.javagl.jsonmodelgen.json.schema.v3.StringSchema;

/**
 * A class for generating classes from the {@link Schema} information that
 * was generated by a {@link SchemaGenerator}
 */
public class ClassGenerator
{
    /**
     * The logger used in this class
     */
    private static final Logger logger = 
        Logger.getLogger(ClassGenerator.class.getName());
    
    /**
     * The log level used for the resolving process
     */
    private static final Level resolvingLogLevel = Level.FINE;
    
    /**
     * The log level used for the class creating process
     */
    private static final Level creatingLogLevel = Level.FINE;
    
    
    /**
     * The {@link ClassNameGenerator} that will generate the names
     * of classes for {@link ObjectSchema} instances
     */
    private final ClassNameGenerator<ObjectSchema> classNameGenerator = 
        new DefaultClassNameGenerator();
    
    /**
     * The function that receives a {@link Schema} and returns a CodeModel
     * type. Internally, this is just the {@link #doResolveType(Schema)} 
     * method
     */
    private final Function<Schema, JType> typeResolver;
        
    /**
     * The default {@link TypeCreator} implementation
     */
    class DefaultTypeCreator implements TypeCreator
    {
        @Override
        public JType createType(Schema schema)
        {
            return doCreateType(schema);
        }
        
        @Override
        public JType createObjectType(ObjectSchema schema)
        {
            return doCreateObjectType(schema);
        }

        @Override
        public JType createArrayType(ArraySchema schema)
        {
            return doCreateArrayType(schema);
        }

        @Override
        public JType createBooleanType(BooleanSchema schema)
        {
            return codeModel.ref(Boolean.class);
        }

        @Override
        public JType createIntegerType(IntegerSchema schema)
        {
            return codeModel.ref(Integer.class);
        }

        @Override
        public JType createNumberType(NumberSchema schema)
        {
            return codeModel.ref(Float.class);
        }

        @Override
        public JType createStringType(StringSchema schema)
        {
            return codeModel.ref(String.class);
        }
    }
    
    /**
     * The function that creates CodeModel types for the {@link Schema} types
     */
    private TypeCreator typeCreator = new DefaultTypeCreator();

    /**
     * The {@link SchemaGenerator} which generated the {@link Schema} for
     * which this instance should generate the classes 
     */
    private SchemaGenerator schemaGenerator;
    
    /**
     * The code model that will be used to create the classes
     */
    private final JCodeModel codeModel = new JCodeModel();
    
    /**
     * The package name that should be used for the generated classes
     */
    private final String packageName;

    /**
     * The header code that should be inserted into every source code file
     */
    private final String headerCode;
    
    /**
     * The mapping from {@link Schema} instances to code model types that
     * have been created so far
     */
    private Map<Schema, JType> types;
    
    /**
     * Creates a new class generator for the {@link Schema} definitions that
     * have been generated by the given {@link SchemaGenerator}.
     * 
     * @param schemaGenerator The {@link SchemaGenerator}
     * @param packageName The package name that should be used for the
     * generated classes
     * @param headerCode The header code for every file
     */
    public ClassGenerator(
        SchemaGenerator schemaGenerator, String packageName, String headerCode)
    {
        this.schemaGenerator = schemaGenerator;
        this.packageName = packageName;
        this.headerCode = headerCode;
        this.typeResolver = this::doResolveType;

        types = new LinkedHashMap<Schema, JType>();

        Schema schema = schemaGenerator.getRootSchema();
        if (!schema.isObject())
        {
            logger.severe("Root schema was no object");
            return;
        }
        ObjectSchema objectSchema = schema.asObject();
        typeResolver.apply(objectSchema);
    }
    
    /**
     * Write the generated classes to the given destination directory
     * 
     * @param destinationDirectory The destination directory
     * @throws IOException If an IO error occurs
     */
    public void generate(File destinationDirectory) throws IOException
    {
        CodeWriter source = new CodeWriter()
        {
            CodeWriter delegate = new FileCodeWriter(destinationDirectory);
            
            @Override
            public OutputStream openBinary(JPackage pkg, String fileName)
                throws IOException
            {
                OutputStream result = delegate.openBinary(pkg, fileName);
                if (headerCode != null)
                {
                    result.write(headerCode.getBytes());
                }
                return result;
            }
            
            @Override
            public void close() throws IOException
            {
                delegate.close();
            }
        };
        CodeWriter resource = new FileCodeWriter(destinationDirectory);
        codeModel.build(source, resource);
    }
    
    /**
     * Returns the fully qualified name for the given class name
     * 
     * @param className The class name
     * @return The fully qualified name
     */
    private String fullyQualifiedName(String className)
    {
        if (packageName.length() == 0)
        {
            return className;
        }
        return packageName+"."+className;
    }
    
    /**
     * Try to resolve the type that is described by the given {@link Schema}.
     * If the type already has been created, it will be returned. Otherwise,
     * this will create the corresponding type, store it internally, and
     * return the result
     * 
     * @param schema The {@link Schema}
     * @return The type
     */
    private JType doResolveType(Schema schema)
    {
        JType type = types.get(schema);
        if (type != null)
        {
            return type;
        }

        if (logger.isLoggable(resolvingLogLevel))
        {
            logger.log(resolvingLogLevel, "resolveType");
            logger.log(resolvingLogLevel, "    uri       "+
                schemaGenerator.getCanonicalUri(schema));
            logger.log(resolvingLogLevel, "    schema    "+
                SchemaUtils.createShortSchemaDebugString(schema));
        }
        
        type = typeCreator.createType(schema);
        types.put(schema, type);
        return type;
    }
    
    /**
     * Create the code model type for the given {@link Schema}. This delegates
     * to the {@link #typeCreator} methods, depending on the {@link Schema} 
     * type
     *  
     * @param schema The {@link Schema}
     * @return The type
     */
    private JType doCreateType(Schema schema)
    {
        if (logger.isLoggable(creatingLogLevel))
        {
            logger.log(creatingLogLevel, "createType");
            logger.log(creatingLogLevel, "    uri       "+
                schemaGenerator.getCanonicalUri(schema));
            logger.log(creatingLogLevel, "    schema    "+
                SchemaUtils.createShortSchemaDebugString(schema));
        }
        
        if (schema.isObject())
        {
            ObjectSchema objectSchema = schema.asObject();
            JType type = typeCreator.createObjectType(objectSchema);
            return type;
        }
        if (schema.isArray())
        {
            ArraySchema arraySchema = schema.asArray();
            JType type = typeCreator.createArrayType(arraySchema);
            return type;
        }
        if (schema.isBoolean())
        {
            BooleanSchema booleanSchema = schema.asBoolean();
            JType type = typeCreator.createBooleanType(booleanSchema);
            return type;
        }
        if (schema.isInteger())
        {
            IntegerSchema integerSchema = schema.asInteger();
            JType type = typeCreator.createIntegerType(integerSchema);
            return type;
        }
        if (schema.isNumber())
        {
            NumberSchema numberSchema = schema.asNumber();
            JType type = typeCreator.createNumberType(numberSchema);
            return type;
        }
        if (schema.isString())
        {
            StringSchema stringSchema = schema.asString();
            JType type = typeCreator.createStringType(stringSchema);
            return type;
        }

        if (logger.isLoggable(creatingLogLevel))
        {
            logger.log(creatingLogLevel, 
                "createType: WARNING: Could not create type");
            logger.log(creatingLogLevel, "    uri       "+
                schemaGenerator.getCanonicalUri(schema));
            logger.log(creatingLogLevel, "    schema    "+
                SchemaUtils.createShortSchemaDebugString(schema));
            logger.log(creatingLogLevel, "    using Object.class");
        }
        return codeModel.ref(Object.class);
    }
    
    
    /**
     * Create the code model type for the given {@link ObjectSchema}
     * 
     * @param objectSchema The {@link ObjectSchema}
     * @return The type
     */
    private JType doCreateObjectType(ObjectSchema objectSchema)
    {
        if (logger.isLoggable(creatingLogLevel))
        {
            logger.log(creatingLogLevel, "createObjectType");
            logger.log(creatingLogLevel, "    uri       "+
                schemaGenerator.getCanonicalUri(objectSchema));
            logger.log(creatingLogLevel, "    schema    "+
                SchemaUtils.createShortSchemaDebugString(objectSchema));
        }
        
//        boolean debug = true;
//        if (debug)
//        {
//            logger.info("    Schema details:");
//            URI uri = schemaGenerator.getCanonicalUri(objectSchema);
//            logger.info(SchemaUtils.createSchemaDebugString(uri, objectSchema));
//        }
        
        if (!containsRelevantInformation(objectSchema))
        {
            return doCreateObjectTypeFromExtended(objectSchema);
        }

        List<URI> uris = schemaGenerator.getUris(objectSchema);
        String className = 
            classNameGenerator.generateClassName(objectSchema, uris);
        
//        if (debug)
//        {
//            logger.info("    className "+className);
//            logger.info("    Schema details:");
//            URI uri = schemaGenerator.getCanonicalUri(objectSchema);
//            logger.info(SchemaUtils.createSchemaDebugString(uri, objectSchema));
//        }
        
        JDefinedClass definedClass = 
            resolveDefinedClass(objectSchema, className, ClassType.CLASS);
        handleObjectTypeExtended(definedClass, objectSchema);

        handleObjectTypeProperties(objectSchema, definedClass);
        handleObjectTypeAdditionalProperties(objectSchema, definedClass);
        
        return definedClass;
    }
    
    /**
     * Returns whether the schema has any {@link ObjectSchema#getProperties()}
     * or {@link ObjectSchema#getPatternProperties()} or 
     * {@link ObjectSchema#getDependencies()}
     * 
     * @param objectSchema The {@link ObjectSchema}
     * @return Whether the schema contains relevant information
     */
    private static boolean containsRelevantInformation(
        ObjectSchema objectSchema)
    {
        return 
            objectSchema.getProperties() != null ||
            objectSchema.getPatternProperties() != null ||
            objectSchema.getDependencies() != null;
    }
    

    /**
     * If the given {@link ObjectSchema} has 
     * {@link ObjectSchema#getAdditionalProperties() additionalProperties},
     * this creates a map for the properties as a field in the given class
     * 
     * @param objectSchema The {@link ObjectSchema}
     * @param definedClass The class that the property may be added to
     */
    private void handleObjectTypeAdditionalProperties(
        ObjectSchema objectSchema, JDefinedClass definedClass)
    {
        Schema additionalPropertiesSchema = 
            objectSchema.getAdditionalProperties();
        if (additionalPropertiesSchema == null)
        {
            return;
        }
        JClass mapType = codeModel.ref(Map.class);
        JClass keyType = codeModel.ref(String.class);
        JType valueType = typeResolver.apply(additionalPropertiesSchema);
        JClass boxedValueType = valueType.boxify();
        JClass typedMapType = mapType.narrow(keyType, boxedValueType);

        addField(definedClass, "additionalProperties", 
            typedMapType, additionalPropertiesSchema);
        CodeModelMethods.addSetter(definedClass, "additionalProperties", 
            typedMapType, additionalPropertiesSchema);
        CodeModelMethods.addGetter(definedClass, "additionalProperties", 
            typedMapType, additionalPropertiesSchema);
        
        if (GlTFConfig.CREATE_ADDERS_AND_REMOVERS)
        {
            CodeModelMethods.addAdderForMap(
                definedClass, "additionalProperties", 
                typedMapType, additionalPropertiesSchema);
            CodeModelMethods.addRemoverForMap(
                definedClass, "additionalProperties", 
                typedMapType, additionalPropertiesSchema);
        }

    }

    /**
     * Assuming that the given {@link ObjectSchema} does not contain 
     * {@link #containsRelevantInformation(ObjectSchema) relevant information},
     * create a type for it, based on its extended type
     * 
     * @param objectSchema The {@link ObjectSchema}
     * @return The type
     */
    private JType doCreateObjectTypeFromExtended(ObjectSchema objectSchema)
    {
        if (objectSchema.getAdditionalProperties() != null)
        {
            Schema additionalPropertiesSchema = 
                objectSchema.getAdditionalProperties();

            JClass mapType = codeModel.ref(Map.class);
            JClass keyType = codeModel.ref(String.class);
            JType valueType = typeResolver.apply(additionalPropertiesSchema);
            JClass boxedValueType = valueType.boxify();
            JClass typedMapType = mapType.narrow(keyType, boxedValueType);
            return typedMapType;
        }
        
        Schema extendsSchema = objectSchema.getExtendsSchema();
        if (extendsSchema != null)
        {
            logger.fine("No important properties in "+
                SchemaUtils.createShortSchemaDebugString(objectSchema)+
                ", using extended: "+
                SchemaUtils.createShortSchemaDebugString(extendsSchema));
            return typeResolver.apply(extendsSchema);
        }
        
        logger.warning("No important properties in "+
            SchemaUtils.createShortSchemaDebugString(objectSchema)+
            ", and no extended, using Object");
        return codeModel._ref(Object.class);
    }

    
    /**
     * If the given {@link ObjectSchema} is extending another schema (as
     * given by {@link ObjectSchema#getExtendsSchema()}), then the extended
     * type will be resolved and set as the supertype of the given class
     * 
     * @param definedClass The class for the given {@link ObjectSchema}
     * @param objectSchema The {@link ObjectSchema}
     */
    private void handleObjectTypeExtended(JDefinedClass definedClass,
        ObjectSchema objectSchema)
    {
        Schema extendedSchema = objectSchema.getExtendsSchema();
        if (extendedSchema != null)
        {
            JType extendedType = typeResolver.apply(extendedSchema);
            if (extendedType instanceof JClass)
            {
                JClass extendedClass = (JClass)extendedType;
                definedClass._extends(extendedClass);
            }
            else
            {
                logger.severe("Extended type is not a class: " +
                    SchemaUtils.createShortSchemaDebugString(extendedSchema));
            }
        }
    }

    /**
     * If the given {@link ObjectSchema} has 
     * {@link ObjectSchema#getProperties()}, then this method will add 
     * fields for these properties to the given class
     * 
     * @param objectSchema The {@link ObjectSchema}
     * @param definedClass The target class for the fields
     */
    private void handleObjectTypeProperties(
        ObjectSchema objectSchema, JDefinedClass definedClass)
    {
        Map<String, Schema> properties = objectSchema.getProperties();
        if (properties == null)
        {
            return;
        }
        for (Entry<String, Schema> entry : properties.entrySet())
        {
            String propertyName = entry.getKey();
            Schema propertySchema = entry.getValue();
            JType propertyType = typeResolver.apply(propertySchema);

            addField(definedClass, propertyName, propertyType, propertySchema);
            CodeModelMethods.addSetter(
                definedClass, propertyName, propertyType, propertySchema);
            CodeModelMethods.addGetter(
                definedClass, propertyName, propertyType, propertySchema);
            
            if (GlTFConfig.CREATE_ADDERS_AND_REMOVERS)
            {
                if (CodeModels.isSubtypeOf(propertyType, Map.class))
                {
                    CodeModelMethods.addAdderForMap(
                        definedClass, propertyName, 
                        propertyType, propertySchema);
                    CodeModelMethods.addRemoverForMap(
                        definedClass, propertyName, 
                        propertyType, propertySchema);
                }
                else if (CodeModels.isSubtypeOf(propertyType, List.class))
                {
                    CodeModelMethods.addAdderForList(
                        definedClass, propertyName, 
                        propertyType, propertySchema);
                    CodeModelMethods.addRemoverForList(
                        definedClass, propertyName, 
                        propertyType, propertySchema);
                }
            }

            if (GlTFConfig.CREATE_GETTERS_WITH_DEFAULT)
            {
                if (propertySchema.isRequired() != Boolean.TRUE &&
                    propertySchema.getDefaultString() != null)
                {
                    CodeModelMethods.addGetterWithDefault(
                        definedClass, propertyName, 
                        propertyType, propertySchema);
                }
            }
        }
    }
    
    /**
     * Create a field for the specified property in the given class
     * 
     * @param definedClass The target class
     * @param propertyName The property name (will be the field name)
     * @param propertyType The property type
     * @param propertySchema The property schema
     */
    private void addField(JDefinedClass definedClass, String propertyName,
        JType propertyType, Schema propertySchema)
    {
        JFieldVar fieldVar = definedClass.field(
            JMod.PRIVATE, propertyType, propertyName);
        
        if (propertySchema.isRequired() == Boolean.TRUE)
        {
        	JExpression initializer = createInitializer(propertySchema);
        	if (initializer != null)
        	{
        		fieldVar.init(initializer);
        	}
        	else
        	{
        	    // This is not critical, as it refers to fields
        	    // that should be filled by the JSON parser
        		logger.fine(
        			"Found required field, but could not " + 
        		    "create initializer for "+
        			SchemaUtils.createShortSchemaDebugString(propertySchema));
        	}
        }
        
        JDocComment docComment = fieldVar.javadoc();
        StringBuilder sb = new StringBuilder();
        String description = CodeModelDocs.createJavaDocDescription(
            definedClass.name(), propertyName, propertySchema);
        sb.append(StringUtils.format(description, 
            CodeModelDocs.MAX_COMMENT_LINE_LENGTH));
        docComment.append(sb.toString());
        
    }

    
    
    


    /**
     * Create the initializer expression for the initialization of the
     * field that is created for the given property {@link Schema}
     * 
     * @param propertySchema The property {@link Schema}
     * @return The initializer expression
     */
    private JExpression createInitializer(Schema propertySchema)
    {
        JType propertyType = typeResolver.apply(propertySchema);
        
        JExpression initializer = 
            CodeModelInitializers.createInitializer(
                codeModel, propertySchema.getDefaultString(), propertyType);
        if (initializer != null)
        {
            return initializer;
        }
        //return deriveInitializer(propertySchema);
        return null;
        
    }
    
    
    /**
     * Resolves the defined class with the given name for the given schema
     * in the current code model. If the class is not yet known, it is
     * created, stored in the code model, and returned.
     * 
     * @param schema The {@link Schema}
     * @param className The (unqualified) class name
     * @param classType The class type
     * @return The defined class
     */
    private JDefinedClass resolveDefinedClass(
        Schema schema, String className, ClassType classType)
    {
        JDefinedClass definedClass = 
            codeModel._getClass(fullyQualifiedName(className));
        if (definedClass == null)
        {
            try
            {
                definedClass = codeModel._class(
                    fullyQualifiedName(className), classType);
            }
            catch (JClassAlreadyExistsException e)
            {
                // Can not happen here
                e.printStackTrace();
            }
        }
        JDocComment docComment = definedClass.javadoc();
        StringBuilder sb = new StringBuilder();
        String description = schema.getDescription();
        if (description != null)
        {
            sb.append(description);
            sb.append("\n");
            sb.append("\n");
        }
        URI canonicalUri = schemaGenerator.getCanonicalUri(schema);
        sb.append("Auto-generated for "+
            StringUtils.extractSchemaName(canonicalUri));
        docComment.append(StringUtils.format(sb.toString(), 
            CodeModelDocs.MAX_COMMENT_LINE_LENGTH));
        return definedClass;
    }
    
    
    /**
     * Creates the code model type for the given {@link ArraySchema}
     * 
     * @param arraySchema The {@link ArraySchema}
     * @return The code model type
     */
    private JType doCreateArrayType(ArraySchema arraySchema)
    {
        Collection<Schema> items = arraySchema.getItems();
        if (items == null || items.size() > 1)
        {
            return codeModel.ref(Object.class);
        }
        Schema itemSchema = items.iterator().next();
        JType itemType = typeResolver.apply(itemSchema);
        
        if (arraySchema.getMinItems() != null &&
            arraySchema.getMaxItems() != null)
        {
            JType unboxifiedItemType = itemType.unboxify();
            return unboxifiedItemType.array();
        }
        
        // The order of elements must be retained, even
        // when they are "unique items", so it must ALWAYS 
        // be a list (and can not be a set)
        //if (arraySchema.getUniqueItems() == Boolean.TRUE)
        //{
        //    return codeModel.ref(Set.class).narrow(itemType);
        //}
        
        return codeModel.ref(List.class).narrow(itemType);
    }

}
