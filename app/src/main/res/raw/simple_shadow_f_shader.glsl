precision mediump float;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 modelMatrix;
uniform vec3 viewPosition;
uniform vec3 lightPosition;

varying vec3 v_vertex;
varying vec3 v_normal;
varying vec4 v_color;

varying vec4 light_space_coord;
uniform sampler2D depthMap;

float shadowCalculation(vec4 light_space_coord, float bias)
{
    vec3 projCoords = light_space_coord.xyz / light_space_coord.w;
    projCoords = projCoords * 0.5 + 0.5;

    float shadow = 0.0;
    if(projCoords.z <= 1.0){
        float closestDepth = texture2D(depthMap, projCoords.xy).r;
        float currentDepth = projCoords.z;
        shadow = currentDepth - bias > closestDepth  ? 1.0 : 0.0;
    }
    return shadow;
}

void main() {

   vec3 lightDirection = lightPosition - v_vertex;
   float lightDistance = dot(lightDirection, lightDirection);
   lightDirection = normalize(lightDirection);
   vec3 viewDirection = normalize(viewPosition - v_vertex);
   vec3 halfWayVector = normalize(viewDirection + lightDirection);
   vec3 lightColor = vec3(1.0, 1.0, 1.0);

   float ambient = 0.1;

   float diffuseStrength = 0.5/(lightDistance + 1.0);
   float diffuse = max(dot(v_normal, lightDirection), 0.0) * diffuseStrength;

   float specularStrength = 0.5/(lightDistance + 1.0);
   float specular = pow(max(dot(v_normal, halfWayVector), 0.0), 16.0) * specularStrength;

   float bias = max(0.05 * (1.0 - dot(v_normal, lightDirection)), 0.001);
   float shadow = shadowCalculation(light_space_coord, bias);

    gl_FragColor = vec4((min((ambient + (diffuse + specular)*(1.0 - shadow)), 1.0) * lightColor), 1.0) * v_color;
}