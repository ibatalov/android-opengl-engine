// vertex shader for drawing 2D shapes

attribute vec4 a_vertex;
attribute vec4 a_color;

uniform mat4 modelMatrix;
uniform mat4 projectionMatrix;

varying vec4 v_color;

void main(){

	v_color = a_color;
	gl_Position = projectionMatrix * modelMatrix * a_vertex;
}