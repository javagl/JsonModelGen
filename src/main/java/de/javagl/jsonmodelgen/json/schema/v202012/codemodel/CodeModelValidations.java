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

import java.util.List;
import java.util.logging.Logger;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JStatement;
import com.sun.codemodel.JType;

import de.javagl.jsonmodelgen.json.schema.codemodel.CodeModels;
import de.javagl.jsonmodelgen.json.schema.v202012.ArraySchema;
import de.javagl.jsonmodelgen.json.schema.v202012.NumberSchema;
import de.javagl.jsonmodelgen.json.schema.v202012.Schema;

/**
 * Methods to insert statements into blocks for the validation of arguments
 * that are passed to setter methods.
 */
class CodeModelValidations
{
    /**
     * The logger used in this class
     */
    private static final Logger logger =
        Logger.getLogger(CodeModelValidations.class.getName());

    /**
     * Insert a statement into the given block that checks whether a variable
     * with the given name is <code>null</code>, and throws a 
     * {@link NullPointerException} in this case.
     * <pre><code>
     * if (name == null)
     * {
     *     throw new NullPointerException(...);
     * }
     * </code></pre>
     * 
     * @param block The block
     * @param codeModel The code model
     * @param name The variable name
     */
    static void createNullCheckStatement(
        JBlock block, JCodeModel codeModel, String name)
    {
        JExpression conditionExpression = 
            JExpr.ref(name).eq(JExpr.ref("null"));;
        JExpression exceptionMessageExpression = 
            JExpr.lit("The " + name + " may not be null");
        JExpression exceptionExpression = 
            JExpr._new(codeModel._ref(NullPointerException.class)).
                arg(exceptionMessageExpression);
        block._if(conditionExpression)._then()._throw(exceptionExpression);
    }
    
    /**
     * Insert statements into the given block (of a setter method) that
     * handle the treatment of possible <code>null</code> arguments.<br>
     * <br>
     * If the property is required and not primitive, then this will insert
     * <pre><code>
     * if (propertyName == null)
     * {
     *     throw new NullPointerException(...);
     * }
     * </code></pre>
     * Otherwise, this will insert
     * <pre><code>
     * if (propertyName == null)
     * {
     *     this.propertyName = propertyName;
     *     return;
     * }
     * </code></pre>
     *
     * @param block The block
     * @param codeModel The code model
     * @param propertyName The property name
     * @param propertyType The property type
     * @param isRequired Whether the property is required
     * @param propertySchema The property schema
     */
    static void createNullHandlingStatements(
        JBlock block, JCodeModel codeModel,
        String propertyName, JType propertyType,
        Schema propertySchema, boolean isRequired)
    {
        if (!propertyType.isPrimitive() && isRequired)
        {
            JExpression conditionExpression =
                JExpr.ref(propertyName).eq(JExpr.ref("null"));;
            JExpression exceptionMessageExpression =
                JExpr.lit("Invalid value for "+propertyName+": ").
                    plus(JExpr.ref(propertyName)).
                    plus(JExpr.lit(", may not be null"));
            JExpression exceptionExpression =
                JExpr._new(codeModel._ref(NullPointerException.class)).
                    arg(exceptionMessageExpression);
            block._if(conditionExpression)._then()._throw(exceptionExpression);
        }
        else
        {
            JExpression conditionExpression =
                JExpr.ref(propertyName).eq(JExpr.ref("null"));
            JBlock ifNullBlock = block._if(conditionExpression)._then();
            ifNullBlock.assign(
                JExpr._this().ref(propertyName),
                JExpr.ref(propertyName));
            ifNullBlock._return();
        }
    }

    /**
     * Insert validation statements into the given block (of a setter method)
     * that perform the validation of the specified property, based on the
     * type of the given {@link Schema}
     *
     * @param block The block
     * @param codeModel The code model
     * @param propertyName The property name
     * @param propertyType The property type
     * @param propertySchema The property schema
     */
    static void createValidationStatements(
        JBlock block, JCodeModel codeModel,
        String propertyName, JType propertyType, Schema propertySchema)
    {
        Iterable<String> enumStrings = propertySchema.getEnumStrings();
        if (enumStrings == null)
        {
            if (propertySchema.getAnyOf() != null)
            {
                List<String> enumStringsFromAnyOf = 
                    SchemaCodeUtils.determineEnumStringsFromAnyOf(
                        propertySchema);
                if (!enumStringsFromAnyOf.isEmpty())
                {
                    enumStrings = enumStringsFromAnyOf;
                }
            }
        }
        if (enumStrings != null)
        {
            createEnumValidationStatements(
                block, codeModel, propertyName, propertyType, enumStrings);
        }
        else if (propertySchema.isInteger())
        {
            createIntegerValidationStatements(
                block, codeModel, propertyName, propertySchema.asInteger());
        }
        else if (propertySchema.isNumber())
        {
            createNumberValidationStatements(
                block, codeModel, propertyName, propertySchema.asNumber());
        }
        if (propertySchema.isArray())
        {
            ArraySchema arraySchema = propertySchema.asArray();
            createArrayValidationStatements(
                block, codeModel, propertyName, propertyType, arraySchema);
        }

        if (propertySchema.isString())
        {
            // TODO String validation statements: String length, pattern...
            logger.warning("String validation statements are not inserted yet");
        }
    }


    // TODO The insertion of the validation statements is a bit clumsy,
    // as an artifact of the change from 
    // "maximum(Number) and exclusiveMaximum(Boolean)"
    // to 
    // "maximum(Number) or exclusiveMaximum(Number)"
    // and this should be refactored a bit...
    
    /**
     * Insert validation statements into the given block (of a setter method)
     * that perform the validation of the specified property, based on the
     * properties that are defined in the given {@link NumberSchema}
     *
     * @param block The block
     * @param codeModel The code model
     * @param propertyName The property name
     * @param propertySchema The property schema
     */
    static void createIntegerValidationStatements(
        JBlock block, JCodeModel codeModel, String propertyName,
        NumberSchema propertySchema)
    {
        if (propertySchema.getMaximum() != null)
        {
            createMaximumValidationStatements(
                block, codeModel, propertyName,
                propertySchema.getMaximum().intValue(),
                null);
        }
        if (propertySchema.getExclusiveMaximum() != null)
        {
            createMaximumValidationStatements(
                block, codeModel, propertyName,
                null,
                propertySchema.getExclusiveMaximum().intValue());
        }
        if (propertySchema.getMinimum() != null)
        {
            createMinimumValidationStatements(
                block, codeModel, propertyName,
                propertySchema.getMinimum().intValue(),
                null);
        }
        if (propertySchema.getExclusiveMinimum() != null)
        {
            createMinimumValidationStatements(
                block, codeModel, propertyName,
                null,
                propertySchema.getExclusiveMinimum().intValue());
        }
    }

    /**
     * Insert validation statements into the given block (of a setter method)
     * that perform the validation of the specified property, based on the
     * properties that are defined in the given {@link NumberSchema}
     *
     * @param block The block
     * @param codeModel The code model
     * @param propertyName The property name
     * @param propertySchema The property schema
     */
    static void createNumberValidationStatements(
        JBlock block, JCodeModel codeModel, String propertyName,
        NumberSchema propertySchema)
    {
        if (propertySchema.getMaximum() != null)
        {
            createMaximumValidationStatements(
                block, codeModel, propertyName,
                propertySchema.getMaximum().doubleValue(),
                null);
        }
        if (propertySchema.getExclusiveMaximum() != null)
        {
            createMaximumValidationStatements(
                block, codeModel, propertyName,
                null,
                propertySchema.getExclusiveMaximum().doubleValue());
        }
        if (propertySchema.getMinimum() != null)
        {
            createMinimumValidationStatements(
                block, codeModel, propertyName,
                propertySchema.getMinimum().doubleValue(),
                null);
        }
        if (propertySchema.getExclusiveMinimum() != null)
        {
            createMinimumValidationStatements(
                block, codeModel, propertyName,
                null,
                propertySchema.getExclusiveMinimum().doubleValue());
        }
    }

    /**
     * Insert validation statements into the given block (of a setter method)
     * that perform the validation of the specified property, based on the
     * given maximum and exclusiveMaximum values.
     *
     * @param block The block
     * @param codeModel The code model
     * @param propertyName The property name
     * @param maximum The maximum value
     * @param exclusiveMaximum The exclusive maximum value
     */
    private static void createMaximumValidationStatements(
        JBlock block, JCodeModel codeModel, String propertyName,
        Number maximum, Number exclusiveMaximum)
    {
        String exceptionMessage;
        JExpression conditionExpression;
        JExpression limitExpression =
            CodeModels.createPrimitiveLiteralExpression(
                maximum != null ? maximum : exclusiveMaximum);
        if (exclusiveMaximum != null)
        {
            exceptionMessage = propertyName+" >= "+exclusiveMaximum;
            conditionExpression = JExpr.ref(propertyName).gte(limitExpression);
        }
        else
        {
            exceptionMessage = propertyName+" > "+maximum;
            conditionExpression = JExpr.ref(propertyName).gt(limitExpression);
        }
        JExpression exceptionExpression =
            JExpr._new(codeModel._ref(IllegalArgumentException.class)).
                arg(JExpr.lit(exceptionMessage));
        block._if(conditionExpression).
            _then()._throw(exceptionExpression);
    }

    /**
     * Insert validation statements into the given block (of a setter method)
     * that perform the validation of the specified property, based on the
     * given maximum and exclusiveMaximum values.
     *
     * @param block The block
     * @param codeModel The code model
     * @param propertyName The property name
     * @param minimum The maximum value
     * @param exclusiveMinimum The exclusive maximum value
     */
    private static void createMinimumValidationStatements(
        JBlock block, JCodeModel codeModel, String propertyName,
        Number minimum, Number exclusiveMinimum)
    {
        String exceptionMessage;
        JExpression conditionExpression;
        JExpression limitExpression =
            CodeModels.createPrimitiveLiteralExpression(
                minimum != null ? minimum : exclusiveMinimum);
        if (exclusiveMinimum != null)
        {
            exceptionMessage = propertyName+" <= "+exclusiveMinimum;
            conditionExpression = JExpr.ref(propertyName).lte(limitExpression);
        }
        else
        {
            exceptionMessage = propertyName+" < "+minimum;
            conditionExpression = JExpr.ref(propertyName).lt(limitExpression);
        }
        JExpression exceptionExpression =
            JExpr._new(codeModel._ref(IllegalArgumentException.class)).
                arg(JExpr.lit(exceptionMessage));
        block._if(conditionExpression).
            _then()._throw(exceptionExpression);
    }


    /**
     * Insert validation statements into the given block (of a setter method)
     * that perform the validation of the specified property, based on the
     * given collection of valid enum strings.
     *
     * @param block The block
     * @param codeModel The code model
     * @param propertyName The property name
     * @param propertyType The property type
     * @param enumStrings The enum strings
     */
    static void createEnumValidationStatements(
        JBlock block, JCodeModel codeModel, String propertyName,
        JType propertyType, Iterable<String> enumStrings)
    {
        if (propertyType.isPrimitive() || propertyType.unboxify().isPrimitive())
        {
            JPrimitiveType primitivePropertyType =
                (JPrimitiveType)propertyType.unboxify();
            createPrimitiveEnumValidationStatements(
                block, codeModel, propertyName,
                primitivePropertyType, enumStrings);
        }
        else if (propertyType.binaryName().equals("java.lang.String"))
        {
            createStringEnumValidationStatements(
                block, codeModel, propertyName, propertyType, enumStrings);
        }
    }

    /**
     * Insert validation statements into the given block (of a setter method)
     * that perform the validation of the specified property, based on the
     * given collection of valid enum strings.
     *
     * @param block The block
     * @param codeModel The code model
     * @param propertyName The property name
     * @param primitivePropertyType The property type
     * @param enumStrings The enum strings
     */
    static void createPrimitiveEnumValidationStatements(
        JBlock block, JCodeModel codeModel, String propertyName,
        JPrimitiveType primitivePropertyType, Iterable<String> enumStrings)
    {
        JExpression conditionExpression = null;
        for (String enumString : enumStrings)
        {
            JExpression valueExpression =
                CodeModels.createPrimitiveLiteralExpression(
                    enumString, primitivePropertyType);

            if (conditionExpression == null)
            {
                conditionExpression =
                    JExpr.ref(propertyName).ne(valueExpression);
            }
            else
            {
                conditionExpression = conditionExpression.cand(
                    JExpr.ref(propertyName).ne(valueExpression));
            }
        }
        JExpression exceptionMessageExpression =
            JExpr.lit("Invalid value for "+propertyName+": ").
                plus(JExpr.ref(propertyName)).
                plus(JExpr.lit(", valid: "+enumStrings));
        JExpression exceptionExpression =
            JExpr._new(codeModel._ref(IllegalArgumentException.class)).
                arg(exceptionMessageExpression);
        block._if(conditionExpression)._then()._throw(exceptionExpression);
    }


    /**
     * Insert validation statements into the given block (of a setter method)
     * that perform the validation of the specified property, based on the
     * given collection of valid enum strings.
     *
     * @param block The block
     * @param codeModel The code model
     * @param propertyName The property name
     * @param propertyType The property type
     * @param enumStrings The enum strings
     */
    static void createStringEnumValidationStatements(
        JBlock block, JCodeModel codeModel, String propertyName,
        JType propertyType, Iterable<String> enumStrings)
    {
        JExpression conditionExpression = null;
        for (String enumString : enumStrings)
        {
            JExpression valueExpression = JExpr.lit(enumString);
            if (conditionExpression == null)
            {
                conditionExpression =
                    valueExpression.invoke("equals").arg(
                        JExpr.ref(propertyName)).not();
            }
            else
            {
                conditionExpression = conditionExpression.cand(
                    valueExpression.invoke("equals").arg(
                        JExpr.ref(propertyName)).not());
            }
        }
        JExpression exceptionMessageExpression =
            JExpr.lit("Invalid value for "+propertyName+": ").
                plus(JExpr.ref(propertyName)).
                plus(JExpr.lit(", valid: "+enumStrings));
        JExpression exceptionExpression =
            JExpr._new(codeModel._ref(IllegalArgumentException.class)).
                arg(exceptionMessageExpression);
        block._if(conditionExpression)._then()._throw(exceptionExpression);

    }

    /**
     * Insert validation statements into the given block (of a setter method)
     * that perform the validation of the specified property, based on the
     * given {@link ArraySchema}
     *
     * @param block The block
     * @param codeModel The code model
     * @param propertyName The property name
     * @param propertyType The property type
     * @param arraySchema The {@link ArraySchema}
     */
    static void createArrayValidationStatements(
        JBlock block, JCodeModel codeModel, String propertyName,
        JType propertyType, ArraySchema arraySchema)
    {
        JType elementType = codeModel.ref(Object.class);
        JExpression lengthOrSizeExpression = null;
        if (propertyType.isArray())
        {
            lengthOrSizeExpression =
                JExpr.ref(propertyName).ref("length");
            elementType = propertyType.elementType();
        }
        else
        {
            lengthOrSizeExpression =
                JExpr.ref(propertyName).invoke("size");

            JClass c = (JClass)propertyType;
            if (c.getTypeParameters().size() > 0)
            {
                elementType = c.getTypeParameters().get(0);
            }
        }
        if (arraySchema.getMinItems() != null)
        {
            String exceptionMessage =
            	"Number of "+propertyName+
            	" elements is < "+arraySchema.getMinItems();
            JExpression conditionExpression =
                lengthOrSizeExpression.lt(JExpr.lit(arraySchema.getMinItems()));
            JExpression exceptionExpression =
                JExpr._new(codeModel._ref(IllegalArgumentException.class)).
                    arg(JExpr.lit(exceptionMessage));
            block._if(conditionExpression)._then()._throw(exceptionExpression);
        }
        if (arraySchema.getMaxItems() != null)
        {
            String exceptionMessage =
            	"Number of "+propertyName+
            	" elements is > "+arraySchema.getMaxItems();
            JExpression conditionExpression =
                lengthOrSizeExpression.gt(JExpr.lit(arraySchema.getMaxItems()));
            JExpression exceptionExpression =
                JExpr._new(codeModel._ref(IllegalArgumentException.class)).
                    arg(JExpr.lit(exceptionMessage));
            block._if(conditionExpression)._then()._throw(exceptionExpression);
        }

        String elementName = propertyName+"Element";
        Schema itemSchema = arraySchema.getItems();

        JBlock forEachBodyBlock = new JBlock();
        createValidationStatements(forEachBodyBlock, codeModel, elementName,
            elementType, itemSchema);
        if (!forEachBodyBlock.isEmpty())
        {
            JBlock forEachBody =
                block.forEach(elementType, elementName,
                    JExpr.ref(propertyName)).body();
            for (Object object : forEachBodyBlock.getContents())
            {
                if (object instanceof JStatement)
                {
                    JStatement statement = (JStatement)object;
                    forEachBody.add(statement);
                }
            }
        }
    }

    /**
     * Private constructor to prevent instantiations
     */
    private CodeModelValidations()
    {
        // Private constructor to prevent instantiations
    }


}
