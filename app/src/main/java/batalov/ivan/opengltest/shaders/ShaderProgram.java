package batalov.ivan.opengltest.shaders;

import android.content.Context;
import android.opengl.GLES20;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.HashMap;

/**
 * Created by ivan on 2/26/18.
 */

public abstract class ShaderProgram {

	public static final String VERTEX_COORD_ATTRIBUTE = "a_vertex";
	public static final String NORMAL_COORD_ATTRIBUTE = "a_normal";
	public static final String TEXTURE_COORD_ATTRIBUTE = "a_tex_coord";
	public static final String VERTEX_COLOR_ATTRIBUTE = "a_color";


	public final String vertexShaderCode;
	public final String fragmentShaderCode;

	public final int vertexShader;
	public final int fragmentShader;

	public final int program;

	public ShaderProgram(Context context, int vertexShaderRes, int fragmentShaderRes){
		vertexShaderCode = readShaderCode(context, vertexShaderRes);
		fragmentShaderCode = readShaderCode(context, fragmentShaderRes);

		vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
		fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

		program = GLES20.glCreateProgram();
		GLES20.glAttachShader(program, vertexShader);
		GLES20.glAttachShader(program, fragmentShader);
		GLES20.glLinkProgram(program);
	}


	/**
	 * Using this shader program, draw a model stored in the Bundle
	 * @param params - parameters required for the shaders in this program to work
	 */
	public abstract void drawModel(Bundle params, HashMap<String, Buffer> vertexBuffers);

	public static String readShaderCode(Context context, int id){
		StringBuffer vs = new StringBuffer();
		InputStream inputStream = context.getResources().openRawResource(id);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

		try {
			String read = reader.readLine();
			while (read != null) {
				vs.append(read + "\n");
				read = reader.readLine();
			}
		} catch(IOException e){
			System.out.println("Could not read code for the shader: " + id);
			e.printStackTrace();
		}
		vs.deleteCharAt(vs.length() - 1); // remove the last \n
		return vs.toString();
	}

	private static int[] compiled = new int[1];
	public static int loadShader(int type, String shaderCode){

		// create a vertex shader type (GLES20.GL_VERTEX_SHADER)
		// or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
		int shader = GLES20.glCreateShader(type);

		// add the source code to the shader and compile it
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);


		GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] == 0) {
			GLES20.glDeleteShader(shader);
			throw new RuntimeException("Could not compile program:\n" + GLES20.glGetShaderInfoLog(shader) + " | " + shaderCode);
		}

		return shader;
	}
}
