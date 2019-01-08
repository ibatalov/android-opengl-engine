uniform sampler2D depthMap;

varying vec2 v_tex_coord;

void main()
{
    float depthValue = texture2D(depthMap, v_tex_coord).r;
    gl_FragColor = vec4(vec3(depthValue), 1.0);
}