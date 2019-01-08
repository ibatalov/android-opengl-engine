package batalov.ivan.opengltest.models;

import android.opengl.Matrix;
import android.os.Bundle;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;

import batalov.ivan.opengltest.shaders.ShaderProgram;

/**
 * Created by ivan on 3/21/18.
 */

public class Arrow {

	private float[] lineVertices = new float[]{
			-1, -1, 0,
			-1, 1, 0,
			1, 1, 0,
			1, -1, 0
	};
	private float[] lineVertexArray = new float[]{
			-1, -1, 0,
			1, -1, 0,
			-1, 1, 0,
			-1, 1, 0,
			1, -1, 0,
			1, 1, 0
	};

	private float[] arrowHeadVertices = new float[]{
			-1, -1, 0,
			-1, 1, 0,
			1, 1, 0,
			1, -1, 0
	};
	private float[] arrowHeadVertexArray = new float[]{
			-1, -1, 0,
			1, -1, 0,
			-1, 1, 0,
			-1, 1, 0,
			1, -1, 0,
			1, 1, 0
	};

	public float[] vertexArray;
	public FloatBuffer vertexBuffer;
	public HashMap<String, Buffer> vertexBuffers = new HashMap(1);
	private float[] color = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
	public float positionX;
	public float positionY;
	public float[] modelMatrix = new float[16];
	public float[] projectionMatrix = new float[16];

	private float[] arrowHeadDimensions;
	private float lineWidth;
	private float[] ringRadii;
	private float ringThickness;
	private float[] ringColors;

	public boolean showArrow;
	public boolean ready;

	public Arrow(Bundle params){
		ringRadii = params.getFloatArray("ringRadii");
		ringThickness = params.getFloat("ringThickness");
		lineWidth = params.getFloat("lineThickness");
		arrowHeadDimensions = params.getFloatArray("arrowHeadDimensions");
		ringColors = params.getFloatArray("ringColors");

		int count = 100;

		generateRingCoords(count, ringRadii, ringThickness, 0, 0);

		for(int i = 0; i < lineVertexArray.length/3; i++){
			lineVertexArray[3*i + 1] *= lineWidth/2;
		}

		for(int i = 0; i < arrowHeadVertexArray.length/3; i++){
			arrowHeadVertexArray[3*i + 1] *= arrowHeadDimensions[0]/2;
			arrowHeadVertexArray[3*i] *= arrowHeadDimensions[1]/2;
		}

		vertexArray = new float[ringVertexArray.length + lineVertexArray.length + arrowHeadVertexArray.length];
		System.arraycopy(ringVertexArray, 0, vertexArray, 0, ringVertexArray.length);
		System.arraycopy(lineVertexArray, 0, vertexArray, ringVertexArray.length, lineVertexArray.length);
		System.arraycopy(arrowHeadVertexArray, 0, vertexArray, ringVertexArray.length + lineVertexArray.length, arrowHeadVertexArray.length);

	}

	private float[] ringVertices;
	private float[] ringVertexArray;
	private void generateRingCoords(int count, float[] radii, float thickness, float offsetX, float offsetY){
		ringVertices = new float[2*count*3];
		ringVertexArray = new float[2*count*9*radii.length];

		int indexOffset = 0;
		for(int r = 0; r < radii.length; r++) {
			ringVertices[0] = radii[r] - thickness + offsetX;
			ringVertices[1] = offsetY;
			ringVertices[2] = 0;

			double angle = Math.PI * 2 / (2 * count);
			ringVertices[3] = radii[r] * (float) Math.cos(angle) + offsetX;
			ringVertices[4] = radii[r] * (float) Math.sin(angle) + offsetY;
			ringVertices[5] = 0;

			boolean outerSide = false;
			for (int i = 0; i < 2 * count - 2; i++) {
				angle = Math.PI * 2 * (i + 2) / (2 * count);
				float x = (radii[r] - (outerSide ? 0 : thickness)) * (float) Math.cos(angle) + offsetX;
				float y = (radii[r] - (outerSide ? 0 : thickness)) * (float) Math.sin(angle) + offsetY;

				ringVertices[3 * (i + 2)] = x;
				ringVertices[3 * (i + 2) + 1] = y;
				ringVertices[3 * (i + 2) + 2] = 0;


				System.arraycopy(ringVertices, 3 * i, ringVertexArray, 9 * i + indexOffset, 3);
				if (outerSide) {
					// 1 -> 3 -> 2
					System.arraycopy(ringVertices, 3 * (i + 2), ringVertexArray, 9 * i + 3 + indexOffset, 3);
					System.arraycopy(ringVertices, 3 * (i + 1), ringVertexArray, 9 * i + 6 + indexOffset, 3);
				} else {
					// 1 -> 2 -> 3
					System.arraycopy(ringVertices, 3 * (i + 1), ringVertexArray, 9 * i + 3 + indexOffset, 3);
					System.arraycopy(ringVertices, 3 * (i + 2), ringVertexArray, 9 * i + 6 + indexOffset, 3);
				}
				outerSide = !outerSide;
			}
			// 2nd last triangle
			System.arraycopy(ringVertices, 3 * (2 * count - 2), ringVertexArray, 9 * (2 * count - 2) + indexOffset, 3);
			System.arraycopy(ringVertices, 3 * (2 * count - 1), ringVertexArray, 9 * (2 * count - 2) + 3 + indexOffset, 3);
			System.arraycopy(ringVertices, 0, ringVertexArray, 9 * (2 * count - 2) + 6 + indexOffset, 3);

			// last triangle
			System.arraycopy(ringVertices, 3, ringVertexArray, 9 * (2 * count - 1) + indexOffset, 3);
			System.arraycopy(ringVertices, 0, ringVertexArray, 9 * (2 * count - 1) + 3 + indexOffset, 3);
			System.arraycopy(ringVertices, 3 * (2 * count - 1), ringVertexArray, 9 * (2 * count - 1) + 6 + indexOffset, 3);
			indexOffset += 2*count*9;
		}
	}

	float[] transformedArray;
	float[] shortTransformedArray;

	float[] vertexColorArray;
	float[] shortVertexColorArray;

	FloatBuffer colorBuffer;
	FloatBuffer shortColorBuffer;

	public void setLength(float length){

		if(vertexBuffers == null){
			vertexBuffers = new HashMap<>();
		}

		float[] m = new float[16];
		float[] array;

		if(length > ringRadii[0]) {
			if(transformedArray == null){
				transformedArray = new float[vertexArray.length];
				vertexColorArray = new float[vertexArray.length*4/3];
				fillColorArray();
				System.arraycopy(vertexArray, 0, transformedArray, 0, vertexArray.length);
			} else{
				// copy only the moving parts of the shape
				System.arraycopy(vertexArray, ringVertexArray.length, transformedArray, ringVertexArray.length, vertexArray.length - ringVertexArray.length);
			}
			vertexBuffers.put(ShaderProgram.VERTEX_COLOR_ATTRIBUTE, colorBuffer);

			Matrix.setIdentityM(m, 0);
			Matrix.translateM(m, 0, (length + ringRadii[0] - arrowHeadDimensions[0]/2)/2, 0, 0);
			Matrix.scaleM(m, 0, (length - ringRadii[0] - arrowHeadDimensions[0]/2)/2, 1, 1);
			transformCoordinates(transformedArray, ringVertexArray.length, lineVertexArray.length/3, 3, m);

			Matrix.setIdentityM(m, 0);
			Matrix.translateM(m, 0, length, 0, 0);
			transformCoordinates(transformedArray, ringVertexArray.length + lineVertexArray.length, arrowHeadVertexArray.length/3, 3, m);
			array = transformedArray;
		} else{
			if(shortTransformedArray == null){
				shortTransformedArray = new float[ringVertexArray.length + arrowHeadVertexArray.length];
				shortVertexColorArray = new float[(ringVertexArray.length + arrowHeadVertexArray.length)*4/3];
				fillShortColorArray();
				System.arraycopy(ringVertexArray, 0, shortTransformedArray, 0, ringVertexArray.length);
			}
			System.arraycopy(arrowHeadVertexArray, 0, shortTransformedArray, ringVertexArray.length, arrowHeadVertexArray.length);

			vertexBuffers.put(ShaderProgram.VERTEX_COLOR_ATTRIBUTE, shortColorBuffer);

			Matrix.setIdentityM(m, 0);
			Matrix.translateM(m, 0, length, 0, 0);
			transformCoordinates(shortTransformedArray, ringVertexArray.length, arrowHeadVertexArray.length/3, 3, m);
			array = shortTransformedArray;
		}

		ByteBuffer bb = ByteBuffer.allocateDirect(array.length*4);
		bb.order(ByteOrder.nativeOrder());
		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(array);
		vertexBuffer.position(0);

		vertexBuffers.put(ShaderProgram.VERTEX_COORD_ATTRIBUTE, vertexBuffer);
	}

	float[] tempVec = new float[]{1, 1, 1, 1};
	float[] newTempVec = new float[]{1, 1, 1, 1};
	private void transformCoordinates(float[] coords, int offset, int vecCount, int vecLength, float[] matrix){
		for(int i = 0; i < vecCount; i++){
			System.arraycopy(coords, offset + i*vecLength, tempVec, 0, vecLength);
			Matrix.multiplyMV(newTempVec, 0, matrix, 0, tempVec, 0);
			System.arraycopy(newTempVec, 0, coords, offset + i*vecLength, vecLength);
		}
	}

	private void fillColorArray(){
		// fill ring colors
		int ringVertexCount = ringVertexArray.length/3/ringRadii.length;
		for(int ringNum = 0; ringNum < ringRadii.length; ringNum++){
			for(int i = 0; i < ringVertexCount; i++){
				System.arraycopy(ringColors, ringNum*4, vertexColorArray, ringVertexCount*ringNum*4 + i*4, 4);
			}
		}

		// fill line and arrowhead colors
		for(int i = 0; i < (lineVertexArray.length + arrowHeadVertexArray.length)/3; i++){
			System.arraycopy(color, 0, vertexColorArray, ringVertexCount*ringRadii.length*4 + i*4, 4);
		}

		ByteBuffer bb = ByteBuffer.allocateDirect(vertexColorArray.length*4);
		bb.order(ByteOrder.nativeOrder());
		colorBuffer = bb.asFloatBuffer();
		colorBuffer.put(vertexColorArray);
		colorBuffer.position(0);

		//vertexBuffers.put(ShaderProgram.VERTEX_COLOR_ATTRIBUTE, colorBuffer);
	}

	private void fillShortColorArray(){
		// fill ring colors
		int ringVertexCount = ringVertexArray.length/3/ringRadii.length;
		for(int ringNum = 0; ringNum < ringRadii.length; ringNum++){
			for(int i = 0; i < ringVertexCount; i++){
				System.arraycopy(ringColors, ringNum*4, shortVertexColorArray, ringVertexCount*ringNum*4 + i*4, 4);
			}
		}

		// fill line and arrowhead colors
		for(int i = 0; i < arrowHeadVertexArray.length/3; i++){
			System.arraycopy(color, 0, shortVertexColorArray, ringVertexCount*ringRadii.length*4 + i*4, 4);
		}

		ByteBuffer bb = ByteBuffer.allocateDirect(shortVertexColorArray.length*4);
		bb.order(ByteOrder.nativeOrder());
		shortColorBuffer = bb.asFloatBuffer();
		shortColorBuffer.put(shortVertexColorArray);
		shortColorBuffer.position(0);

		//vertexBuffers.put(ShaderProgram.VERTEX_COLOR_ATTRIBUTE, shortColorBuffer);
	}

	public void setColor(float[] color) {
		this.color = color;
		if(vertexColorArray != null) {
			fillColorArray();
		}
		if(shortVertexColorArray != null) {
			fillShortColorArray();
		}
	}

	public float[] getColor(){
		return color;
	}
}
