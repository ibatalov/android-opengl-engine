// fragment shader displaying a texture on a quad

uniform sampler2D texture;

varying vec2 v_tex_coord;

void main()
{
    gl_FragColor = texture2D(texture, v_tex_coord);
}