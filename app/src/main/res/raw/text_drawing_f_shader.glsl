// text drawing fragment shader
uniform sampler2D fontMap;
uniform vec4 textColor;
uniform vec4 backgroundColor;

varying vec2 v_tex_coord;

void main(){
	vec4 textureColor = texture2D(fontMap, v_tex_coord);
	if(textureColor.r + textureColor.g + textureColor.b == 0.0){
		gl_FragColor = backgroundColor;
	} else{
		gl_FragColor = textColor * textureColor;
	}
}