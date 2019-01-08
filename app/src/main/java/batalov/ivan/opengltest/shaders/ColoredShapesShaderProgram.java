package batalov.ivan.opengltest.shaders;

import android.content.Context;
import android.opengl.GLES20;
import android.os.Bundle;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.HashMap;

import batalov.ivan.opengltest.MyUtilities;

/**
 * Created by ivan on 3/21/18.
 */

public class ColoredShapesShaderProgram extends ShaderProgram {
	public ColoredShapesShaderProgram(Context context, int vertexShaderRes, int fragmentShaderRes) {
		super(context, vertexShaderRes, fragmentShaderRes);
	}

	@Override
	public void drawModel(Bundle params, HashMap<String, Buffer> vertexBuffers) {
		float[] color = params.getFloatArray("color");
		float[] projectionMatrix = params.getFloatArray("projectionMatrix");
		float[] modelMatrix = params.getFloatArray("modelMatrix");

		GLES20.glUseProgram(program);

		int colorHandle = GLES20.glGetUniformLocation(program, "color");
		GLES20.glUniform4fv(colorHandle, 1, color, 0);

		int projectionMatrixHandle = GLES20.glGetUniformLocation(program, "projectionMatrix");
		GLES20.glUniformMatrix4fv(projectionMatrixHandle, 1, false, projectionMatrix, 0);

		int modelMatrixHandle = GLES20.glGetUniformLocation(program, "modelMatrix");
		GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, modelMatrix, 0);

		int vertexCoordHandle = MyUtilities.linkBufferToAttribute(program, ShaderProgram.VERTEX_COORD_ATTRIBUTE, vertexBuffers.get(ShaderProgram.VERTEX_COORD_ATTRIBUTE), 3, GLES20.GL_FLOAT, 12);

		Buffer colorBuffer = vertexBuffers.get(ShaderProgram.VERTEX_COLOR_ATTRIBUTE);
		int colorCoordHandle = -1;
		if(colorBuffer != null){
			colorCoordHandle = MyUtilities.linkBufferToAttribute(program, ShaderProgram.VERTEX_COLOR_ATTRIBUTE, colorBuffer, 4, GLES20.GL_FLOAT, 16);
		}

		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexBuffers.get(ShaderProgram.VERTEX_COORD_ATTRIBUTE).capacity()/3);

		GLES20.glDisableVertexAttribArray(vertexCoordHandle);
		if(colorCoordHandle >= 0){
			GLES20.glDisableVertexAttribArray(colorCoordHandle);
		}
	}
}
