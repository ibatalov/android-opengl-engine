package batalov.ivan.opengltest.shaders;

import android.content.Context;
import android.opengl.GLES20;
import android.os.Bundle;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;

import batalov.ivan.opengltest.MyUtilities;

/**
 * Created by ivan on 3/21/18.
 */

public class OverlayTexturesProgram extends ShaderProgram {
	private float[] vertices = new float[]{
			-1f, 1f, 0f,
			1f, 1f, 0f,
			-1f, -1f, 0f,
			1f, 1f, 0f,
			1f, -1f, 0f,
			-1f, -1f, 0f,
	};

	private float[] texCoords = new float[]{
			0f, 1f,
			1f, 1f,
			0f, 0f,
			1f, 1f,
			1f, 0f,
			0f, 0f,
	};

	private FloatBuffer vertexBuffer;
	private FloatBuffer texCoordBuffer;

	public OverlayTexturesProgram(Context context, int vertexShaderRes, int fragmentShaderRes) {
		super(context, vertexShaderRes, fragmentShaderRes);
		ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4); // (number of coordinate values * 4 bytes per float)
		bb.order(ByteOrder.nativeOrder());
		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);

		ByteBuffer bb2 = ByteBuffer.allocateDirect(texCoords.length * 4); // (number of coordinate values * 4 bytes per float)
		bb2.order(ByteOrder.nativeOrder());
		texCoordBuffer = bb2.asFloatBuffer();
		texCoordBuffer.put(texCoords);
		texCoordBuffer.position(0);

	}

	@Override
	public void drawModel(Bundle params, HashMap<String, Buffer> vertexBuffers) {
		int[] texture = params.getIntArray("texture");
		float[] coeffs = params.getFloatArray("coeffs");

		GLES20.glUseProgram(program);
		int vertexCoordHandle = MyUtilities.linkBufferToAttribute(program, ShaderProgram.VERTEX_COORD_ATTRIBUTE, vertexBuffer, 3, GLES20.GL_FLOAT, 12);
		int textureCoordHandle = MyUtilities.linkBufferToAttribute(program, ShaderProgram.TEXTURE_COORD_ATTRIBUTE, texCoordBuffer, 2, GLES20.GL_FLOAT, 8);

		int coeffsHandle = GLES20.glGetUniformLocation(program, "coeffs");
		GLES20.glUniform1fv(coeffsHandle, coeffs.length, coeffs, 0);

		int textureNumber = 0;
		for(int i = 0; i < texture.length; i++){
			int textureHandle = GLES20.glGetUniformLocation(program, "texture[" + i + "]");
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureNumber);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[i]);
			GLES20.glUniform1i(textureHandle, textureNumber);
			textureNumber++;
		}
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.length/3);

		GLES20.glDisableVertexAttribArray(vertexCoordHandle);
		GLES20.glDisableVertexAttribArray(textureCoordHandle);
	}
}
