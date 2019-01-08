package batalov.ivan.opengltest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import batalov.ivan.opengltest.light_parameters.DirectLightParameters;
import batalov.ivan.opengltest.light_parameters.PointDirLightParameters;
import batalov.ivan.opengltest.light_parameters.PointLightParameters;
import batalov.ivan.opengltest.models.Arrow;
import batalov.ivan.opengltest.models.LevelMap;
import batalov.ivan.opengltest.models.MapBlock;
import batalov.ivan.opengltest.models.Model3D;
import batalov.ivan.opengltest.models.NPC;
import batalov.ivan.opengltest.models.NPCGenerator;
import batalov.ivan.opengltest.models.RectangularFrame;
import batalov.ivan.opengltest.models.SquareHall;
import batalov.ivan.opengltest.shaders.BulletShaderProgram;
import batalov.ivan.opengltest.shaders.ColoredShapesShaderProgram;
import batalov.ivan.opengltest.shaders.ComprehensiveShaderProgram;
import batalov.ivan.opengltest.shaders.DiplayPointDepthMapShaderProgram;
import batalov.ivan.opengltest.shaders.DisplaySimpleDepthMapShaderProgram;
import batalov.ivan.opengltest.shaders.DisplayTextureShaderProgram;
import batalov.ivan.opengltest.shaders.OverlayTexturesProgram;
import batalov.ivan.opengltest.shaders.GaussianBlurProgram;
import batalov.ivan.opengltest.shaders.PointDepthMapShaderProgram;
import batalov.ivan.opengltest.shaders.PointShadowShaderProgram;
import batalov.ivan.opengltest.shaders.ShaderProgram;
import batalov.ivan.opengltest.shaders.SimpleDepthMapShaderProgram;
import batalov.ivan.opengltest.shaders.SimpleShadowShaderProgram;
import batalov.ivan.opengltest.shaders.TextShaderProgram;


/**
 * Created by ivan on 10/8/17.
 */

public class MyGLRenderer implements GLSurfaceView.Renderer {

    public static final int PROGRAM_DRAW_SCENE_SIMPLE_SHADOW = 0;
    public static final int PROGRAM_DRAW_SCENE_POINT_SHADOW = 1;
    public static final int PROGRAM_DRAW_2D_DEPTH_BUFFER = 2;
    public static final int PROGRAM_DRAW_CUBEMAP_DEPTH_BUFFER = 3;

    public static int SHADOW_WIDTH = 0;
    public static int SHADOW_HEIGHT = 0;

    private SquareHall mSquareHall;
    private LevelMap levelMap;
    private OpenGLTextWriter fpsText;

	HashMap<String, Buffer> testPathBuffers;
	float[] testPathColor = new float[]{68f/255f, 1f, 121f/255f, 1f};

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mModelMatrix = new float[16];
	private final float[] normalMatrix = new float[16];

    private ShaderProgram simpleShadowProgram;
    private ShaderProgram simpleShadowMapProgram;
    private ShaderProgram displaySimpleShadowProgram;
    private ShaderProgram pointShadowProgram;
    private ShaderProgram pointShadowMapProgram;
    private ShaderProgram displayPointShadowProgram;
    private ShaderProgram textDrawingShaderProgram;
	private ShaderProgram bulletShaderProgram;
	private ShaderProgram blurProgram;
	private ShaderProgram coloredShapesProgram;
	private ShaderProgram overlayTexturesProgram;
	private ShaderProgram displayTextureProgram;

	private ShaderProgram comprehensiveShaderProgram;

	HashMap<String, Buffer> displaySimpleShadowBuffers = new HashMap<>();
	HashMap<String, Buffer> displayPointShadowBuffers = new HashMap<>();


	Model3D sphereModel;
	float sphereRadius = MapBlock.getDefaultWidth()/3;
	ArrayList<float[]> spherePerimeter = new ArrayList<float[]>(); // depending on the point of view, contains 100 points along the perimeter of the sphere's projection
	ArrayList<float[]> activePerimeter;

	Model3D bullet;
	float bulletSize = 0.01f;
	Model3D bulletTrace;
	float bulletTraceMaxLength = 0.5f;

	ArrayList<BulletParameters> listOfBullets = new ArrayList<>();

	float[] lightPosition = new float[]{0f, 0f, 5f};
	float[] characterPosition = new float[4];
	float[] viewPosition = new float[]{0.0f, 0f, 1};

	DirectLightParameters dirLight;
	PointLightParameters envLight;
	PointDirLightParameters flashLight;

	RectangularFrame moveAreaRect;
	Arrow moveArrow;

	ArrayList<NPC> npcList;

    private float[] shadowMapVertices = new float[]{
            -1f, 1f, 0f,
            1f, 1f, 0f,
            -1f, -1f, 0f,
            1f, 1f, 0f,
            1f, -1f, 0f,
            -1f, -1f, 0f,
    };

    private float[] shadowMapTexCoords = new float[]{
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 1f,
            1f, 0f,
            0f, 0f,
    };

    float skyboxVertices[] = {
            // positions
            -1.0f,  1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f,  1.0f, -1.0f,
            -1.0f,  1.0f, -1.0f,

            -1.0f, -1.0f,  1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f,  1.0f, -1.0f,
            -1.0f,  1.0f, -1.0f,
            -1.0f,  1.0f,  1.0f,
            -1.0f, -1.0f,  1.0f,

            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            1.0f,  1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,

            -1.0f, -1.0f,  1.0f,
            -1.0f,  1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            1.0f, -1.0f,  1.0f,
            -1.0f, -1.0f,  1.0f,

            -1.0f,  1.0f, -1.0f,
            1.0f,  1.0f, -1.0f,
            1.0f,  1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            -1.0f,  1.0f,  1.0f,
            -1.0f,  1.0f, -1.0f,

            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f,  1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f,  1.0f,
            1.0f, -1.0f,  1.0f
    };

    private Context context;
    public MyGLRenderer(Context context){
        super();

	    //moveAreaRect.color = new float[]{1.0f, 1.0f, 1.0f, 1.0f};

        if(context != null){
            //System.out.println("my GL renderer initializing");
            this.context = context;

            sphereModel = new Model3D(context, R.raw.sphere_1x1, false);
	        sphereModel.setScale(sphereRadius);
	        int perimeterPointCount = 16;
	        HashMap<Float, float[]> tempPerimeter = new HashMap<Float, float[]>();
	        for(float[] point : sphereModel.getVertexCoords()){
		        float dist = PointF.length(point[0], point[1]);
		        float cosA = point[0]/dist;
		        float sinA = point[1]/dist;
		        float angle;

		        if(sinA >= 0){
			        angle = (float) Math.acos(cosA);
		        } else{
			        angle = (float)(2*Math.PI - Math.acos(cosA));
		        }

		        angle = (float)(Math.floor(angle*perimeterPointCount/(2*Math.PI))*2*Math.PI/perimeterPointCount);
		        if(!tempPerimeter.containsKey(angle)){
			        tempPerimeter.put(angle, point);
		        } else{
			        float[] oldPoint = tempPerimeter.get(angle);
			        if(PointF.length(oldPoint[0], oldPoint[1]) < dist){
				        tempPerimeter.put(angle, point);
			        }
		        }
	        }
	        spherePerimeter.addAll(tempPerimeter.values());

			activePerimeter = getActivePerimeterPoints(0);
	        bullet = new Model3D(context, R.raw.bullet, false);
	        bullet.setScale(bulletSize/2);
	        bulletTrace = new Model3D(context, R.raw.bullet_trace, false);

	        float[] vertexArray = bulletTrace.getFloatArrays().get(ShaderProgram.VERTEX_COORD_ATTRIBUTE);
	        float[] colorArray = bulletTrace.getFloatArrays().get(ShaderProgram.VERTEX_COLOR_ATTRIBUTE);
	        for(int vertexNum = 0; vertexNum < vertexArray.length/3; vertexNum++){
		        colorArray[vertexNum*4 + 3] = (vertexArray[vertexNum*3 + 1] + 1)/2; // setting the alpha channel without moifying the RGB values
	        }

	        ByteBuffer bb4 = ByteBuffer.allocateDirect(colorArray.length * 4); // (number of coordinate values * 4 bytes per float)
	        bb4.order(ByteOrder.nativeOrder());
	        FloatBuffer colorsBuffer = bb4.asFloatBuffer();
	        colorsBuffer.put(colorArray);
	        colorsBuffer.position(0);
	        bulletTrace.getVertexBuffers().put(ShaderProgram.VERTEX_COLOR_ATTRIBUTE, colorsBuffer);

            //System.out.println("Sphere: vertices: " + sphereModel.vertexBuffer.capacity() + "; normals: " + sphereModel.normalBuffer.capacity() + "; texture coords: " + sphereModel.textureBuffer.capacity());
            //System.out.println("sphere model loaded");

        }
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Test OES_depth_texture extension
        String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
        if (extensions.contains("OES_depth_texture")) {
            System.out.println("OES depth texture supported");
            //mHasDepthTextureExtension = true;
        } else{
            System.out.println("OES depth texture NOT supported");
        }

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClearDepthf(1.0f);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        initializeShaderPrograms();

        //mSquareHall = new SquareHall(simpleShadowProgram.program, simpleShadowMapProgram.program, pointShadowProgram.program, pointShadowMapProgram.program);

        int numberOfBlocks = 60;
        ArrayList<Point> blockPositions = new ArrayList<>(numberOfBlocks);
        int[][] blockMap = new int[30][30];
        blockMap[15][0] = 1;
        blockPositions.add(new Point(15, 0));
        Random rand = new Random();
        shiftX = 15.5f;
        shiftY = MapBlock.getDefaultWidth()/2;

	    Matrix.setIdentityM(sphereModel.modelMatrix, 0);
	    Matrix.translateM(sphereModel.modelMatrix, 0, 15.5f, MapBlock.getDefaultWidth()/2, 0.1f);

        while(blockPositions.size() < numberOfBlocks){
            Point lastBlock = blockPositions.get(blockPositions.size() - 1);
            ArrayList<Point> availableBlocks = MyUtilities.getAvailableBlocks(blockMap, lastBlock, false, true);
            //System.out.println("available blocks: " + availableBlocks.size());
            Point nextBlock = availableBlocks.get(rand.nextInt(availableBlocks.size()));
            blockMap[nextBlock.x][nextBlock.y] = 1;
            blockPositions.add(nextBlock);
            //System.out.println("next point: ("  + nextBlock.x + ", " + nextBlock.y + ");");
        }
        //System.out.println("Done picking blocks!");

        levelMap = new LevelMap(blockPositions);
	    ArrayList<Integer> availableBlocks = new ArrayList<>();
	    availableBlocks.add(0);
	    availableBlocks.add(numberOfBlocks-1);
	    npcList = NPCGenerator.generateNPCs(context, 2, availableBlocks, levelMap.getBlocks());

	    //#######################################
	    float[] pos1 = npcList.get(0).position;
	    float[] pos2 = npcList.get(npcList.size() - 1).position;

	    long time1 = System.currentTimeMillis();
	    ArrayList<FloatArray> path = levelMap.findPath(pos1, pos2);
	    System.out.println("path finding time: " + (System.currentTimeMillis() - time1) + " ms.");
	    time1 = System.currentTimeMillis();
	    levelMap.constrainPath(path, npcList.get(0).size/2, npcList.get(0).size/10);
	    System.out.println("path constraining time: " + (System.currentTimeMillis() - time1) + " ms.");
	    time1 = System.currentTimeMillis();
	    levelMap.optimizePath(path, npcList.get(0).size/2);
	    System.out.println("path optimization time: " + (System.currentTimeMillis() - time1) + " ms.");
	    testPathBuffers = MyUtilities.generateLineBuffers(path);
	    float[][] pathArray = MyUtilities.listToArray2D(path, 3);
	    npcList.get(0).addMovementPattern(pathArray, 0, 1);


	    //######################################

        fpsText = new OpenGLTextWriter();
        fpsText.setTextPosition(-1f, 1f, OpenGLTextWriter.TOP_LEFT);
        fpsText.setTextSize(0.15f);
        fpsText.setTextColor(0xc9eeffFF);
        fpsText.setBackgroundColor(0x00000000);

        ByteBuffer bb = ByteBuffer.allocateDirect(shadowMapVertices.length * 4); // (number of coordinate values * 4 bytes per float)
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer mapDisplayVertexBuffer = bb.asFloatBuffer();
        mapDisplayVertexBuffer.put(shadowMapVertices);
        mapDisplayVertexBuffer.position(0);
	    displaySimpleShadowBuffers.put(ShaderProgram.VERTEX_COORD_ATTRIBUTE, mapDisplayVertexBuffer);

        ByteBuffer bb2 = ByteBuffer.allocateDirect(shadowMapTexCoords.length * 4); // (number of coordinate values * 4 bytes per float)
        bb2.order(ByteOrder.nativeOrder());
        FloatBuffer mapDisplayTexCoordBuffer = bb2.asFloatBuffer();
        mapDisplayTexCoordBuffer.put(shadowMapTexCoords);
        mapDisplayTexCoordBuffer.position(0);
	    displaySimpleShadowBuffers.put(ShaderProgram.TEXTURE_COORD_ATTRIBUTE, mapDisplayTexCoordBuffer);


	    ByteBuffer bb3 = ByteBuffer.allocateDirect(skyboxVertices.length * 4); // (number of coordinate values * 4 bytes per float)
        bb3.order(ByteOrder.nativeOrder());
        FloatBuffer pointMapDisplayVertexBuffer = bb3.asFloatBuffer();
        pointMapDisplayVertexBuffer.put(skyboxVertices);
        pointMapDisplayVertexBuffer.position(0);
	    displayPointShadowBuffers.put(ShaderProgram.VERTEX_COORD_ATTRIBUTE, pointMapDisplayVertexBuffer);

	    float strength = 1.0f;
	    dirLight = new DirectLightParameters();
	    dirLight.direction = new float[]{-1, -1, -2};
	    float dirLength = (float)Math.sqrt(Math.pow(dirLight.direction[0], 2) + Math.pow(dirLight.direction[1], 2) + Math.pow(dirLight.direction[2], 2));
	    dirLight.direction[0] /= dirLength;
	    dirLight.direction[1] /= dirLength;
	    dirLight.direction[2] /= dirLength;
	    dirLight.color = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
	    dirLight.ambient = 0.05f*strength;
	    dirLight.diffuse = 0.05f*strength;
	    dirLight.specular = 0.05f*strength;
	    dirLight.castShadow = 0;

	    strength = 5.0f;
	    envLight = new PointLightParameters();
	    envLight.color = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
	    envLight.ambient = 0.1f*strength;
	    envLight.diffuse = 0.6f*strength;
	    envLight.specular = 0.5f*strength;
	    envLight.decayCoeffs = new float[]{0.2f, 5f, 5f};
	    envLight.castShadow = 0;

	    strength = 3.5f;
	    flashLight = new PointDirLightParameters();
	    flashLight.color = new float[]{1.0f, 0.698f, 0.137f, 1.0f};
	    flashLight.ambient = 0.0f*strength;
	    flashLight.diffuse = 0.5f*strength;
	    flashLight.specular = 0.2f*strength;
	    flashLight.decayCoeffs = new float[]{1.0f, 2f, 2f};
	    flashLight.castShadow = 1;
    }

    private int[] depthMapFBO;
    private int[] depthMap;

    private void generateSimpleShadowFBOs(int count){
        // generate Frame Buffer Object
        depthMapFBO = new int[count];
        GLES20.glGenFramebuffers(count, depthMapFBO, 0);

        // generate depth map texture
        depthMap = new int[count];
        GLES20.glGenTextures(count, depthMap, 0);
		for(int i = 0; i < count; i++) {
			// configure depth map texture
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, depthMap[i]);
			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_DEPTH_COMPONENT, SHADOW_WIDTH, SHADOW_HEIGHT, 0, GLES20.GL_DEPTH_COMPONENT, GLES20.GL_UNSIGNED_INT, null);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

			// bind depth map texture to the FBO
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, depthMapFBO[i]);
			GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_TEXTURE_2D, depthMap[i], 0);

			int FBOstatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
			if (FBOstatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
				System.out.println("GL_FRAMEBUFFER_COMPLETE failed, CANNOT use FBO. Status: " + FBOstatus);
				throw new RuntimeException("GL_FRAMEBUFFER_COMPLETE failed, CANNOT use FBO");
			} else {
				System.out.println("Framebuffer " + i + " created!");
			}
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		}
    }

    private int[] pointDepthMapFBO;
    private int[] pointDepthMap;

    private void generatePointShadowFBO(){
        // generate Frame Buffer Object
        pointDepthMapFBO = new int[1];
        GLES20.glGenFramebuffers(1, pointDepthMapFBO, 0);

        // generate depth map texture
        pointDepthMap = new int[1];
        GLES20.glGenTextures(1, pointDepthMap, 0);

        // configure depth map texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, pointDepthMap[0]);

        for( int i = 0; i < 6; i++){
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GLES20.GL_RGBA, Math.max(SHADOW_WIDTH, SHADOW_HEIGHT), Math.max(SHADOW_WIDTH, SHADOW_HEIGHT), 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            //GLES20.glTexImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GLES20.GL_DEPTH_COMPONENT, Math.max(SHADOW_WIDTH, SHADOW_HEIGHT), Math.max(SHADOW_WIDTH, SHADOW_HEIGHT), 0, GLES20.GL_DEPTH_COMPONENT, GLES20.GL_UNSIGNED_INT, null);
        }

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        /////////////////////////////
        int[] depthMap = new int[1];
        GLES20.glGenTextures(1, depthMap, 0);

        // configure depth map texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, depthMap[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_DEPTH_COMPONENT, Math.max(SHADOW_WIDTH, SHADOW_HEIGHT), Math.max(SHADOW_WIDTH, SHADOW_HEIGHT), 0, GLES20.GL_DEPTH_COMPONENT, GLES20.GL_UNSIGNED_INT, null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        ////////////////////////////

        // bind depth map texture to the FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, pointDepthMapFBO[0]);
        //GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, pointDepthMap[0], 0);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_TEXTURE_2D, depthMap[0], 0);

        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, pointDepthMap[0], 0);

        // I don't know why this piece is needed, but it's in all the tutorials...
 /*       int[] depthTextureId = new int[1];
        GLES20.glGenRenderbuffers(1, depthTextureId, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthTextureId[0]);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthTextureId[0]);
*/
        int FBOstatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if(FBOstatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("GL_FRAMEBUFFER_COMPLETE failed, CANNOT use FBO. Status: " + FBOstatus);
            throw new RuntimeException("GL_FRAMEBUFFER_COMPLETE failed, CANNOT use FBO");
        } else{
            System.out.println("Framebuffer created!");
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, 0);
    }

	float[] lightProjection = new float[16];
	float[] lightView = new float[16];
	float[] lookDirection = new float[]{0, 1, 0};

	float[] dirLightPosition = new float[3];

	private void createDepthMaps(){

		Matrix.setIdentityM(dirLight.lightSpaceMatrix, 0);
		Matrix.orthoM(lightProjection, 0, -ratio*mScale*2, ratio*mScale*2, -2*mScale, 2*mScale, 0.02f, 5f);

		dirLightPosition[0] = viewPosition[0] + dirLight.direction[0]*viewPosition[2]/dirLight.direction[2];
		dirLightPosition[1] = viewPosition[1] + dirLight.direction[1]*viewPosition[2]/dirLight.direction[2];
		dirLightPosition[2] = viewPosition[2];

		Matrix.setLookAtM(lightView, 0, dirLightPosition[0], dirLightPosition[1], dirLightPosition[2], viewPosition[0], viewPosition[1], 0f, 0f, 1.0f, 0.0f);

		//System.out.println("dirLightPosition: (" + dirLightPosition[0] + ", " + dirLightPosition[1] + ", " + dirLightPosition[2] + "); viewPosition: (" + viewPosition[0] + ", " + viewPosition[1] + ", " + viewPosition[2] + ");");
		//Matrix.setLookAtM(lightView, 0, viewPosition[0], viewPosition[1], 0, viewPosition[0], viewPosition[1], viewPosition[2], 0f, 1.0f, 0.0f);
		Matrix.multiplyMM(dirLight.lightSpaceMatrix, 0, lightProjection, 0, lightView, 0);
		dirLight.depthMap = depthMap[0];

		createSimpleShadowMap(dirLight.lightSpaceMatrix, depthMapFBO[0]);

		characterPosition[0] = viewPosition[0];
		characterPosition[1] = viewPosition[1];
		characterPosition[2] = sphereRadius;
		flashLight.position = characterPosition;

		Matrix.setIdentityM(flashLight.lightSpaceMatrix, 0);
		Matrix.perspectiveM(lightProjection, 0, 60, 1, 0.02f, 3f);
		Matrix.setLookAtM(lightView, 0, characterPosition[0], characterPosition[1], characterPosition[2], characterPosition[0] + lookDirection[0], characterPosition[1] + lookDirection[1], characterPosition[2] + lookDirection[2], 0f, 0.0f, 1.0f);

		Matrix.multiplyMM(flashLight.lightSpaceMatrix, 0, lightProjection, 0, lightView, 0);
		flashLight.depthMap = depthMap[1];

		createSimpleShadowMap(flashLight.lightSpaceMatrix, depthMapFBO[1]);
	}


    private void createSimpleShadowMap(float[] lightSpaceMatrix, int fbo){

        GLES20.glViewport(0, 0, SHADOW_WIDTH, SHADOW_HEIGHT);
        Matrix.setIdentityM(mModelMatrix, 0);


        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo);
        //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        //mSquareHall.drawForDepthBuffer(lightSpaceMatrix, mModelMatrix);

        Bundle params = new Bundle();
        params.putFloatArray("lightSpaceMatrix", lightSpaceMatrix);
        params.putFloatArray("modelMatrix", mModelMatrix);

        levelMap.setupVertexDataForDrawing(viewPosition[0], viewPosition[1], 4*mScale, 4*mScale);

	    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
	    simpleShadowMapProgram.drawModel(params, levelMap.getVertexBuffers());

	    //params.putFloatArray("modelMatrix", sphereModel.modelMatrix);
	    //simpleShadowMapProgram.drawModel(params, sphereModel.getVertexBuffers());

	    for(NPC npc : npcList){
		    params.putFloatArray("modelMatrix", npc.getModelMatrix());
		    simpleShadowMapProgram.drawModel(params, npc.model.getVertexBuffers());
	    }

	    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    ArrayList<float[]> lightViewMatrices = new ArrayList<>(6);
    ArrayList<float[]> lightSpaceMatrices = new ArrayList<>(6);
    float nearPlane = 0.001f;
    float farPlane = 5f;

    private void createPointShadowMap(){
        if(lightViewMatrices.isEmpty()){
            for(int i = 0; i < 6; i++){
                float[] matrix1 = new float[16];
                lightViewMatrices.add(matrix1);
                float[] matrix2 = new float[16];
                lightSpaceMatrices.add(matrix2);
            }
        }
        GLES20.glViewport(0, 0, Math.max(SHADOW_WIDTH, SHADOW_HEIGHT), Math.max(SHADOW_WIDTH, SHADOW_HEIGHT));
        farPlane = Math.max(viewPosition[2] + 0.001f, 4*mScale);

        Matrix.perspectiveM(lightProjection, 0, 90, 1, Math.max(0.005f, lightPosition[2] - MapBlock.getDefaultHeight() - 0.001f), farPlane);

        Matrix.setLookAtM(lightViewMatrices.get(0), 0, lightPosition[0], lightPosition[1], lightPosition[2], lightPosition[0] + 1.0f, lightPosition[1], lightPosition[2], 0.0f, -1.0f, 0.0f);
        Matrix.setLookAtM(lightViewMatrices.get(1), 0, lightPosition[0], lightPosition[1], lightPosition[2], lightPosition[0] - 1.0f, lightPosition[1], lightPosition[2], 0.0f, -1.0f, 0.0f);
        Matrix.setLookAtM(lightViewMatrices.get(2), 0, lightPosition[0], lightPosition[1], lightPosition[2], lightPosition[0], lightPosition[1] + 1.0f, lightPosition[2], 0.0f, 0.0f, 1.0f);
        Matrix.setLookAtM(lightViewMatrices.get(3), 0, lightPosition[0], lightPosition[1], lightPosition[2], lightPosition[0], lightPosition[1] - 1.0f, lightPosition[2], 0.0f, 0.0f, -1.0f);
        Matrix.setLookAtM(lightViewMatrices.get(4), 0, lightPosition[0], lightPosition[1], lightPosition[2], lightPosition[0], lightPosition[1], lightPosition[2] + 1.0f, 0.0f, -1.0f, 0.0f);
        Matrix.setLookAtM(lightViewMatrices.get(5), 0, lightPosition[0], lightPosition[1], lightPosition[2], lightPosition[0], lightPosition[1], lightPosition[2] - 1.0f, 0.0f, -1.0f, 0.0f);

        Matrix.multiplyMM(lightSpaceMatrices.get(0), 0, lightProjection, 0, lightViewMatrices.get(0), 0);
        Matrix.multiplyMM(lightSpaceMatrices.get(1), 0, lightProjection, 0, lightViewMatrices.get(1), 0);
        Matrix.multiplyMM(lightSpaceMatrices.get(2), 0, lightProjection, 0, lightViewMatrices.get(2), 0);
        Matrix.multiplyMM(lightSpaceMatrices.get(3), 0, lightProjection, 0, lightViewMatrices.get(3), 0);
        Matrix.multiplyMM(lightSpaceMatrices.get(4), 0, lightProjection, 0, lightViewMatrices.get(4), 0);
        Matrix.multiplyMM(lightSpaceMatrices.get(5), 0, lightProjection, 0, lightViewMatrices.get(5), 0);

        Matrix.setIdentityM(mModelMatrix, 0);
        //System.out.println("##############");
        for(int i = 0; i < 6; i++){
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, pointDepthMapFBO[0]);
            //GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, pointDepthMap[0], 0);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, pointDepthMap[0], 0);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            //mSquareHall.drawForPointDepthBuffer(lightSpaceMatrices.get(i), mModelMatrix, lightPosition, farPlane);

            Bundle params = new Bundle();
            params.putFloatArray("lightSpaceMatrix", lightSpaceMatrices.get(i));
            params.putFloatArray("modelMatrix", mModelMatrix);
            params.putFloatArray("lightPosition", lightPosition);
            params.putFloat("farPlane", farPlane);
            //levelMap.draw(viewPosition[0], viewPosition[1], 4*mScale, 4*mScale, params);
            levelMap.setupVertexDataForDrawing(viewPosition[0], viewPosition[1], 4*mScale, 4*mScale);
	        try {
		        pointShadowMapProgram.drawModel(params, levelMap.getVertexBuffers());
	        } catch (Exception e) {
		        e.printStackTrace();
	        }

	        params.putFloatArray("modelMatrix", sphereModel.modelMatrix);
	        try {
		        pointShadowMapProgram.drawModel(params, sphereModel.getVertexBuffers());
	        } catch (Exception e) {
		        e.printStackTrace();
	        }
	        //printMatrix(lightSpaceMatrices.get(i), 4, 4, "      ");
            //System.out.println("-----------------");
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    private void displaySimpleShadowMap(int num){
        Bundle params = new Bundle();
        params.putInt("depthMap", depthMap[num]);
	    displaySimpleShadowProgram.drawModel(params, displaySimpleShadowBuffers);
    }

    float tempLength;
    float angle;
    float[] lookAtVector = new float[]{0f, 0f, -1f};
    private void displayPointShadowMap(){
        Matrix.perspectiveM(mProjectionMatrix, 0, 120, ratio, 0.4f, 1f);
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 0, lookAtVector[0], lookAtVector[1], lookAtVector[2], 0.0f, 1.0f, 0.0f);

        Bundle params = new Bundle();
        params.putInt("depthMap", pointDepthMap[0]);
        params.putFloatArray("viewMatrix", mViewMatrix);
        params.putFloatArray("projectionMatrix", mProjectionMatrix);
        params.putFloatArray("modelMatrix", mModelMatrix);

	    try {
		    displayPointShadowProgram.drawModel(params, displayPointShadowBuffers);
	    } catch (Exception e) {
		    e.printStackTrace();
	    }
    }

    private void drawSceneWithSimpleShadow(){

        if(ratio >= 1){
            Matrix.frustumM(mProjectionMatrix, 0, -ratio*mScale, ratio*mScale, -1*mScale, 1*mScale, viewPosition[2] - MapBlock.getDefaultHeight() - 0.0001f, viewPosition[2] + 0.001f); // !!! test if this works and try to change to 1.0f
        } else{
            Matrix.frustumM(mProjectionMatrix, 0, -1*mScale, 1*mScale, -1/ratio*mScale, 1/ratio*mScale, viewPosition[2] - MapBlock.getDefaultHeight() - 0.0001f, viewPosition[2] + 0.001f);
        }

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, viewPosition[0], viewPosition[1], viewPosition[2], viewPosition[0], viewPosition[1], viewPosition[2] - 1, 0f, 1.0f, 0.0f);

        // Create a rotation for the triangle
        // long time = SystemClock.uptimeMillis() % 4000L;
        // float angle = 0.090f * ((int) time);
        Matrix.rotateM(mViewMatrix, 0, mAngle, 0, 0, 1.0f);

        Matrix.setIdentityM(mModelMatrix, 0);

        //mSquareHall.draw(mViewMatrix, mProjectionMatrix, mModelMatrix, lightSpaceMatrix, depthMap[0], GLES20.GL_TEXTURE_2D, lightPosition, viewPosition, farPlane);

        Bundle params = new Bundle();
        params.putFloatArray("viewMatrix", mViewMatrix);
        params.putFloatArray("projectionMatrix", mProjectionMatrix);
        params.putFloatArray("modelMatrix", mModelMatrix);
        params.putFloatArray("lightSpaceMatrix", dirLight.lightSpaceMatrix);
        params.putInt("depthMap", depthMap[0]);
        params.putFloatArray("lightPosition", lightPosition);
        params.putFloatArray("viewPosition", viewPosition);

        //levelMap.draw(viewPosition[0], viewPosition[1], 4*mScale, 4*mScale, params);
        levelMap.setupVertexDataForDrawing(viewPosition[0], viewPosition[1], 4*mScale, 4*mScale);
	    try {
		    simpleShadowProgram.drawModel(params, levelMap.getVertexBuffers());
	    } catch (Exception e) {
		    e.printStackTrace();
	    }
	    //printMatrix(finalMVPMatrix, 4, 4, "      ");
        //printMatrix(mNormalMatrix, 4, 4, "      ");
    }

    private void drawSceneWithPointShadow(){

        GLES20.glViewport(0, 0, width, height);
        if(ratio >= 1){
            Matrix.frustumM(mProjectionMatrix, 0, -ratio*mScale, ratio*mScale, -1*mScale, 1*mScale, viewPosition[2] - MapBlock.getDefaultHeight() - 0.0001f, viewPosition[2] + 0.001f); // !!! test if this works and try to change to 1.0f
        } else{
            Matrix.frustumM(mProjectionMatrix, 0, -1*mScale, 1*mScale, -1/ratio*mScale, 1/ratio*mScale, viewPosition[2] - MapBlock.getDefaultHeight() - 0.0001f, viewPosition[2] + 0.001f);
        }

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, viewPosition[0], viewPosition[1], viewPosition[2], viewPosition[0], viewPosition[1], viewPosition[2] - 1, 0f, 1.0f, 0.0f);

        // Create a rotation for the triangle
        // long time = SystemClock.uptimeMillis() % 4000L;
        // float angle = 0.090f * ((int) time);
        Matrix.rotateM(mViewMatrix, 0, mAngle, 0, 0, 1.0f);

        Matrix.setIdentityM(mModelMatrix, 0);

        //mSquareHall.draw(mViewMatrix, mProjectionMatrix, mModelMatrix, lightSpaceMatrix, pointDepthMap[0], GLES20.GL_TEXTURE_CUBE_MAP, lightPosition, viewPosition, farPlane);

        Bundle params = new Bundle();
        params.putFloatArray("viewMatrix", mViewMatrix);
        params.putFloatArray("projectionMatrix", mProjectionMatrix);
        params.putFloatArray("modelMatrix", mModelMatrix);
        //params.putFloatArray("lightSpaceMatrix", lightSpaceMatrix);
        params.putInt("depthMap", pointDepthMap[0]);
        params.putFloatArray("lightPosition", lightPosition);
        params.putFloatArray("viewPosition", viewPosition);
        params.putFloat("farPlane", farPlane);

        //levelMap.draw(viewPosition[0], viewPosition[1], 4*mScale, 4*mScale, params);
        levelMap.setupVertexDataForDrawing(viewPosition[0], viewPosition[1], 4*mScale, 4*mScale);
	    pointShadowProgram.drawModel(params, levelMap.getVertexBuffers());

	    params.putFloatArray("modelMatrix", sphereModel.modelMatrix);
	    pointShadowProgram.drawModel(params, sphereModel.getVertexBuffers());

	    if(!listOfBullets.isEmpty()) {
			params = new Bundle();
			params.putFloatArray("viewMatrix", mViewMatrix);
			params.putFloatArray("projectionMatrix", mProjectionMatrix);
			//params.putFloatArray("lightSpaceMatrix", lightSpaceMatrix);
			params.putFloatArray("lightPosition", lightPosition);
			params.putFloatArray("viewPosition", viewPosition);

		    synchronized (listOfBullets) {
			    Iterator<BulletParameters> iterator = listOfBullets.iterator();
			    while (iterator.hasNext()) {
				    BulletParameters bp = iterator.next();
				    params.putInt("applyLighting", 1);
				    params.putFloatArray("modelMatrix", bp.bulletModelMatrix);
				    bulletShaderProgram.drawModel(params, bullet.getVertexBuffers());

				    params.putFloatArray("modelMatrix", bp.traceModelMatrix);
				    params.putInt("applyLighting", 0);
				    bulletShaderProgram.drawModel(params, bulletTrace.getVertexBuffers());

				    if (bp.finished) {
					    iterator.remove();
				    }
			    }
		    }
		}
	    //printMatrix(finalMVPMatrix, 4, 4, "      ");
        //printMatrix(mNormalMatrix, 4, 4, "      ");

    }


	float[] tempMatrix = new float[16];

	private void drawSceneWithComprehensiveShaders(){

		GLES20.glViewport(0, 0, width, height);
		if(ratio >= 1){
			Matrix.frustumM(mProjectionMatrix, 0, -ratio*mScale, ratio*mScale, -1*mScale, 1*mScale, viewPosition[2] - MapBlock.getDefaultHeight() - 0.0001f, viewPosition[2] + 0.001f); // !!! test if this works and try to change to 1.0f
		} else{
			Matrix.frustumM(mProjectionMatrix, 0, -1*mScale, 1*mScale, -1/ratio*mScale, 1/ratio*mScale, viewPosition[2] - MapBlock.getDefaultHeight() - 0.0001f, viewPosition[2] + 0.001f);
		}
		Matrix.setLookAtM(mViewMatrix, 0, viewPosition[0], viewPosition[1], viewPosition[2], viewPosition[0], viewPosition[1], viewPosition[2] - 1, 0f, 1.0f, 0.0f);
		Matrix.rotateM(mViewMatrix, 0, mAngle, 0, 0, 1.0f);
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.setIdentityM(normalMatrix, 0);

		Bundle params = new Bundle();
		params.putFloatArray("viewMatrix", mViewMatrix);
		params.putFloatArray("projectionMatrix", mProjectionMatrix);
		params.putFloatArray("modelMatrix", mModelMatrix);
		params.putFloatArray("viewPosition", viewPosition);
		params.putFloatArray("normalMatrix", mModelMatrix);
		params.putInt("colorSource", 1);
		//params.putSerializable("dirLight_0", dirLight);
		params.putSerializable("pointLight_0", envLight);
		params.putSerializable("pointDirLight_0", flashLight);

		levelMap.setupVertexDataForDrawing(viewPosition[0], viewPosition[1], 4*mScale, 4*mScale);
		//System.out.println("drawing the level");
		comprehensiveShaderProgram.drawModel(params, levelMap.getVertexBuffers());

		params.putInt("colorSource", 3);
		//params.putFloatArray("u_color", levelMap.centerLineColor);
		//comprehensiveShaderProgram.drawModel(params, levelMap.getCenterPathBuffers());

		params.putFloatArray("u_color", testPathColor);
		comprehensiveShaderProgram.drawModel(params, testPathBuffers);

		//System.out.println("drawing the character");

		// WTF is this code for? Check if removing it does anything and remove it if it doesn't!!!!!
		Matrix.invertM(tempMatrix, 0, sphereModel.modelMatrix, 0);
		Matrix.transposeM(normalMatrix, 0, tempMatrix, 0);
		// end of the WTF code

		params.putInt("colorSource", 1);
		params.putFloatArray("modelMatrix", sphereModel.modelMatrix);
		params.putFloatArray("normalMatrix", sphereModel.modelMatrix);
		comprehensiveShaderProgram.drawModel(params, sphereModel.getVertexBuffers());

		for(NPC npc : npcList){
			if(MyUtilities.distance(npc.position, characterPosition, 3) < 4*mScale) {
				params.putFloatArray("modelMatrix", npc.getModelMatrix());
				params.putFloatArray("normalMatrix", npc.getModelMatrix());
				comprehensiveShaderProgram.drawModel(params, npc.model.getVertexBuffers());
			}
		}

		if(!listOfBullets.isEmpty()) {
			params = new Bundle();
			params.putFloatArray("viewMatrix", mViewMatrix);
			params.putFloatArray("projectionMatrix", mProjectionMatrix);
			params.putFloatArray("lightPosition", lightPosition);
			params.putFloatArray("viewPosition", viewPosition);

			synchronized (listOfBullets) {
				Iterator<BulletParameters> iterator = listOfBullets.iterator();
				while (iterator.hasNext()) {
					BulletParameters bp = iterator.next();
					params.putInt("applyLighting", 1);
					params.putFloatArray("modelMatrix", bp.bulletModelMatrix);
					bulletShaderProgram.drawModel(params, bullet.getVertexBuffers());

					params.putFloatArray("modelMatrix", bp.traceModelMatrix);
					params.putInt("applyLighting", 0);
					bulletShaderProgram.drawModel(params, bulletTrace.getVertexBuffers());

					if (bp.finished) {
						iterator.remove();
					}
				}
			}
		}
	}

	Bitmap mapBitmap;
	int[][] map;
	int[] texture = new int[1];

	private void drawBitmap(float scale){
		GLES20.glViewport(0, 0, width, height);
		if(mapBitmap == null){
			map = levelMap.getLevelMap(16*2);
			mapBitmap = MyUtilities.convertArrayToBitmap(map);

			GLES20.glGenTextures(1, texture, 0);
			// load (bind) the texture into OpenGL
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mapBitmap, 0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

			System.out.println("Font map created. Bitmap size: " + mapBitmap.getWidth() + " x " + mapBitmap.getHeight());
		}
		Bundle params = new Bundle();
		params.putInt("texture", texture[0]);

		float scaleX = 1;
		float scaleY = 1;
		// adjust scale based on the screen side ratio
		if(ratio > 1){
			scaleX = 1/ratio;
		} else{
			scaleY = ratio;
		}
		// adjust scale based on the image side ratio
		float bitmapAspectRatio = ((float)mapBitmap.getWidth())/mapBitmap.getHeight();
		if(bitmapAspectRatio > 1){
			scaleY *= 1/bitmapAspectRatio;
		} else{
			scaleX *= bitmapAspectRatio;
		}
		// adjusting scale such that the image is fitted within the display
		float fitToEdgesScale = Math.max(scaleX, scaleY);
		scaleX /= fitToEdgesScale;
		scaleY /= fitToEdgesScale;
		// adjust scale based on zoom
		scaleX *= scale;
		scaleY *= scale;

		//System.out.println("scaleX: " + scaleX + "; scaleY: " + scaleY);

		Matrix.setIdentityM(mProjectionMatrix, 0);
		Matrix.scaleM(mProjectionMatrix, 0, scaleX, scaleY, 1);
		params.putFloatArray("projectionMatrix", mProjectionMatrix);

		displayTextureProgram.drawModel(params, displaySimpleShadowBuffers);
	}

    private long lastTimeStamp = 0;
    private long currTimeStamp = 0;
    private float fps = 0;

    public void onDrawFrame(GL10 unused) {

	    if(lastTimeStamp == 0){
		    lastTimeStamp = System.currentTimeMillis();
		    currTimeStamp = System.currentTimeMillis();
	    } else{
		    lastTimeStamp = currTimeStamp;
		    currTimeStamp = System.currentTimeMillis();
		    fps = 0.02f*(1000f/(currTimeStamp - lastTimeStamp)) + 0.98f*fps;
	    }
        updateCharacterPosition();
	    GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        // Redraw background color
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_FRONT);

        //createSimpleShadowMap();

        GLES20.glDisable(GLES20.GL_BLEND); // disable blending because I use color buffer's alpha channel (as wel as other channels) to write the point shadow map
        //createPointShadowMap();
        createDepthMaps();

	    GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glBlendEquation(GLES20.GL_FUNC_ADD); // this line is unnecessary, but I'll leave it here in case I want to change it later.

        GLES20.glDisable(GLES20.GL_CULL_FACE);

	    //GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        // Draw shadow map on a rectangle filling the whole screen. Only for testing purposes
        //displaySimpleShadowMap(0);
        //displayPointShadowMap();

        //GLES20.glCullFace(GLES20.GL_BACK);
		//drawBitmap(1);

	    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, sceneFBO[0]);
	    GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
	    //drawSceneWithSimpleShadow();
        //drawSceneWithPointShadow();

	    drawSceneWithComprehensiveShaders();

        // disable depth test so the text can be displayed on top of everything
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        fpsText.setText("FPS: " + Math.round(fps));
        fpsText.prepareTextForDrawing();
        Bundle params = new Bundle();
        params.putFloatArray("textColor", fpsText.getTextColorVector());
        params.putFloatArray("backgroundColor", fpsText.getBackgroundColorVector());
        params.putInt("fontMap", fpsText.getFontMapTexture());

	    textDrawingShaderProgram.drawModel(params, fpsText.getVertexBuffers());
	    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);


	    params = new Bundle();
	    params.putFloatArray("color", moveAreaRect.color);
	    params.putFloatArray("modelMatrix", moveAreaRect.modelMatrix);
	    params.putFloatArray("projectionMatrix", moveAreaRect.projectionMatrix);

	    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, blurFBOs[2]);
	    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
	    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
	    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

	    coloredShapesProgram.drawModel(params, moveAreaRect.vertexBuffers);

	    if(moveArrow.ready){
		    moveArrow.ready = false;
		    params = new Bundle();
		    params.putFloatArray("color", moveArrow.getColor());
		    params.putFloatArray("modelMatrix", moveArrow.modelMatrix);
		    params.putFloatArray("projectionMatrix", moveArrow.projectionMatrix);
		    coloredShapesProgram.drawModel(params, moveArrow.vertexBuffers);
	    }
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

	    GLES20.glViewport(0, 0, width/scaleBlur, height/scaleBlur);
	    int resultingTexture = applyBlur(blurTextures[2], 3);

	    params = new Bundle();
	    params.putIntArray("texture", new int[]{sceneTexture[0], blurTextures[2], resultingTexture});
	    //params.putFloatArray("coeffs", new float[]{0.0f, 0.0f, ((float)Math.sin((double)currTimeStamp/1000*Math.PI % 2*Math.PI)+ 2f)/2});
	    params.putFloatArray("coeffs", new float[]{0.0f, 0.0f, 1f});
	    GLES20.glViewport(0, 0, width, height);
		overlayTexturesProgram.drawModel(params, null);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

	    int debugInfo = GLES20.glGetError();

	    if (debugInfo != GLES20.GL_NO_ERROR) {
		    String msg = "OpenGL error: " + debugInfo;
		    System.out.println(msg);
	    }
	    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    private int width;
    private int height;
    float ratio = 1;
    float shadowMapRatio = 1.0f;

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        this.width = width;
        this.height = height;

        fpsText.setWindowDimensions(width, height);

        SHADOW_HEIGHT = Math.round(height/shadowMapRatio);
        SHADOW_WIDTH = Math.round(width/shadowMapRatio);

        //GLES20.glViewport(0, 0, width, height);

	    if(ratio > 1){
		    moveAreaRect.positionX /= ratio;
	    } else{
		    moveAreaRect.positionY *= ratio;
	    }

        ratio = (float) width / height;
        //generateSimpleShadowFBO();
        //generatePointShadowFBO();
		generateSimpleShadowFBOs(2);

	    createBlurFBOs();
	    createSceneFBOs();

	    if(ratio > 1){
		    Matrix.orthoM(moveAreaRect.projectionMatrix, 0, -ratio, ratio, -1f, 1f, -1, 1);
		    Matrix.orthoM(moveArrow.projectionMatrix, 0, -ratio, ratio, -1f, 1f, -1, 1);
	    } else{
		    Matrix.orthoM(moveAreaRect.projectionMatrix, 0, -1, 1, -1f/ratio, 1f/ratio, -1, 1);
		    Matrix.orthoM(moveArrow.projectionMatrix, 0, -1, 1, -1f/ratio, 1f/ratio, -1, 1);

	    }

	    positionMoveAreaRect(moveAreaRect.positionX, moveAreaRect.positionY);

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        //Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    private static int[] compiled = new int[1];
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);


        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            GLES20.glDeleteShader(shader);
            throw new RuntimeException("Could not compile program:\n" + GLES20.glGetShaderInfoLog(shader) + " | " + shaderCode);
        }

        return shader;
    }

    private volatile float mAngle = 0f;

    public float getAngle() {
        return mAngle;
    }

    public void setAngle(float angle) {
        mAngle = angle;
    }

    private volatile float mScale = 0.4f;

    public float getScale() {
        return mScale;
    }

    public void setScale(float scale) {
        mScale = scale;
    }

    private volatile float shiftX;
    private volatile float shiftY;

    public float getShiftY() {
        return shiftY;
    }

    public void setShiftY(float shiftY) {
        this.shiftY = shiftY;
    }

    public float getShiftX() {
        return shiftX;
    }

    public void setShiftX(float shiftX) {
        this.shiftX = shiftX;
    }

    public static void printMatrix(float[] matrix, int dim1, int dim2, String spacer){
        for(int row = 0; row < dim2; row++){
            System.out.print("||");
            for(int col = 0; col < dim1; col++){
                System.out.print(matrix[col*dim1 + row] + spacer);
            }
            System.out.println("||");
        }
    }

	private float moveSpeed;
    private PointF moveVector = new PointF();
    private PointF aimPosition = new PointF();
    private boolean isMoving = false;
    private boolean isAiming = false;

    private static final float maxDisplacement = 0.2f;
    private static final float maxSpeed = 0.0004f; // =distance/millisecond
    float currDisplacement = 0;
    float currMoveLength = 0;
    PointF desiredDestination = new PointF();
    PointF currentPosition = new PointF();

	float characterRotationAngle = 0;

	float bulletSpeed = 0.01f; // meters per millisecond

    private void updateCharacterPosition(){

	    //moving NPCs
	    if(npcList != null && !npcList.isEmpty()){
		    float deltaTime = (currTimeStamp - lastTimeStamp)/1000f;
		    for(NPC npc : npcList){
			    npc.updatePosition(deltaTime, levelMap);
		    }
	    }

	    //setting up the move arrow
	    if(moveArrow.showArrow){
		    moveArrow.setLength(moveVector.length());
		    Matrix.setIdentityM(moveArrow.modelMatrix, 0);
		    Matrix.translateM(moveArrow.modelMatrix, 0, moveArrow.positionX, moveArrow.positionY, 0);
		    Matrix.rotateM(moveArrow.modelMatrix, 0, getRotationAngle(moveVector.x, moveVector.y)*180/(float)Math.PI, 0, 0, 1);
	        moveArrow.ready = true;
	    }

	    // moving the character
	    if(isMoving){
	        //System.out.println("moving!");

            if(moveSpeed > 0) {
	            currMoveLength = moveVector.length();
                currDisplacement = moveSpeed*(currTimeStamp - lastTimeStamp)/1000f;
                currentPosition.x = shiftX;
                currentPosition.y = shiftY;
                desiredDestination.x = shiftX + moveVector.x / currMoveLength * currDisplacement;
                desiredDestination.y = shiftY + moveVector.y / currMoveLength * currDisplacement;

                //System.out.println("Start: (" + currentPosition.x + ", " + currentPosition.y + "). Desired destination: (" + desiredDestination.x + ", " + desiredDestination.y + "); ");
                desiredDestination = levelMap.getCorrectedDestination(currentPosition, desiredDestination, 10, 1f, spherePerimeter);
                //System.out.println("corrected destination: (" + desiredDestination.x + ", " + desiredDestination.y + ");");

                shiftX = desiredDestination.x;
                shiftY = desiredDestination.y;
            }
        }

        viewPosition[0] = shiftX;
        viewPosition[1] = shiftY;

        lightPosition[0] = viewPosition[0];
        lightPosition[1] = viewPosition[1];

	    envLight.position = viewPosition;

	    // updating character's model matrix and lookAt direction
	    if(isAiming || moveVector.length() > 0){
		    if(isAiming) {
			    characterRotationAngle = getRotationAngle(aimPosition.x, aimPosition.y);
			    //System.out.println("aiming vector: (" + aimPosition.x + ", " + aimPosition.y + ");");

			    lookDirection[0] = aimPosition.x;
			    lookDirection[1] = aimPosition.y;
		    } else{
			    characterRotationAngle = getRotationAngle(moveVector.x, moveVector.y);
			    //System.out.println("moving vector: (" + moveVector.x + ", " + moveVector.y + ");");

			    lookDirection[0] = moveVector.x;
			    lookDirection[1] = moveVector.y;
		    }
		    //System.out.println((isAiming ? "Aiming. " : "Moving. ") + "Rotation angle: " + (characterRotationAngle * 180 / (float) Math.PI - 90));
		    Matrix.setIdentityM(sphereModel.modelMatrix, 0);
		    Matrix.translateM(sphereModel.modelMatrix, 0, viewPosition[0], viewPosition[1], 0.1f);
		    Matrix.rotateM(sphereModel.modelMatrix, 0, characterRotationAngle * 180 / (float) Math.PI - 90, 0, 0, 1);
		    //Matrix.scaleM(sphereModel.modelMatrix, 0, sphereRadius, sphereRadius, sphereRadius);
	    }

	    // updating bullet positions
	    if(!listOfBullets.isEmpty()){
		    synchronized (listOfBullets) {
			    for (BulletParameters bp : listOfBullets) {
				    bp.lastUpdateTime = System.currentTimeMillis();
				    float[] desiredPosition = new float[3];
				    desiredPosition[0] = bp.startPosition[0] + bp.moveDirection[0] * bp.speed * (bp.lastUpdateTime - bp.startTime);
				    desiredPosition[1] = bp.startPosition[1] + bp.moveDirection[1] * bp.speed * (bp.lastUpdateTime - bp.startTime);
				    float traveledLength = PointF.length(desiredPosition[0] - bp.startPosition[0], desiredPosition[1] - bp.startPosition[1]);
				    bp.lastPosition = levelMap.getFurthestAvailablePoint(bp.startPosition, desiredPosition, Math.max(1, (int)(traveledLength*50)));
				    traveledLength = PointF.length(bp.lastPosition[0] - bp.startPosition[0], bp.lastPosition[1] - bp.startPosition[1]);
				    bp.rotationAngle = getRotationAngle(bp.moveDirection[0], bp.moveDirection[1]) * 180 / (float) Math.PI - 90;
				    bp.traceLength = Math.min(traveledLength - bulletSize / 2, bulletTraceMaxLength);
				    bp.traceTranslation[0] = bp.lastPosition[0] - bp.moveDirection[0] * (bp.traceLength / 2 + bulletSize);
				    bp.traceTranslation[1] = bp.lastPosition[1] - bp.moveDirection[1] * (bp.traceLength / 2 + bulletSize);

				    if (!(bp.lastPosition[0] == desiredPosition[0] && bp.lastPosition[1] == desiredPosition[1])) {
					    bp.finished = true;
				    }

				    Matrix.setIdentityM(bp.bulletModelMatrix, 0);
				    Matrix.translateM(bp.bulletModelMatrix, 0, bp.lastPosition[0], bp.lastPosition[1], MapBlock.getDefaultHeight() / 2);
				    Matrix.rotateM(bp.bulletModelMatrix, 0, bp.rotationAngle, 0, 0, 1);

				    Matrix.setIdentityM(bp.traceModelMatrix, 0);
				    Matrix.translateM(bp.traceModelMatrix, 0, bp.traceTranslation[0], bp.traceTranslation[1], MapBlock.getDefaultHeight() / 2);
				    Matrix.rotateM(bp.traceModelMatrix, 0, bp.rotationAngle, 0, 0, 1);
				    Matrix.scaleM(bp.traceModelMatrix, 0, bulletSize / 4.5f, bp.traceLength / 2, bulletSize / 4.5f);
			    }
		    }
	    }

        //System.out.println("shift: (" + shiftX + ", " + shiftY + ");");
    }

	private ArrayList<float[]> getActivePerimeterPoints(float angle){

		float[] dir = new float[]{-(float)Math.sin(angle), (float)Math.cos(angle), 0};

		HashMap<float[], Float> pointLeftovers = new HashMap<>();
		TreeMap<Float, float[]> pointMap = new TreeMap<>();

		for(float[] point : spherePerimeter){
			float parallel = point[0]*dir[0] + point[1]*dir[1] + point[2]*dir[2];
			float[] leftoverVec = new float[]{point[0], point[1], point[2]};
			leftoverVec[0] -= parallel*dir[0];
			leftoverVec[1] -= parallel*dir[1];
			leftoverVec[2] -= parallel*dir[2];
			float leftover = (float)Math.sqrt(leftoverVec[0]*leftoverVec[0] + leftoverVec[1]*leftoverVec[1] + leftoverVec[2]*leftoverVec[2]);

			// checking if an already added point with higher projection onto the direction vector have higher leftover part
			// if yes, don't add the current point
			TreeMap.Entry<Float, float[]> higherPoint = pointMap.ceilingEntry(parallel);
			if(higherPoint == null){
				pointMap.put(parallel, point);
				pointLeftovers.put(point, leftover);
			} else if(pointLeftovers.get(higherPoint.getValue()) < leftover){
				pointMap.put(parallel, point);
				pointLeftovers.put(point, leftover);
			}
			// checking if any of already added points with lower projection onto the direction vector have lower leftover parts
			// if yes, remove those
			boolean lowerPointsChecked = false;
			TreeMap.Entry<Float, float[]> lowerPoint = pointMap.floorEntry(parallel);
			while(lowerPoint != null && !lowerPointsChecked){
				if(pointLeftovers.get(lowerPoint.getValue()) < leftover){
					pointMap.remove(parallel);
					pointLeftovers.remove(point);
					lowerPoint = pointMap.floorEntry(parallel);
				} else{
					lowerPointsChecked = true;
				}
			}

		}
		ArrayList<float[]> result = new ArrayList<>();
		result.addAll(pointLeftovers.keySet());
		//System.out.println("total perimeter points: " + spherePerimeter.size() + "; active points: " + result.size());

		return result;
	}

	public static float getRotationAngle(float x, float y){
		float dist = PointF.length(x, y);
		float cosA = x/dist;
		float angle;

		if(y >= 0){
			angle = (float) Math.acos(cosA);
		} else{
			angle = (float)(2*Math.PI - Math.acos(cosA));
		}
		return angle;
	}

    public void setMoveVector(PointF moveVector){
        this.moveVector = moveVector;
    }
    public void setMoveVector(float x, float y){
        this.moveVector.x = x;
        this.moveVector.y = y;
    }

    public void setAimPosition(PointF aimPosition){
        this.aimPosition = aimPosition;
    }
    public void setAimPosition(float x, float y){
        this.aimPosition.x = x;
        this.aimPosition.y = y;
    }

    public boolean isAiming() {
        return isAiming;
    }

    public void setAiming(boolean aiming) {
        isAiming = aiming;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public void setMoving(boolean moving) {
        isMoving = moving;
    }

	public void addBullet() {
		BulletParameters bp = new BulletParameters();

		float length = aimPosition.length();
		bp.moveDirection[0] = aimPosition.x/length;
		bp.moveDirection[1] = aimPosition.y/length;
		bp.speed = bulletSpeed;
		bp.startTime = System.currentTimeMillis();

		bp.startPosition[0] = viewPosition[0] + bp.moveDirection[0] * (sphereRadius - bulletSize/2);
		bp.startPosition[1] = viewPosition[1] + bp.moveDirection[1] * (sphereRadius - bulletSize/2);
		synchronized (listOfBullets) {
			listOfBullets.add(bp);
		}
	}

	private void initializeShaderPrograms(){
        simpleShadowProgram = new SimpleShadowShaderProgram(context, R.raw.simple_shadow_v_shader, R.raw.simple_shadow_f_shader);
        simpleShadowMapProgram = new SimpleDepthMapShaderProgram(context, R.raw.shadow_map_v_shader, R.raw.shadow_map_f_shader);
        displaySimpleShadowProgram = new DisplaySimpleDepthMapShaderProgram(context, R.raw.simple_shadow_render_v_shader, R.raw.simple_shadow_render_f_shader);
        pointShadowProgram = new PointShadowShaderProgram(context, R.raw.point_shadow_v_shader, R.raw.point_shadow_f_shader);
        pointShadowMapProgram = new PointDepthMapShaderProgram(context, R.raw.point_shadow_map_v_shader, R.raw.point_shadow_map_f_shader);
        displayPointShadowProgram = new DiplayPointDepthMapShaderProgram(context, R.raw.display_point_shadow_v_shader, R.raw.display_point_shadow_f_shader);
        textDrawingShaderProgram = new TextShaderProgram(context, R.raw.text_drawing_v_shader, R.raw.text_drawing_f_shader);
	    bulletShaderProgram = new BulletShaderProgram(context, R.raw.bullet_v_shader, R.raw.bullet_f_shader);
		comprehensiveShaderProgram = new ComprehensiveShaderProgram(context, R.raw.comprehensive_v_shader, R.raw.comprehensive_f_shader);
        blurProgram = new GaussianBlurProgram(context, R.raw.blur_v_shader, R.raw.blur_f_shader);
		coloredShapesProgram = new ColoredShapesShaderProgram(context, R.raw.colored_shapes_v_shader, R.raw.colored_shapes_f_shader);
		overlayTexturesProgram = new OverlayTexturesProgram(context, R.raw.blur_v_shader, R.raw.add_textures_f_shader);
		displayTextureProgram = new DisplayTextureShaderProgram(context, R.raw.display_texture_v_shader, R.raw.display_texture_f_shader);
	}

	int[] sceneFBO;
	int[] sceneTexture;
	private void createSceneFBOs(){
		sceneFBO = new int[1];
		sceneTexture = new int[1];
		int[] depthTexture = new int[1];

		GLES20.glGenFramebuffers(sceneFBO.length, sceneFBO, 0);
		GLES20.glGenTextures(sceneTexture.length, sceneTexture, 0);
		GLES20.glGenTextures(depthTexture.length, depthTexture, 0);

		for(int i = 0; i < sceneFBO.length; i++) {
			// configure depth map texture
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, sceneTexture[i]);
			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

			// bind depth map texture to the FBO
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, sceneFBO[i]);
			GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, sceneTexture[i], 0);

			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, depthTexture[i]);
			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_DEPTH_COMPONENT, width, height, 0, GLES20.GL_DEPTH_COMPONENT, GLES20.GL_UNSIGNED_INT, null);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

			GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_TEXTURE_2D, depthTexture[i], 0);

			int FBOstatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
			if (FBOstatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
				System.out.println("GL_FRAMEBUFFER_COMPLETE failed, CANNOT use FBO. Status: " + FBOstatus);
				throw new RuntimeException("GL_FRAMEBUFFER_COMPLETE failed, CANNOT use FBO");
			} else {
				System.out.println("Framebuffer " + i + " created!");
			}
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		}

	}
	int scaleBlur = 4;

	int[] blurFBOs;
	int[] blurTextures;
	private void createBlurFBOs(){
		blurFBOs = new int[3];
		blurTextures = new int[3];

		GLES20.glGenFramebuffers(blurFBOs.length, blurFBOs, 0);
		GLES20.glGenTextures(blurTextures.length, blurTextures, 0);

		for(int i = 0; i < blurFBOs.length; i++) {
			// configure depth map texture
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, blurTextures[i]);

			if(i == 2){
				GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			} else {
				GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width / scaleBlur, height / scaleBlur, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			}

			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

			// bind depth map texture to the FBO
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, blurFBOs[i]);
			GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, blurTextures[i], 0);

			int FBOstatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
			if (FBOstatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
				System.out.println("GL_FRAMEBUFFER_COMPLETE failed, CANNOT use FBO. Status: " + FBOstatus);
				throw new RuntimeException("GL_FRAMEBUFFER_COMPLETE failed, CANNOT use FBO");
			} else {
				System.out.println("Framebuffer " + i + " created!");
			}
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		}

	}

	private int applyBlur(int texture, int passCount){

		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, blurFBOs[0]);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, blurFBOs[1]);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

		int orientation = 0;
		for(int i = 0; i < 2*passCount; i++){
			Bundle params = new Bundle();
			if(i == 0) {
				params.putInt("texture", texture);
			} else{
				params.putInt("texture", blurTextures[orientation == 0 ? 1 : 0]);
			}
			params.putInt("orientation", orientation);
			params.putFloatArray("textureSize", new float[]{width, height});

			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, blurFBOs[orientation]);
			//GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
			blurProgram.drawModel(params, null);
			orientation = orientation == 0 ? 1 : 0;
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
		}
		return blurTextures[orientation == 0 ? 1 :0];
	}

	public void createMoveAreaRect(float width, float height, float centerX, float centerY, float thickness, float[] color){
		moveAreaRect  = new RectangularFrame(width, height, thickness);
		moveAreaRect.color = color;

		positionMoveAreaRect(centerX, centerY);
	}

	public void createMoveArrow(float[] ringRadii, float[] ringColors, float thickness, float[] arrowHeadDimensions){
		Bundle params = new Bundle();
		params.putFloatArray("ringRadii", ringRadii);
		params.putFloatArray("ringColors", ringColors);
		params.putFloat("ringThickness", thickness);
		params.putFloat("lineThickness", thickness);
		params.putFloatArray("arrowHeadDimensions", arrowHeadDimensions);

		moveArrow = new Arrow(params);
		moveArrow.setColor(new float[]{179f/255f, 244f/255f, 66f/255f, 1});
	}

	private void positionMoveAreaRect(float centerX, float centerY){
		if(ratio > 1){
			moveAreaRect.positionX = ratio*centerX;
			moveAreaRect.positionY = centerY;
		} else{
			moveAreaRect.positionX = centerX;
			moveAreaRect.positionY = centerY/ratio;
		}

		float[] mm = moveAreaRect.modelMatrix;
		Matrix.setIdentityM(mm, 0);
		Matrix.translateM(mm, 0, moveAreaRect.positionX, moveAreaRect.positionY, 0);
	}

	public void setMoveTouchStart(float x, float y){
		moveArrow.positionX = x;
		moveArrow.positionY = y;
		moveArrow.showArrow = true;
	}

	public void endMoveTouch(){
		moveArrow.showArrow = false;
	}

	public void setMoveSpeed(float moveSpeed) {
		this.moveSpeed = moveSpeed;
	}

	public void setMoveArrowColor(float[] color){
		moveArrow.setColor(color);
	}
}
