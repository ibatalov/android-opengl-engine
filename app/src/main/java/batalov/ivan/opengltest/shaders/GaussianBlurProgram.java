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

public class GaussianBlurProgram extends ShaderProgram {
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

	public GaussianBlurProgram(Context context, int vertexShaderRes, int fragmentShaderRes) {
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

	float[] weights = new float[]{0.197641f, 0.174868f, 0.121117f, 0.065666f, 0.027867f, 0.009255f, 0.002406f};

	@Override
	public void drawModel(Bundle params, HashMap<String, Buffer> vertexBuffers) {
		int texture = params.getInt("texture");
		int orientation = params.getInt("orientation");
		float[] textureSize = params.getFloatArray("textureSize");

		GLES20.glUseProgram(program);
		int vertexCoordHandle = MyUtilities.linkBufferToAttribute(program, ShaderProgram.VERTEX_COORD_ATTRIBUTE, vertexBuffer, 3, GLES20.GL_FLOAT, 12);
		int textureCoordHandle = MyUtilities.linkBufferToAttribute(program, ShaderProgram.TEXTURE_COORD_ATTRIBUTE, texCoordBuffer, 2, GLES20.GL_FLOAT, 8);

		int textureHandle = GLES20.glGetUniformLocation(program, "texture");
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
		GLES20.glUniform1i(textureHandle, 0);

		int orientationHandle = GLES20.glGetUniformLocation(program, "orientation");
		GLES20.glUniform1i(orientationHandle, orientation);

		int textureSizeHandle = GLES20.glGetUniformLocation(program, "textureSize");
		GLES20.glUniform2fv(textureSizeHandle, 1, textureSize, 0);

		int weightHandle = GLES20.glGetUniformLocation(program, "weight");
		//GLES20.glUniform1fv(weightHandle, 5,  new float[] {0.227027f, 0.1945946f, 0.1216216f, 0.054054f, 0.016216f}, 0);
		GLES20.glUniform1fv(weightHandle, weights.length,  weights, 0);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.length/3);

		GLES20.glDisableVertexAttribArray(vertexCoordHandle);
		GLES20.glDisableVertexAttribArray(textureCoordHandle);
	}
}
