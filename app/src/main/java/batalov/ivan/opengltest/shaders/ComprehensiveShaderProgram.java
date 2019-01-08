package batalov.ivan.opengltest.shaders;

import android.content.Context;
import android.opengl.GLES20;
import android.os.Bundle;

import java.nio.Buffer;
import java.util.HashMap;

import batalov.ivan.opengltest.MyGLRenderer;
import batalov.ivan.opengltest.MyUtilities;
import batalov.ivan.opengltest.light_parameters.DirectLightParameters;
import batalov.ivan.opengltest.light_parameters.PointDirLightParameters;
import batalov.ivan.opengltest.light_parameters.PointLightParameters;

/**
 * Created by ivan on 3/10/18.
 */

public class ComprehensiveShaderProgram extends ShaderProgram {

	public ComprehensiveShaderProgram(Context context, int vertexShaderRes, int fragmentShaderRes) {
		super(context, vertexShaderRes, fragmentShaderRes);
	}

	@Override
	public void drawModel(Bundle params, HashMap<String, Buffer> vertexBuffers) {
		float[] viewMatrix = params.getFloatArray("viewMatrix");
		float[] projectionMatrix = params.getFloatArray("projectionMatrix");
		float[] modelMatrix = params.getFloatArray("modelMatrix");
		float[] normalMatrix = params.getFloatArray("normalMatrix");
		float[] viewPosition = params.getFloatArray("viewPosition");
		int colorSource = params.getInt("colorSource");
		int texture = params.getInt("texture");

		GLES20.glUseProgram(program);
		int textureNumber = 0;

		int viewMatrixHandle = GLES20.glGetUniformLocation(program, "viewMatrix");
		GLES20.glUniformMatrix4fv(viewMatrixHandle, 1, false, viewMatrix, 0);

		int projectionMatrixHandle = GLES20.glGetUniformLocation(program, "projectionMatrix");
		GLES20.glUniformMatrix4fv(projectionMatrixHandle, 1, false, projectionMatrix, 0);

		int modelMatrixHandle = GLES20.glGetUniformLocation(program, "modelMatrix");
		GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, modelMatrix, 0);

		int normalMatrixHandle = GLES20.glGetUniformLocation(program, "normalMatrix");
		GLES20.glUniformMatrix4fv(normalMatrixHandle, 1, false, normalMatrix, 0);

		int viewPositionHandle = GLES20.glGetUniformLocation(program, "viewPosition");
		GLES20.glUniform3fv(viewPositionHandle, 1, viewPosition, 0);

		int colorSourceHandle = GLES20.glGetUniformLocation(program, "colorSource");
		GLES20.glUniform1i(colorSourceHandle, colorSource);
/*
		System.out.println("viewMatrixHandle: " + viewMatrixHandle + "; viewMatrix: ");
		MyGLRenderer.printMatrix(viewMatrix, 4, 4, "\t");
		System.out.println("projectionMatrixHandle: " + projectionMatrixHandle + "; projectionMatrix: ");
		MyGLRenderer.printMatrix(projectionMatrix, 4, 4, "\t");
		System.out.println("modelMatrixHandle: " + modelMatrixHandle + "; modelMatrix: ");
		MyGLRenderer.printMatrix(modelMatrix, 4, 4, "\t");
		System.out.println("normalMatrixHandle: " + normalMatrixHandle + "; normalMatrix: ");
		MyGLRenderer.printMatrix(normalMatrix, 4, 4, "\t");
		System.out.println("viewPositionHandle: " + viewPositionHandle + "; viewPosition: ");
		MyGLRenderer.printMatrix(viewPosition, 1, 3, "\t");
		System.out.println("colorSourceHandle: " + colorSourceHandle + "; colorSource: " + colorSource);
*/
		int vertexColorHandle = -1;
		if(colorSource == 2) { // get color from texture
			int textureHandle = GLES20.glGetUniformLocation(program, "texture");
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureNumber);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
			GLES20.glUniform1i(textureHandle, textureNumber);
			textureNumber++;
			//System.out.println("textureHandle: " + textureHandle + "; textureNumber: " + texture);
		} else if(colorSource == 1){ // get color from vertex array
			if(vertexBuffers.containsKey(ShaderProgram.VERTEX_COLOR_ATTRIBUTE)){
				vertexColorHandle = MyUtilities.linkBufferToAttribute(program, ShaderProgram.VERTEX_COLOR_ATTRIBUTE, vertexBuffers.get(ShaderProgram.VERTEX_COLOR_ATTRIBUTE), 4, GLES20.GL_FLOAT, 16); // 16 = 2 coords * 4 bytes
				//System.out.println("vertex color handle: " + vertexColorHandle);
			}
		} else if(colorSource == 3){ // get color from a uniform
			float[] color = params.getFloatArray("u_color");
			int u_colorHandle = GLES20.glGetUniformLocation(program, "u_color");
			GLES20.glUniform4fv(u_colorHandle, 1, color, 0);
		}

		// setting up directional light sources
		int i = 0;
		while(params.containsKey("dirLight_" + i)) {
			DirectLightParameters lightParams = (DirectLightParameters)params.get("dirLight_" + i);
			int lightDirectionHandle = GLES20.glGetUniformLocation(program, "dirLight[" + i + "].dir");
			GLES20.glUniform3fv(lightDirectionHandle, 1, lightParams.direction, 0);

			int lightColorHandle = GLES20.glGetUniformLocation(program, "dirLight[" + i + "].color");
			GLES20.glUniform4fv(lightColorHandle, 1, lightParams.color, 0);

			int ambientHandle = GLES20.glGetUniformLocation(program, "dirLight[" + i + "].ambient");
			GLES20.glUniform1f(ambientHandle, lightParams.ambient);

			int diffuseHandle = GLES20.glGetUniformLocation(program, "dirLight[" + i + "].diffuse");
			GLES20.glUniform1f(diffuseHandle, lightParams.diffuse);

			int specularHandle = GLES20.glGetUniformLocation(program, "dirLight[" + i + "].specular");
			GLES20.glUniform1f(specularHandle, lightParams.specular);

			int castShadowHandle = GLES20.glGetUniformLocation(program, "dirLight[" + i + "].castShadow");
			GLES20.glUniform1i(castShadowHandle, lightParams.castShadow);

			int biasHandle = GLES20.glGetUniformLocation(program, "dirLight[" + i + "].bias_on");
			GLES20.glUniform1i(biasHandle, 0);
/*
			System.out.println("direct light: ");
			System.out.println("dir handle: " + lightDirectionHandle + "; dir: ");
			MyGLRenderer.printMatrix(lightParams.direction, 1, 3, "\t");
			System.out.println("color handle: " + lightColorHandle + "; color: ");
			MyGLRenderer.printMatrix(lightParams.color, 1, 4, "\t");
			System.out.println("ambient handle: " + ambientHandle + "; ambient: " + lightParams.ambient);
			System.out.println("diffuse handle: " + diffuseHandle + "; ambient: " + lightParams.diffuse);
			System.out.println("specular handle: " + specularHandle + "; specular: " + lightParams.specular);
			System.out.println("cast shadow handle: " + castShadowHandle + "; cast shadow: " + lightParams.castShadow);
			System.out.println("bias handle: " + biasHandle);
*/
			if(lightParams.castShadow == 1){
				int lightSpaceMatrixHandle = GLES20.glGetUniformLocation(program, "dirLight[" + i + "].lightSpaceMatrix");
				GLES20.glUniformMatrix4fv(lightSpaceMatrixHandle, 1, false, lightParams.lightSpaceMatrix, 0);


				int shadowMapTextureHandle = GLES20.glGetUniformLocation(program, "depthMaps2D_Dir[" + i + "]");
				GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureNumber);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, lightParams.depthMap);
				GLES20.glUniform1i(shadowMapTextureHandle, textureNumber);
				textureNumber++;

				//System.out.println("light space matrix handle: " + lightSpaceMatrixHandle + "; light space matrix: ");
				//MyGLRenderer.printMatrix(lightParams.lightSpaceMatrix, 4, 4, "\t");
				//System.out.println("shadow map texture handle: " + shadowMapTextureHandle + "; shadow map texture: " + lightParams.depthMap);
			}
			i++;
		}
		// setting up point light sources
		i = 0;
		while(params.containsKey("pointLight_" + i)) {
			PointLightParameters lightParams = (PointLightParameters)params.get("pointLight_" + i);

			int lightPositionHandle = GLES20.glGetUniformLocation(program, "pointLight[" + i + "].position");
			GLES20.glUniform3fv(lightPositionHandle, 1, lightParams.position, 0);

			int lightColorHandle = GLES20.glGetUniformLocation(program, "pointLight[" + i + "].color");
			GLES20.glUniform4fv(lightColorHandle, 1, lightParams.color, 0);

			int ambientHandle = GLES20.glGetUniformLocation(program, "pointLight[" + i + "].ambient");
			GLES20.glUniform1f(ambientHandle, lightParams.ambient);

			int diffuseHandle = GLES20.glGetUniformLocation(program, "pointLight[" + i + "].diffuse");
			GLES20.glUniform1f(diffuseHandle, lightParams.diffuse);

			int specularHandle = GLES20.glGetUniformLocation(program, "pointLight[" + i + "].specular");
			GLES20.glUniform1f(specularHandle, lightParams.specular);

			int decayCoeffsHandle = GLES20.glGetUniformLocation(program, "pointLight[" + i + "].decayCoeffs");
			GLES20.glUniform3fv(decayCoeffsHandle, 1, lightParams.decayCoeffs, 0);

			int castShadowHandle = GLES20.glGetUniformLocation(program, "pointLight[" + i + "].castShadow");
			GLES20.glUniform1i(castShadowHandle, lightParams.castShadow);

			int biasHandle = GLES20.glGetUniformLocation(program, "pointLight[" + i + "].bias_on");
			GLES20.glUniform1i(biasHandle, 0);
/*
			System.out.println("point light: ");
			System.out.println("position handle: " + lightPositionHandle + "; position: ");
			MyGLRenderer.printMatrix(lightParams.position, 1, 3, "\t");
			System.out.println("color handle: " + lightColorHandle + "; color: ");
			MyGLRenderer.printMatrix(lightParams.color, 1, 4, "\t");
			System.out.println("ambient handle: " + ambientHandle + "; ambient: " + lightParams.ambient);
			System.out.println("diffuse handle: " + diffuseHandle + "; ambient: " + lightParams.diffuse);
			System.out.println("specular handle: " + specularHandle + "; specular: " + lightParams.specular);
			System.out.println("decayCoeffs handle: " + decayCoeffsHandle + "; decay coeffs: ");
			MyGLRenderer.printMatrix(lightParams.decayCoeffs, 3, 1, "\t");
			System.out.println("cast shadow handle: " + castShadowHandle + "; cast shadow: " + lightParams.castShadow);
			System.out.println("bias handle: " + biasHandle);
*/
			if(lightParams.castShadow == 1){
				int farPlaneHandle = GLES20.glGetUniformLocation(program, "pointLight[" + i + "].far_plane");
				GLES20.glUniform1f(farPlaneHandle, lightParams.farPlane);

				int shadowMapTextureHandle = GLES20.glGetUniformLocation(program, "depthCubeMaps[" + i + "]");
				GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureNumber);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, lightParams.depthMap);
				GLES20.glUniform1i(shadowMapTextureHandle, textureNumber);
				textureNumber++;

				//System.out.println("far plane handle: " + farPlaneHandle + "; far plane: " + lightParams.farPlane);
				//System.out.println("shadow map texture handle: " + shadowMapTextureHandle + "; shadow map texture: " + lightParams.depthMap);
			}
			i++;
		}

		// setting up point light sources
		i = 0;
		while(params.containsKey("pointDirLight_" + i)) {
			PointDirLightParameters lightParams = (PointDirLightParameters)params.get("pointDirLight_" + i);

			int lightPositionHandle = GLES20.glGetUniformLocation(program, "pointDirLight[" + i + "].position");
			GLES20.glUniform3fv(lightPositionHandle, 1, lightParams.position, 0);

			int lightColorHandle = GLES20.glGetUniformLocation(program, "pointDirLight[" + i + "].color");
			GLES20.glUniform4fv(lightColorHandle, 1, lightParams.color, 0);

			int ambientHandle = GLES20.glGetUniformLocation(program, "pointDirLight[" + i + "].ambient");
			GLES20.glUniform1f(ambientHandle, lightParams.ambient);

			int diffuseHandle = GLES20.glGetUniformLocation(program, "pointDirLight[" + i + "].diffuse");
			GLES20.glUniform1f(diffuseHandle, lightParams.diffuse);

			int specularHandle = GLES20.glGetUniformLocation(program, "pointDirLight[" + i + "].specular");
			GLES20.glUniform1f(specularHandle, lightParams.specular);

			int decayCoeffsHandle = GLES20.glGetUniformLocation(program, "pointDirLight[" + i + "].decayCoeffs");
			GLES20.glUniform3fv(decayCoeffsHandle, 1, lightParams.decayCoeffs, 0);

			int castShadowHandle = GLES20.glGetUniformLocation(program, "pointDirLight[" + i + "].castShadow");
			GLES20.glUniform1i(castShadowHandle, lightParams.castShadow);

			int biasHandle = GLES20.glGetUniformLocation(program, "pointDirLight[" + i + "].bias_on");
			GLES20.glUniform1i(biasHandle, 0);
/*
			System.out.println("point dir light: ");
			System.out.println("position handle: " + lightPositionHandle + "; position: ");
			MyGLRenderer.printMatrix(lightParams.position, 1, 3, "\t");
			System.out.println("color handle: " + lightColorHandle + "; color: ");
			MyGLRenderer.printMatrix(lightParams.color, 1, 4, "\t");
			System.out.println("ambient handle: " + ambientHandle + "; ambient: " + lightParams.ambient);
			System.out.println("diffuse handle: " + diffuseHandle + "; ambient: " + lightParams.diffuse);
			System.out.println("specular handle: " + specularHandle + "; specular: " + lightParams.specular);
			System.out.println("decayCoeffs handle: " + decayCoeffsHandle + "; decay coeffs: ");
			MyGLRenderer.printMatrix(lightParams.decayCoeffs, 1, 3, "\t");
			System.out.println("cast shadow handle: " + castShadowHandle + "; cast shadow: " + lightParams.castShadow);
			System.out.println("bias handle: " + biasHandle);
*/
			if(lightParams.castShadow == 1){
				int lightSpaceMatrixHandle = GLES20.glGetUniformLocation(program, "pointDirLight[" + i + "].lightSpaceMatrix");
				GLES20.glUniformMatrix4fv(lightSpaceMatrixHandle, 1, false, lightParams.lightSpaceMatrix, 0);

				int shadowMapTextureHandle = GLES20.glGetUniformLocation(program, "depthMaps2D_Point[" + i + "]");
				GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureNumber);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, lightParams.depthMap);
				GLES20.glUniform1i(shadowMapTextureHandle, textureNumber);
				textureNumber++;

				//System.out.println("light space matrix handle: " + lightSpaceMatrixHandle + "; light space matrix: ");
				//MyGLRenderer.printMatrix(lightParams.lightSpaceMatrix, 4, 4, "\t");
				//System.out.println("shadow map texture handle: " + shadowMapTextureHandle + "; shadow map texture: " + lightParams.depthMap);
			}
			i++;
		}

		int vertexCoordHandle = MyUtilities.linkBufferToAttribute(program, ShaderProgram.VERTEX_COORD_ATTRIBUTE, vertexBuffers.get(ShaderProgram.VERTEX_COORD_ATTRIBUTE), 3, GLES20.GL_FLOAT, 12); // 12 = 3 coords * 4 bytes
		//System.out.println("vertex coord handle: " + vertexCoordHandle);

		int normalHandle = -1;
		if(vertexBuffers.containsKey(ShaderProgram.NORMAL_COORD_ATTRIBUTE)) {
			normalHandle = MyUtilities.linkBufferToAttribute(program, ShaderProgram.NORMAL_COORD_ATTRIBUTE, vertexBuffers.get(ShaderProgram.NORMAL_COORD_ATTRIBUTE), 3, GLES20.GL_FLOAT, 12); // 12 = 3 coords * 4 bytes
			//System.out.println("normal handle: " + normalHandle);
		}

		int vertexTextureHandle = -1;
		if(vertexBuffers.containsKey(ShaderProgram.TEXTURE_COORD_ATTRIBUTE)) {
			vertexTextureHandle = MyUtilities.linkBufferToAttribute(program, ShaderProgram.TEXTURE_COORD_ATTRIBUTE, vertexBuffers.get(ShaderProgram.TEXTURE_COORD_ATTRIBUTE), 2, GLES20.GL_FLOAT, 8); // 8 = 2 coords * 4 bytes
			//System.out.println("texture coord handle: " + vertexTextureHandle);
		}

		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexBuffers.get(ShaderProgram.VERTEX_COORD_ATTRIBUTE).capacity()/3);

		GLES20.glDisableVertexAttribArray(vertexCoordHandle);
		if(normalHandle >= 0) {
			GLES20.glDisableVertexAttribArray(normalHandle);
		}

		if(vertexTextureHandle >= 0) {
			GLES20.glDisableVertexAttribArray(vertexTextureHandle);
		}

		if(vertexColorHandle >= 0){
			GLES20.glDisableVertexAttribArray(vertexColorHandle);
		}
	}
}
