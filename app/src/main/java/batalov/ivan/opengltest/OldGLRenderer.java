package batalov.ivan.opengltest;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import batalov.ivan.opengltest.models.LevelMap;
import batalov.ivan.opengltest.models.MapBlock;
import batalov.ivan.opengltest.models.Model3D;
import batalov.ivan.opengltest.models.SquareHall;


/**
 * Created by ivan on 10/8/17.
 */

public class OldGLRenderer implements GLSurfaceView.Renderer {

    public static final int PROGRAM_DRAW_SCENE_SIMPLE_SHADOW = 0;
    public static final int PROGRAM_DRAW_SCENE_POINT_SHADOW = 1;
    public static final int PROGRAM_DRAW_2D_DEPTH_BUFFER = 2;
    public static final int PROGRAM_DRAW_CUBEMAP_DEPTH_BUFFER = 3;

    public static int SHADOW_WIDTH = 0;
    public static int SHADOW_HEIGHT = 0;

    private SquareHall mSquareHall;
    private LevelMap levelMap;
    private OpenGLTextWriter fpsText;

    private String shadowVertexShaderCode;
    private String shadowFragmentShaderCode;
    private String vertexShaderCode;
    private String fragmentShaderCode;
    private String displayShadowVertexShaderCode;
    private String displayShadowFragmentShaderCode;

    private String pointShadowVertexShaderCode;
    private String pointShadowFragmentShaderCode;
    private String pointShadowMapVertexShaderCode;
    private String pointShadowMapFragmentShaderCode;
    private String displayPointShadowVertexShaderCode;
    private String displayPointShadowFragmentShaderCode;
    private String textDrawingVertexShaderCode;
    private String textDrawingFragmentShaderCode;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mModelMatrix = new float[16];

    private int simpleShadowProgram;
    private int simpleShadowMapProgram;
    private int displaySimpleShadowProgram;
    private int pointShadowProgram;
    private int pointShadowMapProgram;
    private int displayPointShadowProgram;
    private int textDrawingShaderProgram;

    FloatBuffer mapDisplayVertexBuffer;
    FloatBuffer mapDisplayTexCoordBuffer;
    FloatBuffer pointMapDisplayVertexBuffer;

    Model3D sphereModel;

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

    public OldGLRenderer(Context context){
        super();

        if(context != null){
            shadowVertexShaderCode = readShaderCode(context, R.raw.shadow_map_v_shader);
            shadowFragmentShaderCode = readShaderCode(context, R.raw.shadow_map_f_shader);

            displayShadowVertexShaderCode = readShaderCode(context, R.raw.simple_shadow_render_v_shader);
            displayShadowFragmentShaderCode = readShaderCode(context, R.raw.simple_shadow_render_f_shader);

            vertexShaderCode = readShaderCode(context, R.raw.simple_shadow_v_shader);
            fragmentShaderCode = readShaderCode(context, R.raw.simple_shadow_f_shader);

            pointShadowVertexShaderCode = readShaderCode(context, R.raw.point_shadow_v_shader);
            pointShadowFragmentShaderCode = readShaderCode(context, R.raw.point_shadow_f_shader);

            pointShadowMapVertexShaderCode = readShaderCode(context, R.raw.point_shadow_map_v_shader);
            pointShadowMapFragmentShaderCode = readShaderCode(context, R.raw.point_shadow_map_f_shader);

            displayPointShadowVertexShaderCode = readShaderCode(context, R.raw.display_point_shadow_v_shader);
            displayPointShadowFragmentShaderCode = readShaderCode(context, R.raw.display_point_shadow_f_shader);

            textDrawingVertexShaderCode = readShaderCode(context, R.raw.text_drawing_v_shader);
            textDrawingFragmentShaderCode = readShaderCode(context, R.raw.text_drawing_f_shader);

            sphereModel = new Model3D(context, R.raw.sphere_1x1, false);
        }
    }

    public String readShaderCode(Context context, int id){
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

        int regularVertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int regularFragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        simpleShadowProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(simpleShadowProgram, regularVertexShader);
        GLES20.glAttachShader(simpleShadowProgram, regularFragmentShader);
        GLES20.glLinkProgram(simpleShadowProgram);

        int shadowVertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, shadowVertexShaderCode);
        int shadowFragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, shadowFragmentShaderCode);
        simpleShadowMapProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(simpleShadowMapProgram, shadowVertexShader);
        GLES20.glAttachShader(simpleShadowMapProgram, shadowFragmentShader);
        GLES20.glLinkProgram(simpleShadowMapProgram);

        int displayShadowVertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, displayShadowVertexShaderCode);
        int displayShadowFragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, displayShadowFragmentShaderCode);
        displaySimpleShadowProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(displaySimpleShadowProgram, displayShadowVertexShader);
        GLES20.glAttachShader(displaySimpleShadowProgram, displayShadowFragmentShader);
        GLES20.glLinkProgram(displaySimpleShadowProgram);

        int pointShadowVertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, pointShadowVertexShaderCode);
        int pointShadowFragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, pointShadowFragmentShaderCode);
        pointShadowProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(pointShadowProgram, pointShadowVertexShader);
        GLES20.glAttachShader(pointShadowProgram, pointShadowFragmentShader);
        GLES20.glLinkProgram(pointShadowProgram);

        int pointShadowMapVertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, pointShadowMapVertexShaderCode);
        int pointShadowMapFragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, pointShadowMapFragmentShaderCode);
        pointShadowMapProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(pointShadowMapProgram, pointShadowMapVertexShader);
        GLES20.glAttachShader(pointShadowMapProgram, pointShadowMapFragmentShader);
        GLES20.glLinkProgram(pointShadowMapProgram);

        int displayPointShadowVertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, displayPointShadowVertexShaderCode);
        int displayPointShadowFragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, displayPointShadowFragmentShaderCode);
        displayPointShadowProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(displayPointShadowProgram, displayPointShadowVertexShader);
        GLES20.glAttachShader(displayPointShadowProgram, displayPointShadowFragmentShader);
        GLES20.glLinkProgram(displayPointShadowProgram);

        int textDrawingVertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, textDrawingVertexShaderCode);
        int textDrawingFragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, textDrawingFragmentShaderCode);
        textDrawingShaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(textDrawingShaderProgram, textDrawingVertexShader);
        GLES20.glAttachShader(textDrawingShaderProgram, textDrawingFragmentShader);
        GLES20.glLinkProgram(textDrawingShaderProgram);

        mSquareHall = new SquareHall(simpleShadowProgram, simpleShadowMapProgram, pointShadowProgram, pointShadowMapProgram);

        HashMap<Integer, Integer> shaderPrograms = new HashMap<>();
        shaderPrograms.put(MyGLRenderer.PROGRAM_DRAW_2D_DEPTH_BUFFER, simpleShadowMapProgram);
        shaderPrograms.put(MyGLRenderer.PROGRAM_DRAW_CUBEMAP_DEPTH_BUFFER, pointShadowMapProgram);
        shaderPrograms.put(MyGLRenderer.PROGRAM_DRAW_SCENE_SIMPLE_SHADOW, simpleShadowProgram);
        shaderPrograms.put(MyGLRenderer.PROGRAM_DRAW_SCENE_POINT_SHADOW, pointShadowProgram);

        int numberOfBlocks = 60;
        ArrayList<Point> blockPositions = new ArrayList<>(numberOfBlocks);
        int[][] blockMap = new int[30][30];
        blockMap[15][0] = 1;
        blockPositions.add(new Point(15, 0));
        Random rand = new Random();
        shiftX = 15.5f;
        shiftY = 0.0f;
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
/*
        levelMap = new LevelMap(blockPositions, shaderPrograms);


        fpsText = new OpenGLTextWriter(textDrawingShaderProgram);
        fpsText.setTextPosition(-1f, 1f, OpenGLTextWriter.TOP_LEFT);
        fpsText.setTextSize(0.15f);
        fpsText.setTextColor(0xc9eeffFF);
        fpsText.setBackgroundColor(0x00000000);
*/
        ByteBuffer bb = ByteBuffer.allocateDirect(shadowMapVertices.length * 4); // (number of coordinate values * 4 bytes per float)
        bb.order(ByteOrder.nativeOrder());
        mapDisplayVertexBuffer = bb.asFloatBuffer();
        mapDisplayVertexBuffer.put(shadowMapVertices);
        mapDisplayVertexBuffer.position(0);

        ByteBuffer bb2 = ByteBuffer.allocateDirect(shadowMapTexCoords.length * 4); // (number of coordinate values * 4 bytes per float)
        bb2.order(ByteOrder.nativeOrder());
        mapDisplayTexCoordBuffer = bb2.asFloatBuffer();
        mapDisplayTexCoordBuffer.put(shadowMapTexCoords);
        mapDisplayTexCoordBuffer.position(0);

        ByteBuffer bb3 = ByteBuffer.allocateDirect(skyboxVertices.length * 4); // (number of coordinate values * 4 bytes per float)
        bb3.order(ByteOrder.nativeOrder());
        pointMapDisplayVertexBuffer = bb3.asFloatBuffer();
        pointMapDisplayVertexBuffer.put(skyboxVertices);
        pointMapDisplayVertexBuffer.position(0);
    }

    private int[] depthMapFBO;
    private int[] depthMap;

    private void generateSimpleShadowFBO(){
        // generate Frame Buffer Object
        depthMapFBO = new int[1];
        GLES20.glGenFramebuffers(1, depthMapFBO, 0);

        // generate depth map texture
        depthMap = new int[1];
        GLES20.glGenTextures(1, depthMap, 0);

        // configure depth map texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, depthMap[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_DEPTH_COMPONENT, SHADOW_WIDTH, SHADOW_HEIGHT, 0, GLES20.GL_DEPTH_COMPONENT, GLES20.GL_UNSIGNED_INT, null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        // bind depth map texture to the FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, depthMapFBO[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_TEXTURE_2D, depthMap[0], 0);

        int FBOstatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if(FBOstatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("GL_FRAMEBUFFER_COMPLETE failed, CANNOT use FBO. Status: " + FBOstatus);
            throw new RuntimeException("GL_FRAMEBUFFER_COMPLETE failed, CANNOT use FBO");
        } else{
            System.out.println("Framebuffer created!");
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
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
    float[] lightSpaceMatrix = new float[16];
    private void createSimpleShadowMap(){

        GLES20.glViewport(0, 0, SHADOW_WIDTH, SHADOW_HEIGHT);
        float scale = 1f;
        // Matrix.orthoM(lightProjection, 0, -ratio*initialScale, ratio*initialScale, -1.0f*initialScale, 1.0f*initialScale, near_plane, far_plane);
        Matrix.frustumM(lightProjection, 0, -ratio*scale, ratio*scale, -scale, scale, 0.02f, 7f);

        Matrix.setLookAtM(lightView, 0, lightPosition[0], lightPosition[1], lightPosition[2], lightPosition[0], lightPosition[1], lightPosition[2] - 1, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(lightSpaceMatrix, 0, lightProjection, 0, lightView, 0);
        Matrix.setIdentityM(mModelMatrix, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, depthMapFBO[0]);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        //mSquareHall.drawForDepthBuffer(lightSpaceMatrix, mModelMatrix);

        Bundle params = new Bundle();
        params.putInt("shaderProgram", MyGLRenderer.PROGRAM_DRAW_2D_DEPTH_BUFFER);
        params.putFloatArray("lightSpaceMatrix", lightSpaceMatrix);
        params.putFloatArray("modelMatrix", mModelMatrix);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

//        levelMap.draw(viewPosition[0], viewPosition[1], 4*mScale, 4*mScale, params);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    ArrayList<float[]> lightViewMatrices = new ArrayList<>(6);
    ArrayList<float[]> lightSpaceMatrices = new ArrayList<>(6);
    float nearPlane = 0.001f;
    float farPlane = 5f;

    private void createPointShadowMap(){
/*
        shiftX = shiftX > 0.98f ? 0.98f : shiftX;
        shiftX = shiftX < -0.98f ? -0.98f : shiftX;
        shiftY = shiftY > 0.98f ? 0.98f : shiftY;
        shiftY = shiftY < -0.98f ? -0.98f : shiftY;

        if(shiftX < 0.502f && shiftX > -0.502f && shiftY < 0.502f && shiftY > -0.502f){
            float minDistX = Math.min(0.502f - shiftX, shiftX + 0.502f);
            float minDistY = Math.min(0.502f - shiftY, shiftY + 0.502f);
            if(minDistX < minDistY){
                shiftX = shiftX > 0 ? 0.502f : -0.502f;
            } else{
                shiftY = shiftY > 0 ? 0.502f : -0.502f;
            }
        }
*/
        //System.out.println("shift x: " + shiftX + "; shift y: " + shiftY);

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
            params.putInt("shaderProgram", MyGLRenderer.PROGRAM_DRAW_CUBEMAP_DEPTH_BUFFER);
            params.putFloatArray("lightSpaceMatrix", lightSpaceMatrices.get(i));
            params.putFloatArray("modelMatrix", mModelMatrix);
            params.putFloatArray("lightPosition", lightPosition);
            params.putFloat("farPlane", farPlane);
//            levelMap.draw(viewPosition[0], viewPosition[1], 4*mScale, 4*mScale, params);

            //printMatrix(lightSpaceMatrices.get(i), 4, 4, "      ");
            //System.out.println("-----------------");
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    private void displaySimpleShadowMap(){
        GLES20.glViewport(0, 0, width, height);
        GLES20.glUseProgram(displaySimpleShadowProgram);

        int vertexCoordHandle = MyUtilities.linkBufferToAttribute(displaySimpleShadowProgram, "a_vertex", mapDisplayVertexBuffer, 3, GLES20.GL_FLOAT, 12); // 12 = 3 coords * 4 bytes
        int textureCoordHandle = MyUtilities.linkBufferToAttribute(displaySimpleShadowProgram, "a_tex_coord", mapDisplayTexCoordBuffer, 2, GLES20.GL_FLOAT, 8); // 8 = 2 coords * 4 bytes
        int shadowMapTextureHandle = GLES20.glGetUniformLocation(displaySimpleShadowProgram, "depthMap");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, depthMap[0]);
        GLES20.glUniform1i(shadowMapTextureHandle, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, shadowMapVertices.length/3);

        GLES20.glDisableVertexAttribArray(vertexCoordHandle);
        GLES20.glDisableVertexAttribArray(textureCoordHandle);
    }
    float tempLength;
    float angle;
    float[] lookAtVector = new float[]{0f, 0f, -1f};
    private void displayPointShadowMap(){
        GLES20.glViewport(0, 0, width, height);
        GLES20.glUseProgram(displayPointShadowProgram);

        int vertexCoordHandle = MyUtilities.linkBufferToAttribute(displayPointShadowProgram, "a_vertex", pointMapDisplayVertexBuffer, 3, GLES20.GL_FLOAT, 12); // 12 = 3 coords * 4 bytes

        Matrix.perspectiveM(mProjectionMatrix, 0, 120, ratio, 0.4f, 1f);
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 0, lookAtVector[0], lookAtVector[1], lookAtVector[2], 0.0f, 1.0f, 0.0f);

        /*
        tempLength = (float)Math.sqrt(shiftX*shiftX*ratio*ratio + shiftY*shiftY);
        angle = tempLength*100;

        if(angle != 0) {
            Matrix.rotateM(mViewMatrix, 0, angle, 0, -shiftY / tempLength, - shiftX / tempLength);
        }
        */
        int viewMatrixHandle = GLES20.glGetUniformLocation(displayPointShadowProgram, "viewMatrix");
        GLES20.glUniformMatrix4fv(viewMatrixHandle, 1, false, mViewMatrix, 0);

        int projectionMatrixHandle = GLES20.glGetUniformLocation(displayPointShadowProgram, "projectionMatrix");
        GLES20.glUniformMatrix4fv(projectionMatrixHandle, 1, false, mProjectionMatrix, 0);

        int modelMatrixHandle = GLES20.glGetUniformLocation(displayPointShadowProgram, "modelMatrix");
        GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, mModelMatrix, 0);

        int pointShadowMapTextureHandle = GLES20.glGetUniformLocation(displayPointShadowProgram, "depthMap");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, pointDepthMap[0]);
        GLES20.glUniform1i(pointShadowMapTextureHandle, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, skyboxVertices.length/3);

        GLES20.glDisableVertexAttribArray(vertexCoordHandle);
    }

    private void drawSceneWithSimpleShadow(){

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

        //mSquareHall.draw(mViewMatrix, mProjectionMatrix, mModelMatrix, lightSpaceMatrix, depthMap[0], GLES20.GL_TEXTURE_2D, lightPosition, viewPosition, farPlane);

        Bundle params = new Bundle();
        params.putInt("shaderProgram", MyGLRenderer.PROGRAM_DRAW_SCENE_SIMPLE_SHADOW);
        params.putFloatArray("viewMatrix", mViewMatrix);
        params.putFloatArray("projectionMatrix", mProjectionMatrix);
        params.putFloatArray("modelMatrix", mModelMatrix);
        params.putFloatArray("lightSpaceMatrix", lightSpaceMatrix);
        params.putInt("depthMap", depthMap[0]);
        params.putFloatArray("lightPosition", lightPosition);
        params.putFloatArray("viewPosition", viewPosition);

//        levelMap.draw(viewPosition[0], viewPosition[1], 4*mScale, 4*mScale, params);

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
        params.putInt("shaderProgram", MyGLRenderer.PROGRAM_DRAW_SCENE_POINT_SHADOW);
        params.putFloatArray("viewMatrix", mViewMatrix);
        params.putFloatArray("projectionMatrix", mProjectionMatrix);
        params.putFloatArray("modelMatrix", mModelMatrix);
        params.putFloatArray("lightSpaceMatrix", lightSpaceMatrix);
        params.putInt("depthMap", pointDepthMap[0]);
        params.putFloatArray("lightPosition", lightPosition);
        params.putFloatArray("viewPosition", viewPosition);
        params.putFloat("farPlane", farPlane);

//        levelMap.draw(viewPosition[0], viewPosition[1], 4*mScale, 4*mScale, params);

        //printMatrix(finalMVPMatrix, 4, 4, "      ");
        //printMatrix(mNormalMatrix, 4, 4, "      ");

    }

    float[] lightPosition = new float[]{0f, 0f, 0.1f};
    float[] viewPosition = new float[]{0.0f, 0f, 1};

    private long lastTimeStamp = 0;
    private long currTimeStamp = 0;
    private float fps = 0;

    public void onDrawFrame(GL10 unused) {
        lastTimeStamp = currTimeStamp;
        currTimeStamp = System.currentTimeMillis();
        fps = 0.02f*(1000f/(currTimeStamp - lastTimeStamp)) + 0.98f*fps;

        updateCharacterPosition();
        // Redraw background color
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_FRONT);

        //createSimpleShadowMap();

        GLES20.glDisable(GLES20.GL_BLEND); // disable blending because I use color buffer's alpha channel (as wel as other channels) to write the point shadow map
        createPointShadowMap();
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glBlendEquation(GLES20.GL_FUNC_ADD); // this line is unnecessary, but I'll leave it here in case I want to change it later.

        GLES20.glDisable(GLES20.GL_CULL_FACE);


        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        // Draw shadow map on a rectangle filling the whole screen. Only for testing purposes
        //displaySimpleShadowMap();
        //displayPointShadowMap();

        //GLES20.glCullFace(GLES20.GL_BACK);

        //drawSceneWithSimpleShadow();
        drawSceneWithPointShadow();

        // disable depth test so the text can be displayed on top of everything
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        fpsText.setText("FPS: " + Math.round(fps));
//        fpsText.drawText();
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        int debugInfo = GLES20.glGetError();

        if (debugInfo != GLES20.GL_NO_ERROR) {
            String msg = "OpenGL error: " + debugInfo;
            System.out.println(msg);
        }
    }

    private int width;
    private int height;
    float ratio;
    float shadowMapRatio = 1.0f;

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        this.width = width;
        this.height = height;

        fpsText.setWindowDimensions(width, height);

        SHADOW_HEIGHT = Math.round(height/shadowMapRatio);
        SHADOW_WIDTH = Math.round(width/shadowMapRatio);

        //GLES20.glViewport(0, 0, width, height);

        ratio = (float) width / height;
        //generateSimpleShadowFBO();
        generatePointShadowFBO();

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

    private volatile float shiftX = 0.5f;
    private volatile float shiftY = 0.5f;

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

    private PointF moveVector = new PointF();
    private PointF aimPosition = new PointF();
    private boolean isMoving = false;
    private boolean isAiming = false;

    private static final float maxDisplacement = 0.2f;
    private static final float maxSpeed = 0.001f; // =distance/millisecond
    float currDisplacement = 0;
    float currMoveLength = 0;
    PointF desiredDestination = new PointF();
    PointF currentPosition = new PointF();

    private void updateCharacterPosition(){
        if(isMoving){
            currMoveLength = moveVector.length();
            if(currMoveLength > 0) {
                currDisplacement = Math.min(maxDisplacement, Math.min(1, moveVector.length()) * maxSpeed * (currTimeStamp - lastTimeStamp));
                currentPosition.x = shiftX;
                currentPosition.y = shiftY;
                desiredDestination.x = shiftX + moveVector.x / currMoveLength * currDisplacement;
                desiredDestination.y = shiftY + moveVector.y / currMoveLength * currDisplacement;

                System.out.println("Start: (" + currentPosition.x + ", " + currentPosition.y + "). Desired destination: (" + desiredDestination.x + ", " + desiredDestination.y + "); ");

                desiredDestination = levelMap.getCorrectedDestination(currentPosition, desiredDestination, 100, 0.1f);

                //System.out.println("corrected destination: (" + desiredDestination.x + ", " + desiredDestination.y + ");");

                shiftX = desiredDestination.x;
                shiftY = desiredDestination.y;
            }
        }
        if(isAiming){

        }
        viewPosition[0] = shiftX;
        viewPosition[1] = shiftY;

        lightPosition[0] = viewPosition[0];
        lightPosition[1] = viewPosition[1];
        //System.out.println("shift: (" + shiftX + ", " + shiftY + ");");
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
}
