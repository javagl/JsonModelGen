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
        //generateTiles();
    }
    
    /**
     * Performs the actual generation
     * 
     * @throws Exception If an error occurs
     */
    private static void generateGlTF() throws Exception
    {
        String urlString = "https://raw.githubusercontent.com/KhronosGroup/"
            + "glTF/master/specification/2.0/schema/glTF.schema.json";
        String headerCode = createHeaderCode("glTF JSON model"); 
        String packageName = "de.javagl.jgltf.impl.v2";
        
        URI rootUri = new URI(urlString);
        File outputDirectory = new File("./data/output/");
        generate(rootUri, packageName, headerCode, outputDirectory);
    }    
    
    //--------------------------------------------------------------------------
    // Experimental 3D Tiles generation
    /**
     * Performs the actual generation
     * 
     * @throws Exception If an error occurs
     */
    private static void generateTiles() throws Exception
    {
        generateTiles("tileset.schema.json", ".impl");
        //generateTiles("i3dm.featureTable.schema.json", ".impl");
        //generateTiles("pnts.featureTable.schema.json", ".impl");
    }
    
    /**
     * Performs the actual generation
     * 
     * @throws Exception If an error occurs
     */
    private static void generateTiles(
        String fileName, String packageNameSuffixStartingWithDot) 
            throws Exception
    {
        String urlString = "https://raw.githubusercontent.com/CesiumGS/"
            + "3d-tiles/master/specification/schema/" + fileName;
        String headerCode = createHeaderCode("3D Tiles JSON model"); 
        String packageName = 
            "de.javagl.j3dtiles" + packageNameSuffixStartingWithDot;
        
        URI rootUri = new URI(urlString);
        File outputDirectory = new File("./data/output/");
        generate(rootUri, packageName, headerCode, outputDirectory);
    }
    //--------------------------------------------------------------------------
    
    private static ClassGeneratorConfig createGltfConfig()
    {
        ClassGeneratorConfig config = new ClassGeneratorConfig();
        config.set(ClassGeneratorConfig.CREATE_ADDERS_AND_REMOVERS, true);
        config.set(ClassGeneratorConfig.CREATE_GETTERS_WITH_DEFAULT, true);
        return config;
    }
    
    /**
     * Generate the classes for the schema with the given root element,
     * using the given package name, in the given output directory
     * 
     * @param rootUri The root JSON schema URI
     * @param packageName The package name
     * @param headerCode The header code for each file
     * @param outputDirectory The output directory
     * @throws IOException If an IO error occurs
     */
    private static void generate(URI rootUri, String packageName, 
        String headerCode, File outputDirectory) throws IOException
    {
        logger.info("Creating NodeRepository");
        NodeRepository nodeRepository = new NodeRepository(rootUri);
        logger.info("Creating NodeRepository DONE");
        //System.out.println(nodeRepository.createDebugString());
        
        logger.info("Creating SchemaGenerator");
        SchemaGenerator schemaGenerator = new SchemaGenerator(nodeRepository);
        logger.info("Creating SchemaGenerator DONE");
        
        logger.info("Creating ClassGenerator");
        ClassGenerator classGenerator = 
            new ClassGenerator(createGltfConfig(), 
                schemaGenerator, packageName, headerCode);
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

