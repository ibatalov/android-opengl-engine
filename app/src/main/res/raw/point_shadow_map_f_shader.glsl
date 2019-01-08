precision highp float;

uniform vec3 lightPosition;
uniform float far_plane;
varying vec3 v_vertex;

vec4 pack (float depth){
	const vec4 bitSh = vec4(256.0 * 256.0 * 256.0,
							256.0 * 256.0,
							256.0,
							1.0);
	const vec4 bitMsk = vec4(0,
							 1.0 / 256.0,
							 1.0 / 256.0,
							 1.0 / 256.0);
	vec4 comp = fract(depth * bitSh);
	comp -= comp.xxyz * bitMsk;
	return comp;
}

void main()
{
	float distance  = length(lightPosition - v_vertex)/far_plane;
	// pack value into 32-bit RGBA texture
	gl_FragColor = pack(distance);
}