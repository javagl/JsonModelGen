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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.javagl.jsonmodelgen.json.schema.v202012.codemodel.ClassGeneratorConfig;

/**
 * Main class of the JSON model generator
 */
public class JsonModelGenGltfCore 
{
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
        coreGeneratorInput.setUriString(baseUrlString 
            + "/specification/2.0/schema/glTF.schema.json");
        
        coreGeneratorInput.setHeaderCode(
            JsonModelGen.createHeaderCode("glTF JSON model")); 
        coreGeneratorInput.setPackageName(
            "de.javagl.jgltf.impl.v2");
        
        List<GeneratorInput> generatorInputs = new ArrayList<GeneratorInput>();
        generatorInputs.add(coreGeneratorInput);
        File outputDirectory = new File("../JsonModelGenOutputProject/src");
        outputDirectory.mkdirs();
        ClassGeneratorConfig config = 
            JsonModelGen.createDefaultClassGeneratorConfig();
        JsonModelGen.generate(config, generatorInputs, outputDirectory);
    }
}

