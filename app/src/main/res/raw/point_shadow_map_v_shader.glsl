attribute vec3 a_vertex;

uniform mat4 modelMatrix;
uniform mat4 lightSpaceMatrix;

varying vec3 v_vertex;

void main()
{
   gl_Position = lightSpaceMatrix * modelMatrix * vec4(a_vertex, 1.0);
   v_vertex = vec3(modelMatrix * vec4(a_vertex, 1.0));
}