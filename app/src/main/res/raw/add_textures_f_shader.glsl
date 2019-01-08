//fragment shader for drawing texture on a simple quad
#define TEXTURE_COUNT 3
uniform sampler2D texture[TEXTURE_COUNT];
uniform float coeffs[TEXTURE_COUNT];

varying vec2 v_tex_coord;

void main(){
	vec4 color = vec4(0.0);
	for(int i = 0; i < TEXTURE_COUNT; i++){
		color = texture2D(texture[i], v_tex_coord);
		if(coeffs[i] > 0.0){
			color.rgb = color.rgb * coeffs[i];
			gl_FragColor = vec4(color.rgb + gl_FragColor.rgb, 1.0);

		} else{
			gl_FragColor = vec4(color.a*color.rgb + (1.0-color.a)*gl_FragColor.rgb, 1.0);
		}

		
/*
		if(coeffs[i] == 0.0 && (color.r + color.g + color.b) > 0.0){
			gl_FragColor = color;
		}

		if(coeffs[i] > 0.0){
			gl_FragColor = gl_FragColor + coeffs[i]*color;
		}
		*/
	}
}