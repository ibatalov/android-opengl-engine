package batalov.ivan.opengltest.models;

import android.opengl.GLES20;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class SquareHall {

    private FloatBuffer vertexBuffer;
    private FloatBuffer normalBuffer;
    private FloatBuffer colorBuffer;

    public static final String vertexShaderCode =

            "uniform mat4 viewMatrix;" +
                    "uniform mat4 projectionMatrix;" +
                    "uniform mat4 modelMatrix;" +
                    "attribute vec3 a_vertex;" +
                    "attribute vec3 a_normal;"+
                    "attribute vec4 a_color;"+

                    "varying vec3 v_vertex;"+
                    "varying vec3 v_normal;"+
                    "varying vec4 v_color;"+

                    "void main() {" +
                    "   gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(a_vertex, 1.0);" +
                    "   v_vertex = vec3(modelMatrix * vec4(a_vertex, 1.0));" +
                    "   v_normal = normalize(mat3(modelMatrix) * a_normal);" +
                    //     "   v_normal = normalize(a_normal);" +
                    "   v_color = a_color;" +
                    "}";

    public static final String fragmentShaderCode =
            "precision mediump float;" +

                    "uniform mat4 viewMatrix;" +
                    "uniform mat4 projectionMatrix;" +
                    "uniform mat4 modelMatrix;" +
                    "uniform vec3 viewPosition;" +
                    "uniform vec3 lightPosition;" +

                    "varying vec3 v_vertex;" +
                    "varying vec3 v_normal;" +
                    "varying vec4 v_color;" +

                    "void main() {" +
                    "   vec3 lightDirection = mat3(projectionMatrix * viewMatrix)*lightPosition - v_vertex;" +
                    "   float lightDistance = dot(lightDirection, lightDirection);" +
                    "   lightDirection = normalize(lightDirection);" +
                    "   vec3 viewDirection = normalize(viewPosition - v_vertex);" +
                    "   vec3 halfWayVector = normalize(viewDirection + lightDirection);" +
                    "   vec3 lightColor = vec3(1.0, 1.0, 1.0);" +

                    "   float ambient = 0.1;" +

                    "   float diffuseStrength = 4.0/lightDistance;" +
                    "   float diffuse = max(dot(v_normal, lightDirection), 0.0) * diffuseStrength;" +

                    "   float specularStrength = 10.0/lightDistance;" +
                    "   vec3 reflectedDirection = reflect(-lightDirection, v_normal);" +
                    "   float specular = pow(max(dot(viewDirection, halfWayVector), 0.0), 16.0) * specularStrength;" +

                    "   gl_FragColor = vec4((min((ambient + diffuse + specular), 1.0) * lightColor), 1.0) * v_color;" +
                    //        "   gl_FragColor.rgb = pow(gl_FragColor.rgb, vec3(1.0/2.2));" +

                    "}";

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float vertexCoords[] = {   // in counterclockwise order:
            0.99f, 0.99f, 0,            // 0  top NE outer corner
            -0.99f, 0.99f, 0,           // 1  top NW outer corner
            -0.99f, -0.99f, 0,          // 2  top SW outer corner
            0.99f, -0.99f, 0,           // 3  top SE outer corner
            0.99f, 0.99f, -0.99f,           // 4  bottom NE outer corner
            -0.99f, 0.99f, -0.99f,          // 5  bottom NW outer corner
            -0.99f, -0.99f, -0.99f,         // 6  bottom SW outer corner
            0.99f, -0.99f, -0.99f,          // 7  bottom SE outer corner
            0.5f, 0.5f, 0,      // 8  top NE inner corner
            -0.5f, 0.5f, 0,     // 9  top NW inner corner
            -0.5f, -0.5f, 0,    // 10 top SW inner corner
            0.5f, -0.5f, 0,     // 11 top SE inner corner
            0.5f, 0.5f, -0.99f,     // 12 bottom NE inner corner
            -0.5f, 0.5f, -0.99f,    // 13 bottom NW inner corner
            -0.5f, -0.5f, -0.99f,   // 14 bottom SW inner corner
            0.5f, -0.5f, -0.99f,    // 15 bottom SE inner corner
    };

    private short drawOrder[] = {4, 5, 6, 4, 6, 7, 0, 1, 5, 0, 5, 4, 1, 2, 6, 1, 6, 5, 2, 3, 7, 2, 7, 6, 3, 0, 4, 3,
            4, 7, 9, 8, 12, 9, 12, 13, 9, 13, 10, 13, 14, 10, 15, 11, 10, 15, 10, 14, 8, 11, 15, 8,
            15, 12, 8, 10, 11, 8, 9, 10 }; // order to draw vertices

    private float[] finalVertexCoords;
    private float[] normalCoords;
    private float[] vertexColors;

    private final int mProgram;
    private final int mShadowProgram;
    private final int mPointShadowProgram;
    private final int mPointShadowMapProgram;

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 1f, 1f, 1f, 1.0f };

    public SquareHall(int regularShaderProgram, int shadowShaderProgram, int pointShadowProgram, int pointShadowMapProgram) {

        mProgram = regularShaderProgram;
        mShadowProgram = shadowShaderProgram;
        mPointShadowProgram = pointShadowProgram;
        mPointShadowMapProgram = pointShadowMapProgram;

        finalVertexCoords = new float[drawOrder.length * 3];
        normalCoords = new float[drawOrder.length * 3];
        vertexColors = new float[drawOrder.length * 4];

        // loop over each triangle to compose all the necessary vertex data arrays
        for (int triangleNum = 0; triangleNum < drawOrder.length / 3; triangleNum++) {
            for (int vertexNum = 0; vertexNum < 3; vertexNum++) {
                finalVertexCoords[9 * triangleNum + 3 * vertexNum] = vertexCoords[3*drawOrder[3*triangleNum + vertexNum]];
                finalVertexCoords[9 * triangleNum + 3 * vertexNum + 1] = vertexCoords[3*drawOrder[3*triangleNum + vertexNum] + 1];
                finalVertexCoords[9 * triangleNum + 3 * vertexNum + 2] = vertexCoords[3*drawOrder[3*triangleNum + vertexNum] + 2];
            }

            float[] normal = getNormalVector(Arrays.copyOfRange(finalVertexCoords, 9*triangleNum, 9*(triangleNum+1)));
            for (int j = 0; j < 3; j++) {
                normalCoords[9 * triangleNum + 3 * j] = normal[0];
                normalCoords[9 * triangleNum + 3 * j + 1] = normal[1];
                normalCoords[9 * triangleNum + 3 * j + 2] = normal[2];

                vertexColors[12 * triangleNum + 4 * j] = color[0];
                vertexColors[12 * triangleNum + 4 * j + 1] = color[1];
                vertexColors[12 * triangleNum + 4 * j + 2] = color[2];
                vertexColors[12 * triangleNum + 4 * j + 3] = color[3];
            }
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(finalVertexCoords.length * 4); // (number of coordinate values * 4 bytes per float)
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(finalVertexCoords);
        vertexBuffer.position(0);

        ByteBuffer bb2 = ByteBuffer.allocateDirect(normalCoords.length * 4); // (number of coordinate values * 4 bytes per float)
        bb2.order(ByteOrder.nativeOrder());
        normalBuffer = bb2.asFloatBuffer();
        normalBuffer.put(normalCoords);
        normalBuffer.position(0);

        ByteBuffer bb3 = ByteBuffer.allocateDirect(vertexColors.length * 4); // (number of coordinate values * 4 bytes per float)
        bb3.order(ByteOrder.nativeOrder());
        colorBuffer = bb3.asFloatBuffer();
        colorBuffer.put(vertexColors);
        colorBuffer.position(0);

/*        System.out.println("vertex coordinates:");
        for(int i = 0; i < finalVertexCoords.length; i++){
            System.out.print(finalVertexCoords[i] + "   ");
            if(i > 0 && (i + 1) % 3 == 0){
                System.out.println();
            }
        }
        System.out.println();

        System.out.println("vertex normals:");
        for(int i = 0; i < normalCoords.length; i++){
            System.out.print(normalCoords[i] + "   ");
            if(i > 0 && (i + 1) % 3 == 0){
                System.out.println();
            }
        }
        System.out.println();        System.out.println("vertex colors:");
        for(int i = 0; i < vertexColors.length; i++){
            System.out.print(vertexColors[i] + "   ");
            if(i > 0 && (i + 1) % 4 == 0){
                System.out.println();
            }
        }
        System.out.println();
*/
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

    private int mColorHandle;
    long frameNum = 1;

    public void draw(float[] viewMatrix, float[] projectionMatrix, float[] modelMatrix, float[] lightSpaceMatrix, int depthMap, int depthMapTarget, float[] lightPosition, float[] viewPosition, float farPlane) { // pass in the calculated transformation matrix

        int program = 0;
        if(depthMapTarget == GLES20.GL_TEXTURE_2D){
            program = mProgram;

            GLES20.glUseProgram(program);
            int lightSpaceMatrixHandle = GLES20.glGetUniformLocation(program, "lightSpaceMatrix");
            GLES20.glUniformMatrix4fv(lightSpaceMatrixHandle, 1, false, lightSpaceMatrix, 0);

        } else if(depthMapTarget == GLES20.GL_TEXTURE_CUBE_MAP){
            program = mPointShadowProgram;

            GLES20.glUseProgram(program);
            int farPlaneHandle = GLES20.glGetUniformLocation(program, "far_plane");
            GLES20.glUniform1f(farPlaneHandle, farPlane);

            //System.out.println("far plane: " + farPlaneHandle);
        } else{
            System.out.println("WRONG TEXTURE TARGET PROVIDED. ONLY GLES20.TEXTURE_2D AND GLES20.TEXTURE_CUBE_MAP ARE SUPPORTED.");
        }

        int vertexCoordHandle = linkBufferToAttribute(program, "a_vertex", vertexBuffer, 3, GLES20.GL_FLOAT, 12); // 12 = 3 coords * 4 bytes
        int normalHandle = linkBufferToAttribute(program, "a_normal", normalBuffer, 3, GLES20.GL_FLOAT, 12); // 12 = 3 coords * 4 bytes
        int vertexColorHandle = linkBufferToAttribute(program, "a_color", colorBuffer, 4, GLES20.GL_FLOAT, 16); // 12 = 3 coords * 4 bytes

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
        GLES20.glBindTexture(depthMapTarget, depthMap);
        GLES20.glUniform1i(shadowMapTextureHandle, 0);

        //System.out.println("light position: " + lightPositionHandle + "; viewPosition: " + viewPositionHandle + "; viewMatrix: " + viewMatrixHandle + "; projectionMatrix: " + projectionMatrixHandle + "; modelMatrix: " + modelMatrixHandle);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, finalVertexCoords.length/3);

        GLES20.glDisableVertexAttribArray(vertexCoordHandle);
        GLES20.glDisableVertexAttribArray(normalHandle);
        GLES20.glDisableVertexAttribArray(vertexColorHandle);

    }

    public void drawForDepthBuffer(float[] lightSpaceMatrix, float[] modelMatrix){
        GLES20.glUseProgram(mShadowProgram);
        int vertexCoordHandle = linkBufferToAttribute(mShadowProgram, "a_vertex", vertexBuffer, 3, GLES20.GL_FLOAT, 12); // 12 = 3 coords * 4 bytes

        int lightSpaceMatrixHandle = GLES20.glGetUniformLocation(mShadowProgram, "lightSpaceMatrix");
        GLES20.glUniformMatrix4fv(lightSpaceMatrixHandle, 1, false, lightSpaceMatrix, 0);

        int modelMatrixHandle = GLES20.glGetUniformLocation(mShadowProgram, "modelMatrix");
        GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, modelMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, finalVertexCoords.length/3);
        //GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES20.glDisableVertexAttribArray(vertexCoordHandle);

        //System.out.println("vertexCoordHandle: " + vertexCoordHandle);
        //System.out.println("lightSpaceMatrixHandle: " + lightSpaceMatrixHandle);
        //System.out.println("modelMatrixHandle: " + modelMatrixHandle);

    }

    public void drawForPointDepthBuffer(float[] lightSpaceMatrix, float[] modelMatrix, float[] lightPosition, float farPlane){
        GLES20.glUseProgram(mPointShadowMapProgram);
        int vertexCoordHandle = linkBufferToAttribute(mPointShadowMapProgram, "a_vertex", vertexBuffer, 3, GLES20.GL_FLOAT, 12); // 12 = 3 coords * 4 bytes

        int lightSpaceMatrixHandle = GLES20.glGetUniformLocation(mPointShadowMapProgram, "lightSpaceMatrix");
        GLES20.glUniformMatrix4fv(lightSpaceMatrixHandle, 1, false, lightSpaceMatrix, 0);

        int modelMatrixHandle = GLES20.glGetUniformLocation(mPointShadowMapProgram, "modelMatrix");
        GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, modelMatrix, 0);

        int lightPositionHandle = GLES20.glGetUniformLocation(mPointShadowMapProgram, "lightPosition");
        GLES20.glUniform3fv(lightPositionHandle, 1, lightPosition, 0);

        int farPlaneHandle = GLES20.glGetUniformLocation(mPointShadowMapProgram, "far_plane");
        GLES20.glUniform1f(farPlaneHandle, farPlane);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, finalVertexCoords.length/3);
        //GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES20.glDisableVertexAttribArray(vertexCoordHandle);

        //System.out.println("vertexCoordHandle: " + vertexCoordHandle);
        //System.out.println("lightSpaceMatrixHandle: " + lightSpaceMatrixHandle);
        //System.out.println("modelMatrixHandle: " + modelMatrixHandle);

    }

    /**
     * Links a buffer with an attribute
     * @param attrName
     * @param buffer
     * @param program
     */
    public static int linkBufferToAttribute(int program, String attrName, Buffer buffer, int sizePerVertex, int dataType, int stride){
        //System.out.println("blip");
        int attrPositionHandle = GLES20.glGetAttribLocation(program, attrName);
        GLES20.glEnableVertexAttribArray(attrPositionHandle);
        GLES20.glVertexAttribPointer(attrPositionHandle, sizePerVertex, dataType, false, stride, buffer);
        return attrPositionHandle;
    }
}