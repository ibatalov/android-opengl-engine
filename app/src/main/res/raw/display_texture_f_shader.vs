uniform sampler2D texture;

varying vec2 v_tex_coord;

void main()
{
	/*
	if(abs(v_tex_coord.x) > 1.0 || abs(v_tex_coord.y) > 1.0){
		gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
	} else{
		gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
	}
	*/
    gl_FragColor = texture2D(texture, v_tex_coord);
}