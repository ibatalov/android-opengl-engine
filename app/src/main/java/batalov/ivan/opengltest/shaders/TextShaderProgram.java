package batalov.ivan.opengltest.shaders;

import android.content.Context;
import android.opengl.GLES20;
import android.os.Bundle;

import java.nio.Buffer;
import java.util.HashMap;

import batalov.ivan.opengltest.models.SquareHall;

/**
 * Created by ivan on 3/10/18.
 */

public class TextShaderProgram extends ShaderProgram {

	public TextShaderProgram(Context context, int vertexShaderRes, int fragmentShaderRes) {
		super(context, vertexShaderRes, fragmentShaderRes);
	}

	@Override
	public void drawModel(Bundle params, HashMap<String, Buffer> vertexBuffers){
		float[] textColorVector = params.getFloatArray("textColor");
		float[] backgroundColorVector = params.getFloatArray("backgroundColor");
		int fontMapTexture = params.getInt("fontMap");

		GLES20.glUseProgram(program);

		int vertexCoordHandle = SquareHall.linkBufferToAttribute(program, ShaderProgram.VERTEX_COORD_ATTRIBUTE, vertexBuffers.get(ShaderProgram.VERTEX_COORD_ATTRIBUTE), 3, GLES20.GL_FLOAT, 12); // 12 = 3 coords * 4 bytes
		int textureCoordHandle = SquareHall.linkBufferToAttribute(program, ShaderProgram.TEXTURE_COORD_ATTRIBUTE, vertexBuffers.get(ShaderProgram.TEXTURE_COORD_ATTRIBUTE), 2, GLES20.GL_FLOAT, 8); // 8 = 2 coords * 4 bytes

		int textColorHandle = GLES20.glGetUniformLocation(program, "textColor");
		GLES20.glUniform4fv(textColorHandle, 1, textColorVector, 0);

		int backgroundColorHandle = GLES20.glGetUniformLocation(program, "backgroundColor");
		GLES20.glUniform4fv(backgroundColorHandle, 1, backgroundColorVector, 0);

		//System.out.println("text color handle: " + textColorHandle + "; background color handle: " + backgroundColorHandle);

		int fontMapTextureHandle = GLES20.glGetUniformLocation(program, "fontMap");
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fontMapTexture);
		GLES20.glUniform1i(fontMapTextureHandle, 0);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexBuffers.get(ShaderProgram.VERTEX_COORD_ATTRIBUTE).capacity() / 3);

		//System.out.println("vertex coord handle: " + vertexCoordHandle + "; tex coord handle: " + textureCoordHandle + "; font map texture handle: " +  fontMapTextureHandle);

		GLES20.glDisableVertexAttribArray(vertexCoordHandle);
		GLES20.glDisableVertexAttribArray(textureCoordHandle);
	}
}
