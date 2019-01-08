attribute vec3 a_vertex;
attribute vec2 a_tex_coord;

varying vec2 v_tex_coord;

void main()
{
    gl_Position = vec4(a_vertex, 1.0);
    v_tex_coord = a_tex_coord;
}