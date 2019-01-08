// fragment shader, draws scene with point shadow
precision highp float;
uniform mat4 modelMatrix;
uniform vec3 viewPosition;
uniform vec3 lightPosition;

uniform float far_plane;

varying vec3 v_vertex;
varying vec3 v_normal;
varying vec4 v_color;

uniform samplerCube depthMap;

// unpack colour to depth value
float unpack (vec4 colour)
{
    const vec4 bitShifts = vec4(1.0 / (256.0 * 256.0 * 256.0),
                                1.0 / (256.0 * 256.0),
                                1.0 / 256.0,
                                1);
    return dot(colour , bitShifts);
}

float shadowCalculation(vec3 lightDirection, float lightDistance, float bias)
{ 
    float currentDepth = lightDistance/far_plane;
    float closestDepth = unpack(textureCube(depthMap, lightDirection));
    float shadow = currentDepth - bias > closestDepth  ? 1.0 : 0.0;

  return shadow;
}

void main() {

   vec3 lightDirection = lightPosition - v_vertex;
   float lightDistance = length(lightDirection);
   float lightDistanceSquared = pow(lightDistance, 2.0);
   lightDirection = normalize(lightDirection);
   vec3 viewDirection = normalize(viewPosition - v_vertex);
   vec3 halfWayVector = normalize(viewDirection + lightDirection);
   vec3 lightColor = vec3(1.0, 1.0, 1.0);

   float ambient = 0.05;

   float diffuseEverything = 0.2 * max(dot(v_normal, lightDirection), 0.0);
   float diffuseStrength = 0.8/(lightDistanceSquared + 2.0);
   float diffuse = diffuseEverything * diffuseStrength * 10.0;

   float specularStrength = 0.6/(lightDistanceSquared + 2.0);
   float specular = pow(max(dot(v_normal, halfWayVector), 0.0), 16.0) * specularStrength;

   //float bias = max(0.05 * (1.0 - dot(v_normal, lightDirection)), 0.005);
   float bias = 0.0;
   float shadow = shadowCalculation(-lightDirection, lightDistance, bias);
   //float shadow = 0.0;
    gl_FragColor = vec4((min((ambient + diffuseEverything + (diffuse + specular)*(1.0 - shadow)), 1.0) * lightColor), 1.0) * v_color;
}