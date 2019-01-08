package batalov.ivan.opengltest.models;

import android.content.Context;
import android.opengl.Matrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import batalov.ivan.opengltest.shaders.ShaderProgram;

/**
 * Created by ivan on 2/25/18.
 */

public class Model3D {
	public static final String VERTEX_COORD = "v";
	public static final String TEXTURE_COORD = "vt";
	public static final String FACE_INDICES = "f";
	public static final String NORMAL_COORD = "vn";

	protected ArrayList<int[]> faceIndices = new ArrayList<>();

	public float[] modelMatrix = new float[16];
	public float[] projecionMatrix = new float[16];
	public float[] viewMatrix = new float[16];

	private final boolean useVertexIndexing;

	private float scaleX = 1.0f;
	private float scaleY = 1.0f;
	private float scaleZ = 1.0f;


	int[] vertexIndicesArray;

	HashMap<String, ArrayList<float[]>> coordLists = new HashMap<>();
	HashMap<String, float[]> floatArrays = new HashMap<>();
	HashMap<String, Buffer> vertexBuffers = new HashMap<>();

	public Model3D(Context context, int id, boolean useVertexIndexing){
		this.useVertexIndexing = useVertexIndexing;

		InputStream inputStream = context.getResources().openRawResource(id);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

		ArrayList<float[]> vertexCoords = new ArrayList<>();
		ArrayList<float[]> vertexNormals = new ArrayList<>();
		ArrayList<float[]> textureCoords = new ArrayList<>();
		ArrayList<float[]> vertexColors = new ArrayList<>();

		try {
			String read = reader.readLine();
			while (read != null) {

				String[] strArray = read.split(" +");
				if(strArray.length >= 3) {
					switch (strArray[0]) {
						case VERTEX_COORD:
							// example string:
							// v 0.000000 -1.000000 0.000000
							if(strArray.length >= 4){
								vertexCoords.add(new float[]{Float.valueOf(strArray[1]), Float.valueOf(strArray[2]), Float.valueOf(strArray[3])});
							}
							break;
						case TEXTURE_COORD:
							// example string:
							//vt 0.453124 1.000000
							if(strArray.length >= 3){ // I know this is redundant, but if I remove it, it will screw me over at some point in the future, when I decide to modify this code
								textureCoords.add(new float[]{Float.valueOf(strArray[1]), Float.valueOf(strArray[2])});
							}
							break;
						case NORMAL_COORD:
							// example string:
							// vn 0.4913 0.6326 0.5987
							if(strArray.length >= 4){
								vertexNormals.add(new float[]{Float.valueOf(strArray[1]), Float.valueOf(strArray[2]), Float.valueOf(strArray[3])});
							}
							break;
						case FACE_INDICES:
							// example string: vertex/texture/normal
							// f 314/362/337 330/373/337 315/364/337
							if(strArray.length >= 4){
								int[] face = new int[9];
								for(int vertex = 1; vertex <= 3; vertex++){
									String[] vertexIndices = strArray[vertex].split("/");
									face[(vertex-1)*3] = vertexIndices[0].isEmpty() ? -1 : Integer.valueOf(vertexIndices[0]);
									face[(vertex-1)*3 + 1] = vertexIndices[1].isEmpty() ? -1 : Integer.valueOf(vertexIndices[1]);
									face[(vertex-1)*3 + 2] = vertexIndices[2].isEmpty() ? -1 : Integer.valueOf(vertexIndices[2]);
								}
								faceIndices.add(face);
							}
							break;
					}
				}
				read = reader.readLine();
			}
		} catch(IOException e){
			System.out.println("Could not read code for the shader: " + id);
			e.printStackTrace();
		}

		coordLists.put(ShaderProgram.VERTEX_COORD_ATTRIBUTE, vertexCoords);
		coordLists.put(ShaderProgram.TEXTURE_COORD_ATTRIBUTE, textureCoords);
		coordLists.put(ShaderProgram.NORMAL_COORD_ATTRIBUTE, vertexNormals);
		coordLists.put(ShaderProgram.VERTEX_COLOR_ATTRIBUTE, vertexColors);

		if(!vertexCoords.isEmpty() && !faceIndices.isEmpty()){

			if(useVertexIndexing){
				// TODO
			} else{
				float[] vertexArray = new float[faceIndices.size()*9]; // 3 vertices/triangle + 3 coords/vertex
				float[] textureArray = null;
				if(textureCoords.size() > 0) {
					textureArray = new float[faceIndices.size() * 6]; // 3 vertices/triangle + 2 coords/vertex
					floatArrays.put(ShaderProgram.TEXTURE_COORD_ATTRIBUTE, textureArray);
				}
				float[] normalsArray = null;
				if(vertexNormals.size() > 0) {
					normalsArray = new float[faceIndices.size() * 9]; // 3 normals/triangle + 3 coords/normal
					floatArrays.put(ShaderProgram.NORMAL_COORD_ATTRIBUTE, normalsArray);
				}
				float[] colorsArray = new float[faceIndices.size()*12];
				Arrays.fill(colorsArray, 1.0f);

				floatArrays.put(ShaderProgram.VERTEX_COORD_ATTRIBUTE, vertexArray);
				floatArrays.put(ShaderProgram.VERTEX_COLOR_ATTRIBUTE, colorsArray);

				for(int triangleNum = 0; triangleNum < faceIndices.size(); triangleNum++){

					int[] triangleIndices = faceIndices.get(triangleNum);

					System.arraycopy(vertexCoords.get(triangleIndices[0] - 1), 0 , vertexArray, triangleNum*9, 3);
					System.arraycopy(vertexCoords.get(triangleIndices[3] - 1), 0 , vertexArray, triangleNum*9 + 3, 3);
					System.arraycopy(vertexCoords.get(triangleIndices[6] - 1), 0 , vertexArray, triangleNum*9 + 6, 3);

					if(textureArray != null) {
						System.arraycopy(textureCoords.get(triangleIndices[1] - 1), 0, textureArray, triangleNum * 6, 2);
						System.arraycopy(textureCoords.get(triangleIndices[4] - 1), 0, textureArray, triangleNum * 6 + 2, 2);
						System.arraycopy(textureCoords.get(triangleIndices[7] - 1), 0, textureArray, triangleNum * 6 + 4, 2);
					}
					if(normalsArray != null) {
						System.arraycopy(vertexNormals.get(triangleIndices[2] - 1), 0, normalsArray, triangleNum * 9, 3);
						System.arraycopy(vertexNormals.get(triangleIndices[5] - 1), 0, normalsArray, triangleNum * 9 + 3, 3);
						System.arraycopy(vertexNormals.get(triangleIndices[8] - 1), 0, normalsArray, triangleNum * 9 + 6, 3);
					}
				}

				ByteBuffer bb = ByteBuffer.allocateDirect(vertexArray.length * 4); // (number of coordinate values * 4 bytes per float)
				bb.order(ByteOrder.nativeOrder());
				FloatBuffer vertexBuffer = bb.asFloatBuffer();
				vertexBuffer.put(vertexArray);
				vertexBuffer.position(0);
				vertexBuffers.put(ShaderProgram.VERTEX_COORD_ATTRIBUTE, vertexBuffer);


				if(textureArray != null) {
					ByteBuffer bb2 = ByteBuffer.allocateDirect(textureArray.length * 4); // (number of coordinate values * 4 bytes per float)
					bb2.order(ByteOrder.nativeOrder());
					FloatBuffer textureBuffer = bb2.asFloatBuffer();
					textureBuffer.put(textureArray);
					textureBuffer.position(0);
					vertexBuffers.put(ShaderProgram.TEXTURE_COORD_ATTRIBUTE, textureBuffer);
				}
				if(normalsArray != null) {
					ByteBuffer bb3 = ByteBuffer.allocateDirect(normalsArray.length * 4); // (number of coordinate values * 4 bytes per float)
					bb3.order(ByteOrder.nativeOrder());
					FloatBuffer normalBuffer = bb3.asFloatBuffer();
					normalBuffer.put(normalsArray);
					normalBuffer.position(0);
					vertexBuffers.put(ShaderProgram.NORMAL_COORD_ATTRIBUTE, normalBuffer);
				}

				ByteBuffer bb4 = ByteBuffer.allocateDirect(colorsArray.length * 4); // (number of coordinate values * 4 bytes per float)
				bb4.order(ByteOrder.nativeOrder());
				FloatBuffer colorsBuffer = bb4.asFloatBuffer();
				colorsBuffer.put(colorsArray);
				colorsBuffer.position(0);
				vertexBuffers.put(ShaderProgram.VERTEX_COLOR_ATTRIBUTE, colorsBuffer);
			}
		}
	}

	public boolean isUseVertexIndexing() {
		return useVertexIndexing;
	}

	public ArrayList<float[]> getVertexCoords() {
		return coordLists.get(ShaderProgram.VERTEX_COORD_ATTRIBUTE);
	}

	public ArrayList<float[]> getTextureCoords() {
		return coordLists.get(ShaderProgram.TEXTURE_COORD_ATTRIBUTE);
	}

	public ArrayList<float[]> getNormalCoords() {
		return coordLists.get(ShaderProgram.NORMAL_COORD_ATTRIBUTE);
	}

	public ArrayList<int[]> getFaceIndices() {
		return faceIndices;
	}

	public void setScale(float scale) {
		setScale(scale, scale, scale);
	}

	public HashMap<String, ArrayList<float[]>> getCoordLists() {
		return coordLists;
	}

	public HashMap<String, float[]> getFloatArrays() {
		return floatArrays;
	}

	public HashMap<String, Buffer> getVertexBuffers() {
		return vertexBuffers;
	}

	public void setVertexColorArray(float[] colorsArray){
		floatArrays.put(ShaderProgram.VERTEX_COLOR_ATTRIBUTE, colorsArray);

		ByteBuffer bb4 = ByteBuffer.allocateDirect(colorsArray.length * 4); // (number of coordinate values * 4 bytes per float)
		bb4.order(ByteOrder.nativeOrder());
		FloatBuffer colorsBuffer = bb4.asFloatBuffer();
		colorsBuffer.put(colorsArray);
		colorsBuffer.position(0);
		vertexBuffers.put(ShaderProgram.VERTEX_COLOR_ATTRIBUTE, colorsBuffer);
	}

	public void setScale(float scaleX, float scaleY, float scaleZ){
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.scaleZ = scaleZ;

		float[] vertexArray = floatArrays.get(ShaderProgram.VERTEX_COORD_ATTRIBUTE);

		// scaling the model coordinates
		float[] scaleM = new float[16];
		float[] newVertexArray = new float[vertexArray.length];
		Matrix.setIdentityM(scaleM, 0);
		Matrix.scaleM(scaleM, 0, scaleX, scaleY, scaleZ);

		for(int vertexNum = 0; vertexNum < vertexArray.length/3 - 1; vertexNum++){
			// this is 4x4 matrix and 4x1 vectors, but I store only 3 coordiantes,
			// so every time I create an extra 4th element
			// But it gets rewritten in the next loop iteration, so it's fine
			// However, that means I need to process the last vertex separately to avoid ArrayIndexOutOfBound exception
			Matrix.multiplyMV(newVertexArray, vertexNum*3, scaleM, 0, vertexArray, vertexNum*3);
		}
		// processing the last vertex
		float[] lastPoint = new float[]{vertexArray[vertexArray.length - 3], vertexArray[vertexArray.length - 2], vertexArray[vertexArray.length - 1], 0};
		float[] newLastPoint = new float[4];
		Matrix.multiplyMV(newLastPoint, 0, scaleM, 0, lastPoint, 0);
		// copy the result in the new array
		System.arraycopy(newLastPoint, 0, newVertexArray, newVertexArray.length - 3, 3);
		floatArrays.put(ShaderProgram.VERTEX_COORD_ATTRIBUTE, newVertexArray);

		ByteBuffer bb = ByteBuffer.allocateDirect(newVertexArray.length * 4); // (number of coordinate values * 4 bytes per float)
		bb.order(ByteOrder.nativeOrder());
		FloatBuffer vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(newVertexArray);
		vertexBuffer.position(0);
		vertexBuffers.put(ShaderProgram.VERTEX_COORD_ATTRIBUTE, vertexBuffer);

		ArrayList<float[]> vertexCoords = coordLists.get(ShaderProgram.VERTEX_COORD_ATTRIBUTE);
		for(float[] coord : vertexCoords){
			System.arraycopy(coord, 0, lastPoint, 0, 3);
			lastPoint[3] = 0;
			Matrix.multiplyMV(newLastPoint, 0, scaleM, 0, lastPoint, 0);
			System.arraycopy(newLastPoint, 0, coord, 0, 3);
		}
	}

}
