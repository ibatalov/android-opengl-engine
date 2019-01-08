// fragment shader for drawing simple 2D shapes

uniform vec4 color;

varying vec4 v_color;

void main(){
	if(v_color.r + v_color.g + v_color.b > 0.0){
		gl_FragColor = v_color;
	} else{
		gl_FragColor = color;
	}
}