package batalov.ivan.opengltest.models;

import android.graphics.Point;
import android.graphics.PointF;
import android.opengl.Matrix;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import batalov.ivan.opengltest.FloatArray;
import batalov.ivan.opengltest.IntArray;
import batalov.ivan.opengltest.MyUtilities;
import batalov.ivan.opengltest.shaders.ShaderProgram;

/**
 * Created by ivan on 12/25/17.
 */

public class LevelMap {

    public static final int LINEAR_VERTICAL_MAP = 0;
    public static final int LINEAR_HORIZONTAL_MAP = 1;
    public static final int RECTANGULAR_MAP = 2;

    private static final int BLOCK_SIZE = 1;


    private int mapType;
    private HashMap<Point, MapBlock> blockList;
	private ArrayList<MapBlock> blocks;

	private HashMap<String, float[]> floatArrays = new HashMap<>();
	private HashMap<String, Buffer> vertexBuffers = new HashMap<>();

    //private int mProgram;
    //private int mPointShadowProgram;
    //private int mSimpleShadowMapProgram;
    //private int mPointShadowMapProgram;

    public LevelMap(ArrayList<Point> blockShifts){

        HashMap<String, Integer> params = new HashMap<>();
        params.put("entrance_side", MapBlock.SIDE_SOUTH);
        params.put("exit_side", MapBlock.SIDE_NORTH);
        params.put("entrance_position", 0);
        params.put("exit_position", 0);

        //params.put("extra_turns", 0);
        //params.put("contains_room", 0);

        //params.put("in_point1_x", 0);
        //params.put("in_point1_y", 0);
        //params.put("in_point2_x", 0);
        //params.put("in_point2_y", 0);

	    blocks = new ArrayList<>(blockShifts.size());
        blockList = new HashMap<>(blockShifts.size());
        int lastEntrSide;
        int lastExitSide = MapBlock.SIDE_NORTH;
        int lastEntrPos;
        int lastExitPos = MapBlock.POSITION_CENTER;
        Random rand = new Random();

        for(int i = 0; i < blockShifts.size(); i++){
            lastEntrSide = MapBlock.oppositeSide(lastExitSide);
            lastEntrPos = 2 - lastExitPos;

            lastExitSide = MapBlock.oppositeSide(lastEntrSide); // this is redundant!
            lastExitPos = rand.nextInt(3);

            if(i + 1 < blockShifts.size()){
                Point nextPosition = blockShifts.get(i+1);
                Point currPosition = blockShifts.get(i);
                Point dir = new Point(nextPosition.x - currPosition.x, nextPosition.y - currPosition.y);
                lastExitSide = MapBlock.directionSides.get(dir);
            }

            params.put("entrance_side", lastEntrSide);
            params.put("exit_side", lastExitSide);
            params.put("entrance_position",  lastEntrPos);
            params.put("exit_position", lastExitPos);

            MapBlock mapBlock = new MapBlock(params);
            mapBlock.blockPosition = blockShifts.get(i);
            blockList.put(mapBlock.blockPosition, mapBlock);
	        blocks.add(mapBlock);
        }



        /* // Testing turn blocks

        int lastEntrSide = 0;
        int lastExitSide = 0;
        int lastEntrPos = 0;
        int lastExitPos = 1;

        for(int extraShift = 0; extraShift < 9; extraShift++) {
            //System.out.println("new 2x2 block");
            for (int x = 0; x < 2; x++) {
                for (int y = 0; y < 2; y++) {
                    PointF entranceDir = new PointF((0.5f - x)*2, 0);
                    PointF exitDir = new PointF(0, (0.5f - y)*2);
                    for(HashMap.Entry<Integer, PointF> entry : MapBlock.sideDirections.entrySet()){
                        if(entry.getValue().equals(entranceDir)){
                            lastEntrSide = entry.getKey();
                            //System.out.print("in: " + lastEntrSide + "; ");
                        }
                        if(entry.getValue().equals(exitDir)){
                            lastExitSide = entry.getKey();
                            //System.out.print("out: " + lastExitSide + "; ");
                        }
                    }
                    //System.out.println("in: " + lastEntrSide + "; out: " + lastExitSide + "; (x,y)=(" + x + "," + y + ");");
                    if(extraShift == 0){
                        lastEntrPos = 0;
                        lastExitPos = 2;
                    } else if(extraShift == 1){
                        lastEntrPos = 1;
                        lastExitPos = 1;
                    } else if(extraShift == 2){
                        lastEntrPos = 2;
                        lastExitPos = 0;
                    } else if(extraShift == 3){
                        lastEntrPos = 0;
                        lastExitPos = 1;
                    } else if(extraShift == 4){
                        lastEntrPos = 0;
                        lastExitPos = 0;
                    } else if(extraShift == 5){
                        lastEntrPos = 1;
                        lastExitPos = 2;
                    } else if(extraShift == 6){
                        lastEntrPos = 2;
                        lastExitPos = 2;
                    } else if(extraShift == 7){
                        lastEntrPos = 1;
                        lastExitPos = 0;
                    } else if(extraShift == 8){
                        lastEntrPos = 2;
                        lastExitPos = 1;
                    }
                    params.put("entrance_side", lastEntrSide);
                    params.put("exit_side", lastExitSide);
                    params.put("entrance_position",  (x - y) % 2 == 0 ? lastEntrPos : 2 - lastEntrPos);
                    params.put("exit_position", (x - y) % 2 == 0 ? lastExitPos : 2 - lastExitPos);

                    MapBlock mapBlock = new MapBlock(params);
                    mapBlock.blockPosition.x = x;
                    mapBlock.blockPosition.y = y + extraShift*2;
                    blockList.put(mapBlock.blockPosition, mapBlock);
                }
            }
            //System.out.println();
        }

         */

        /* test of straight shifted blocks

        for(int y = 0; y < 10; y++){
                //System.out.println("entr. pos: " + lastEntrPos + "; last exit pos: " + lastExitPos);

                params.put("entrance_position", lastEntrPos);
                params.put("exit_position", lastExitPos);

                MapBlock mapBlock = new MapBlock(params);
                mapBlock.blockPosition.x = x;
                mapBlock.blockPosition.y = y;
                blockList.put(mapBlock.blockPosition, mapBlock);

                if(lastEntrPos == 0){
                    lastEntrPos = 1;
                    lastExitPos = 0;
                } else if(lastEntrPos == 2){
                    lastEntrPos = 1;
                    lastExitPos = 2;
                } else if (lastEntrPos == 1) {
                    if(lastExitPos == 0){
                        lastEntrPos = 2;
                        lastExitPos = 1;
                    } else{
                        lastEntrPos = 0;
                        lastExitPos = 1;
                    }
                }
            }

         */
    }

    float oldX0 = 0;
    float oldY0 = 0;
    float oldWidth = 0;
    float oldHeight = 0;


    /**
     *  Draw the level around the center (x0, y0) with the specified width/height
     * @param x0 - coordinate of the player position around which to draw the level
     * @param y0 - coordinate of the player position around which to draw the level
     * @param width - width of the scene to draw
     * @param height - height of the scene to draw
     */
    public void setupVertexDataForDrawing(float x0, float y0, float width, float height){
        if(oldX0 != x0 || oldY0 != y0 || oldWidth != width || oldHeight != height) {

            oldX0 = x0;
            oldY0 = y0;
            oldWidth = width;
            oldHeight = height;

            ArrayList<MapBlock> blocksToDraw = new ArrayList();
            Point p = new Point();
            int arraySize = 0;

            //System.out.println("center: (" + x0 + ", " + y0 + "). Width: " + width + "; height: " + height);

            for (int x = (int) Math.floor(x0 - width / 2); x < 1 + Math.ceil(x0 + width / 2); x++) {
                for (int y = (int) Math.floor(y0 - height / 2); y < 1 + Math.ceil(y0 + height / 2); y++) {
                    p.x = x;
                    p.y = y;
                    if (blockList.containsKey(p)) {
                        //System.out.print(y + " ");

                        MapBlock block = blockList.get(p);
                        blocksToDraw.add(block);
                        arraySize += block.getVertexArray().length;
                    }
                }
                //System.out.println();
            }

	        float[] vertexCoords = new float[arraySize];
	        float[] normalCoords = new float[arraySize];
	        float[] colorCoords = new float[(arraySize*4)/3];
            Arrays.fill(colorCoords, 1.0f);

	        floatArrays.put(ShaderProgram.VERTEX_COORD_ATTRIBUTE, vertexCoords);
	        floatArrays.put(ShaderProgram.NORMAL_COORD_ATTRIBUTE, normalCoords);
	        floatArrays.put(ShaderProgram.VERTEX_COLOR_ATTRIBUTE, colorCoords);


	        int currentIndex = 0;
            for (MapBlock block : blocksToDraw) {
                int currLength = block.getVertexArray().length;
                Point blockPosition = block.blockPosition;
                float[] blockVertexCoords = block.getVertexArray();
                float[] blockNormalCoords = block.getNormalsArray();

                for(int i = 0; i < currLength; i++){
                    normalCoords[currentIndex + i] = blockNormalCoords[i];
                    if(i % 3 == 0){
                        // x coordinate
                        vertexCoords[currentIndex + i] = blockVertexCoords[i] + blockPosition.x;
                    } else if(i % 3 == 1){
                        // y coordinate
                        vertexCoords[currentIndex + i] = blockVertexCoords[i] + blockPosition.y;
                    } else{
                        vertexCoords[currentIndex + i] = blockVertexCoords[i];
                    }
                }
                //System.arraycopy(block.getVertexArray(), 0, vertexCoords, currentIndex, currLength);
                System.arraycopy(block.getNormalsArray(), 0, normalCoords, currentIndex, currLength);
                currentIndex += currLength;
            }

	        ByteBuffer bb = ByteBuffer.allocateDirect(vertexCoords.length * 4); // (number of coordinate values * 4 bytes per float)
	        bb.order(ByteOrder.nativeOrder());
	        FloatBuffer vertexBuffer = bb.asFloatBuffer();
	        vertexBuffer.put(vertexCoords);
	        vertexBuffer.position(0);
	        vertexBuffers.put(ShaderProgram.VERTEX_COORD_ATTRIBUTE, vertexBuffer);

	        ByteBuffer bb2 = ByteBuffer.allocateDirect(normalCoords.length * 4); // (number of coordinate values * 4 bytes per float)
	        bb2.order(ByteOrder.nativeOrder());
	        FloatBuffer normalBuffer = bb2.asFloatBuffer();
	        normalBuffer.put(normalCoords);
	        normalBuffer.position(0);
	        vertexBuffers.put(ShaderProgram.NORMAL_COORD_ATTRIBUTE, normalBuffer);

	        ByteBuffer bb3 = ByteBuffer.allocateDirect(colorCoords.length * 4); // (number of coordinate values * 4 bytes per float)
	        bb3.order(ByteOrder.nativeOrder());
	        FloatBuffer colorBuffer = bb3.asFloatBuffer();
	        colorBuffer.put(colorCoords);
	        colorBuffer.position(0);
	        vertexBuffers.put(ShaderProgram.VERTEX_COLOR_ATTRIBUTE, colorBuffer);
        }

    }

    /**
     *
     * @param coord
     * @return >0 if there is no wall, =0 if there is a wall at the specified coordinate
     */
    public int getMapValue(PointF coord){
        Point roundedCoord = new Point((int)Math.floor(coord.x/BLOCK_SIZE) * BLOCK_SIZE, (int)Math.floor(coord.y/BLOCK_SIZE) * BLOCK_SIZE);
        if(blockList.containsKey(roundedCoord)){
            PointF localNormalizedCoord = new PointF((coord.x - roundedCoord.x)/BLOCK_SIZE, (coord.y - roundedCoord.y)/BLOCK_SIZE);
	        int result = blockList.get(roundedCoord).getMapValue(localNormalizedCoord);;
            return result;
        } else{
            return 0;
        }
    }

	Point roundedCoord = new Point();
	PointF localNormalizedCoord = new PointF();

	public int getMapValue(float x, float y){
		roundedCoord.x = (int)Math.floor(x/BLOCK_SIZE) * BLOCK_SIZE;
		roundedCoord.y = (int)Math.floor(y/BLOCK_SIZE) * BLOCK_SIZE;
		if(blockList.containsKey(roundedCoord)){
			localNormalizedCoord.x = (x - roundedCoord.x)/BLOCK_SIZE;
			localNormalizedCoord.y = (y - roundedCoord.y)/BLOCK_SIZE;
			int result = blockList.get(roundedCoord).getMapValue(localNormalizedCoord);;
			return result;
		} else{
			return 0;
		}
	}

	public boolean isPathBlocked(float[] start, float[] finish, int maxDepth){
		float[] currPoint = new float[2];
		if(getMapValue(start[0], start[1]) > 0 && getMapValue(finish[0], finish[1]) > 0) {
			for (int depth = 1; depth <= maxDepth; depth++) {
				int denominator = (int)Math.pow(2, depth);
				for(int i = 1; i < denominator; i += 2){
					currPoint[0] = start[0] + (finish[0] - start[0]) * (float)i / denominator;
					currPoint[1] = start[1] + (finish[1] - start[1]) * (float)i / denominator;
					if(getMapValue(currPoint[0], currPoint[1]) == 0){
						return true;
					}
				}
			}
			return false;
		} else return true;
	}

	public boolean isPathBlocked(PointF start, PointF finish, int maxDepth){
		PointF currPoint = new PointF();
		if(getMapValue(start) > 0 && getMapValue(finish) > 0) {
			for (int depth = 1; depth <= maxDepth; depth++) {
				int denominator = (int)Math.pow(2, depth);
				for(int i = 1; i < denominator; i += 2){
					currPoint.x = start.x + (finish.x - start.x) * (float)i / denominator;
					currPoint.y = start.y + (finish.y - start.y) * (float)i / denominator;
					if(getMapValue(currPoint) == 0){
						return true;
					}
				}
			}
			return false;
		} else return true;
	}

	private PointF step = new PointF();
    /**
     *
     * @param start - initial position
     * @param finish - final desired position
     * @param stepCount - number of intervals to divide the whole desired path into (finish - start)
     * @return the furthest point along the desired path that is allowed to be moved to
     */
    public PointF getFurthestAvailablePoint(PointF start, PointF finish, int stepCount){

        PointF currPoint = new PointF();
        step.x = (finish.x - start.x)/stepCount;
	    step.y = (finish.y - start.y)/stepCount;
        boolean pointAllowed = true;
        for(int stepNum = 1; (stepNum <= stepCount) && pointAllowed; stepNum++){
            currPoint.x = finish.x - (stepCount - stepNum)*step.x;
            currPoint.y = finish.y - (stepCount - stepNum)*step.y;
            if(getMapValue(currPoint) == 0){
                pointAllowed = false;
                currPoint.x = finish.x - (stepCount - stepNum + 1)*step.x;
                currPoint.y = finish.y - (stepCount - stepNum + 1)*step.y;
            }
        }
        return currPoint;
    }

	float[] stepF = new float[2];
	public float[] getFurthestAvailablePoint(float[] start, float[] finish, int stepCount){

		float[] currPoint = new float[2];
		stepF[0] = (finish[0] - start[0])/stepCount;
		stepF[1] = (finish[1] - start[1])/stepCount;

		boolean pointAllowed = true;
		for(int stepNum = 1; (stepNum <= stepCount) && pointAllowed; stepNum++){
			currPoint[0] = finish[0] - (stepCount - stepNum)*stepF[0];
			currPoint[1] = finish[1] - (stepCount - stepNum)*stepF[1];

			if(getMapValue(currPoint[0], currPoint[1]) == 0){
				pointAllowed = false;
				currPoint[0] = finish[0] - (stepCount - stepNum + 1)*stepF[0];
				currPoint[1] = finish[1] - (stepCount - stepNum + 1)*stepF[1];
			}
		}
		return currPoint;
	}

    /**
     *
     * @param start
     * @param finish
     * @param stepCount
     * @return
     */
    public PointF getCorrectedDestination(PointF start, PointF finish, int stepCount, float angleTolerance){
        PointF finalPoint = getFurthestAvailablePoint(start, finish, stepCount);
        //System.out.println("furthest point along desired direction: (" + finalPoint.x + ", " + finalPoint.y + ");");
        if(!finalPoint.equals(finish)) {
            // unit vector pointing towards the desired destination
            PointF initialPath = new PointF(finish.x - start.x, finish.y - start.y);
            PointF leftoverPath = new PointF(finish.x - finalPoint.x, finish.y - finalPoint.y);
            float initialLength = initialPath.length();
            float residualLength = leftoverPath.length();

            float stepsLeft = stepCount*residualLength/initialLength;
	        int maxDepth = (int)Math.ceil(Math.log(stepsLeft)/Math.log(2));
	        float[] initDirVec = new float[]{initialPath.x/initialLength, initialPath.y/initialLength, 0, 0};
	        float[] rotatedVec = new float[4];
	        //
            float lowerAngle = 0;
            float higherAngle = 90;
            float angle;
            float[] rotM = new float[16];
            Matrix.setIdentityM(rotM, 0);

            float length;
            PointF newFinish = new PointF();
	        int angleSign = 0;

            while (higherAngle - lowerAngle > angleTolerance) {
                angle = (higherAngle + lowerAngle) / 2;
                length = (float) (residualLength * Math.cos(angle*Math.PI/180));
	            //System.out.println("angle range: (" + lowerAngle + ", " + higherAngle + ");");
	            if(angleSign == 0) {

		            //rotate CCW first
		            Matrix.setRotateM(rotM, 0, angle, 0, 0, 1);
		            Matrix.multiplyMV(rotatedVec, 0, rotM, 0, initDirVec, 0);
		            newFinish.x = finalPoint.x + rotatedVec[0] * length;
		            newFinish.y = finalPoint.y + rotatedVec[1] * length;
					boolean ccwPassed = !isPathBlocked(finalPoint, newFinish, maxDepth);

		            Matrix.setRotateM(rotM, 0, -angle, 0, 0, 1);
		            Matrix.multiplyMV(rotatedVec, 0, rotM, 0, initDirVec, 0);
		            newFinish.x = finalPoint.x + rotatedVec[0] * length;
		            newFinish.y = finalPoint.y + rotatedVec[1] * length;
		            boolean cwPassed = !isPathBlocked(finalPoint, newFinish, maxDepth);
		            if(ccwPassed){
			            higherAngle = angle;
			            if(!cwPassed){
				            angleSign = 1;
			            }
		            } else{
			            if(cwPassed){
				            higherAngle = angle;
				            angleSign = -1;
			            } else{
				            lowerAngle = angle;
			            }
		            }
	            } else{
		            Matrix.setRotateM(rotM, 0, angleSign*angle, 0, 0, 1);
		            Matrix.multiplyMV(rotatedVec, 0, rotM, 0, initDirVec, 0);
		            newFinish.x = finalPoint.x + rotatedVec[0] * length;
		            newFinish.y = finalPoint.y + rotatedVec[1] * length;
		            if(!isPathBlocked(finalPoint, newFinish, maxDepth)){
			            higherAngle = angle;
		            } else{
			            lowerAngle = angle;
		            }
	            }
            }
	        length = (float) (residualLength * Math.cos(higherAngle*Math.PI/180));
	        Matrix.setRotateM(rotM, 0, (angleSign == 0 ? 1 : angleSign)*higherAngle, 0, 0, 1);
	        Matrix.multiplyMV(rotatedVec, 0, rotM, 0, initDirVec, 0);
	        newFinish.x = finalPoint.x + rotatedVec[0] * length;
	        newFinish.y = finalPoint.y + rotatedVec[1] * length;
	        finalPoint = newFinish;
	        //System.out.println("angle: " + higherAngle + "; length ratio: " + length/residualLength);
	        //System.out.println("corrected destination: (" + finalPoint.x + ", " + finalPoint.y + ");");
        }
        return finalPoint;
    }

	public PointF getFurthestAvailablePoint(PointF start, PointF finish, int stepCount, ArrayList<float[]> perimeter){
		PointF currPoint = new PointF();
		PointF currPerimPoint = new PointF();
		PointF step = new PointF((finish.x - start.x)/stepCount, (finish.y - start.y)/stepCount);
		boolean pointAllowed = true;
		for(int stepNum = 1; (stepNum <= stepCount) && pointAllowed; stepNum++){
			currPoint.x = start.x + stepNum*step.x;
			currPoint.y = start.y + stepNum*step.y;

			for(int i = 0; i < perimeter.size() && pointAllowed; i++){
				float[] point = perimeter.get(i);
				currPerimPoint.x = currPoint.x + point[0];
				currPerimPoint.y = currPoint.y + point[1];

				if(getMapValue(currPerimPoint) == 0){
					pointAllowed = false;
					currPoint.x = start.x + (stepNum - 1)*step.x;
					currPoint.y = start.y + (stepNum - 1)*step.y;
				}
			}
		}
		// do this to avoid rounding errors and return the perfect desired destination
		// if the path is not blocked
		if(pointAllowed){
			currPoint.x = finish.x;
			currPoint.y = finish.y;
		}
		return currPoint;
	}

	public boolean isPathBlocked(PointF start, PointF finish, int maxDepth, ArrayList<float[]> perimeter){
		PointF currPoint = new PointF();
		PointF currPerimPoint = new PointF();

		// check start and finish points for blockage
		for(float[] point : perimeter){
			currPerimPoint.x = start.x + point[0];
			currPerimPoint.y = start.y + point[1];
			if(getMapValue(currPerimPoint) == 0){
				return true;
			}
			currPerimPoint.x = finish.x + point[0];
			currPerimPoint.y = finish.y + point[1];
			if(getMapValue(currPerimPoint) == 0){
				return true;
			}
		}

		for (int depth = 1; depth <= maxDepth; depth++) {
			int denominator = (int)Math.pow(2, depth);
			for(int i = 1; i < denominator; i += 2){
				currPoint.x = start.x + (finish.x - start.x) * (float)i / denominator;
				currPoint.y = start.y + (finish.y - start.y) * (float)i / denominator;

				for(float[] point : perimeter){
					currPerimPoint.x = currPoint.x + point[0];
					currPerimPoint.y = currPoint.y + point[1];

					if(getMapValue(currPerimPoint) == 0){
						System.out.println("middle point is blocked");
						return true;
					}
				}
			}
		}
		return false;
	}

	public PointF getCorrectedDestination(PointF start, PointF finish, int stepCount, float angleTolerance, ArrayList<float[]> perimeter){
		PointF finalPoint = getFurthestAvailablePoint(start, finish, stepCount, perimeter);
		//System.out.println("furthest point along desired direction: (" + finalPoint.x + ", " + finalPoint.y + ");");

		if(!finalPoint.equals(finish)) {
			// unit vector pointing towards the desired destination
			PointF initialPath = new PointF(finish.x - start.x, finish.y - start.y);
			PointF leftoverPath = new PointF(finish.x - finalPoint.x, finish.y - finalPoint.y);
			float initialLength = initialPath.length();
			float residualLength = leftoverPath.length();

			float stepsLeft = stepCount*residualLength/initialLength;
			int maxDepth = (int)Math.ceil(Math.log(stepsLeft)/Math.log(2));
			float[] initDirVec = new float[]{initialPath.x/initialLength, initialPath.y/initialLength, 0, 0};
			float[] rotatedVec = new float[4];
			//
			float lowerAngle = 0;
			float higherAngle = 90;
			float angle;
			float[] rotM = new float[16];
			Matrix.setIdentityM(rotM, 0);

			float length;
			PointF newFinish = new PointF();
			int angleSign = 0;

			while (higherAngle - lowerAngle > angleTolerance) {
				angle = (higherAngle + lowerAngle) / 2;
				length = (float) (residualLength * Math.cos(angle*Math.PI/180));
				//System.out.println("angle range: (" + lowerAngle + ", " + higherAngle + ");");
				if(angleSign == 0) {

					//rotate CCW first
					Matrix.setRotateM(rotM, 0, angle, 0, 0, 1);
					Matrix.multiplyMV(rotatedVec, 0, rotM, 0, initDirVec, 0);
					newFinish.x = finalPoint.x + rotatedVec[0] * length;
					newFinish.y = finalPoint.y + rotatedVec[1] * length;
					boolean ccwPassed = !isPathBlocked(finalPoint, newFinish, maxDepth, perimeter);
					// then rotate CW
					Matrix.setRotateM(rotM, 0, -angle, 0, 0, 1);
					Matrix.multiplyMV(rotatedVec, 0, rotM, 0, initDirVec, 0);
					newFinish.x = finalPoint.x + rotatedVec[0] * length;
					newFinish.y = finalPoint.y + rotatedVec[1] * length;
					boolean cwPassed = !isPathBlocked(finalPoint, newFinish, maxDepth, perimeter);
					if(ccwPassed){
						higherAngle = angle;
						if(!cwPassed){
							angleSign = 1;
						}
					} else{
						if(cwPassed){
							higherAngle = angle;
							angleSign = -1;
						} else{
							lowerAngle = angle;
						}
					}
				} else{
					Matrix.setRotateM(rotM, 0, angleSign*angle, 0, 0, 1);
					Matrix.multiplyMV(rotatedVec, 0, rotM, 0, initDirVec, 0);
					newFinish.x = finalPoint.x + rotatedVec[0] * length;
					newFinish.y = finalPoint.y + rotatedVec[1] * length;
					if(!isPathBlocked(finalPoint, newFinish, maxDepth, perimeter)){
						higherAngle = angle;
					} else{
						lowerAngle = angle;
					}
				}
			}
			length = (float) (residualLength * Math.cos(higherAngle*Math.PI/180));
			Matrix.setRotateM(rotM, 0, (angleSign == 0 ? 1 : angleSign)*higherAngle, 0, 0, 1);
			Matrix.multiplyMV(rotatedVec, 0, rotM, 0, initDirVec, 0);
			newFinish.x = finalPoint.x + rotatedVec[0] * length;
			newFinish.y = finalPoint.y + rotatedVec[1] * length;
			finalPoint = newFinish;
			//System.out.println("angle: " + higherAngle + "; length ratio: " + length/residualLength);
			//System.out.println("corrected destination: (" + finalPoint.x + ", " + finalPoint.y + ");");
		}
		return finalPoint;
	}


	public FloatBuffer getVertexBuffer() {
		return (FloatBuffer)vertexBuffers.get(ShaderProgram.VERTEX_COORD_ATTRIBUTE);
	}

	public FloatBuffer getColorBuffer() {
		return (FloatBuffer)vertexBuffers.get(ShaderProgram.VERTEX_COLOR_ATTRIBUTE);
	}

	public FloatBuffer getNormalBuffer() {
		return (FloatBuffer)vertexBuffers.get(ShaderProgram.NORMAL_COORD_ATTRIBUTE);
	}

	public float[] getNormalCoords() {
		return floatArrays.get(ShaderProgram.NORMAL_COORD_ATTRIBUTE);
	}

	public float[] getVertexCoords() {
		return floatArrays.get(ShaderProgram.VERTEX_COORD_ATTRIBUTE);
	}

	public float[] getColorCoords() {
		return floatArrays.get(ShaderProgram.VERTEX_COLOR_ATTRIBUTE);
	}

	public HashMap<String, float[]> getFloatArrays() {
		return floatArrays;
	}

	public HashMap<String, Buffer> getVertexBuffers() {
		return vertexBuffers;
	}

	public ArrayList<MapBlock> getBlocks() {
		return blocks;
	}

	HashMap<String, Buffer> centerPathBuffers;
	public float[] centerLineColor = new float[]{1.0f, 0.0f, 0.0f, 1.0f};

	public HashMap<String, Buffer> getCenterPathBuffers(){
		if(centerPathBuffers == null){
			centerPathBuffers = new HashMap<>();

			ArrayList<float[]> vertexCoordList = new ArrayList<>(blocks.size());
			int capacity = 0;
			float[] a = new float[3];
			float[] b = new float[3];
			float L;
			float d = 0.01f; // half thickness of the drawn line
			// create vertex array
			for(MapBlock block : blocks){
				float[] centerPath = block.getCenterPath();
				float[] array = new float[(centerPath.length/3 - 1)*18];
				capacity += array.length;
				for(int i = 0; i < centerPath.length/3 - 1; i++){
					a[0] = centerPath[(i+1)*3] - centerPath[(i)*3];
					a[1] = centerPath[(i+1)*3 + 1] - centerPath[(i)*3 + 1];
					a[2] = 0;

					L = MyUtilities.vectorLength(a, 3);
					a[0] /= L;
					a[1] /= L;
					a[2] /= L;

					b[0] = a[1];
					b[1] = -a[0];
					b[2] = 0;
					// triangle 1 point 1
					array[i*18] = centerPath[(i)*3] - b[0]*d;
					array[i*18 + 1] = centerPath[(i)*3 + 1] - b[1]*d;
					array[i*18 + 2] = 0.01f;
					// triangle 1 point 2
					array[i*18 + 3] = centerPath[(i)*3] + b[0]*d;
					array[i*18 + 4] = centerPath[(i)*3 + 1] + b[1]*d;
					array[i*18 + 5] = 0.01f;
					// triangle 1 point 3
					array[i*18 + 6] = centerPath[(i + 1)*3] + b[0]*d;
					array[i*18 + 7] = centerPath[(i + 1)*3 + 1] + b[1]*d;
					array[i*18 + 8] = 0.01f;
					// triangle 2 point 1
					array[i*18 + 9] = centerPath[(i)*3] - b[0]*d;
					array[i*18 + 10] = centerPath[(i)*3 + 1] - b[1]*d;
					array[i*18 + 11] = 0.01f;
					// triangle 2 point 2
					array[i*18 + 12] = centerPath[(i + 1)*3] + b[0]*d;
					array[i*18 + 13] = centerPath[(i + 1)*3 + 1] + b[1]*d;
					array[i*18 + 14] = 0.01f;
					// triangle 2 point 3
					array[i*18 + 15] = centerPath[(i + 1)*3] - b[0]*d;
					array[i*18 + 16] = centerPath[(i + 1)*3 + 1] - b[1]*d;
					array[i*18 + 17] = 0.01f;
				}
				vertexCoordList.add(array);
			}
			float[] fullArray = new float[capacity];
			int offset = 0;
			for(float[] array : vertexCoordList){
				System.arraycopy(array, 0, fullArray, offset, array.length);
				offset += array.length;
			}

			float[] normalsArray = new float[capacity];
			for(int i = 0; i < capacity/3; i++){
				normalsArray[i*3 + 2] = 1;
			}

			ByteBuffer bb = ByteBuffer.allocateDirect(capacity*4);
			bb.order(ByteOrder.nativeOrder());
			FloatBuffer fb = bb.asFloatBuffer();
			fb.put(fullArray);
			fb.position(0);
			centerPathBuffers.put(ShaderProgram.VERTEX_COORD_ATTRIBUTE, fb);

			ByteBuffer bb2 = ByteBuffer.allocateDirect(capacity*4);
			bb2.order(ByteOrder.nativeOrder());
			FloatBuffer fb2 = bb2.asFloatBuffer();
			fb2.put(normalsArray);
			fb2.position(0);
			centerPathBuffers.put(ShaderProgram.NORMAL_COORD_ATTRIBUTE, fb2);

		}
		return centerPathBuffers;
	}

	int[][] map;
	public Point minPoint;
	public Point maxPoint;
	public int[][] getLevelMap(int blockPxSize){
		if(map == null){
			minPoint = new Point(blockList.keySet().iterator().next());
			maxPoint = new Point(minPoint);
			for(Point point : blockList.keySet()){
				if(point.x < minPoint.x){
					minPoint.x = point.x;
				}
				if(point.y < minPoint.y){
					minPoint.y = point.y;
				}
				if(point.x > maxPoint.x){
					maxPoint.x = point.x;
				}
				if(point.y > maxPoint.y){
					maxPoint.y = point.y;
				}
			}
			map = new int[blockPxSize*(maxPoint.x - minPoint.x + 1)][blockPxSize*(maxPoint.y - minPoint.y + 1)];
			for(Point point : blockList.keySet()){
				MapBlock block = blockList.get(point);
				float width = block.getWidth();
				float length = block.getLength();
				for(int x = 0; x < blockPxSize*width; x++){
					for(int y = 0; y < blockPxSize*length; y++){
						map[(point.x - minPoint.x)*blockPxSize + x][(point.y - minPoint.y)*blockPxSize + y] = block.getMapValue((x + 0.5f)/blockPxSize/width, (y + 0.5f)/blockPxSize/length);
					}
				}
			}
		}
		return map;
	}


	private static float mapPxSize = MapBlock.getDefaultWidth()/4;

	private float heuristic(float[] a, float[] b){
		return MyUtilities.distance(a, b, 2);
	}

	public ArrayList<FloatArray> findPath(float[] startArray, float[] finishArray){
		if(startArray.length > 2){
			startArray = new float[]{startArray[0], startArray[1]};
		}
		if(finishArray.length > 2){
			finishArray = new float[]{finishArray[0], finishArray[1]};
		}
		FloatArray start = new FloatArray(startArray);
		FloatArray finish = new FloatArray(finishArray);

		if(getMapValue(start.array[0], start.array[1]) != getMapValue(finish.array[0],finish.array[1])){
			System.out.println("Path finding error: start and finish have different map values!");
			return null;
		}
		int mapValue = getMapValue(start.array[0],start.array[1]);
		System.out.println("path finding info: start: (" + startArray[0] + ", " + startArray[1] + "), finish: (" + finishArray[0] + ", " + finishArray[1] + "), map value = " + mapValue);

		HashSet<FloatArray> closedSet = new HashSet<>();
		HashSet<FloatArray> openSet = new HashSet<>();
		openSet.add(start);

		HashMap<FloatArray, FloatArray> cameFrom = new HashMap<>();
		HashMap<FloatArray, Float> gScore = new HashMap<>();
		gScore.put(start, new Float(0));

		HashMap<FloatArray, Float> fScore = new HashMap<>();
		fScore.put(start, heuristic(startArray, finishArray));

		while(!openSet.isEmpty()){
			// find the node with the lowest gScore;
			FloatArray current = openSet.iterator().next();
			float lowestFscore = fScore.get(current);
			float tempFscore;
			for(FloatArray node : openSet){
				tempFscore = fScore.get(node);
				if(tempFscore < lowestFscore){
					current = node;
					lowestFscore = tempFscore;
				}
			}
			if(current.distance(finish, 2) <= mapPxSize){
				System.out.println("path finding: path found!");
				cameFrom.put(finish, current);
				return MyUtilities.reconstructPath(cameFrom, current);
			}

			openSet.remove(current);
			closedSet.add(current);

			for(FloatArray neighbor : getNeighbors(current, mapValue, mapPxSize)){
				if(!closedSet.contains(neighbor)){
					if(!openSet.contains(neighbor)){
						openSet.add(neighbor);
					}
					float tentativeGscore = gScore.get(current) + heuristic(current.array, neighbor.array);
					if(!gScore.containsKey(neighbor) || tentativeGscore < gScore.get(neighbor)){
						cameFrom.put(neighbor, current);
						gScore.put(neighbor, tentativeGscore);
						fScore.put(neighbor, tentativeGscore + heuristic(neighbor.array, finish.array));
						//System.out.println("path finding: gScore and fScore updated.");
					}
				}
			}
		}
		System.out.println("Path finding error: could not find the path.");
		return null;
	}

	public void optimizePath(ArrayList<FloatArray> points, float padding){
		for(int i = 1; i < points.size() - 1;){
			FloatArray previous = points.get(i-1);
			FloatArray next = points.get(i+1);
			int maxDepth = (int)Math.ceil(Math.log(MyUtilities.distance(previous.array, next.array, 2)*100)/Math.log(2));
			//System.out.println("Path optimization. Max depth: " + maxDepth);

			if(!isPathBlocked(previous.array, next.array, maxDepth)){
				float[] borderStart = Arrays.copyOf(previous.array, previous.array.length);
				float[] borderEnd = Arrays.copyOf(next.array, next.array.length);
				float[] dir = new float[]{next.array[0] - previous.array[0], next.array[1] - previous.array[1]};
				float dirLength = MyUtilities.vectorLength(dir, 2);
				dir[0] /= dirLength;
				dir[1] /= dirLength;
				float[] perpDir = new float[]{-dir[1], dir[0]};
				borderStart[0] += perpDir[0]*padding;
				borderStart[1] += perpDir[1]*padding;
				borderEnd[0] += perpDir[0]*padding;
				borderEnd[1] += perpDir[1]*padding;

				if(!isPathBlocked(borderStart, borderEnd, maxDepth)){
					borderStart[0] -= 2*perpDir[0]*padding;
					borderStart[1] -= 2*perpDir[1]*padding;
					borderEnd[0] -= 2*perpDir[0]*padding;
					borderEnd[1] -= 2*perpDir[1]*padding;

					if(!isPathBlocked(borderStart, borderEnd, maxDepth)){
						points.remove(i);
					} else{
						i++;
					}
				} else{
					i++;
				}
			} else{
				i++;
			}
		}
	}

	public void constrainPath(ArrayList<FloatArray> points, float minDist, float step){

		boolean pointTested;
		int loops;

		for(FloatArray point : points){
			pointTested = false;
			loops = 0;
			System.out.println("\ntesting new point");
			while(!pointTested && loops < 3) {
				loops++;
				pointTested = true;

				float[] averagePoint = new float[2];
				float[] closestPoint = new float[2];
				float closestDistance = minDist;
				int numPoints = 0;
				for (float dx = -minDist; dx < minDist; dx += step) {
					for (float dy = -minDist; dy < minDist; dy += step) {
						float currDist = (float)Math.sqrt(dx * dx + dy * dy);
						if (currDist <= minDist && getMapValue(point.array[0] + dx, point.array[1] + dy) == 0) {
							averagePoint[0] += dx;
							averagePoint[1] += dy;
							numPoints++;
							pointTested = false;
							if(currDist < closestDistance){
								closestPoint[0] = dx;
								closestPoint[1] = dy;
								closestDistance = currDist;
							}
						}
					}
				}
				if(numPoints > 0 && (averagePoint[0] != 0 || averagePoint[1] != 0)) {
					//averagePoint[0] /= numPoints;
					//averagePoint[1] /= numPoints;
					float avLength = MyUtilities.vectorLength(averagePoint, 2);
					float movingDist = (minDist - closestDistance + step); // add 1 pixel of length for safety
					averagePoint[0] *= movingDist / avLength;
					averagePoint[1] *= movingDist / avLength;
					System.out.println("moving point: iteration " + loops + ", moving vector: (" + averagePoint[0] + ", " + averagePoint[1] + ").");

					point.array[0] += -averagePoint[0];
					point.array[1] += -averagePoint[1];
				}
			}
		}
	}

	private ArrayList<FloatArray> getNeighbors(FloatArray point, int neighborValue, float pxSize){
		ArrayList<FloatArray> result = new ArrayList<>();
		boolean left = false;
		boolean right = false;
		boolean top = false;
		boolean bottom = false;

		// left
		if(getMapValue(point.array[0]-pxSize, point.array[1]) == neighborValue){
			left = true;
			result.add(new FloatArray(new float[]{point.array[0] - pxSize, point.array[1]}));
		}
		//right
		if(getMapValue(point.array[0]+pxSize,point.array[1]) == neighborValue){
			right = true;
			result.add(new FloatArray(new float[]{point.array[0] + pxSize, point.array[1]}));
		}
		//bottom
		if(getMapValue(point.array[0],point.array[1]-pxSize) == neighborValue){
			bottom = true;
			result.add(new FloatArray(new float[]{point.array[0], point.array[1] - pxSize}));
		}
		//top
		if(getMapValue(point.array[0],point.array[1]+pxSize) == neighborValue){
			top = true;
			result.add(new FloatArray(new float[]{point.array[0], point.array[1] + pxSize}));
		}
		// bottom left
		if(getMapValue(point.array[0]-pxSize, point.array[1]-pxSize) == neighborValue &&
				bottom && left){
			result.add(new FloatArray(new float[]{point.array[0] - pxSize, point.array[1] - pxSize}));
		}
		// top left
		if(getMapValue(point.array[0]-pxSize,point.array[1]+pxSize) == neighborValue &&
				top && left){
			result.add(new FloatArray(new float[]{point.array[0] - pxSize, point.array[1] + pxSize}));
		}
		// bottom right
		if(getMapValue(point.array[0]+pxSize,point.array[1]-pxSize) == neighborValue &&
				bottom && right){
			result.add(new FloatArray(new float[]{point.array[0] + pxSize, point.array[1] - pxSize}));
		}
		//top right
		if(getMapValue(point.array[0]+pxSize,point.array[1]+pxSize) == neighborValue &&
				top && right){
			result.add(new FloatArray(new float[]{point.array[0] + pxSize, point.array[1] + pxSize}));
		}

		return result;
	}
}
