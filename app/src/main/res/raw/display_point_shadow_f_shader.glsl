// fragment shader, draws point shadow cube map
uniform samplerCube depthMap;

varying vec3 v_vertex;

float linearizeDepth(float depth, float near, float far) 
{
    float z = depth * 2.0 - 1.0; // back to NDC 
    return (2.0 * near * far) / (far + near - z * (far - near));	
}

// unpack colour to depth value
float unpack (vec4 colour)
{
    const vec4 bitShifts = vec4(1.0 / (256.0 * 256.0 * 256.0),
                                1.0 / (256.0 * 256.0),
                                1.0 / 256.0,
                                1);
    return dot(colour , bitShifts);
}

void main()
{
    vec4 tex_color = textureCube(depthMap, v_vertex);
    gl_FragColor = vec4(vec3(unpack(tex_color)), 1.0);
    //gl_FragColor = vec4(vec3(tex_color.r), 1.0);
    //gl_FragColor = vec4(vec3(length(v_vertex)), 1.0)/2.0;
}