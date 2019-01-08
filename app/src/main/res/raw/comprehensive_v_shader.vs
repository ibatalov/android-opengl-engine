// comprehensive vertex shader
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 modelMatrix;
uniform mat4 normalMatrix;

attribute vec3 a_vertex;
attribute vec3 a_normal;
attribute vec4 a_color;
attribute vec2 a_tex_coord;

varying vec3 v_vertex;
varying vec3 v_normal;
varying vec4 v_color;
varying vec2 v_tex_coord;

void main() {
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(a_vertex, 1.0);
    v_vertex = vec3(modelMatrix * vec4(a_vertex, 1.0));
    v_normal = normalize(mat3(normalMatrix) * a_normal);
    if(a_color.r == 0.0 && a_color.g == 0.0 && a_color.b == 0.0){
    	v_color = vec4(1.0);
    } else{
    	v_color = a_color;
	}
	v_tex_coord = a_tex_coord;
}