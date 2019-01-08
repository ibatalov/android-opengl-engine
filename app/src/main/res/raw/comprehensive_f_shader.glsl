// comprehensive fragment shader
precision highp float;

#define DIR_LIGHT_COUNT 1
#define POINT_LIGHT_COUNT 1
#define POINT_DIR_LIGHT_COUNT 1

#define VERTEX_COLOR 1
#define USE_TEXTURE 2
#define UNIFORM_COLOR 3

struct DirLight{
	vec3 dir;
	vec4 color;
	float ambient;
	float diffuse;
	float specular;
	int castShadow; // 0 - no, 1 - yes
	int bias_on;
	mat4 lightSpaceMatrix;
};
struct PointLight{
	vec3 position;
	vec4 color;
	float ambient;
	float diffuse;
	float specular;
	vec3 decayCoeffs;
	int castShadow; // 0 - no, 1 - yes
	int bias_on;
	float far_plane;
};
struct PointDirLight{
	vec3 position;
	vec4 color;
	float ambient;
	float diffuse;
	float specular;
	vec3 decayCoeffs;
	int castShadow; // 0 - no, 1 - yes
	int bias_on;
	mat4 lightSpaceMatrix;
};

uniform sampler2D depthMaps2D_Dir[DIR_LIGHT_COUNT];
uniform sampler2D depthMaps2D_Point[POINT_DIR_LIGHT_COUNT];
uniform samplerCube cubeDepthMaps[POINT_LIGHT_COUNT];

uniform DirLight dirLight[DIR_LIGHT_COUNT];
uniform PointLight pointLight[POINT_LIGHT_COUNT];
uniform PointDirLight pointDirLight[POINT_DIR_LIGHT_COUNT];

uniform sampler2D texture;
uniform vec3 viewPosition;

uniform int colorSource; // 0 - default, 1 - vertex color, 2 - texture, 3 - uniform
uniform vec4 u_color;

varying vec3 v_vertex;
varying vec3 v_normal;
varying vec4 v_color;
varying vec2 v_tex_coord;

// unpack colour to depth value
float unpack (vec4 colour){
    const vec4 bitShifts = vec4(1.0 / (256.0 * 256.0 * 256.0),
                                1.0 / (256.0 * 256.0),
                                1.0 / 256.0,
                                1);
    return dot(colour , bitShifts);
}

float pointShadowCalculation(vec3 lightDirection, float lightDistance, samplerCube depthMap, float far_plane, float bias){
    float currentDepth = lightDistance/far_plane;
    float closestDepth = unpack(textureCube(depthMap, lightDirection));
    float shadow = currentDepth - bias > closestDepth  ? 1.0 : 0.0;
  return shadow;
}

float simpleShadowCalculation(vec4 light_space_coord, sampler2D depthMap, float bias){
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

float dirLightCalculation(int lightNum, vec3 viewDirection){
	float shadow = 0.0;
	if(dirLight[lightNum].castShadow == 1){
		vec4 lightSpaceCoord = dirLight[lightNum].lightSpaceMatrix * vec4(v_vertex, 1.0);

		float bias = 0.0;
		if(dirLight[lightNum].bias_on != 0){
			bias = max(0.05 * (1.0 - dot(v_normal, -dirLight[lightNum].dir)), 0.005);
		}
		shadow = simpleShadowCalculation(lightSpaceCoord, depthMaps2D_Dir[lightNum], bias);
	}

	float diffuse = 0.0;
	float specular = 0.0;
	if(shadow == 0.0){
		diffuse = max(dot(v_normal, -dirLight[lightNum].dir), 0.0);
		if(diffuse > 0.0){
			vec3 halfWayVector = normalize(viewDirection - dirLight[lightNum].dir);
			specular = pow(max(dot(v_normal, halfWayVector), 0.0), 16.0);
		}
	}
	return dirLight[lightNum].ambient + dirLight[lightNum].diffuse*diffuse + dirLight[lightNum].specular*specular;
}

float pointLightCalculation(int lightNum, vec3 viewDirection){
	vec3 lightDirection = pointLight[lightNum].position - v_vertex;
	float lightDistance = length(lightDirection);
   	lightDirection = lightDirection/lightDistance;

   	float shadow = 0.0;
	if(pointLight[lightNum].castShadow == 1){
		float bias = 0.0;
		if(pointLight[lightNum].bias_on != 0){
			bias = max(0.05 * (1.0 - dot(v_normal, lightDirection)), 0.005);
		}
		//shadow = pointShadowCalculation(lightDirection, lightDistance, cubeDepthMaps[lightNum], pointLight[lightNum].far_plane, bias);
	}
	float diffuse = 0.0;
	float specular = 0.0;
	if(shadow == 0.0){
		diffuse = max(dot(v_normal, lightDirection), 0.0);
		if(diffuse > 0.0){
			vec3 halfWayVector = normalize(viewDirection + lightDirection);
			specular = pow(max(dot(v_normal, halfWayVector), 0.0), 16.0);
		}
	}
	float combinedLight = pointLight[lightNum].ambient + pointLight[lightNum].diffuse*diffuse + pointLight[lightNum].specular*specular;
	float lightDecay = pointLight[lightNum].decayCoeffs[0] + pointLight[lightNum].decayCoeffs[1]*lightDistance + pointLight[lightNum].decayCoeffs[2]*lightDistance*lightDistance;
	return combinedLight/lightDecay;
}

float pointDirLightCalculation(int lightNum, vec3 viewDirection){
	vec4 lightSpaceCoord = pointDirLight[lightNum].lightSpaceMatrix*vec4(v_vertex, 1.0);
	
	
	if(lightSpaceCoord.x/lightSpaceCoord.w > 1.0 || lightSpaceCoord.x/lightSpaceCoord.w < -1.0 ||  lightSpaceCoord.y/lightSpaceCoord.w > 1.0 || lightSpaceCoord.z/lightSpaceCoord.w > 1.0 ||  lightSpaceCoord.z/lightSpaceCoord.w < -1.0){
		return 0.0;
	}
	
	vec3 lightDirection = pointDirLight[lightNum].position - v_vertex;
	float lightDistance = length(lightDirection);
   	lightDirection = lightDirection/lightDistance;


   	float shadow = 0.0;
	if(pointDirLight[lightNum].castShadow == 1){
		float bias = 0.0;
		if(pointDirLight[lightNum].bias_on != 0){
			bias = max(0.05 * (1.0 - dot(v_normal, lightDirection)), 0.005);
		}
		shadow = simpleShadowCalculation(lightSpaceCoord, depthMaps2D_Point[lightNum], bias);
	}
	
	float diffuse = 0.0;
	float specular = 0.0;
	if(shadow == 0.0){
		diffuse = max(dot(v_normal, lightDirection), 0.0);
		if(diffuse > 0.0){
			vec3 halfWayVector = normalize(viewDirection + lightDirection);
			specular = pow(max(dot(v_normal, halfWayVector), 0.0), 16.0);
		}
	}
	float combinedLight = pointDirLight[lightNum].ambient + pointDirLight[lightNum].diffuse*diffuse + pointDirLight[lightNum].specular*specular;
	float lightDecay = pointDirLight[lightNum].decayCoeffs[0] + pointDirLight[lightNum].decayCoeffs[1]*lightDistance + pointDirLight[lightNum].decayCoeffs[2]*lightDistance*lightDistance;
	return combinedLight/lightDecay;
}

void main() {
	vec4 totalLightColor = vec4(0.0);
   	vec3 viewDirection = normalize(viewPosition - v_vertex);
   	//loop over dirLIght
   	int i = 0;
/*   	for(i = 0; i < DIR_LIGHT_COUNT; i++){
   		totalLightColor += dirLight[i].color * min(1.0, dirLightCalculation(i, viewDirection));
  	}
*/  	
  	//loop over pointLIght
	for(i = 0; i < POINT_LIGHT_COUNT; i++){
   		totalLightColor += pointLight[i].color*pointLightCalculation(i, viewDirection);
  	}

   	// loop over pointDirLIght
   	for(i = 0; i < POINT_LIGHT_COUNT; i++){
  		totalLightColor += pointDirLight[i].color * min(1.0, pointDirLightCalculation(i, viewDirection));
  	}
   	//float bias = max(0.05 * (1.0 - dot(v_normal, lightDirection)), 0.005);
  	//float shadow = 0.0;
  	totalLightColor.a = 1.0;
  	if(colorSource == USE_TEXTURE){
  		gl_FragColor = texture2D(texture, v_tex_coord) * totalLightColor;
  	} else if(colorSource == VERTEX_COLOR){
  		gl_FragColor = v_color * totalLightColor;
  	} else if(colorSource == UNIFORM_COLOR){
  		gl_FragColor = u_color * totalLightColor;
  	} else {
  		gl_FragColor = totalLightColor;
  	}
  	//gl_FragColor = vec4(1.0);
}