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

public class DiplayPointDepthMapShaderProgram extends ShaderProgram {

	public DiplayPointDepthMapShaderProgram(Context context, int vertexShaderRes, int fragmentShaderRes) {
		super(context, vertexShaderRes, fragmentShaderRes);
	}

	@Override
	public void drawModel(Bundle params, HashMap<String, Buffer> vertexBuffers){

		float[] viewMatrix = params.getFloatArray("viewMatrix");
		float[] projectionMatrix = params.getFloatArray("projectionMatrix");
		float[] modelMatrix = params.getFloatArray("modelMatrix");
		int depthMap = params.getInt("depthMap");

		GLES20.glUseProgram(program);

		int vertexCoordHandle = MyUtilities.linkBufferToAttribute(program, ShaderProgram.VERTEX_COORD_ATTRIBUTE, vertexBuffers.get(ShaderProgram.VERTEX_COORD_ATTRIBUTE), 3, GLES20.GL_FLOAT, 12); // 12 = 3 coords * 4 bytes

		int viewMatrixHandle = GLES20.glGetUniformLocation(program, "viewMatrix");
		GLES20.glUniformMatrix4fv(viewMatrixHandle, 1, false, viewMatrix, 0);

		int projectionMatrixHandle = GLES20.glGetUniformLocation(program, "projectionMatrix");
		GLES20.glUniformMatrix4fv(projectionMatrixHandle, 1, false, projectionMatrix, 0);

		int modelMatrixHandle = GLES20.glGetUniformLocation(program, "modelMatrix");
		GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, modelMatrix, 0);

		int pointShadowMapTextureHandle = GLES20.glGetUniformLocation(program, "depthMap");
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, depthMap);
		GLES20.glUniform1i(pointShadowMapTextureHandle, 0);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexBuffers.get(ShaderProgram.VERTEX_COORD_ATTRIBUTE).capacity()/3);

		GLES20.glDisableVertexAttribArray(vertexCoordHandle);
	}
}
