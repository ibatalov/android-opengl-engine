attribute vec3 a_vertex;
attribute vec2 a_tex_coord;

uniform mat4 projectionMatrix;

varying vec2 v_tex_coord;


void main()
{
    gl_Position = projectionMatrix*vec4(a_vertex, 1.0);
    v_tex_coord = a_tex_coord;
}