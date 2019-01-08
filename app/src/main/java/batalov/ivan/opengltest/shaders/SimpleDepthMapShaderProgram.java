package batalov.ivan.opengltest.shaders;

import android.content.Context;
import android.opengl.GLES20;
import android.os.Bundle;

import java.nio.Buffer;
import java.util.HashMap;

import batalov.ivan.opengltest.MyUtilities;

/**
 * Created by ivan on 3/10/18.
 */

public class SimpleDepthMapShaderProgram extends ShaderProgram {

	public SimpleDepthMapShaderProgram(Context context, int vertexShaderRes, int fragmentShaderRes) {
		super(context, vertexShaderRes, fragmentShaderRes);
	}

	@Override
	public void drawModel(Bundle params, HashMap<String, Buffer> vertexBuffers){
		GLES20.glUseProgram(program);

		float[] lightSpaceMatrix = params.getFloatArray("lightSpaceMatrix");
		float[] modelMatrix = params.getFloatArray("modelMatrix");

		int vertexCoordHandle = MyUtilities.linkBufferToAttribute(program, ShaderProgram.VERTEX_COORD_ATTRIBUTE, vertexBuffers.get(ShaderProgram.VERTEX_COORD_ATTRIBUTE), 3, GLES20.GL_FLOAT, 12); // 12 = 3 coords * 4 bytes

		int lightSpaceMatrixHandle = GLES20.glGetUniformLocation(program, "lightSpaceMatrix");
		GLES20.glUniformMatrix4fv(lightSpaceMatrixHandle, 1, false, lightSpaceMatrix, 0);

		int modelMatrixHandle = GLES20.glGetUniformLocation(program, "modelMatrix");
		GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, modelMatrix, 0);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexBuffers.get(ShaderProgram.VERTEX_COORD_ATTRIBUTE).capacity()/3);
		//GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

		GLES20.glDisableVertexAttribArray(vertexCoordHandle);
	}
}
