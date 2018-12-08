# JsonModelGen - Model Generation from JSON Schema

A library for generating source code from JSON schema definitions.

**Note:** This library is not intended for public use. If you want to create
Java source code from an arbitrary JSON schema, have a look at 
[jsonschema2pojo](https://github.com/joelittlejohn/jsonschema2pojo) or
other libraries.

The goal of this project mainly was to create a code model for 
[glTF](https://github.com/KhronosGroup/glTF/), with the purpose
of creating a basic data model for [JglTF](https://github.com/javagl/JglTF). 
This library only supports the 
[JSON Schema Version 03](https://tools.ietf.org/html/draft-zyp-json-schema-03),
which was used for the specification of glTF, and only the parts of the
schema that are relevant for glTF. The library generates a reasonable model 
for glTF, but details may change arbitrarily in the future. Details of 
the model generation process may not be configured extensively.



# What may be useful about this library
  
One of the goals of this library was to create a comparatively sophisticated
model from the JSON Schema. That is, it should create JavaDoc comments and
validation statements, based on the constraints that are stated in the
Schema definition. 

For example, the following excerpt from the 
[glTF camera.perspective.schema.json](https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/schema/camera.perspective.schema.json)

```javascript
{
    ...
    "properties" : {
        ...
        "znear" : {
            "type" : "number",
            "description" : "The floating-point distance to the near clipping plane.",
            "required" : true,
            "minimum" : 0.0,
            "exclusiveMinimum" : true,
            "gltf_detailedDescription" : "The floating-point distance to the near clipping plane.  zfar must be greater than znear."
        }
    },
}
```

will result in the following excerpt of Java source code 

```
/**
 * A perspective camera containing properties to create a perspective 
 * projection matrix. 
 * 
 * Auto-generated for camera.perspective.schema.json 
 * 
 */
public class CameraPerspective
    extends GlTFProperty
{
    ...
    /**
     * The floating-point distance to the near clipping plane. (required)<br> 
     * Minimum: 0.0 (exclusive) 
     * 
     */
    private Float znear;


    ...
    
    /**
     * The floating-point distance to the near clipping plane. (required)<br> 
     * Minimum: 0.0 (exclusive) 
     * 
     * @param znear The znear to set
     * @throws NullPointerException If the given value is <code>null</code>
     * @throws IllegalArgumentException If the given value does not meet
     * the given constraints
     * 
     */
    public void setZnear(Float znear) {
        if (znear == null) {
            throw new NullPointerException((("Invalid value for znear: "+ znear)+", may not be null"));
        }
        if (znear<= 0.0D) {
            throw new IllegalArgumentException("znear <= 0.0");
        }
        this.znear = znear;
    }

    /**
     * The floating-point distance to the near clipping plane. (required)<br> 
     * Minimum: 0.0 (exclusive) 
     * 
     * @return The znear
     * 
     */
    public Float getZnear() {
        return this.znear;
    }

}
``` 


