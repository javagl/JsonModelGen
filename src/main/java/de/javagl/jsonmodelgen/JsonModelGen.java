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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import de.javagl.jsonmodelgen.json.NodeRepository;
import de.javagl.jsonmodelgen.json.URIs;
import de.javagl.jsonmodelgen.json.schema.v202012.SchemaGenerator;
import de.javagl.jsonmodelgen.json.schema.v202012.codemodel.ClassGenerator;
import de.javagl.jsonmodelgen.json.schema.v202012.codemodel.ClassGeneratorConfig;

/**
 * Main class of the JSON model generator
 */
public class JsonModelGen 
{
    /**
     * The logger used in this class
     */
    private static final Logger logger = 
        Logger.getLogger(JsonModelGen.class.getName());
    
    /**
     * Create a {@link ClassGeneratorConfig} with some unspecified default
     * settings, mainly for generating the glTF- and 3D Tiles classes.
     * 
     * @return The {@link ClassGeneratorConfig}
     */
    private static ClassGeneratorConfig createDefaultClassGeneratorConfig()
    {
        ClassGeneratorConfig config = new ClassGeneratorConfig();
        config.set(ClassGeneratorConfig.CREATE_ADDERS_AND_REMOVERS, true);
        config.set(ClassGeneratorConfig.CREATE_GETTERS_WITH_DEFAULT, true);
        
        config.addTypeOverride(
            ".*accessor.schema.json#/properties/min", Number[].class);
        config.addTypeOverride(
            ".*accessor.schema.json#/properties/max", Number[].class);
        
        config.setSkippingValidation(
            "de.javagl.jgltf.impl.v2.Image#mimeType", true);
        
        // TODO This is only for 3DTILES_Metadata:
        config.addClassNameOverride("Class", "MetadataClass");
        config.addClassNameOverride("Enum", "MetadataEnum");

        // TODO This is only for 3DTILES_batch_table_hierarchy:
        config.addClassNameOverride("BatchTableHierarchyPropertiesClassesItems",
            "BatchTableHierarchyClass");
        
        return config;
    }
    
    /**
     * Generate the classes for the schema with the given inputs,
     * in the given output directory
     * 
     * @param generatorInputs The {@link GeneratorInput} objects
     * @param outputDirectory The output directory
     * @throws IOException If an IO error occurs
     */
    static void generate(
        List<GeneratorInput> generatorInputs, File outputDirectory) 
            throws IOException
    {
        logger.info("Creating NodeRepository");
        NodeRepository nodeRepository = new NodeRepository();
        for (GeneratorInput generatorInput : generatorInputs)
        {
            String uriString = generatorInput.getUriString();
            URI uri = URIs.create(uriString);
            logger.info("  Populating NodeRepository with root URI " + uri);

            List<String> searchUriStrings = generatorInput.getSearchUriStrings();
            for (String searchUriString : searchUriStrings)
            {
                nodeRepository.addSearchUri(
                    URIs.create(searchUriString));
            }
            nodeRepository.addRootUri(
                URIs.create(uriString));
            
        }
        logger.info("Creating NodeRepository DONE");
        //System.out.println(nodeRepository.createDebugString());
        
        logger.info("Creating SchemaGenerator");
        SchemaGenerator schemaGenerator = new SchemaGenerator(nodeRepository);
        logger.info("Creating SchemaGenerator DONE");
        
        logger.info("Creating ClassGenerator");
        ClassGenerator classGenerator = 
            new ClassGenerator(createDefaultClassGeneratorConfig(), 
                schemaGenerator, generatorInputs);
        logger.info("Creating ClassGenerator DONE");
        
        logger.info("Creating classes");
        classGenerator.generate(outputDirectory);
        logger.info("Creating DONE");
    }
    
    /**
     * Creates the header code to be inserted into every generated class,
     * with the given title
     * 
     * @param headerTitle The header title
     * @return The header code
     */
    static String createHeaderCode(String headerTitle)
    {
        String headerCode = 
            "/*\n" +
            " * " + headerTitle + "\n" +
            " * \n" +
            " * Do not modify this class. It is automatically generated\n" +
            " * with JsonModelGen (https://github.com/javagl/JsonModelGen)\n" +
            " * Copyright (c) 2016-2021 Marco Hutter - http://www.javagl.de\n" +
            " */\n";
        return headerCode;
    }
}

