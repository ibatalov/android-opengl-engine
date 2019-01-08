package batalov.ivan.opengltest.shaders;

import android.content.Context;
import android.opengl.GLES20;
import android.os.Bundle;

import java.nio.Buffer;
import java.util.HashMap;

import batalov.ivan.opengltest.MyUtilities;

/**
 * Created by ivan on 4/27/18.
 */

public class DisplayTextureShaderProgram extends ShaderProgram {

	public DisplayTextureShaderProgram(Context context, int vertexShaderRes, int fragmentShaderRes) {
		super(context, vertexShaderRes, fragmentShaderRes);
	}

	@Override
	public void drawModel(Bundle params, HashMap<String, Buffer> vertexBuffers) {
		GLES20.glUseProgram(program);

		int vertexCoordHandle = MyUtilities.linkBufferToAttribute(program, ShaderProgram.VERTEX_COORD_ATTRIBUTE, vertexBuffers.get(ShaderProgram.VERTEX_COORD_ATTRIBUTE), 3, GLES20.GL_FLOAT, 12); // 12 = 3 coords * 4 bytes
		int textureCoordHandle = MyUtilities.linkBufferToAttribute(program, ShaderProgram.TEXTURE_COORD_ATTRIBUTE, vertexBuffers.get(ShaderProgram.TEXTURE_COORD_ATTRIBUTE), 2, GLES20.GL_FLOAT, 8); // 8 = 2 coords * 4 bytes

		int textureHandle = GLES20.glGetUniformLocation(program, "texture");
		int texture = params.getInt("texture");

		float[] projectionMatrix = params.getFloatArray("projectionMatrix");
		int projectionMatrixHandle = GLES20.glGetUniformLocation(program, "projectionMatrix");
		GLES20.glUniformMatrix4fv(projectionMatrixHandle, 1, false, projectionMatrix, 0);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
		GLES20.glUniform1i(textureHandle, 0);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexBuffers.get(ShaderProgram.VERTEX_COORD_ATTRIBUTE).capacity()/3);

		GLES20.glDisableVertexAttribArray(vertexCoordHandle);
		GLES20.glDisableVertexAttribArray(textureCoordHandle);
	}
}
