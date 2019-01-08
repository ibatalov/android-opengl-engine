package batalov.ivan.opengltest.models;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.opengl.Matrix;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import batalov.ivan.opengltest.MyUtilities;

/**
 * Created by ivan on 12/25/17.
 */

public class MapBlock {
    public static final int SIDE_WEST = 0;
    public static final int SIDE_NORTH = 1;
    public static final int SIDE_EAST = 2;
    public static final int SIDE_SOUTH = 3;

    public Point blockPosition = new Point();

    public static final HashMap<Integer, Point> sideDirections;
    public static final HashMap<Point, Integer> directionSides;
    static{
        sideDirections = new HashMap<>(4);
        sideDirections.put(SIDE_WEST, new Point(-1, 0));
        sideDirections.put(SIDE_NORTH, new Point(0, 1));
        sideDirections.put(SIDE_EAST, new Point(1, 0));
        sideDirections.put(SIDE_SOUTH, new Point(0, -1));

        directionSides = new HashMap<>(4);
        directionSides.put(new Point(-1, 0), SIDE_WEST);
        directionSides.put(new Point(0, 1), SIDE_NORTH);
        directionSides.put(new Point(1, 0), SIDE_EAST);
        directionSides.put(new Point(0, -1), SIDE_SOUTH);
    }

    public static final int POSITION_LEFT = 0;
    public static final int POSITION_CENTER = 1;
    public static final int POSITION_RIGHT = 2;


    private static final float[] gateCoords = {0.0833333f, 0.25f, 0.41666667f, 0.58333333f, 0.75f, 0.916666667f};
    private static final float defaultWidth = 0.16666667f;
    private static final float defaultHeight = 0.16666667f;

    private float width = 1.0f;
    private float length = 1.0f;

    private float[] vertexArray;
    private float[] normalsArray;
    private float[] textureCoordArray;

    private Bitmap blockMap;
	private float[] centerPath;

    private ArrayList<float[]> triangles; // stores coords of triangles

    private float[] entranceCoords;
    private float[] exitCoords;
    private int numberOfExtraTurns;
    private int containsRoom;
    private int exitSide;
    private int entranceSide;
    private int exitPosition;
    private int entrancePosition;

    /*
        available parameters:
            in_point_1_x - if any of in points are not defined, use the bottom middle as the entrance (2,0); (3,0).
            in_point_1_y
            in_point_2_x
            in_point_2_y

            out_point_1_x - if any of out points are not defined, check with the available exit locations
            out_point_1_y
            out_point_2_x
            out_point_2_y

            out_west_available - indicates available sides of the block for the exit point
            out_east_available
            out_north_available
            out_south_available

            block_scheme - optional, specifies the scheme for the block

     */
    public MapBlock(HashMap<String, Integer> params){

        if(params.containsKey("entrance_side")){
            entranceSide = params.get("entrance_side");
        } else{
            entranceSide = SIDE_SOUTH;
        }
        if(params.containsKey("exit_side")){
            exitSide = params.get("exit_side");
        } else{
            exitSide = SIDE_NORTH;
        }
        //System.out.println("entr. side: " + entranceSide + "; exit side: " + exitSide);

        if(params.containsKey("entrance_position")){
            entrancePosition = params.get("entrance_position");
        } else{
            entrancePosition = POSITION_CENTER;
        }
        if(params.containsKey("exit_position")){
            exitPosition = params.get("exit_position");
        } else{
            exitPosition = POSITION_CENTER;
        }
        if(params.containsKey("extra_turns")){
            numberOfExtraTurns = params.get("extra_turns");
        } else{
            numberOfExtraTurns = 0;
        }
        if(params.containsKey("contains_room")){
            containsRoom = params.get("contains_room");
        } else{
            containsRoom = 0;
        }

        if(params.containsKey("in_point1_x") &&
                params.containsKey("in_point1_y") &&
                params.containsKey("in_point2_x") &&
                params.containsKey("in_point2_y")){

            entranceCoords = new float[4];
            entranceCoords[0] = params.get("in_point1_x");
            entranceCoords[1] = params.get("in_point1_y");
            entranceCoords[2] = params.get("in_point2_x");
            entranceCoords[3] = params.get("in_point2_y");

            if(entranceCoords[0] == 0 && entranceCoords[2] == 0){
                entranceSide = SIDE_WEST;
            } else if(entranceCoords[0] == 1 && entranceCoords[2] == 1){
                entranceSide = SIDE_EAST;
            } else if(entranceCoords[1] == 0 && entranceCoords[3] == 0){
                entranceSide = SIDE_SOUTH;
            } else if(entranceCoords[1] == 1 && entranceCoords[3] == 1){
                entranceSide = SIDE_NORTH;
            }

        } else{
            switch(entranceSide){
                case SIDE_SOUTH:
                    entranceCoords = new float[]{gateCoords[4 - entrancePosition*2], 0f, gateCoords[5 - entrancePosition*2], 0f};
                    break;
                case SIDE_NORTH:
                    entranceCoords = new float[]{gateCoords[entrancePosition*2], 1f, gateCoords[1 + entrancePosition*2], 1f};
                    break;
                case SIDE_WEST:
                    entranceCoords = new float[]{0f, gateCoords[entrancePosition*2], 0f, gateCoords[1 + entrancePosition*2]};
                    break;
                case SIDE_EAST:
                    entranceCoords = new float[]{1f, gateCoords[4 - entrancePosition*2], 1f, gateCoords[5 - entrancePosition*2]};
                    break;
            }
        }

        if(params.containsKey("out_point1_x") &&
                params.containsKey("out_point1_y") &&
                params.containsKey("out_point2_x") &&
                params.containsKey("out_point2_y")){

            exitCoords = new float[4];
            exitCoords[0] = params.get("out_point1_x");
            exitCoords[1] = params.get("out_point1_y");
            exitCoords[2] = params.get("out_point2_x");
            exitCoords[3] = params.get("out_point2_y");

            if(entranceCoords[0] == 0 && entranceCoords[2] == 0){
                exitSide = SIDE_WEST;
            } else if(entranceCoords[0] == 1 && entranceCoords[2] == 1){
                exitSide = SIDE_EAST;
            } else if(entranceCoords[1] == 0 && entranceCoords[3] == 0){
                exitSide = SIDE_SOUTH;
            } else if(entranceCoords[1] == 1 && entranceCoords[3] == 1){
                exitSide = SIDE_NORTH;
            }

        } else{
            switch(exitSide){
                case SIDE_SOUTH:
                    exitCoords = new float[]{gateCoords[4 - exitPosition*2], 0f, gateCoords[5 - exitPosition*2], 0f};
                    break;
                case SIDE_NORTH:
                    exitCoords = new float[]{gateCoords[exitPosition*2], 1f, gateCoords[1 + exitPosition*2], 1f};
                    break;
                case SIDE_WEST:
                    exitCoords = new float[]{0f, gateCoords[exitPosition*2], 0f, gateCoords[1 + exitPosition*2]};
                    break;
                case SIDE_EAST:
                    exitCoords = new float[]{1f, gateCoords[4 - exitPosition*2], 1f, gateCoords[5 - exitPosition*2]};
                    break;
            }
        }
        buildPath();
        normalsArray = MapBlock.getTriangleNormals(vertexArray);
    }


    /**
     * Once entrance/exit coords are set up, this method fills in the vertex coords of the path connecting them
     */
    private void buildPath(){

        blockMap = Bitmap.createBitmap(12, 12, Bitmap.Config.ARGB_8888);
        //blockMap.setHasAlpha(true);
        Canvas canvas = new Canvas(blockMap);
        Paint paint = new Paint();
	    paint.setStyle(Paint.Style.FILL_AND_STROKE);
	    paint.setStrokeWidth(0);
        paint.setColor(0xFFAAAAAA);
        canvas.drawColor(0x00000000);

        boolean straightBlock = (entranceSide + exitSide) % 2 == 0; // if entrance and exit sides are opposide to each other
        if(straightBlock){
            int shift = entrancePosition - (2 - exitPosition); // <0 - left shift, >0 - right shift, =0 - no shift

            if(shift == 0){
                // just connect entrance and exit with a straight tunnel
                float offset = (2*(2 - entrancePosition) + 0.5f)*defaultWidth;

	            centerPath = new float[]{offset + defaultWidth/2, 0, 0, offset + defaultWidth/2, 1, 0};

                float[][] coords = new float[12][3]; // list of all vertices. 3 coords per vertex
                coords[0] = new float[]{0, 0, defaultHeight};
                coords[1] = new float[]{offset, 0f, defaultHeight};
                coords[2] = new float[]{offset, 1f, defaultHeight};
                coords[3] = new float[]{0, 1f, defaultHeight};
                coords[4] = new float[]{1, 1, defaultHeight};
                coords[5] = new float[]{offset + defaultWidth, 1f, defaultHeight};
                coords[6] = new float[]{offset + defaultWidth, 0f, defaultHeight};
                coords[7] = new float[]{1f, 0f, defaultHeight};
                coords[8] = new float[]{coords[1][0], coords[1][1], 0};
                coords[9] = new float[]{coords[2][0], coords[2][1], 0};
                coords[10] = new float[]{coords[5][0], coords[5][1], 0};
                coords[11] = new float[]{coords[6][0], coords[6][1], 0};

                int[] order = new int[]{0, 1, 2,    // triangle 1
                                        0, 2, 3,    // triangle 2
                                        4, 5, 6,    // triangle 3
                                        4, 6, 7,    // triangle 4
                                        8, 11, 10,  // triangle 5
                                        8, 10, 9,   // triangle 6
                                        1, 9, 2,    // triangle 7
                                        1, 8, 9,    // triangle 8
                                        6, 5, 10,   // triangle 9
                                        6, 10, 11,  // triangle 10
                                        };

                float rotationAngle = 0;
                switch(entranceSide){
                    case SIDE_SOUTH:
                        break;
                    case SIDE_EAST:
                        rotationAngle = 90;
	                    break;
                    case SIDE_NORTH:
                        rotationAngle = 180;
	                    break;
                    case SIDE_WEST:
                        rotationAngle = 270;
	                    break;
                }
                coords = MapBlock.rotateCoords(coords, rotationAngle, 0.5f, 0.5f);
	            centerPath = MapBlock.rotateCoords(centerPath, 3, rotationAngle, 0.5f, 0.5f);
                // if vector product is negative, invert the triangle direction
                vertexArray = MapBlock.generateVertexCoords(coords, order, false);

                int offsetInt = 1 + (2 - entrancePosition)*4;

                canvas.save();
                canvas.rotate(-rotationAngle, (float)blockMap.getWidth()/2, (float)blockMap.getHeight()/2);
                canvas.drawRect(offsetInt, 0, offsetInt + 2, blockMap.getHeight(), paint);
                canvas.restore();
                //System.out.println("straight block: no shift. Offset: " + offsetInt);
            } else{ // straight path with a shift
                float[][] coords = new float[22][];

	            centerPath = new float[]{
			            defaultWidth, 0, 0,
			            defaultWidth, 0.5f, 0,
			            defaultWidth*(1 + Math.abs(shift)*2), 0.5f, 0,
                        defaultWidth*(1 + Math.abs(shift)*2), 1, 0
	            };

	            coords[0] = new float[]{0, 0, defaultHeight};
                coords[1] = new float[]{defaultWidth/2, 0, defaultHeight};
                coords[2] = new float[]{coords[1][0], 0.5f + defaultWidth/2, defaultHeight};
                coords[3] = new float[]{0, coords[2][1], defaultHeight};

                coords[6] = new float[]{0, 1, defaultHeight};
                coords[7] = new float[]{defaultWidth*3/2, 0, defaultHeight};
                coords[8] = new float[]{1, 0, defaultHeight};
                coords[9] = new float[]{1, 0.5f - defaultWidth/2, defaultHeight};
                coords[10] = new float[]{coords[7][0], coords[9][1], defaultHeight};

                coords[12] = new float[]{1, 1, defaultHeight};

                if(Math.abs(shift) == 1){
                    coords[4] = new float[]{0.5f - defaultWidth/2, 0.5f + defaultWidth/2, defaultHeight};
                    coords[5] = new float[]{coords[4][0], 1, defaultHeight};
                    coords[11] = new float[]{0.5f + defaultWidth/2, coords[9][1], defaultHeight};
                } else if(Math.abs(shift) == 2){
                    coords[4] = new float[]{1f - defaultWidth*3/2, 0.5f + defaultWidth/2, defaultHeight};
                    coords[5] = new float[]{coords[4][0], 1, defaultHeight};
                    coords[11] = new float[]{1f - defaultWidth/2, coords[9][1], defaultHeight};
                }

                coords[13] = new float[]{coords[11][0], 1, defaultHeight};
                coords[14] = new float[]{coords[1][0], coords[1][1], 0};
                coords[15] = new float[]{coords[2][0], coords[2][1], 0};
                coords[16] = new float[]{coords[4][0], coords[4][1], 0};
                coords[17] = new float[]{coords[5][0], coords[5][1], 0};
                coords[18] = new float[]{coords[13][0], coords[13][1], 0};
                coords[19] = new float[]{coords[11][0], coords[11][1], 0};
                coords[20] = new float[]{coords[10][0], coords[10][1], 0};
                coords[21] = new float[]{coords[7][0], coords[7][1], 0};


                int[] order = new int[]{
                        0, 1, 2,    // triangle 1
                        0, 2, 3,    // triangle 2
                        3, 4, 5,    // triangle 3
                        3, 5, 6,
                        7, 8, 9,
                        7, 9, 10,
                        11, 9, 12,
                        11, 12, 13,
                        14, 21, 20,
                        14, 20, 15,
                        15, 20, 19,
                        15, 19, 16,
                        16, 19, 18,
                        16, 18, 17,
                        1, 15, 2,
                        1, 14, 15,
                        2, 15, 16,
                        2, 16, 4,
                        4, 16, 17,
                        4, 17, 5,
                        13, 18, 19,
                        13, 19, 11,
                        11, 19, 20,
                        11, 20, 10,
                        10, 20, 21,
                        10, 21, 7
                };

                boolean mirror = shift < 0; // mirror the coords if the turn is left

                float offset = 0;
                int offsetInt = 0;
                if(Math.abs(shift) == 1 && entrancePosition == POSITION_CENTER){
                    offset = defaultWidth*2*shift;
                    offsetInt = 4*shift;
                }

                float rotationAngle = 0;
                switch(entranceSide){
                    case SIDE_SOUTH:
                        break;
                    case SIDE_EAST:
                        rotationAngle = 90;
                        break;
                    case SIDE_NORTH:
                        rotationAngle = 180;
                        break;
                    case SIDE_WEST:
                        rotationAngle = 270;
                        break;
                }
                boolean reverseTriangleOrientation = false;
                if(mirror) {
                    reverseTriangleOrientation = true;
                    MapBlock.mirrorCoordsY(coords, 0.5f);
	                MapBlock.mirrorCoordsY(centerPath, 3, 0.5f);
                }
                if(offset != 0) {
                    int[] coordNumsToOffset = new int[]{1, 2, 4, 5, 7, 10, 11, 13, 14, 15, 16, 17, 18, 19, 20, 21};
                    MapBlock.offsetCoords(coords, offset, 0f, coordNumsToOffset);
	                MapBlock.offsetCoords(centerPath, 3, offset, 0f, null);
                }
                if(rotationAngle != 0) {
	                coords = MapBlock.rotateCoords(coords, rotationAngle, 0.5f, 0.5f);
	                centerPath = MapBlock.rotateCoords(centerPath, 3, rotationAngle, 0.5f, 0.5f);
                }
                vertexArray = MapBlock.generateVertexCoords(coords, order, reverseTriangleOrientation);

                // draw the block map
                canvas.save();
	            if(rotationAngle != 0) {
		            canvas.rotate(-rotationAngle, (float)blockMap.getWidth()/2, (float)blockMap.getHeight()/2);
	            }
	            if(offset != 0){
		            canvas.translate(offsetInt, 0);
	            }
	            if(mirror) {
	                canvas.scale(-1, 1, (float)blockMap.getWidth()/2, 0);
                }


                canvas.drawRect(1, 5, 3, blockMap.getHeight(), paint);
                if(Math.abs(shift) == 1){
                    canvas.drawRect(5, 0, 7, 7, paint);
                    canvas.drawRect(3, 5, 5, 7, paint);
                } else if(Math.abs(shift) == 2){
                    canvas.drawRect(9, 0, 11, 7, paint);
                    canvas.drawRect(3, 5, 9, 7, paint);
                }
                canvas.restore();
                //System.out.println("straight block: shift: " + shift + ", offset: " + offsetInt);
            }

        } else{
            // connect entrance and exit with a turning tunnel
            Point entrDirection = sideDirections.get(entranceSide);
            Point exitDirection = sideDirections.get(exitSide);
            // turn direction = vector product. +1 - right Turn, -1 - left Turn.
            float turnDirection = entrDirection.x*exitDirection.y - entrDirection.y*exitDirection.x;
            //System.out.println(turnDirection > 0 ? "right turn" : "left turn" );
            //System.out.println("block with a " + (turnDirection > 0 ? "right" : "left") + " turn");
            int entrLength = 0;
            int exitLength = 0;

            if(turnDirection > 0){ // right turn
                entrLength = 2 - exitPosition;
                exitLength = entrancePosition;
            } else{ // left turn
                entrLength = exitPosition;
                exitLength = 2 - entrancePosition;
            }

	        centerPath = new float[]{
			        (1 + (2 - exitLength)*2)*defaultWidth, 0, 0,
			        (1 + (2 - exitLength)*2)*defaultWidth, (1 + entrLength*2)*defaultWidth, 0,
			        1, (1 + entrLength*2)*defaultWidth, 0
	        };

            float[][] coords = new float[17][];
            coords[0] = new float[]{0, 0, defaultHeight};
            coords[1] = new float[]{(0.5f + (2-exitLength)*2)*defaultWidth, 0, defaultHeight};
            coords[2] = new float[]{coords[1][0], (1.5f + entrLength*2)*defaultWidth, defaultHeight};
            coords[3] = new float[]{0, coords[2][1], defaultHeight};
            coords[4] = new float[]{1, coords[2][1], defaultHeight};
            coords[5] = new float[]{1, 1, defaultHeight};
            coords[6] = new float[]{0, 1, defaultHeight};
            coords[7] = new float[]{coords[1][0] + defaultWidth, 0, defaultHeight};
            coords[8] = new float[]{1, 0, defaultHeight};
            coords[9] = new float[]{1, (0.5f + entrLength*2)*defaultWidth, defaultHeight};
            coords[10] = new float[]{coords[7][0], coords[9][1], defaultHeight};
            coords[11] = new float[]{coords[1][0], coords[1][1], 0};
            coords[12] = new float[]{coords[2][0], coords[2][1], 0};
            coords[13] = new float[]{coords[4][0], coords[4][1], 0};
            coords[14] = new float[]{coords[9][0], coords[9][1], 0};
            coords[15] = new float[]{coords[10][0], coords[10][1], 0};
            coords[16] = new float[]{coords[7][0], coords[7][1], 0};

            int[] order = new int[]{
                    0, 1, 2,
                    0, 2, 3,
                    3, 4, 5,
                    3, 5, 6,
                    7, 8, 9,
                    7, 9, 10,
                    11, 16, 15,
                    11, 15, 12,
                    12, 15, 14,
                    12, 14, 13,
                    1, 11, 12,
                    1, 12, 2,
                    2, 12, 13,
                    2, 13, 4,
                    9, 14, 15,
                    9, 15, 10,
                    10, 15, 16,
                    10, 16, 7
            };
            boolean reverseTriangleOrientation = false;
            if(turnDirection == -1){
                reverseTriangleOrientation = true;
                MapBlock.mirrorCoordsY(coords, 0.5f);
                MapBlock.mirrorCoordsY(centerPath, 3, 0.5f);
            }
            float rotationAngle = 0;
            switch(entranceSide){
                case SIDE_SOUTH:
                    break;
                case SIDE_EAST:
                    rotationAngle = 90;
                    break;
                case SIDE_NORTH:
                    rotationAngle = 180;
                    break;
                case SIDE_WEST:
                    rotationAngle = 270;
                    break;
            }
            if(rotationAngle != 0) {
                //System.out.println("rotation angle: " + rotationAngle);
                coords = MapBlock.rotateCoords(coords, rotationAngle, 0.5f, 0.5f);
	            centerPath = MapBlock.rotateCoords(centerPath, 3, rotationAngle, 0.5f, 0.5f);
            }
            vertexArray = MapBlock.generateVertexCoords(coords, order, reverseTriangleOrientation);

            // draw the block map
            canvas.save();
            if(turnDirection == -1) {
                canvas.scale(-1, 1, (float)blockMap.getWidth()/2, 0);
            }
            if(rotationAngle != 0) {
                canvas.rotate(-rotationAngle*turnDirection, (float)blockMap.getWidth()/2, (float)blockMap.getHeight()/2);
            }
            canvas.drawRect(1 + (2-exitLength)*4, 1 + (2 - entrLength)*4, 3 + (2-exitLength)*4, blockMap.getHeight(), paint);
            canvas.drawRect(3 + (2-exitLength)*4, 1 + (2 - entrLength)*4, blockMap.getWidth(), 3 + (2 - entrLength)*4, paint);
            canvas.restore();
            //System.out.println("Turning block. " + (turnDirection == 1 ? "Right " : "Left ") + "turn. Entr. length: " + entrLength + ", exit length: " + exitLength);
        }
/*
	    for(int y = 0; y < blockMap.getHeight(); y++){
		    System.out.print("line " + y + ":\t");
		    for(int x = 0; x < blockMap.getWidth(); x++){
			    System.out.print((Color.alpha(blockMap.getPixel(x, y)) > 0 ? "x " : "- "));
		    }
		    System.out.println();
	    }
*/
	    /*
	    byte[] mapArray = getPixels(blockMap);
	    int mapWidth = blockMap.getWidth();
        for(int px = 0; px < mapArray.length; px++){
	        if(px % mapWidth == 0){
		        System.out.println();
	        }
	        System.out.print(mapArray[px] > 0 ? "x" : "-");
        }
	    System.out.println();
	     */
    }

    public static float[] generateVertexCoords(float[][] coords, int[] order, boolean swapTriangleDirection){
        float[] vertexCoords = new float[order.length*9];
        for(int triangleNum = 0; triangleNum < order.length/3; triangleNum++){
            vertexCoords[triangleNum*9] = coords[order[triangleNum*3]][0];
            vertexCoords[triangleNum*9 + 1] = coords[order[triangleNum*3]][1];
            vertexCoords[triangleNum*9 + 2] = coords[order[triangleNum*3]][2];

            if(!swapTriangleDirection){
                vertexCoords[triangleNum*9 + 3] = coords[order[triangleNum*3 + 1]][0];
                vertexCoords[triangleNum*9 + 4] = coords[order[triangleNum*3 + 1]][1];
                vertexCoords[triangleNum*9 + 5] = coords[order[triangleNum*3 + 1]][2];

                vertexCoords[triangleNum*9 + 6] = coords[order[triangleNum*3 + 2]][0];
                vertexCoords[triangleNum*9 + 7] = coords[order[triangleNum*3 + 2]][1];
                vertexCoords[triangleNum*9 + 8] = coords[order[triangleNum*3 + 2]][2];
            } else{
                vertexCoords[triangleNum*9 + 3] = coords[order[triangleNum*3 + 2]][0];
                vertexCoords[triangleNum*9 + 4] = coords[order[triangleNum*3 + 2]][1];
                vertexCoords[triangleNum*9 + 5] = coords[order[triangleNum*3 + 2]][2];

                vertexCoords[triangleNum*9 + 6] = coords[order[triangleNum*3 + 1]][0];
                vertexCoords[triangleNum*9 + 7] = coords[order[triangleNum*3 + 1]][1];
                vertexCoords[triangleNum*9 + 8] = coords[order[triangleNum*3 + 1]][2];
            }
        }
        return vertexCoords;
    }


    public float[] getVertexArray() {
        return vertexArray;
    }

    public float[] getNormalsArray() {
        return normalsArray;
    }

    public static float[] getGateCoords() {
        return gateCoords;
    }

    public int getExitSide() {
        return exitSide;
    }

    public int getExitPosition() {
        return exitPosition;
    }

    public float[] getExitCoords() {
        return exitCoords;
    }

    public int getEntranceSide() {
        return entranceSide;
    }

    public int getEntrancePosition() {
        return entrancePosition;
    }

    public float[] getEntranceCoords() {
        return entranceCoords;
    }

    public static float getDefaultWidth() {
        return defaultWidth;
    }

    public static float getDefaultHeight() {
        return defaultHeight;
    }

    public int getContainsRoom() {
        return containsRoom;
    }

    public int getNumberOfExtraTurns() {
        return numberOfExtraTurns;
    }

    public Bitmap getBlockMap() {
        return blockMap;
    }

    public static float[] getTriangleNormals(float[] vertexCoords){
        float[] normals = new float[vertexCoords.length];
        for(int triangleNum = 0; triangleNum < vertexCoords.length/9; triangleNum++){
            float[] normal = getNormalVector(Arrays.copyOfRange(vertexCoords, 9*triangleNum, 9*(triangleNum+1)));
            for (int j = 0; j < 3; j++) {
                normals[9 * triangleNum + 3 * j] = normal[0];
                normals[9 * triangleNum + 3 * j + 1] = normal[1];
                normals[9 * triangleNum + 3 * j + 2] = normal[2];
            }
        }
        return normals;
    }

    /**
     *
     * @param triangleVertices - array with triangle vertex coordinates (total size 9, 3 per vertex)
     *                         arranged in the counter-clockwise order
     * @return normal to the plane of the triangle pointing outside of the triangle
     */
    public static float[] getNormalVector(float[] triangleVertices){
        float[] normal = new float[3];
        float[] vector1 = new float[3];
        float[] vector2 = new float[3];

        vector1[0] = triangleVertices[3] - triangleVertices[0];
        vector1[1] = triangleVertices[4] - triangleVertices[1];
        vector1[2] = triangleVertices[5] - triangleVertices[2];

        vector2[0] = triangleVertices[6] - triangleVertices[0];
        vector2[1] = triangleVertices[7] - triangleVertices[1];
        vector2[2] = triangleVertices[8] - triangleVertices[2];

        normal[0] = vector1[1] * vector2[2] - vector1[2] * vector2[1];
        normal[1] = vector1[2] * vector2[0] - vector1[0] * vector2[2];
        normal[2] = vector1[0] * vector2[1] - vector1[1] * vector2[0];

        float magnitude = (float)Math.sqrt(normal[0]*normal[0] + normal[1]*normal[1] + normal[2]*normal[2]);
        normal[0] /= magnitude;
        normal[1] /= magnitude;
        normal[2] /= magnitude;

        return normal;
    }

    public static void mirrorCoordsY(float[][] coords, float x0){
        for(int i = 0; i < coords.length; i++){
            coords[i][0] = 2*x0 - coords[i][0];
        }
    }

	public static void mirrorCoordsY(float[] coords, int stride, float x0){
		for(int i = 0; i < coords.length/stride; i++){
			coords[i*stride] = 2*x0 - coords[i*stride];
		}
	}

    /**
     *
     * @param coords
     * @param x0
     * @param y0
     * @param coordNums - if null, all coords are offset. If not null, contains list of indices
     *                  corresponding to the coords to be moved
     */
    public static void offsetCoords(float[][] coords, float x0, float y0, int[] coordNums){
        if(coordNums != null){
            for (int i = 0; i < coordNums.length; i++) {
                int coordNum = coordNums[i];
                    coords[coordNum][0] += x0;
                    coords[coordNum][1] += y0;
            }
        } else {
            for (int i = 0; i < coords.length; i++) {
                coords[i][0] += x0;
                coords[i][1] += y0;
            }
        }
    }

	public static void offsetCoords(float[] coords, int stride, float x0, float y0, int[] coordNums){
		if(coordNums != null){
			for (int i = 0; i < coordNums.length; i++) {
				int coordNum = coordNums[i];
				coords[coordNum*stride] += x0;
				coords[coordNum*stride + 1] += y0;
			}
		} else {
			for (int i = 0; i < coords.length/stride; i++) {
				coords[i*stride] += x0;
				coords[i*stride + 1] += y0;
			}
		}
	}

    public static float[][] rotateCoords(float[][] coords, float angle, float x0, float y0){
        float[][] result = new float[coords.length][3];

        float[] rotationMatrix = new float[16];
        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.rotateM(rotationMatrix, 0, angle, 0f, 0f, 1f);

        float[] rotatedCoord = new float[4];
        float[] coord = new float[4];
        for(int i = 0; i < coords.length; i++){
            coord[0] = coords[i][0] - x0;
            coord[1] = coords[i][1] - y0;
            coord[2] = coords[i][2];
            Matrix.multiplyMV(rotatedCoord, 0, rotationMatrix, 0, coord, 0);
            result[i][0] = rotatedCoord[0] + x0;
            result[i][1] = rotatedCoord[1] + y0;
            result[i][2] = rotatedCoord[2];
        }
        return result;
    }

	public static float[] rotateCoords(float[] coords, int stride, float angle, float x0, float y0){
        float[] result = new float[coords.length];

        float[] rotationMatrix = new float[16];
        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.rotateM(rotationMatrix, 0, angle, 0f, 0f, 1f);

        float[] rotatedCoord = new float[4];
        float[] coord = new float[4];
        for(int i = 0; i < coords.length/stride; i++){
            System.arraycopy(coords, i*stride, coord, 0, stride);
            coord[0] -= x0;
            coord[1] -= y0;
            Matrix.multiplyMV(rotatedCoord, 0, rotationMatrix, 0, coord, 0);
            rotatedCoord[0] += x0;
            rotatedCoord[1] += y0;
            System.arraycopy(rotatedCoord, 0, result, i*stride, stride);
        }
        return result;
	}

    /**
     * gateEdges should have size of 4
     * @return
     */
    public static float[] rearrangeGateEdges(float[] gateEdges){
        float[] v1 = new float[]{gateEdges[0] - 0.5f, gateEdges[1] - 0.5f};
        float[] v2 = new float[]{gateEdges[2] - 0.5f, gateEdges[3] - 0.5f};
        float vectorProduct = v1[0]*v2[1] - v1[1]*v2[0];
        float[] result = new float[4];
        if(vectorProduct < 0){
            result[0] = gateEdges[2];
            result[1] = gateEdges[3];
            result[2] = gateEdges[0];
            result[3] = gateEdges[1];
        } else{
            result = Arrays.copyOf(gateEdges, gateEdges.length);
        }
        return result;
    }

    public static int oppositeSide(int side){
        if(side == MapBlock.SIDE_SOUTH){
            return MapBlock.SIDE_NORTH;
        } else if(side == MapBlock.SIDE_NORTH){
            return MapBlock.SIDE_SOUTH;
        } else if(side == MapBlock.SIDE_EAST){
            return MapBlock.SIDE_WEST;
        } else if(side == MapBlock.SIDE_WEST){
            return MapBlock.SIDE_EAST;
        }
        return -1;
    }

    /**
     *
     * @param x = [0,1]
     * @param y = [0,1]
     * @return 0x00 if there is a wall, 0xFF if there is a not a wall
     */
    public int getBlockMapPixelValue(float x, float y){
        return blockMap.getPixel((int)(x*blockMap.getWidth()), (int)((1f-y)*blockMap.getHeight()));
    }

    /**
     *
     * @param normalizedCoord - local coordinate within the block. Should be within [0,1).
     * @return
     */
    public int getMapValue(PointF normalizedCoord){
        Point coord = new Point((int)(normalizedCoord.x*blockMap.getWidth()), Math.min(blockMap.getHeight() - 1, (int)((1-normalizedCoord.y)*blockMap.getHeight())));
        return Color.alpha(blockMap.getPixel(coord.x, coord.y));
    }

    /**
     *
     * @param  - local coordinate within the block. Should be within [0,1).
     * @return
     */
    public int getMapValue(float x, float y){
        Point coord = new Point((int)(x*blockMap.getWidth()), Math.min(blockMap.getHeight() - 1, (int)((1-y)*blockMap.getHeight())));
        return Color.alpha(blockMap.getPixel(coord.x, coord.y));
    }

	public static byte[] getPixels(Bitmap bmp) {
		int bytes = bmp.getRowBytes() * bmp.getHeight();
		ByteBuffer buffer = ByteBuffer.allocate(bytes);
		bmp.copyPixelsToBuffer(buffer);
		return buffer.array();
	}

	private float centerPathLength;
    public float[] generateNPCLocation(float pathFraction){
	    float[] location = new float[3];

		if(centerPathLength == 0){
			for(int i = 1; i < centerPath.length/3; i++){
				centerPathLength += Math.sqrt(
						Math.pow(centerPath[3*i] - centerPath[3*(i-1)], 2) +
						Math.pow(centerPath[3*i+1] - centerPath[3*(i-1)+1], 2) +
						Math.pow(centerPath[3*i+2] - centerPath[3*(i-1)+2], 2));
			}
		}

	    float currLength = 0;
	    for(int i = 1; i < centerPath.length/3 && currLength < centerPathLength*pathFraction; i++){
		    float[] nextVector = new float[]{
				    centerPath[3*i] - centerPath[3*(i-1)],
				    centerPath[3*i+1] - centerPath[3*(i-1)+1],
				    centerPath[3*i+2] - centerPath[3*(i-1)+2]
		    };
		    float nextLength = MyUtilities.vectorLength(nextVector, 3);
		    if(currLength + nextLength > pathFraction*centerPathLength){
				System.arraycopy(centerPath, (i-1)*3, location, 0, 3);
			    float lengthFraction = (pathFraction*centerPathLength - currLength)/nextLength;
			    location[0] += nextVector[0]*lengthFraction;
			    location[1] += nextVector[1]*lengthFraction;
			    location[2] += nextVector[2]*lengthFraction;
		    } else{
                System.arraycopy(centerPath, i*3, location, 0, 3);
            }
		    currLength +=nextLength;
	    }
        //System.out.println("location: (" + location[0] + ", " + location[1] + ", " + location[2] + ");");

        location[0] += blockPosition.x;
        location[1] += blockPosition.y;
	    return location;
    }

    public float[] getCenterPath(){
        float[] result = new float[centerPath.length];
        System.arraycopy(centerPath, 0, result, 0, centerPath.length);


        //System.out.println("center path:");
        for(int i = 0; i < result.length/3; i++) {
            result[3 * i] += blockPosition.x;
            result[3 * i + 1] += blockPosition.y;
            //System.out.println("(" + centerPath[3 * i] + ", " + centerPath[3 * i + 1] + ", " + centerPath[3 * i + 2] + ");");
        }

        return result;
    }

    public float getWidth() {
        return width;
    }

    public float getLength() {
        return length;
    }
}
