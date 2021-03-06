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

public class PointShadowShaderProgram extends ShaderProgram {

	public PointShadowShaderProgram(Context context, int vertexShaderRes, int fragmentShaderRes) {
		super(context, vertexShaderRes, fragmentShaderRes);
	}

	@Override
	public void drawModel(Bundle params, HashMap<String, Buffer> vertexBuffers){
		float[] viewMatrix = params.getFloatArray("viewMatrix");
		float[] projectionMatrix = params.getFloatArray("projectionMatrix");
		float[] modelMatrix = params.getFloatArray("modelMatrix");
		int depthMap = params.getInt("depthMap");
		float[] lightPosition = params.getFloatArray("lightPosition");
		float[] viewPosition = params.getFloatArray("viewPosition");
		float farPlane = params.getFloat("farPlane");

		GLES20.glUseProgram(program);
		int farPlaneHandle = GLES20.glGetUniformLocation(program, "far_plane");
		GLES20.glUniform1f(farPlaneHandle, farPlane);

		int vertexCoordHandle = MyUtilities.linkBufferToAttribute(program, ShaderProgram.VERTEX_COORD_ATTRIBUTE, vertexBuffers.get(ShaderProgram.VERTEX_COORD_ATTRIBUTE), 3, GLES20.GL_FLOAT, 12); // 12 = 3 coords * 4 bytes
		int normalHandle = -1;
		if(vertexBuffers.containsKey(ShaderProgram.NORMAL_COORD_ATTRIBUTE)) {
			normalHandle = MyUtilities.linkBufferToAttribute(program, ShaderProgram.NORMAL_COORD_ATTRIBUTE, vertexBuffers.get(ShaderProgram.NORMAL_COORD_ATTRIBUTE), 3, GLES20.GL_FLOAT, 12); // 12 = 3 coords * 4 bytes
		}
/*
                int vertexTextureHandle = -1;
                if(vertexBuffers.containsKey(ShaderProgram.TEXTURE_COORD_ATTRIBUTE)) {
                    vertexTextureHandle = MyUtilities.linkBufferToAttribute(program, ShaderProgram.TEXTURE_COORD_ATTRIBUTE, vertexBuffers.get(ShaderProgram.TEXTURE_COORD_ATTRIBUTE), 4, GLES20.GL_FLOAT, 8); // 8 = 2 coords * 4 bytes
                }
*/
		int vertexColorHandle = -1;
		if(vertexBuffers.containsKey(ShaderProgram.VERTEX_COLOR_ATTRIBUTE)){
			vertexColorHandle = MyUtilities.linkBufferToAttribute(program, ShaderProgram.VERTEX_COLOR_ATTRIBUTE, vertexBuffers.get(ShaderProgram.VERTEX_COLOR_ATTRIBUTE), 4, GLES20.GL_FLOAT, 16); // 16 = 2 coords * 4 bytes
		}

		int lightPositionHandle = GLES20.glGetUniformLocation(program, "lightPosition");
		GLES20.glUniform3fv(lightPositionHandle, 1, lightPosition, 0);

		int viewPositionHandle = GLES20.glGetUniformLocation(program, "viewPosition");
		GLES20.glUniform3fv(viewPositionHandle, 1, viewPosition, 0);

		int viewMatrixHandle = GLES20.glGetUniformLocation(program, "viewMatrix");
		GLES20.glUniformMatrix4fv(viewMatrixHandle, 1, false, viewMatrix, 0);

		int projectionMatrixHandle = GLES20.glGetUniformLocation(program, "projectionMatrix");
		GLES20.glUniformMatrix4fv(projectionMatrixHandle, 1, false, projectionMatrix, 0);

		int modelMatrixHandle = GLES20.glGetUniformLocation(program, "modelMatrix");
		GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, modelMatrix, 0);

		int shadowMapTextureHandle = GLES20.glGetUniformLocation(program, "depthMap");
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, depthMap);
		GLES20.glUniform1i(shadowMapTextureHandle, 0);

		//System.out.println("light position: " + lightPositionHandle + "; viewPosition: " + viewPositionHandle + "; viewMatrix: " + viewMatrixHandle + "; projectionMatrix: " + projectionMatrixHandle + "; modelMatrix: " + modelMatrixHandle);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexBuffers.get(ShaderProgram.VERTEX_COORD_ATTRIBUTE).capacity()/3);

		GLES20.glDisableVertexAttribArray(vertexCoordHandle);
		if(normalHandle >= 0) {
			GLES20.glDisableVertexAttribArray(normalHandle);
		}
	            /*
                if(vertexTextureHandle >= 0) {
                    GLES20.glDisableVertexAttribArray(vertexTextureHandle);
                }
                */
		if(vertexColorHandle >= 0){
			GLES20.glDisableVertexAttribArray(vertexColorHandle);
		}
	}
}
