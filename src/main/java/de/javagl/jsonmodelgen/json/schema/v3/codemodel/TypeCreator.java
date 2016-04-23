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

import com.sun.codemodel.JType;

import de.javagl.jsonmodelgen.json.schema.v3.ArraySchema;
import de.javagl.jsonmodelgen.json.schema.v3.BooleanSchema;
import de.javagl.jsonmodelgen.json.schema.v3.IntegerSchema;
import de.javagl.jsonmodelgen.json.schema.v3.NumberSchema;
import de.javagl.jsonmodelgen.json.schema.v3.ObjectSchema;
import de.javagl.jsonmodelgen.json.schema.v3.Schema;
import de.javagl.jsonmodelgen.json.schema.v3.StringSchema;

/**
 * Interface for classes that can create code model types based on 
 * {@link Schema} types
 */
interface TypeCreator
{
    /**
     * Create the type for the given {@link Schema}
     * 
     * @param schema The {@link Schema}
     * @return The type
     */
    JType createType(Schema schema);

    /**
     * Create the type for the given {@link ObjectSchema}
     * 
     * @param schema The {@link ObjectSchema}
     * @return The type
     */
    JType createObjectType(ObjectSchema schema);

    /**
     * Create the type for the given {@link ArraySchema}
     * 
     * @param schema The {@link ArraySchema}
     * @return The type
     */
    JType createArrayType(ArraySchema schema);

    /**
     * Create the type for the given {@link BooleanSchema}
     * 
     * @param schema The {@link BooleanSchema}
     * @return The type
     */
    JType createBooleanType(BooleanSchema schema);

    /**
     * Create the type for the given {@link IntegerSchema}
     * 
     * @param schema The {@link IntegerSchema}
     * @return The type
     */
    JType createIntegerType(IntegerSchema schema);
    
    /**
     * Create the type for the given {@link NumberSchema}
     * 
     * @param schema The {@link NumberSchema}
     * @return The type
     */
    JType createNumberType(NumberSchema schema);

    /**
     * Create the type for the given {@link StringSchema}
     * 
     * @param schema The {@link StringSchema}
     * @return The type
     */
    JType createStringType(StringSchema schema);
}