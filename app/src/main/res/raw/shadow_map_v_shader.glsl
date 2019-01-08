attribute vec3 a_vertex;

uniform mat4 lightSpaceMatrix;
uniform mat4 modelMatrix;

void main()
{
   gl_Position = lightSpaceMatrix * modelMatrix * vec4(a_vertex, 1.0);
}