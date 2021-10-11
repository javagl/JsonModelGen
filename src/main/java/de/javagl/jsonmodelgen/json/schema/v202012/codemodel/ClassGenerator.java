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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
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

import de.javagl.jsonmodelgen.GeneratorInput;
import de.javagl.jsonmodelgen.json.schema.codemodel.ClassNameGenerator;
import de.javagl.jsonmodelgen.json.schema.codemodel.CodeModelInitializers;
import de.javagl.jsonmodelgen.json.schema.codemodel.CodeModels;
import de.javagl.jsonmodelgen.json.schema.codemodel.NameUtils;
import de.javagl.jsonmodelgen.json.schema.codemodel.StringUtils;
import de.javagl.jsonmodelgen.json.schema.v202012.ArraySchema;
import de.javagl.jsonmodelgen.json.schema.v202012.BooleanSchema;
import de.javagl.jsonmodelgen.json.schema.v202012.IntegerSchema;
import de.javagl.jsonmodelgen.json.schema.v202012.NumberSchema;
import de.javagl.jsonmodelgen.json.schema.v202012.ObjectSchema;
import de.javagl.jsonmodelgen.json.schema.v202012.Schema;
import de.javagl.jsonmodelgen.json.schema.v202012.SchemaGenerator;
import de.javagl.jsonmodelgen.json.schema.v202012.SchemaUtils;
import de.javagl.jsonmodelgen.json.schema.v202012.StringSchema;

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
     * An indentation level for log messages
     */
    private int resolvingLogIndent = 0;

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
            String id = schema.getId();
            Class<?> typeOverride = config.getTypeOverride(id);
            if (typeOverride != null)
            {
                logger.info(
                    "Using type override " + typeOverride + " for " + id);
                return codeModel._ref(typeOverride);
            }
            return doCreateType(schema);
        }

        @Override
        public JType createObjectType(ObjectSchema schema)
        {
            if (!containsRelevantInformation(schema) &&
                !usesImplicitExtension(schema))
            {
                return doCreateObjectTypeFromExtended(schema);
            }
            List<URI> uris = schemaGenerator.getUris(schema);
            String className =
                classNameGenerator.generateClassName(schema, uris);
            String classNameOverride =
                config.getClassNameOverride(className);
            if (classNameOverride != null)
            {
                className = classNameOverride;
            }
            JDefinedClass definedClass =
                resolveDefinedClass(schema, className, ClassType.CLASS);
            if (!pendingTypes.contains(schema))
            {
                pendingTypes.add(schema);
                initializeObjectType(definedClass, schema);
                pendingTypes.remove(schema);
            }
            return definedClass;
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
    private final TypeCreator typeCreator;
    
    /**
     * The set of {@link Schema} instances for which the type is currently
     * being constructed, used to resolve types that directly or indirectly
     * refer to them self.
     */
    private final Set<Schema> pendingTypes;

    /**
     * The {@link SchemaGenerator} which generated the {@link Schema} for
     * which this instance should generate the classes
     */
    private final SchemaGenerator schemaGenerator;

    /**
     * The code model that will be used to create the classes
     */
    private final JCodeModel codeModel;

    /**
     * The mapping from {@link Schema} instances to code model types that
     * have been created so far
     */
    private Map<Schema, JType> types;

    /**
     * The {@link ClassGeneratorConfig}
     */
    private final ClassGeneratorConfig config;
    
    /**
     * The {@link GeneratorInput} objects
     */
    private final List<GeneratorInput> generatorInputs;
    
    /**
     * Creates a new class generator for the {@link Schema} definitions that
     * have been generated by the given {@link SchemaGenerator}.
     * 
     * @param config The {@link ClassGeneratorConfig}
     * @param schemaGenerator The {@link SchemaGenerator}
     * @param generatorInputs The {@link GeneratorInput} objects
     */
    public ClassGenerator(
        ClassGeneratorConfig config,
        SchemaGenerator schemaGenerator, List<GeneratorInput> generatorInputs)
    {
        this.config = Objects.requireNonNull(
            config, "The config may not be null");
        this.schemaGenerator = Objects.requireNonNull(
            schemaGenerator, "The schemaGenerator may not be null");
        this.generatorInputs = Objects.requireNonNull(
            generatorInputs, "The generatorInputs may not be null");
        this.typeCreator = new DefaultTypeCreator();
        this.pendingTypes = new LinkedHashSet<Schema>();
        this.typeResolver = this::doResolveType;
        this.codeModel = new JCodeModel();
        this.types = new LinkedHashMap<Schema, JType>();

        List<Schema> rootSchemas = schemaGenerator.getRootSchemas();
        for (Schema rootSchema : rootSchemas)
        {
            if (!rootSchema.isObject())
            {
                logger.severe("Root schema was no object");
                continue;
            }
            ObjectSchema objectSchema = rootSchema.asObject();
            typeResolver.apply(objectSchema);
        }
    }
    
    /**
     * Tries to find the header code that should be used for classes in
     * the specified package.
     * 
     * @param packageName The package name
     * @return The header code (or <code>null</code>)
     */
    private String findHeaderCodeForPackageName(String packageName)
    {
        for (GeneratorInput generatorInput : generatorInputs)
        {
            if (generatorInput.getPackageName().equals(packageName))
            {
                return generatorInput.getHeaderCode();
            }
        }
        return null;
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
            private final CodeWriter delegate = 
                new FileCodeWriter(destinationDirectory);

            @Override
            public OutputStream openBinary(JPackage pkg, String fileName)
                throws IOException
            {
                OutputStream result = delegate.openBinary(pkg, fileName);
                String headerCode = findHeaderCodeForPackageName(pkg.name());
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
        logger.log(resolvingLogLevel,
            indent(resolvingLogIndent) + "Resolve " + schema.getTitle());
        resolvingLogIndent+=4;
        
        JType type = types.get(schema);
        if (type != null)
        {
            resolvingLogIndent-=4;
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
        resolvingLogIndent-=4;
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
     * Initialize the code model type for the given {@link ObjectSchema}
     *
     * @param definedClass The code model class
     * @param objectSchema The {@link ObjectSchema}
     * @return The type
     */
    private JType initializeObjectType(
        JDefinedClass definedClass, ObjectSchema objectSchema)
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
//            logger.info("    className "+className);
//            logger.info("    Schema details:");
//            URI uri = schemaGenerator.getCanonicalUri(objectSchema);
//            logger.info(SchemaUtils.createSchemaDebugString(uri, objectSchema));
//        }

        // Handle this "implicit extension" workaround. This is somewhat
        // brittle, but the check method already prints a warning...
        if (usesImplicitExtension(objectSchema))
        {
            List<Schema> allOf = objectSchema.getAllOf();
            Schema probableBaseClass = allOf.get(0);
            ObjectSchema probableExtension = allOf.get(1).asObject();
            JType extendedType = typeResolver.apply(probableBaseClass);
            if (extendedType instanceof JClass)
            {
                JClass extendedClass = (JClass)extendedType;
                definedClass._extends(extendedClass);
            }
            handleObjectTypeProperties(probableExtension, definedClass);
            handleObjectTypeAdditionalProperties(
                probableExtension, definedClass);
        } 
        else
        {
            handleObjectTypeExtended(definedClass, objectSchema);
            handleObjectTypeProperties(objectSchema, definedClass);
            handleObjectTypeAdditionalProperties(objectSchema, definedClass);
        }
        handleDefinitions(objectSchema, definedClass);
        
        return definedClass;
    }


    /**
     * Returns whether the schema has any {@link ObjectSchema#getProperties()}
     * or {@link ObjectSchema#getPatternProperties()} or
     * {@link ObjectSchema#getDependentRequired()}
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
            objectSchema.getDependentRequired() != null;
    }
    
    /**
     * Check whether the given {@link ObjectSchema} uses the "implicit 
     * extension" mechanism.
     * 
     * People applied tricky workarounds to model extensions in the JSON 
     * schema. Among them being constructs like
     * <pre><code>
     * "allOf" : [{
     *   "$ref" : "parent.schema.json"
     * }, {
     *   "properties" : {
     *      ...
     *    }
     * }]     
     * </code></pre>
     * where <code>allOf</code> referred to the parent class, but the 
     * properties had been added via an "anonymous" object schema.
     *  
     * This method checks whether this pattern can be found here, but
     * only by checking whether there are two elements in allOf,
     * and return true (and prints a warning) if this is the case.
     * 
     * @param objectSchema The {@link ObjectSchema}
     * @return Whether the implicit extension is used
     */
    private static boolean usesImplicitExtension(ObjectSchema objectSchema)
    {
        List<Schema> allOf = objectSchema.getAllOf();
        if (allOf == null)
        {
            return false;
        }
        if (allOf.size() != 2)
        {
            return false;
        }
        logger.warning("Assuming implicit extension due to allOf with size 2 "
            + "in " + SchemaUtils.createShortSchemaDebugString(objectSchema));
        return true;
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

        PropertyInfo propertyInfo = new PropertyInfo(
            definedClass, "additionalProperties", typedMapType, 
            additionalPropertiesSchema, false);
        
        addField(propertyInfo);
        CodeModelMethods.addSetter(propertyInfo, true);
        CodeModelMethods.addGetter(propertyInfo);
        
        if (config.is(ClassGeneratorConfig.CREATE_ADDERS_AND_REMOVERS))
        {
            CodeModelMethods.addAdderForMap(propertyInfo);
            CodeModelMethods.addRemoverForMap(propertyInfo);
        }
    }
    
    
    /**
     * If the given {@link ObjectSchema} has
     * {@link ObjectSchema#getDefinitions()}, then this method
     * will create types for these definitions.
     *
     * TODO It should be configurable whether these are inner classes
     * or top-level classes. Right now, they are simple top-level classes.
     *
     * @param objectSchema The {@link ObjectSchema}
     * @param definedClass The target class
     */
    private void handleDefinitions(
        ObjectSchema objectSchema, JDefinedClass definedClass)
    {
        Map<String, Schema> definitions = objectSchema.getDefinitions();
        if (definitions == null)
        {
            return;
        }
        for (Entry<String, Schema> entry : definitions.entrySet())
        {
            Schema definitionSchema = entry.getValue();
            typeResolver.apply(definitionSchema);
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

        List<Schema> allOf = objectSchema.getAllOf();
        if (allOf != null)
        {
            if (allOf.size() == 1)
            {
                Schema extendedSchema = allOf.get(0);
                logger.fine("No important properties in "+
                    SchemaUtils.createShortSchemaDebugString(objectSchema)+
                    ", using the only type in allOf: "+
                    SchemaUtils.createShortSchemaDebugString(extendedSchema));
                return typeResolver.apply(extendedSchema);
            }
            else
            {
                logger.warning("Found allOf with size !=1 in "
                    + SchemaUtils.createShortSchemaDebugString(objectSchema));
            }
        }
        
        // In some cases (like glTF), extensible enumerations are modeled
        // like this:
        // "anyOf": [
        //   { "enum": [ 5120 ] },
        //   { "enum": [ 5121 ] },
        //   ...
        //   { "type": "integer" }
        // ]
        // And sometimes (after some update), the elements contain type 
        // information as well. 
        List<Schema> anyOf = objectSchema.getAnyOf();
        if (anyOf != null)
        {
            // Try to find the COMMON type of all elements
            Schema commonTypeSchema = 
                SchemaCodeUtils.determineCommonTypeFromAnyOf(objectSchema);
            if (commonTypeSchema != null)
            {
                logger.info("The schema "+
                    SchemaUtils.createShortSchemaDebugString(objectSchema)+
                    " uses 'anyOf'. Using the common information from one of"+
                    " its sub-schemas: "+
                    SchemaUtils.createShortSchemaDebugString(commonTypeSchema));
                return doCreateType(commonTypeSchema);
            }
            
            // If there is no COMMON type, try to find the single one
            // that may contain the type information:
            Schema nonObjectSchema = 
                SchemaCodeUtils.determineTypeFromUntypedAnyOf(objectSchema);
            if (nonObjectSchema != null)
            {
                logger.info("The schema "+
                    SchemaUtils.createShortSchemaDebugString(objectSchema)+
                    " uses 'anyOf'. Using the type information from one of"+
                    " its sub-schemas: "+
                    SchemaUtils.createShortSchemaDebugString(nonObjectSchema));
                return doCreateType(nonObjectSchema);
            }
            
            logger.warning("Could not determine type from anyOf in "+
                SchemaUtils.createShortSchemaDebugString(objectSchema) +
                ", using Object");
            return codeModel._ref(Object.class);
        }
        

        logger.warning("No important properties in "+
            SchemaUtils.createShortSchemaDebugString(objectSchema)+
            ", and not exactly one type in allOf, using Object");
        return codeModel._ref(Object.class);
    }
    
    
    /**
     * If the given {@link ObjectSchema} is extending another schema (as
     * given by {@link ObjectSchema#getAllOf()}, 
     * {@link ObjectSchema#getAnyOf()} 
     * or {@link ObjectSchema#getOneOf()} then the extended
     * type will be resolved and set as the supertype of the given class.
     * If there is not exactly one supertype, then a warning will be printed.
     *
     * @param definedClass The class for the given {@link ObjectSchema}
     * @param objectSchema The {@link ObjectSchema}
     * 
     * @deprecated There is no real "inheritance" in JSON schemas. See 
     * http://stackoverflow.com/questions/27410216/json-schema-and-inheritance
     * For backward compatibility, this method tries to handle the different
     * approaches that have been used to handle inheritance in existing 
     * schemas.
     */
    private void handleObjectTypeExtended(JDefinedClass definedClass,
        ObjectSchema objectSchema)
    {
        
        Schema extendedSchema = getExtendedSchema(objectSchema);
        if (extendedSchema != null)
        {
            JType extendedType = typeResolver.apply(extendedSchema);
            if (extendedType instanceof JClass)
            {
                JClass extendedClass = (JClass)extendedType;
                definedClass._extends(extendedClass);
                return;
            }
            else
            {
                logger.severe("Extended type is not a class: " +
                    SchemaUtils.createShortSchemaDebugString(extendedSchema));
            }
        }
        
        List<Schema> anyOf = objectSchema.getAnyOf();
        if (anyOf != null && !anyOf.isEmpty())
        {
            logger.warning("Cannot translate anyOf property: "+
                SchemaUtils.createShortSchemaDebugString(anyOf));
        }
        
        List<Schema> oneOf = objectSchema.getOneOf();
        if (oneOf != null && !oneOf.isEmpty())
        {
            logger.warning("Cannot translate oneOf property: "+
                SchemaUtils.createShortSchemaDebugString(oneOf));
        }
    }
    
    /**
     * Returns the schema that is the only element of the "allOf" property
     * of the given schema, or <code>null</code> if the given schema does
     * not have an allOf property, or there is NOT exactly one element in
     * the allOf property
     * 
     * @param objectSchema The schema
     * @return The extended schema
     */
    private Schema getExtendedSchema(ObjectSchema objectSchema)
    {
        List<Schema> allOf = objectSchema.getAllOf();
        if (allOf != null)
        {
            if (allOf.size() > 1)
            {
                // TODO The type in question must become "Object"!!!
                logger.warning("Cannot extend multiple classes: "+
                    SchemaUtils.createShortSchemaDebugString(allOf));
                return null;
            }
            else if (allOf.size() == 1)
            {
                Schema extendedSchema = allOf.get(0);
                return extendedSchema;
            }
        }
        return null;
    }
    
    /**
     * Returns whether the specified property was already declared in one
     * of the {@link #getExtendedSchema(ObjectSchema) extended schemas}
     * of the given schema
     * 
     * @param objectSchema The object schema
     * @param propertyName The property name
     * @return Whether the property was inherited
     */
    private boolean isInheritedProperty(
        ObjectSchema objectSchema, String propertyName)
    {
        Schema ancestorSchema = getExtendedSchema(objectSchema);
        while (ancestorSchema != null)
        {
            if (!ancestorSchema.isObject())
            {
                return false;
            }
            ObjectSchema ancestorObjectSchema = ancestorSchema.asObject();
            Map<String, Schema> ancestorProperties = 
                ancestorObjectSchema.getProperties();
            if (ancestorProperties.containsKey(propertyName))
            {
                return true;
            }
            ancestorSchema = getExtendedSchema(ancestorObjectSchema);
        }
        return false;
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

            if (isInheritedProperty(objectSchema, propertyName))
            {
                logger.info("Skipping inherited property \"" + propertyName
                    + "\" in " + objectSchema.getId());
                continue;
            }
            
            boolean isRequired = 
                SchemaUtils.isRequired(objectSchema, propertyName);

            Schema propertySchema = entry.getValue();
            JType propertyType = typeResolver.apply(propertySchema);
            
            PropertyInfo propertyInfo = new PropertyInfo(
                definedClass, propertyName, propertyType, 
                propertySchema, isRequired);

            String fullContainingClassName = definedClass.fullName();
            String fullPropertyName = 
                fullContainingClassName + "#" + propertyName;
            boolean performValidation = 
                !config.isSkippingValidation(fullPropertyName);
            
            addField(propertyInfo);
            CodeModelMethods.addSetter(propertyInfo, performValidation);
            CodeModelMethods.addGetter(propertyInfo);
            
            if (config.is(ClassGeneratorConfig.CREATE_ADDERS_AND_REMOVERS))
            {
                if (CodeModels.isSubtypeOf(propertyType, Map.class))
                {
                    CodeModelMethods.addAdderForMap(propertyInfo);
                    CodeModelMethods.addRemoverForMap(propertyInfo);
                }
                else if (CodeModels.isSubtypeOf(propertyType, List.class))
                {
                    CodeModelMethods.addAdderForList(propertyInfo);
                    CodeModelMethods.addRemoverForList(propertyInfo);
                }
            }

            if (config.is(ClassGeneratorConfig.CREATE_GETTERS_WITH_DEFAULT))
            {
                if (!isRequired && propertySchema.getDefaultString() != null)
                {
                    CodeModelMethods.addDefaultGetter(propertyInfo);
                }
            }
        }
    }

    /**
     * Create a field for the specified property in the given class
     * 
     * @param propertyInfo The {@link PropertyInfo}
     */
    private void addField(PropertyInfo propertyInfo)
    {
        JDefinedClass definedClass = propertyInfo.getDefinedClass();
        String propertyName = propertyInfo.getPropertyName();
        JType propertyType = propertyInfo.getPropertyType();
        Schema propertySchema = propertyInfo.getPropertySchema();
        boolean isRequired = propertyInfo.isRequired();
        
        String sanitizedPropertyName = 
            NameUtils.makeValidJavaIdentifier(propertyName);
        JFieldVar fieldVar = definedClass.field(
            JMod.PRIVATE, propertyType, sanitizedPropertyName);

        if (isRequired)
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
            definedClass.name(), propertyName, propertySchema, isRequired);
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
     * Tries to find the package name for the class that corresponds to
     * the given schema, based on the {@link Schema#getId()}.
     * 
     * @param schema The schema
     * @return The package name
     */
    private String findPackageName(Schema schema)
    {
        try
        {
            String id = schema.getId();
            URI schemaBaseUri = new URI(id);
            schemaBaseUri = schemaBaseUri.resolve(".");
            for (GeneratorInput generatorInput : generatorInputs)
            {
                URI baseUri = new URI(generatorInput.getUrlString());
                baseUri = baseUri.resolve(".");
                if (baseUri.equals(schemaBaseUri))
                {
                    return generatorInput.getPackageName();
                }
            }
        }
        catch (URISyntaxException e)
        {
            logger.severe(e.getMessage());
        }
        logger.severe("Could not find package name for schema " + schema);
        return "UNKNOWN";
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
        String packageName = findPackageName(schema);
        String fullClassName = packageName + "." + className;
        
        JDefinedClass definedClass =
            codeModel._getClass(fullClassName);
        if (definedClass == null)
        {
            try
            {
                definedClass = codeModel._class(fullClassName, classType);
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
        Schema items = arraySchema.getItems();
        if (items == null)
        {
            // This should probably never be the case
            return codeModel.ref(Object.class);
        }
        JType itemType = typeResolver.apply(items);

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

    /**
     * Create an empty string with the given length. 
     * Only used for log indentation.
     * 
     * @param n The length
     * @return The string
     */
    private static String indent(int n)
    {
        String s = "";
        for (int i = 0; i < n; i++)
        {
            s += " ";
        }
        return s;
    }
}
