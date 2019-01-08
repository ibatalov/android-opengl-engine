// vertex shader, draws point shadow map
attribute vec3 a_vertex;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

varying vec3 v_vertex;

void main()
{
	v_vertex = vec3(modelMatrix * vec4(a_vertex, 1.0));
	gl_Position = projectionMatrix * viewMatrix * vec4(v_vertex, 1.0);
}