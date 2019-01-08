package batalov.ivan.opengltest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.opengl.GLES20;

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import batalov.ivan.opengltest.models.MapBlock;
import batalov.ivan.opengltest.shaders.ShaderProgram;

/**
 * Created by ivan on 2/13/18.
 */

public class MyUtilities {
    /**
     * Links a buffer with an attribute
     * @param attrName
     * @param buffer
     * @param program
     */
    public static int linkBufferToAttribute(int program, String attrName, Buffer buffer, int sizePerVertex, int dataType, int stride){
        //System.out.println("blip");
        int attrPositionHandle = GLES20.glGetAttribLocation(program, attrName);
        if(attrPositionHandle >= 0) {
            GLES20.glEnableVertexAttribArray(attrPositionHandle);
            GLES20.glVertexAttribPointer(attrPositionHandle, sizePerVertex, dataType, false, stride, buffer);
        } else{
            System.out.println("BUFFER HANDLE NOT FOUND!!!");
        }
        return attrPositionHandle;
    }

    /**
     *
     * @param blockMap
     * @param initialBlock
     * @param preventSeparation - if true, will exclude adjacent blocks that result in splitting the rest of the available
     *                          blocks in 2 contiguous areas
     * @param excludeDeadEnds - if true and separation is not allowed, remove blocks that lead into the smaller contiguous area
     * @return
     */
    public static ArrayList<Point> getAvailableBlocks(int[][] blockMap, Point initialBlock, boolean preventSeparation, boolean excludeDeadEnds){
        ArrayList<Point> result = getAdjacentUnoccupiedBlocks(blockMap, initialBlock, 0);
        //System.out.println("blocks initially available: " + result.size());
        if(excludeDeadEnds){
            ArrayList<ArrayList<Point>> contiguousAreas = getContiguousAreas(blockMap, 0, result);
            if(!contiguousAreas.isEmpty()) {
                int maxArea = 0;
                ArrayList<Point> maxAreaPoints = null;
                for(ArrayList<Point> list : contiguousAreas){
                    if(list.size() > maxArea){
                        maxArea = list.size();
                        maxAreaPoints = list;
                    }
                }
                ListIterator<Point> iterator = result.listIterator();
                while(iterator.hasNext()){
                    Point point = iterator.next();
                    if(!maxAreaPoints.contains(point)){
                        iterator.remove();
                        //System.out.println("removing point: (" + point.x + ", " + point.y + ");");
                    }
                }
            }

        }

        if(preventSeparation) {
            ListIterator<Point> iterator = result.listIterator();
            while (iterator.hasNext()) {
                Point point = iterator.next();
                ArrayList<Point> adjBlocks = getAdjacentUnoccupiedBlocks(blockMap, point, 0);
                int contiguousAreasBefore = getContiguousAreas(blockMap, 0, adjBlocks).size();
                blockMap[point.x][point.y] = 1;
                int contiguousAreasAfter = getContiguousAreas(blockMap, 0, adjBlocks).size();
                blockMap[point.x][point.y] = 0;
                if(contiguousAreasAfter - contiguousAreasBefore > 0){
                    iterator.remove();
                }
            }
        }

        return result;
    }

    /**
     *
     * @param map
     * @param areaValue - this method will search for contiguous islands formed by this value
     * @return
     */
    private static ArrayList<ArrayList<Point>> getContiguousAreas(int[][] map, int areaValue){
        ArrayList<ArrayList<Point>> result = new ArrayList<>(1);

        // copy the map so I can fill the processed areas
        int[][] tempMap = new int[map.length][];
        for(int i = 0; i < map.length; i++) {
            tempMap[i] = Arrays.copyOf(map[i], map[i].length);
        }
        for(int x = 0; x < tempMap.length; x++){
            for(int y = 0; y < tempMap[0].length; y++){
                if(tempMap[x][y] == areaValue){
                    Point startPoint = new Point(x, y);
                    ArrayList<Point> pointList = new ArrayList<>();
                    tempMap[x][y] = 1;
                    pointList.add(startPoint);
                    ArrayList<Point> adjacentPoints = getAdjacentUnoccupiedBlocks(tempMap, startPoint, 0);
                    while(!adjacentPoints.isEmpty()){
                        for(Point p : adjacentPoints){
                            tempMap[p.x][p.y] = 1;
                        }
                        pointList.addAll(adjacentPoints);
                        adjacentPoints = getAdjacentUnoccupiedBlocks(tempMap, adjacentPoints, 0);
                    }
                    result.add(pointList);
                }
            }
        }
        return result;
    }

    private static ArrayList<ArrayList<Point>> getContiguousAreas(int[][] map, int areaValue, ArrayList<Point> initialPoints){
        ArrayList<ArrayList<Point>> result = new ArrayList<>(1);

        // copy the map so I can fill the processed areas
        int[][] tempMap = new int[map.length][];
        for(int i = 0; i < map.length; i++) {
            tempMap[i] = Arrays.copyOf(map[i], map[i].length);
        }
        for(Point point : initialPoints){
                if(tempMap[point.x][point.y] == areaValue){
                    Point startPoint = new Point(point.x, point.y);
                    ArrayList<Point> pointList = new ArrayList<>();
                    tempMap[point.x][point.y] = 1;
                    pointList.add(startPoint);
                    ArrayList<Point> adjacentPoints = getAdjacentUnoccupiedBlocks(tempMap, startPoint, 0);
                    while(!adjacentPoints.isEmpty()){
                        for(Point p : adjacentPoints){
                            tempMap[p.x][p.y] = 1;
                        }
                        pointList.addAll(adjacentPoints);
                        adjacentPoints = getAdjacentUnoccupiedBlocks(tempMap, adjacentPoints, 0);
                    }
                    result.add(pointList);
                }
        }
        return result;
    }

    private static ArrayList<Point> getAdjacentUnoccupiedBlocks(int[][] blockMap, Point initialBlock, int blockValue){
        ArrayList<Point> result = new ArrayList<>(3);
        if(initialBlock.x - 1 >= 0 && blockMap[initialBlock.x - 1][initialBlock.y] == blockValue){
            result.add(new Point(initialBlock.x - 1, initialBlock.y));
        }
        if(initialBlock.x + 1 < blockMap.length && blockMap[initialBlock.x + 1][initialBlock.y] == blockValue){
            result.add(new Point(initialBlock.x + 1, initialBlock.y));
        }
        if(initialBlock.y - 1 >= 0 && blockMap[initialBlock.x][initialBlock.y - 1] == blockValue){
            result.add(new Point(initialBlock.x, initialBlock.y - 1));
        }
        if(initialBlock.y + 1 < blockMap[0].length && blockMap[initialBlock.x][initialBlock.y + 1] == blockValue){
            result.add(new Point(initialBlock.x, initialBlock.y + 1));
        }
        return result;
    }

    private static ArrayList<Point> getAdjacentUnoccupiedBlocks(int[][] blockMap, ArrayList<Point> initialBlocks, int blockValue){
        ArrayList<Point> result = new ArrayList<>(3);
        for(Point initialBlock : initialBlocks) {
            if (initialBlock.x - 1 >= 0 && blockMap[initialBlock.x - 1][initialBlock.y] == blockValue) {
                Point newPoint = new Point(initialBlock.x - 1, initialBlock.y);
                if(!result.contains(newPoint) && !initialBlocks.contains(newPoint)) {
                    result.add(newPoint);
                }
            }
            if (initialBlock.x + 1 < blockMap.length && blockMap[initialBlock.x + 1][initialBlock.y] == blockValue) {
                Point newPoint = new Point(initialBlock.x + 1, initialBlock.y);
                if(!result.contains(newPoint) && !initialBlocks.contains(newPoint)) {
                    result.add(newPoint);
                }
            }
            if (initialBlock.y - 1 >= 0 && blockMap[initialBlock.x][initialBlock.y - 1] == blockValue) {
                Point newPoint = new Point(initialBlock.x, initialBlock.y - 1);
                if(!result.contains(newPoint) && !initialBlocks.contains(newPoint)) {
                    result.add(newPoint);
                }
            }
            if (initialBlock.y + 1 < blockMap[0].length && blockMap[initialBlock.x][initialBlock.y + 1] == blockValue) {
                Point newPoint = new Point(initialBlock.x, initialBlock.y + 1);
                if(!result.contains(newPoint) && !initialBlocks.contains(newPoint)) {
                    result.add(newPoint);
                }
            }
        }
        return result;
    }

    public static String readShaderCode(Context context, int id){
        StringBuffer vs = new StringBuffer();
        InputStream inputStream = context.getResources().openRawResource(id);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            String read = reader.readLine();
            while (read != null) {
                vs.append(read + "\n");
                read = reader.readLine();
            }
        } catch(IOException e){
            System.out.println("Could not read code for the shader: " + id);
            e.printStackTrace();
        }
        vs.deleteCharAt(vs.length() - 1); // remove the last \n
        return vs.toString();
    }

    public static float distance(float[] p1, float[] p2, int length){
	    float result = 0;
	    for(int i = 0; i < length; i++){
		    result += (p1[i]-p2[i])*(p1[i]-p2[i]);
	    }
	    return (float)Math.sqrt(result);
    }

	public static float distance(int[] p1, int[] p2, int length){
		float result = 0;
		for(int i = 0; i < length; i++){
			result += (p1[i]-p2[i])*(p1[i]-p2[i]);
		}
		return (float)Math.sqrt(result);
	}

    public static float vectorLength(float[] v, int size){
        float lengthSquared = 0;
        for(int i = 0; i < size; i++){
            lengthSquared += v[i]*v[i];
        }
        return (float)Math.sqrt(lengthSquared);
    }

    public static float[][] convertCoordArray(float[] coords, int stride){
        float[][] newCoords = new float[coords.length/stride][stride];
        for(int i = 0; i < coords.length/stride; i++){
            System.arraycopy(coords, i*stride, newCoords[i], 0, stride);
        }
        return newCoords;
    }

    public static float[] convertCoordArray(float[][] coords){
        float[] newCoords = new float[coords.length*coords[0].length];
        for(int i = 0; i < coords.length; i++){
            System.arraycopy(coords[i], 0, newCoords, i*coords[i].length, coords[i].length);
        }
        return newCoords;
    }

	/**
     *  Implementation of A* pathfinding algorithm
     * @param startArray
     * @param finishArray
     * @param map
     * @return
     */
    public static ArrayList<IntArray> findPath(int[] startArray, int[] finishArray, int[][] map){
	    if(startArray.length > 2){
		    startArray = new int[]{startArray[0], startArray[1]};
	    }
	    if(finishArray.length > 2){
		    finishArray = new int[]{finishArray[0], finishArray[1]};
	    }
	    IntArray start = new IntArray(startArray);
	    IntArray finish = new IntArray(finishArray);

	    if(map[start.array[0]][start.array[1]] != map[finish.array[0]][finish.array[1]]){
		    System.out.println("Path finding error: start and finish have different map values!");
		    return null;
	    }
		int mapValue = map[start.array[0]][start.array[1]];
	    System.out.println("path finding info: start: (" + startArray[0] + ", " + startArray[1] + "), finish: (" + finishArray[0] + ", " + finishArray[1] + "), map value = " + mapValue);

        HashSet<IntArray> closedSet = new HashSet<>();
        HashSet<IntArray> openSet = new HashSet<>();
        openSet.add(start);

	    HashMap<IntArray, IntArray> cameFrom = new HashMap<>();
	    HashMap<IntArray, Float> gScore = new HashMap<>();
	    gScore.put(start, new Float(0));

	    HashMap<IntArray, Float> fScore = new HashMap<>();
	    fScore.put(start, MyUtilities.distance(startArray, finishArray, 2));

	    while(!openSet.isEmpty()){
		    // find the node with the lowest gScore;
		    IntArray current = openSet.iterator().next();
		    float lowestFscore = fScore.get(current);
		    float tempFscore;
		    for(IntArray node : openSet){
			    tempFscore = fScore.get(node);
			    if(tempFscore < lowestFscore){
				    current = node;
				    lowestFscore = tempFscore;
			    }
		    }
		    if(current.equals(finish)){
			    System.out.println("path finding: path found!");
			    return reconstructPath(cameFrom, current);
		    }

		    openSet.remove(current);
		    closedSet.add(current);

		    for(IntArray neighbor : getNeighbors(current, map, mapValue)){
				if(!closedSet.contains(neighbor)){
					if(!openSet.contains(neighbor)){
						openSet.add(neighbor);
					}
					float tentativeGscore = gScore.get(current) + MyUtilities.distance(current.array, neighbor.array, 2);
					if(!gScore.containsKey(neighbor) || tentativeGscore < gScore.get(neighbor)){
						cameFrom.put(neighbor, current);
						gScore.put(neighbor, tentativeGscore);
						fScore.put(neighbor, tentativeGscore + MyUtilities.distance(neighbor.array, finish.array, 2));
						//System.out.println("path finding: gScore and fScore updated.");
					}
				}
		    }
	    }
	    System.out.println("Path finding error: could not find the path.");
	    return null;
    }

	public static ArrayList<IntArray> reconstructPath(HashMap<IntArray, IntArray> cameFrom, IntArray current){
		ArrayList<IntArray> result = new ArrayList<>();
		result.add(current);
		while(cameFrom.containsKey(current)){
			current = cameFrom.get(current);
			result.add(0, current);
		}
		return result;
	}

	public static ArrayList<FloatArray> reconstructPath(HashMap<FloatArray, FloatArray> cameFrom, FloatArray current){
		ArrayList<FloatArray> result = new ArrayList<>();
		result.add(current);
		while(cameFrom.containsKey(current)){
			current = cameFrom.get(current);
			result.add(0, current);
		}
		return result;
	}

	private static ArrayList<IntArray> getNeighbors(IntArray point, int[][] map, int neighborValue){
		ArrayList<IntArray> result = new ArrayList<>();
		if(point.array[0] > 0){
			if(map[point.array[0]-1][point.array[1]] == neighborValue){
				result.add(new IntArray(new int[]{point.array[0] - 1, point.array[1]}));
			}

			if(point.array[1] > 0 && map[point.array[0]-1][point.array[1]-1] == neighborValue){
				result.add(new IntArray(new int[]{point.array[0] - 1, point.array[1] - 1}));
			}
			if(point.array[1] < map[0].length - 1 && map[point.array[0]-1][point.array[1]+1] == neighborValue){
				result.add(new IntArray(new int[]{point.array[0] - 1, point.array[1] + 1}));
			}
		}
		if(point.array[0] < map.length - 1){
			if(map[point.array[0]+1][point.array[1]] == neighborValue){
				result.add(new IntArray(new int[]{point.array[0] + 1, point.array[1]}));
			}
			if(point.array[1] > 0 && map[point.array[0]+1][point.array[1]-1] == neighborValue){
				result.add(new IntArray(new int[]{point.array[0] + 1, point.array[1] - 1}));
			}
			if(point.array[1] < map[0].length - 1 && map[point.array[0]+1][point.array[1]+1] == neighborValue){
				result.add(new IntArray(new int[]{point.array[0] + 1, point.array[1] + 1}));
			}
		}
		if(point.array[1] > 0 && map[point.array[0]][point.array[1]-1] == neighborValue){
			result.add(new IntArray(new int[]{point.array[0], point.array[1] - 1}));
		}
		if(point.array[1] < map[0].length - 1 && map[point.array[0]][point.array[1]+1] == neighborValue){
			result.add(new IntArray(new int[]{point.array[0], point.array[1] + 1}));
		}
		return result;
	}

	public static Bitmap convertArrayToBitmap(int[][] map){
		Bitmap result = Bitmap.createBitmap(map.length, map[0].length, Bitmap.Config.ARGB_8888);
		float[] hsv = new float[3];
		hsv[1] = 1;
		hsv[2] = 1;
		for(int x = 0; x < map.length; x++){
			for(int y = 0; y < map[0].length; y++){
				hsv[0] = (map[x][y]*60) % 360;
				result.setPixel(x, map[0].length - y - 1, Color.HSVToColor(hsv));
			}
		}
		return result;
	}

	public static HashMap<String, Buffer> generateLineBuffers(ArrayList<FloatArray> points){

		HashMap<String, Buffer> buffers = new HashMap<>();
		float[] vertexArray = new float[(points.size() - 1)*18];
		float[] a = new float[3];
		float[] b = new float[3];
		float L;
		float L2;
		float d = 0.01f; // half thickness of the drawn line
		int dimensions = points.get(0).array.length;
		// create vertex array
		for(int i = 0; i < points.size() - 1; i++){
			a[0] = points.get(i+1).array[0] - points.get(i).array[0];
			a[1] = points.get(i+1).array[1] - points.get(i).array[1];
			if(dimensions >= 3) {
				a[2] = points.get(i + 1).array[2] - points.get(i).array[2];
			}
			L = MyUtilities.vectorLength(a, 3);
			a[0] /= L;
			a[1] /= L;
			a[2] /= L;

			b[0] = a[1];
			b[1] = -a[0];
			b[2] = 0;
			L2 = MyUtilities.vectorLength(b, 3);
			b[0] /= L2;
			b[1] /= L2;

			// triangle 1 point 1
			vertexArray[i*18] = points.get(i).array[0] - b[0]*d;
			vertexArray[i*18 + 1] = points.get(i).array[1] - b[1]*d;
			// triangle 1 point 2
			vertexArray[i*18 + 3] = points.get(i).array[0] + b[0]*d;
			vertexArray[i*18 + 4] = points.get(i).array[1] + b[1]*d;
			// triangle 1 point 3
			vertexArray[i*18 + 6] = points.get(i+1).array[0] + b[0]*d;
			vertexArray[i*18 + 7] = points.get(i+1).array[1] + b[1]*d;
			// triangle 2 point 1
			vertexArray[i*18 + 9] = points.get(i).array[0] - b[0]*d;
			vertexArray[i*18 + 10] = points.get(i).array[1] - b[1]*d;
			// triangle 2 point 2
			vertexArray[i*18 + 12] = points.get(i+1).array[0] + b[0]*d;
			vertexArray[i*18 + 13] = points.get(i+1).array[1] + b[1]*d;
			// triangle 2 point 3
			vertexArray[i*18 + 15] = points.get(i+1).array[0] - b[0]*d;
			vertexArray[i*18 + 16] = points.get(i+1).array[1] - b[1]*d;

			if(dimensions >= 3){
				vertexArray[i*18 + 2] = points.get(i).array[2] - b[2]*d;
				vertexArray[i*18 + 5] = points.get(i).array[2] + b[2]*d;
				vertexArray[i*18 + 8] = points.get(i+1).array[2] + b[2]*d;
				vertexArray[i*18 + 11] = points.get(i).array[2] - b[2]*d;
				vertexArray[i*18 + 14] = points.get(i+1).array[2] + b[2]*d;
				vertexArray[i*18 + 17] = points.get(i+1).array[2] - b[2]*d;
			} else{
				vertexArray[i*18 + 2] = 0.001f;
				vertexArray[i*18 + 5] = 0.001f;
				vertexArray[i*18 + 8] = 0.001f;
				vertexArray[i*18 + 11] = 0.001f;
				vertexArray[i*18 + 14] = 0.001f;
				vertexArray[i*18 + 17] = 0.001f;
			}
		}

		float[] normalsArray = new float[vertexArray.length];
		for(int i = 0; i < vertexArray.length/3; i++){
			normalsArray[i*3 + 2] = 1;
		}

		ByteBuffer bb = ByteBuffer.allocateDirect(vertexArray.length*4);
		bb.order(ByteOrder.nativeOrder());
		FloatBuffer fb = bb.asFloatBuffer();
		fb.put(vertexArray);
		fb.position(0);
		buffers.put(ShaderProgram.VERTEX_COORD_ATTRIBUTE, fb);

		ByteBuffer bb2 = ByteBuffer.allocateDirect(vertexArray.length*4);
		bb2.order(ByteOrder.nativeOrder());
		FloatBuffer fb2 = bb2.asFloatBuffer();
		fb2.put(normalsArray);
		fb2.position(0);
		buffers.put(ShaderProgram.NORMAL_COORD_ATTRIBUTE, fb2);

		return buffers;
	}

	public static float[][] listToArray2D(List<FloatArray> list, int vectorSize){
		if(list != null && !list.isEmpty()) {
			float[][] result = new float[list.size()][vectorSize];
			for(int i = 0; i < list.size(); i++){
				System.arraycopy(list.get(i).array, 0, result[i], 0, Math.min(vectorSize, list.get(i).array.length));
			}
			return result;
		}
		return null;
	}
}
