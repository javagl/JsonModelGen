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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import de.javagl.jsonmodelgen.json.NodeRepository;
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
     * Entry point of the application
     * 
     * @param args Not used
     * @throws Exception If an error occurs
     */
    public static void main(String[] args) throws Exception
    {
        LoggerUtil.initLogging();
        Locale.setDefault(Locale.ENGLISH);
        
        generateGlTF();
    }
    
    /**
     * Generate the glTF classes from the schema
     * 
     * @throws IOException If an IO error occurs
     */
    private static void generateGlTF() throws IOException
    {
        String baseUrlString = 
            "https://raw.githubusercontent.com/KhronosGroup/glTF/main/";
        
        GeneratorInput coreGeneratorInput = new GeneratorInput();
        coreGeneratorInput.setUrlString(baseUrlString 
            + "/specification/2.0/schema/glTF.schema.json");
        coreGeneratorInput.setHeaderCode(
            createHeaderCode("glTF JSON model")); 
        coreGeneratorInput.setPackageName(
            "de.javagl.jgltf.impl.v2");
        
        
        List<GeneratorInput> generatorInputs = new ArrayList<GeneratorInput>();
        generatorInputs.add(coreGeneratorInput);
        generatorInputs.add(createExtensionGeneratorInput(
            baseUrlString, "mesh.primitive", "KHR_draco_mesh_compression"));
        generatorInputs.add(createExtensionGeneratorInput(
            baseUrlString, "glTF", "KHR_lights_punctual"));
        generatorInputs.add(createExtensionGeneratorInput(
            baseUrlString, "glTF", "KHR_materials_clearcoat"));
        generatorInputs.add(createExtensionGeneratorInput(
            baseUrlString, "glTF", "KHR_materials_ior"));
        generatorInputs.add(createExtensionGeneratorInput(
            baseUrlString, "glTF", "KHR_materials_pbrSpecularGlossiness"));
        generatorInputs.add(createExtensionGeneratorInput(
            baseUrlString, "glTF", "KHR_materials_sheen"));
        generatorInputs.add(createExtensionGeneratorInput(
            baseUrlString, "glTF", "KHR_materials_specular"));
        generatorInputs.add(createExtensionGeneratorInput(
            baseUrlString, "glTF", "KHR_materials_transmission"));
        generatorInputs.add(createExtensionGeneratorInput(
            baseUrlString, "glTF", "KHR_materials_unlit"));
        // TODO No sensible output for now
        //generatorInputs.add(createExtensionGeneratorInput(
        //    baseUrlString, "glTF", "KHR_materials_variants"));
        generatorInputs.add(createExtensionGeneratorInput(
            baseUrlString, "glTF", "KHR_materials_volume"));
        // TODO No schema
        //generatorInputs.add(createExtensionGeneratorInput(
        //    baseUrlString, "glTF", "KHR_mesh_quantization"));
        generatorInputs.add(createExtensionGeneratorInput(
            baseUrlString, "glTF", "KHR_techniques_webgl"));
        generatorInputs.add(createExtensionGeneratorInput(
            baseUrlString, "texture", "KHR_texture_basisu"));
        // TODO Different main file name
        //generatorInputs.add(createExtensionGeneratorInput(
        //    baseUrlString, "glTF", "KHR_texture_transform"));
        // TODO Invalid field name
        //generatorInputs.add(createExtensionGeneratorInput(
        //    baseUrlString, "glTF", "KHR_xmp"));
        generatorInputs.add(createExtensionGeneratorInput(
            baseUrlString, "glTF", "KHR_xmp_json_ld"));
        
        File outputDirectory = new File("./data/output");
        generate(generatorInputs, outputDirectory);
    }
    
    /**
     * Create a {@link GeneratorInput} for the default "KHR_" extension
     * with the given name
     * 
     * @param baseUrlString The base URL string
     * @param mainFilePrefix The main file prefix. Well...
     * @param extensionName The extension name
     * @return the {@link GeneratorInput}
     */
    private static GeneratorInput createExtensionGeneratorInput(
        String baseUrlString, String mainFilePrefix, String extensionName)
    {
        GeneratorInput generatorInput = new GeneratorInput();
        generatorInput.setUrlString(baseUrlString 
            + "extensions/2.0/Khronos/"+extensionName+"/schema/"
            + mainFilePrefix + "." +extensionName+".schema.json");
        generatorInput.setHeaderCode(
            createHeaderCode("glTF "+extensionName+" JSON model")); 
        String packageNamePart = extensionName;
        if (packageNamePart.startsWith("KHR_")) 
        {
            packageNamePart = packageNamePart.substring(4);
        }
        generatorInput.setPackageName(
            "de.javagl.jgltf.impl.v2.ext." + packageNamePart);
        return generatorInput;
    }
    
    
    /**
     * Create a {@link ClassGeneratorConfig} that is supposed to be used
     * for generating the glTF classes
     * 
     * @return The {@link ClassGeneratorConfig}
     */
    private static ClassGeneratorConfig createGltfConfig()
    {
        ClassGeneratorConfig config = new ClassGeneratorConfig();
        config.set(ClassGeneratorConfig.CREATE_ADDERS_AND_REMOVERS, true);
        config.set(ClassGeneratorConfig.CREATE_GETTERS_WITH_DEFAULT, true);
        
        config.addTypeOverride(
            ".*accessor.schema.json#/properties/min", Number[].class);
        config.addTypeOverride(
            ".*accessor.schema.json#/properties/max", Number[].class);
        
        config.setSkippingValidation("TEMPPACKAGENAME.Image#mimeType", true);
        
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
    private static void generate(
        List<GeneratorInput> generatorInputs, File outputDirectory) 
            throws IOException
    {
        logger.info("Creating NodeRepository");
        NodeRepository nodeRepository = new NodeRepository();
        for (GeneratorInput generatorInput : generatorInputs)
        {
            URI uri = null;
            try 
            {
                uri = new URI(generatorInput.getUrlString());
            }
            catch (URISyntaxException e)
            {
                throw new IOException(e);
            }
            logger.info("  Populating NodeRepository with " + uri);
            nodeRepository.generateNodes(uri);
            
        }
        logger.info("Creating NodeRepository DONE");
        //System.out.println(nodeRepository.createDebugString());
        
        logger.info("Creating SchemaGenerator");
        SchemaGenerator schemaGenerator = new SchemaGenerator(nodeRepository);
        logger.info("Creating SchemaGenerator DONE");
        
        logger.info("Creating ClassGenerator");
        ClassGenerator classGenerator = 
            new ClassGenerator(createGltfConfig(), 
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
    private static String createHeaderCode(String headerTitle)
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

