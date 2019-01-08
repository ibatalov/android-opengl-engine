// blur fragment shader
#define GAUSS_KERNEL_SIZE 7
uniform float weight[GAUSS_KERNEL_SIZE];
uniform int orientation;
uniform vec2 textureSize;
uniform sampler2D texture;

varying vec2 v_tex_coord;

void main(){
	vec2 unitVector = 1.0/textureSize;
	vec4 color = texture2D(texture, v_tex_coord) * weight[0];

	if(orientation == 0){ //vertical
		unitVector.x = 0.0;
	} else{ // horizontal
		unitVector.y = 0.0;
	}
	for(int i = 1; i < GAUSS_KERNEL_SIZE; i++){
		color += texture2D(texture, v_tex_coord + float(i) * unitVector) * weight[i];
		color += texture2D(texture, v_tex_coord - float(i) * unitVector) * weight[i];
	}
	gl_FragColor = color;
}