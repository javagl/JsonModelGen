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

import java.net.URI;
import java.util.Collection;

import de.javagl.jsonmodelgen.json.schema.codemodel.ClassNameGenerator;
import de.javagl.jsonmodelgen.json.schema.codemodel.ClassNameUtils;
import de.javagl.jsonmodelgen.json.schema.v3.ObjectSchema;

/**
 * Default implementation of a {@link ClassNameGenerator} for 
 * {@link ObjectSchema} instances
 */
class DefaultClassNameGenerator implements ClassNameGenerator<ObjectSchema>
{
    @Override
    public String generateClassName(ObjectSchema schema, Collection<URI> uris)
    {
        String className = ClassNameUtils.deriveClassName(uris);
        return className;
    }
}