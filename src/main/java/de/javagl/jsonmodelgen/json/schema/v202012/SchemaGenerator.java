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
package de.javagl.jsonmodelgen.json.schema.v202012;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import de.javagl.jsonmodelgen.json.JsonUtils;
import de.javagl.jsonmodelgen.json.NodeRepository;
import de.javagl.jsonmodelgen.json.URIs;
import de.javagl.jsonmodelgen.json.schema.codemodel.SchemaGeneratorUtils;

/**
 * A class that generates a {@link Schema} from a {@link NodeRepository}
 * that contains JSON Schema nodes. It will traverse the node hierarchy
 * from the {@link NodeRepository}, and create {@link Schema} instances
 * for the nodes.
 */
public final class SchemaGenerator
{
    /**
     * The logger used in this class
     */
    private static final Logger logger =
        Logger.getLogger(SchemaGenerator.class.getName());

    /**
     * The debug log level
     */
    private static final Level level = Level.FINE;

    /**
     * Debug logging utility method
     *
     * @param s The string for the log message
     */
    private static void log(String s)
    {
        if (logger.isLoggable(level))
        {
            logger.log(level, s);
        }
    }

    /**
     * The {@link NodeRepository} on which this generator operates
     */
    private final NodeRepository nodeRepository;

    /**
     * A function that returns a Schema for a given URI. Internally, this
     * just calls {@link #resolveSchema(URI)}.
     */
    private final Function<URI, Schema> schemaResolver;
    
    /**
     * The mapping from Nodes of the {@link NodeRepository} to {@link Schema}
     * instances
     */
    private final Map<JsonNode, Schema> schemas;

    /**
     * The mapping from {@link Schema} instances to the lists of URIs that
     * defined the respective {@link Schema}. This just maps the values of
     * the {@link #schemas} mapping to the URIs that are obtained for the
     * respective keys of the {@link #schemas} using the
     * {@link NodeRepository#computeNodeToUrisMapping()}
     */
    private final Map<Schema, List<URI>> schemaToUris;

    /**
     * Create a new schema generator that operates on the given
     * {@link NodeRepository}
     *
     * @param nodeRepository The {@link NodeRepository}
     */
    public SchemaGenerator(NodeRepository nodeRepository)
    {
        this.nodeRepository = nodeRepository;
        this.schemaResolver = this::resolveSchema;
        this.schemas = new LinkedHashMap<JsonNode, Schema>();

        List<URI> rootUris = nodeRepository.getRootUris();
        for (URI rootUri : rootUris) 
        {
            resolveSchema(rootUri);
        }
        this.schemaToUris = computeSchemaToUrisMapping();
    }

    /**
     * Resolve the {@link Schema} for the given URI. If the {@link Schema} for
     * the given URI is already known, then it is returned. Otherwise, it is
     * created from the JSON node that is found in the {@link NodeRepository}
     * for the given URI. (This may cause recursive calls to this method,
     * if the Schema refers to other Schemas)
     *
     * @param uri The URI
     * @return The {@link Schema} for the given URI
     */
    private Schema resolveSchema(URI uri)
    {
        log("resolveSchema");
        log("    uri    : " + uri);

        JsonNode node = resolveNode(uri);
        Schema schema = null;
        
        // Empty nodes should not cause the same ("any")-Schema object to 
        // be used every time. The returned ("any")-Schema should contain
        // a URI/id that is specific for THIS empty node.
        if (node.size() != 0 || node.isValueNode())
        {
            schema = schemas.get(node);
            if (schema != null)
            {
                log("    found  : "
                    + SchemaUtils.createShortSchemaDebugString(schema));
                return schema;
            }
        }
        log("    resolveSchema calls generate...");

        schema = generateSchema(uri);
        schemas.put(node, schema);
        processSchema(uri, schema);

        log("resolveSchema generated");
        log("    uri    : " + uri);
        log("    created: " + SchemaUtils.createShortSchemaDebugString(schema));

        return schema;
    }

    /**
     * Generate the {@link Schema} for the given URI
     *
     * @param uri The URI
     * @return The {@link Schema}
     */
    private Schema generateSchema(URI uri)
    {
        JsonNode node = resolveNode(uri);

        log("generateSchema entry");
        log("    uri : " + uri);
        log("    node: " + node);

        if (node == null)
        {
            logger.warning("generateSchema: No node for "+uri);
            return null;
        }

        Set<String> typeStrings = JsonUtils.getTypeStringsOptional(node, null);

        if (typeStrings != null && typeStrings.size() == 1)
        {
            String typeString = typeStrings.iterator().next();
            Schema schema = SchemaFactory.createSchema(typeString);
            URI fullSchemaUri = nodeRepository.getFullUri(node);
            schema.setUri(fullSchemaUri);
            schema.setTypeStrings(typeStrings);

            log("generateSchema: Found single type");
            log("    uri          " + uri);
            log("    type strings " + typeStrings);

            return schema;
        }

        if (typeStrings != null && typeStrings.size() > 0)
        {
            ObjectSchema schema = new ObjectSchema();
            URI fullSchemaUri = nodeRepository.getFullUri(node);
            schema.setUri(fullSchemaUri);
            schema.setTypeStrings(typeStrings);

            log("generateSchema: WARNING: Found multiple types");
            log("    uri          " + uri);
            log("    type strings " + typeStrings);

            logger.warning("Found multiple types: "+typeStrings);

            return schema;
        }
        
        // Schema does not contain a "type" property. Check if
        // it extends schemas, and take the type strings
        // from the extended schemas.
        if (node.has("allOf"))
        {
            return generateSchemaFromAllOf(uri, node);
        }
        if (node.has("anyOf"))
        {
            return generateSchemaFromAnyOf(uri, node);
        }
        if (node.has("oneOf"))
        {
            return generateSchemaFromOneOf(uri, node);
        }
        if (node.has("not"))
        {
            return generateSchemaFromNot(uri, node);
        }
        if (node.has("$ref"))
        {
            return generateSchemaFromRef(uri, node);
        }

        ObjectSchema schema = new ObjectSchema(true);
        URI fullSchemaUri = nodeRepository.getFullUri(node);
        schema.setUri(fullSchemaUri);
        schema.setTypeStrings(Collections.singleton("any"));

        log("generateSchema: NOTE: Found no type strings and no "
            + "extended schema - generate any");
        log("    uri          " + uri);
        log("    type strings " + schema.getTypeStrings());

        return schema;
    }

    /**
     * Generate the schema from a node that contains no type, but
     * a "$ref" property
     * 
     * @param uri The URI
     * @param node The node
     * @return The schema
     */
    private Schema generateSchemaFromRef(URI uri, JsonNode node)
    {
        ObjectSchema schema = new ObjectSchema();
        URI fullSchemaUri = nodeRepository.getFullUri(node);
        schema.setUri(fullSchemaUri);
        
        JsonNode refNode = node.get("$ref");
        String refString = refNode.textValue();
        
        URI refUri = null;
        if (refString.startsWith("#"))
        {
            refUri = uri.resolve(refString);
        }
        else
        {
            refUri = URI.create(refString);
        }
        Schema subSchema = resolveSchema(refUri);
        schema.setRef(subSchema);
        schema.setTypeStrings(subSchema.getTypeStrings());

        return schema;
    }
    
    /**
     * Generate the schema from a node that contains an "allOf" property
     * 
     * @param uri The URI
     * @param node The node
     * @return The schema
     */
    private Schema generateSchemaFromAllOf(URI uri, JsonNode node)
    {
        ObjectSchema schema = new ObjectSchema();
        URI fullSchemaUri = nodeRepository.getFullUri(node);
        schema.setUri(fullSchemaUri);
        
        List<Schema> subSchemas =
            SchemaGeneratorUtils.getSubSchemasArray(
                uri, node, "allOf", schemaResolver);
        Set<String> allTypeStrings = new LinkedHashSet<String>();
        for (Schema subSchema : subSchemas)
        {
            allTypeStrings.addAll(subSchema.getTypeStrings());                
        }
        schema.setTypeStrings(allTypeStrings);
        schema.setAllOf(subSchemas);
        return schema;
    }

    /**
     * Generate the schema from a node that contains an "anyOf" property
     * 
     * @param uri The URI
     * @param node The node
     * @return The schema
     */
    private Schema generateSchemaFromAnyOf(URI uri, JsonNode node)
    {
        ObjectSchema schema = new ObjectSchema();
        URI fullSchemaUri = nodeRepository.getFullUri(node);
        schema.setUri(fullSchemaUri);
        
        List<Schema> subSchemas =
            SchemaGeneratorUtils.getSubSchemasArray(
                uri, node, "anyOf", schemaResolver);
        Set<String> allTypeStrings = new LinkedHashSet<String>();
        for (Schema subSchema : subSchemas)
        {
            allTypeStrings.addAll(subSchema.getTypeStrings());                
        }
        schema.setTypeStrings(allTypeStrings);
        schema.setAnyOf(subSchemas);
        return schema;
    }

    /**
     * Generate the schema from a node that contains an "oneOf" property
     * 
     * @param uri The URI
     * @param node The node
     * @return The schema
     */
    private Schema generateSchemaFromOneOf(URI uri, JsonNode node)
    {
        ObjectSchema schema = new ObjectSchema();
        URI fullSchemaUri = nodeRepository.getFullUri(node);
        schema.setUri(fullSchemaUri);
        
        List<Schema> subSchemas =
            SchemaGeneratorUtils.getSubSchemasArray(
                uri, node, "oneOf", schemaResolver);
        Set<String> allTypeStrings = new LinkedHashSet<String>();
        for (Schema subSchema : subSchemas)
        {
            allTypeStrings.addAll(subSchema.getTypeStrings());                
        }
        schema.setTypeStrings(allTypeStrings);
        schema.setOneOf(subSchemas);
        return schema;
    }
    
    /**
     * Generate the schema from a node that contains an "not" property
     * 
     * @param uri The URI
     * @param node The node
     * @return The schema
     */
    private Schema generateSchemaFromNot(URI uri, JsonNode node)
    {
        ObjectSchema schema = new ObjectSchema();
        URI fullSchemaUri = nodeRepository.getFullUri(node);
        schema.setUri(fullSchemaUri);
        
        Schema subSchema =
            SchemaGeneratorUtils.getSubSchema(
                uri, node, "not", schemaResolver); 
        schema.setNot(subSchema);
        return schema;
    }





    /**
     * Process the given {@link Schema}, based on its type, by delegating
     * to the specific <code>process*Schema</code> methods.
     *
     * @param uri The URI
     * @param schema The {@link Schema}
     */
    private void processSchema(URI uri, Schema schema)
    {
        log("processSchema");
        log("    uri    " + uri);
        log("    schema " + SchemaUtils.createShortSchemaDebugString(schema));

        processBasicSchema(uri, schema);

        if (schema.isString())
        {
            processStringSchema(uri, schema.asString());
        }
        if (schema.isNumber())
        {
            processNumberSchema(uri, schema.asNumber());
        }
        if (schema.isArray())
        {
            processArraySchema(uri, schema.asArray());
        }
        if (schema.isObject())
        {
            processObjectSchema(uri, schema.asObject());
        }
    }


    /**
     * Set the properties that are common for each {@link Schema},
     * according to the values that are obtained from the
     * {@link #resolveNode(URI) node for the given URI}.
     * <ul>
     *   <li>{@link Schema#setSchemaString(String)}</li>
     *   <li>{@link Schema#setTitle(String)}</li>
     *   <li>{@link Schema#setDescription(String)}</li>
     *   <li>{@link Schema#setId(String)}</li>
     *   <li>{@link Schema#setFormat(String)}</li>
     *   <li>{@link Schema#setDefaultString(String)}</li>
     *   <li>{@link Schema#setRef(Schema)}</li>
     *   <li>{@link Schema#setAllOf(List)}</li>
     *   <li>{@link Schema#setAnyOf(List)}</li>
     *   <li>{@link Schema#setOneOf(List)}</li>
     *   <li>{@link Schema#setNot(Schema)}</li>
     *   <li>{@link Schema#setDefinitions(Map)}</li>
     * </ul>
     * <br>
     *
     * @param uri The URI
     * @param schema The {@link StringSchema}
     */
    private void processBasicSchema(URI uri, Schema schema)
    {
        log("processBasicSchema");
        log("    uri    " + uri);
        log("    schema " + SchemaUtils.createShortSchemaDebugString(schema));

        JsonNode node = resolveNode(uri);

        schema.setSchemaString(
            JsonUtils.getStringOptional(node, "$schema", null));
        schema.setTitle(
            JsonUtils.getStringOptional(node, "title", null));
        schema.setDescription(
            JsonUtils.getStringOptional(node, "description", null));

        String idString = JsonUtils.getStringOptional(node, "$id", null);
        if (idString != null)
        {
            schema.setId(idString);
        } 
        else
        {
            // LEGACY_SCHEMA_DRAFT_V4
            String legacyIdString = 
                JsonUtils.getStringOptional(node, "id", null);
            schema.setId(legacyIdString);
            // LEGACY_SCHEMA_DRAFT_V4
        }
        
        schema.setFormat(
            JsonUtils.getStringOptional(node, "format", null));
        if (node.has("default"))
        {
            JsonNode defaultNode = node.get("default");
            String defaultString = String.valueOf(defaultNode);
            schema.setDefaultString(defaultString);
        }

        // This is done during generation, do NOT overwrite this here!
        //schema.setTypeStrings(JsonUtils.getTypeStringsOptional(node));

        // LEGACY_SCHEMA_DRAFT_V4
        if (node.has("enum"))
        {
            schema.setEnumStrings(new LinkedHashSet<String>(
                JsonUtils.getArrayAsStringsOptional(node, "enum", null)));
        }
        // LEGACY_SCHEMA_DRAFT_V4
        
        
        if (node.has("const"))
        {
            schema.setEnumStrings(Collections.singleton(
                JsonUtils.getStringOptional(node, "const", null)));
        }

        if (node.has("allOf"))
        {
            List<Schema> subSchemas =
                SchemaGeneratorUtils.getSubSchemasArray(
                    uri, node, "allOf", schemaResolver);
            schema.setAllOf(subSchemas);
            if (schema.getTypeStrings() == null)
            {
                log("NOTE: processSchema: allOf overwrites empty types of parent");
            }
        }
        if (node.has("anyOf"))
        {
            List<Schema> subSchemas =
                SchemaGeneratorUtils.getSubSchemasArray(
                    uri, node, "anyOf", schemaResolver);
            schema.setAnyOf(subSchemas);
            if (schema.getTypeStrings() == null)
            {
                log("NOTE: processSchema: anyOf overwrites empty types of parent");
            }
        }
        if (node.has("oneOf"))
        {
            List<Schema> subSchemas =
                SchemaGeneratorUtils.getSubSchemasArray(
                    uri, node, "oneOf", schemaResolver);
            schema.setOneOf(subSchemas);
            if (schema.getTypeStrings() == null)
            {
                log("NOTE: processSchema: oneOf overwrites empty types of parent");
            }
        }
        if (node.has("not"))
        {
            Schema subSchema =
                SchemaGeneratorUtils.getSubSchema(
                    uri, node, "not", schemaResolver);
            schema.setNot(subSchema);
        }
        if (node.has("$ref"))
        {
//            Schema subSchema =
//                SchemaGeneratorUtils.getSubSchema(
//                    uri, node, "$ref", schemaResolver);
//            schema.setRef(subSchema);
            
            JsonNode refNode = node.get("$ref");
            String refString = refNode.textValue();
            
            URI refUri = null;
            if (refString.startsWith("#"))
            {
                refUri = uri.resolve(refString);
            }
            else
            {
                refUri = URI.create(refString);
            }
            Schema subSchema = resolveSchema(refUri);
            schema.setRef(subSchema);
            schema.setTypeStrings(subSchema.getTypeStrings());
            
        }
        
        if (node.has("definitions"))
        {
            Map<String, Schema> subSchemas =
                SchemaGeneratorUtils.getSubSchemasMap(
                    uri, node, "definitions", schemaResolver);
            schema.setDefinitions(subSchemas);
        }

        // XXX TODO Somehow detect unknown field names
        // (note that some fields may be processed
        // outside this "processBasicSchema" method!)
        {
            Set<String> knownFieldNames =
                new LinkedHashSet<String>(Arrays.asList(
                "$schema",
                "title",
                "description",
                "id", // LEGACY_SCHEMA_DRAFT_V4
                "$id",
                "format",
                "disallow",
                "enum", // LEGACY_SCHEMA_DRAFT_V4
                "const",
                "extends",
                "definitions"
            ));
            Set<String> allFieldNames = new LinkedHashSet<String>();
            Iterator<String> fieldNameIterator = node.fieldNames();
            while (fieldNameIterator.hasNext())
            {
                String fieldName = fieldNameIterator.next();
                allFieldNames.add(fieldName);
            }
            Set<String> unknownFieldNames =
                new LinkedHashSet<String>(allFieldNames);
            unknownFieldNames.removeAll(knownFieldNames);

            // TODO Handle unknownFieldNames
            //System.out.println("Found unknownFieldNames "+unknownFieldNames);
        }

    }

    /**
     * Set the properties that are specific for a {@link StringSchema},
     * according to the values that are obtained from the
     * {@link #resolveNode(URI) node for the given URI}.
     * <ul>
     *   <li>{@link StringSchema#setMaxLength(Integer)}</li>
     *   <li>{@link StringSchema#setMinLength(Integer)}</li>
     *   <li>{@link StringSchema#setPattern(String)}</li>
     * </ul>
     *
     * @param uri The URI
     * @param schema The {@link StringSchema}
     */
    private void processStringSchema(URI uri, StringSchema schema)
    {
        log("processStringSchema");
        log("    uri    " + uri);
        log("    schema " + SchemaUtils.createShortSchemaDebugString(schema));

        JsonNode node = resolveNode(uri);
        schema.setMaxLength(
            JsonUtils.getIntegerMinOptional(node, "maxLength", 0, null));
        schema.setMinLength(
            JsonUtils.getIntegerMinOptional(node, "minLength", 0, null));
        schema.setPattern(
            JsonUtils.getStringOptional(node, "pattern", null));
    }

    /**
     * Set the properties that are specific for a {@link NumberSchema},
     * according to the values that are obtained from the
     * {@link #resolveNode(URI) node for the given URI}.
     * <ul>
     *   <li>{@link NumberSchema#setMultipleOf(Number)}</li>
     *   <li>{@link NumberSchema#setMaximum(Number)}</li>
     *   <li>{@link NumberSchema#setExclusiveMaximum(Number)}</li>
     *   <li>{@link NumberSchema#setMinimum(Number)}</li>
     *   <li>{@link NumberSchema#setExclusiveMinimum(Number)}</li>
     * </ul>
     *
     * @param uri The URI
     * @param schema The {@link NumberSchema}
     */
    private void processNumberSchema(URI uri, NumberSchema schema)
    {
        log("processNumberSchema");
        log("    uri    " + uri);
        log("    schema " + SchemaUtils.createShortSchemaDebugString(schema));

        JsonNode node = resolveNode(uri);
        schema.setMultipleOf(
            JsonUtils.getNumberOptional(
                node, "multipleOf", 0.0, true, null, false, null));
        
        Number maximum = JsonUtils.getNumberOptional(node, "maximum", null);
        Number exclusiveMaximum = 
            JsonUtils.getNumberOptional(node, "exclusiveMaximum", null);

        Number minimum = JsonUtils.getNumberOptional(node, "minimum", null);
        Number exclusiveMinimum = 
            JsonUtils.getNumberOptional(node, "exclusiveMinimum", null);
        
        schema.setMaximum(maximum);
        schema.setExclusiveMaximum(exclusiveMaximum);
        schema.setMinimum(minimum);
        schema.setExclusiveMinimum(exclusiveMinimum);
        
        // LEGACY_SCHEMA_DRAFT_V4
        // Handle the old style, where exclusiveMinimum and exclusiveMaximum
        // had been boolean flags
        Boolean exclusiveMaximumFlag = 
            JsonUtils.getBooleanOptional(node, "exclusiveMaximum", null);
        if (Boolean.TRUE.equals(exclusiveMaximumFlag)) 
        {
            logger.warning("Applying exclusiveMaximum upgrade for " + uri);
            schema.setMaximum(null);
            schema.setExclusiveMaximum(maximum);
        }
        Boolean exclusiveMinimumFlag = 
            JsonUtils.getBooleanOptional(node, "exclusiveMinimum", null);
        if (Boolean.TRUE.equals(exclusiveMinimumFlag)) 
        {
            logger.warning("Applying exclusiveMinimum upgrade for " + uri);
            schema.setMinimum(null);
            schema.setExclusiveMinimum(maximum);
        }
        // LEGACY_SCHEMA_DRAFT_V4
        
        
    }



    /**
     * Set the properties that are specific for a {@link ArraySchema},
     * according to the values that are obtained from the
     * {@link #resolveNode(URI) node for the given URI}.
     * <ul>
     *   <li>{@link ArraySchema#setMaxItems(Integer)}</li>
     *   <li>{@link ArraySchema#setMinItems(Integer)}</li>
     *   <li>{@link ArraySchema#setUniqueItems(Boolean)}</li>
     * </ul>
     * If the given node has an <code>"items"</code> property, then
     * they will be processed with {@link #processArraySchemaItems}
     *
     * @param uri The URI
     * @param schema The {@link NumberSchema}
     */
    private void processArraySchema(URI uri, ArraySchema schema)
    {
        log("processArraySchema");
        log("    uri    " + uri);
        log("    schema " + SchemaUtils.createShortSchemaDebugString(schema));

        JsonNode node = resolveNode(uri);
        schema.setMaxItems(
            JsonUtils.getIntegerMinOptional(node, "maxItems", 0, null));
        schema.setMinItems(
            JsonUtils.getIntegerMinOptional(node, "minItems", 0, null));
        schema.setUniqueItems(
            JsonUtils.getBooleanOptional(node, "uniqueItems", null));
        if (node.has("items"))
        {
            processArraySchemaItems(uri, schema);
        }
    }


    /**
     * Process all <code>"items"</code> entries of the
     * {@link #resolveNode(URI) node for the given URI}, and set the
     * resulting collection of {@link Schema} instances as the
     * {@link ArraySchema#setItems(Schema) items} of the given
     * {@link ArraySchema}.
     *
     * @param uri The URI
     * @param schema The {@link ArraySchema}
     */
    private void processArraySchemaItems(
        URI uri, ArraySchema schema)
    {
        JsonNode node = resolveNode(uri);
        JsonNode itemsNode = node.get("items");
        URI itemsNodeUri = URIs.appendToFragment(uri, "items");

        log("processArraySchemaItems:");
        log("    itemsNodeUri " + itemsNodeUri);
        log("    itemsNode    " + itemsNode);
        Schema items = schemaResolver.apply(itemsNodeUri);

        schema.setItems(items);
    }

    /**
     * Set the properties that are specific for a {@link ObjectSchema},
     * according to the values that are obtained from the
     * {@link #resolveNode(URI) node for the given URI}.<br>
     * <br>
     * If the node has an <code>"properties"</code> entry,
     * then this will be processed by
     * {@link #processObjectSchemaProperties}.<br>
     * <br>
     * If the node has an <code>"additionalProperties"</code> entry,
     * then this will be processed by
     * {@link #processObjectSchemaAdditionalProperties}.<br>
     * <br>
     * <br>
     * <b>Note:</b> The following properties are not processed yet:
     * <ul>
     *   <li>patternProperties</li>
     *   <li>dependencies</li>
     * </ul>
     *
     * @param uri The URI
     * @param schema The {@link ObjectSchema}
     */
    private void processObjectSchema(URI uri, ObjectSchema schema)
    {
        log("processObjectSchema");
        log("    uri    " + uri);
        log("    schema " + SchemaUtils.createShortSchemaDebugString(schema));

        JsonNode node = resolveNode(uri);
        if (node.has("required"))
        {
            URI requiredUri =
                URIs.appendToFragment(uri, "required");
            processObjectSchemaRequired(requiredUri, schema);
        }
        if (node.has("properties"))
        {
            URI propertiesUri =
                URIs.appendToFragment(uri, "properties");
            processObjectSchemaProperties(propertiesUri, schema);
        }
        if (node.has("additionalProperties"))
        {
            URI additionalPropertiesUri =
                URIs.appendToFragment(uri, "additionalProperties");
            processObjectSchemaAdditionalProperties(
                additionalPropertiesUri, schema);
        }
        if (node.has("patternProperties"))
        {
            // TODO ObjectSchema patternProperties are not processed yet
            //JsonNode patternPropertiesNode = node.get("patternProperties");
            log("processObjectSchema WARNING: does not handle patternProperties");
            log("    uri    " + uri);

            // XXX patternProperties are not handled yet
            logger.warning("patternProperties are not handled yet");
        }

        if (node.has("dependentRequired"))
        {
            // TODO ObjectSchema dependentRequired are not processed yet
            log("processObjectSchema WARNING: does not handle dependentRequired");
            log("    uri    " + uri);
            logger.warning("dependentRequired are not handled yet");
        }

        // LEGACY_SCHEMA_DRAFT_V4
        if (node.has("dependencies"))
        {
            // TODO ObjectSchema dependencies are not processed yet
            log("processObjectSchema WARNING: does not handle dependencies");
            log("    uri    " + uri);
            logger.warning("dependencies are not handled yet");
        }
        // LEGACY_SCHEMA_DRAFT_V4
    }


    /**
     * Process the required properties that are contained in the
     * {@link #resolveNode(URI) node for the given URI}, and
     * assign them as the {@link ObjectSchema#setRequired(List) required}
     * list in the given {@link ObjectSchema}
     *
     * @param uri The URI
     * @param schema The {@link ObjectSchema}
     */
    private void processObjectSchemaRequired(
        URI uri, ObjectSchema schema)
    {
        JsonNode node = resolveNode(uri);
        if (node.isArray())
        {
            List<String> required = new ArrayList<String>();
            for (int i=0; i<node.size(); i++)
            {
                required.add(node.get(i).asText());
            }
            //System.out.println("Setting required "+required+" for "+uri);
            schema.setRequired(required);
        }
    }

    /**
     * Process the properties that are contained in the
     * {@link #resolveNode(URI) node for the given URI}, and
     * assign them as the {@link ObjectSchema#setProperties(Map) properties}
     * to the given {@link ObjectSchema}
     *
     * @param uri The URI
     * @param schema The {@link ObjectSchema}
     */
    private void processObjectSchemaProperties(
        URI uri, ObjectSchema schema)
    {
        JsonNode node = resolveNode(uri);
        if (node == null)
        {
            logger.warning("Could not resolve node for object schema " + uri);
            return;
        }
        Map<String, Schema> properties = new LinkedHashMap<String, Schema>();
        Iterator<Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext())
        {
            Entry<String, JsonNode> field = fields.next();
            String fieldName = field.getKey();
            JsonNode fieldValue = field.getValue();
            URI propertyUri = URIs.appendToFragment(uri, fieldName);

            log("processObjectSchemaProperties");
            log("    propertyUri    " + propertyUri);
            log("    fieldValue     " + fieldValue);

            Schema propertySchema = schemaResolver.apply(propertyUri);
            properties.put(fieldName, propertySchema);
        }
        if (!properties.isEmpty())
        {
            schema.setProperties(properties);
        }
    }


    /**
     * Process the additional properties for the given {@link ObjectSchema}
     *
     * TODO Proper comment
     *
     * @param uri The URI
     * @param schema The {@link ObjectSchema}
     */
    private void processObjectSchemaAdditionalProperties(
        URI uri, ObjectSchema schema)
    {
        JsonNode node = resolveNode(uri);
        if (node.isBoolean())
        {
            if (node.asBoolean())
            {
                // TODO Generate a schema that accepts everything.
                log("processObjectSchemaAdditionalProperties: " 
                    + "Generate schema for additionalProperties node " + node);
                schema.setAdditionalProperties(null);
            }
            else
            {
                schema.setAdditionalProperties(null);
            }
        }
        else
        {
            log("processObjectSchemaAdditionalProperties");
            log("    uri  " + uri);
            log("    node " + node);

            Schema additionalPropertiesSchema = schemaResolver.apply(uri);
            schema.setAdditionalProperties(additionalPropertiesSchema);
        }
    }

    /**
     * Returns the (unmodifiable) list of URIs that defined the given
     * {@link Schema}
     *
     * @param schema The {@link Schema}
     * @return The list of URIs
     */
    public List<URI> getUris(Schema schema)
    {
        return schemaToUris.get(schema);
    }

    /**
     * Returns the root {@link Schema} instances that have been generated 
     * by this generator
     *
     * @return The root {@link Schema} instances
     */
    public List<Schema> getRootSchemas()
    {
        List<Schema> rootSchemas = new ArrayList<Schema>();
        List<JsonNode> rootNodes = nodeRepository.getRootNodes();
        for (int i = 0; i < rootNodes.size(); i++)
        {
            JsonNode rootNode = rootNodes.get(i);
            Schema schema = schemas.get(rootNode);
            if (schema == null)
            {
                logger.warning("No schema for " + rootNode);
            }
            else
            {
                rootSchemas.add(schema);
            }
        }
        return rootSchemas;
    }

    /**
     * Obtain the node for the given URI from the {@link NodeRepository}
     *
     * @param uri The URI
     * @return The node
     */
    private JsonNode resolveNode(URI uri)
    {
        return nodeRepository.resolveNode(uri);
    }

    /**
     * Computes an unmodifiable mapping from {@link Schema} instances to
     * unmodifiable lists of all URIs that identify the respective
     * {@link Schema}.
     *
     * @return The mapping
     */
    private Map<Schema, List<URI>> computeSchemaToUrisMapping()
    {
        Map<JsonNode, List<URI>> nodeToUris =
            nodeRepository.computeNodeToUrisMapping();
        Map<Schema, List<URI>> schemaToUris =
            new LinkedHashMap<Schema, List<URI>>();
        for (Entry<JsonNode, Schema> entry : schemas.entrySet())
        {
            JsonNode node = entry.getKey();
            Schema schema = entry.getValue();
            List<URI> uris = nodeToUris.get(node);
            schemaToUris.put(schema, uris);
        }
        return Collections.unmodifiableMap(schemaToUris);
    }

    /**
     * Returns an unmodifiable set containing all {@link Schema} instances
     * that have been generated by this class
     *
     * @return The {@link Schema} set
     */
    public Set<Schema> getSchemaSet()
    {
        return Collections.unmodifiableSet(
            new LinkedHashSet<Schema>(schemas.values()));
    }

}


