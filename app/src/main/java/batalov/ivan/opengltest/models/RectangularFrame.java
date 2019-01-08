package batalov.ivan.opengltest.models;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;

import batalov.ivan.opengltest.shaders.ShaderProgram;

/**
 * Created by ivan on 3/21/18.
 */

public class RectangularFrame {
	float[] vertices = new float[]{
			-1, 1, 0,
			1, 1, 0,
			1, -1, 0,
			-1, -1, 0,
			-1, 1, 0,
			1, 1, 0,
			1, -1, 0,
			-1, -1, 0
	};
	int[] indexArray = new int[]{
			0, 4, 1,
			1, 4, 5,
			1, 5, 2,
			5, 6, 2,
			2, 6, 3,
			7, 3, 6,
			3, 7, 0,
			0, 7, 4
	};
	public float width;
	public float height;
	public float thickness;

	public float[] vertexArray = new float[72];
	public FloatBuffer vertexBuffer;
	public HashMap<String, Buffer> vertexBuffers = new HashMap(1);
	public float[] color = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
	public float positionX;
	public float positionY;
	public float[] modelMatrix = new float[16];
	public float[] projectionMatrix = new float[16];

	public RectangularFrame(float width, float height, float thickness){
		this.width = width;
		this.height = height;
		this.thickness = thickness;

		float halfWidth = width / 2;
		float halfHeight = height/2;

		for(int i = 0; i < 4; i++) {
			vertices[i * 3] *= halfWidth;
			vertices[i * 3 + 1] *= halfHeight;
		}

		halfWidth -= thickness;
		halfHeight -= thickness;

		for(int i = 0; i < 4; i++) {
			vertices[12 + i*3] *= halfWidth;
			vertices[12 + i*3 + 1] *= halfHeight;
		}

		for(int i = 0; i < indexArray.length; i++){
			vertexArray[3*i] = vertices[3*indexArray[i]];
			vertexArray[3*i + 1] = vertices[3*indexArray[i] + 1];
			vertexArray[3*i + 2] = vertices[3*indexArray[i] + 2];
		}

		ByteBuffer bb = ByteBuffer.allocateDirect(vertexArray.length*4);
		bb.order(ByteOrder.nativeOrder());
		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(vertexArray);
		vertexBuffer.position(0);

		vertexBuffers.put(ShaderProgram.VERTEX_COORD_ATTRIBUTE, vertexBuffer);
	}
}
