// fragment shader for rendering the bullet
precision highp float;
uniform vec3 viewPosition;
uniform vec3 lightPosition;
uniform int applyLighting;

varying vec3 v_vertex;
varying vec3 v_normal;
varying vec4 v_color;

void main() {
   if(applyLighting > 0){
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

    gl_FragColor = vec4((min(ambient + diffuse + specular, 1.0) * lightColor), 1.0) * v_color;
   } else {
      gl_FragColor = v_color;
   }
}