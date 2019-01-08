// vertex shader for rendering the bullet
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 modelMatrix;

attribute vec3 a_vertex;
attribute vec3 a_normal;
attribute vec4 a_color;

varying vec3 v_vertex;
varying vec3 v_normal;
varying vec4 v_color;

void main() {
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(a_vertex, 1.0);
    v_vertex = vec3(modelMatrix * vec4(a_vertex, 1.0));
    v_normal = normalize(mat3(modelMatrix) * a_normal);
	v_color = a_color;
}