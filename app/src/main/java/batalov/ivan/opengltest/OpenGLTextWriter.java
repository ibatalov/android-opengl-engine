package batalov.ivan.opengltest;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Build;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;

public class OpenGLTextWriter {
    //private int shaderProgram;

    private static final int FIRST_ASCII_CHAR = 32;
    private static final int LAST_ASCII_CHAR = 126;

    public static final int TOP_LEFT = 0;
    public static final int BOTTOM_LEFT = 1;
    public static final int TOP_RIGHT = 2;
    public static final int BOTTOM_RIGHT = 3;

    private int windowWidth;
    private int windowHeight;
    private float aspectRatio;
    private int textSizeCoeff;

    private float textSize = 0.08f; // font size as a fraction of the min window dimension
    private float lineShift;
    private String oldText;
    private String text = "test";
    private int textColor;
    private int backgroundColor;
    private Canvas canvas;
    private Bitmap fontMap;
    private int[] fontMapTexture = new int[1];
    private Paint textPaint;
    private static char[] fontMapText;
    private float[] charAdvanceWidths = new float[LAST_ASCII_CHAR - FIRST_ASCII_CHAR + 1];
    private float[] charPositionsX = new float[LAST_ASCII_CHAR - FIRST_ASCII_CHAR + 1];
    private float[] charPositionsY = new float[LAST_ASCII_CHAR - FIRST_ASCII_CHAR + 1];

    boolean fontMapNeedsUpdating = true;
    boolean textChanged = true;
    private float textPositionX;
    private float textPositionY;
    private int textAlignment;

    private HashMap<String, float[]> vertexArrays = new HashMap<>();
    private HashMap<String, Buffer> vertexBuffers = new HashMap<>();

    static{
        createMapText();
    }

    public OpenGLTextWriter(){
        //this.shaderProgram = shaderProgram;

        textPaint = new Paint();
        canvas = new Canvas();
        textPaint.setAntiAlias(true);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextAlign(Paint.Align.LEFT);
        /*
        if(Build.VERSION.SDK_INT >= 17) {
            textPaint.setTypeface(Typeface.create("sans-serif-regular", Typeface.NORMAL));
        }
        */
        GLES20.glGenTextures(1, fontMapTexture, 0);
    }

    public static void createMapText(){
        // 33 = !
        // 126 = ~
        fontMapText = new char[LAST_ASCII_CHAR-FIRST_ASCII_CHAR+1];
        for(int i = FIRST_ASCII_CHAR; i <= LAST_ASCII_CHAR; i++){
            fontMapText[i - FIRST_ASCII_CHAR] = (char) i;
        }
    }

    public void createFontMap(){
        if(fontMap != null && !fontMap.isRecycled()){
            fontMap.recycle();
        }
        int widthCount = textPaint.getTextWidths(fontMapText, 0, fontMapText.length, charAdvanceWidths);
        if(widthCount != fontMapText.length){
            System.out.println("Advance width count doesn't match with the expected one!");
            try {
                throw new RuntimeException();
            } catch (RuntimeException e){
                e.printStackTrace();
            }
        }
        Rect bounds = new Rect();
        textPaint.getTextBounds(fontMapText, 0, fontMapText.length, bounds);
        int n = (int)Math.ceil(Math.sqrt(bounds.width()/bounds.height()));

        fontMap = Bitmap.createBitmap(bounds.width()/n, (int)Math.ceil(textPaint.getFontSpacing()*(n+1) + lineShift), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(fontMap);
        canvas.drawColor(0x00000000); // transparent black

        textPaint.getTextBounds("(0Gg", 0, 4, bounds);
        System.out.println("left: " + bounds.left + "; top: " + bounds.top + "; right: " + bounds.right + "; bottom: " + bounds.bottom);
        lineShift = bounds.bottom + 0.5f*(textPaint.getFontSpacing() + bounds.top);
        System.out.println("line shift: " + lineShift + "; additional shift: " + 0.5f*(textPaint.getFontSpacing() + bounds.top));

        float usedWidth = 0;
        int lineNum = 0;
        int lastDrawnChar = -1;
        for(int i = 0; i < fontMapText.length; i++){
            if (usedWidth + charAdvanceWidths[i] < fontMap.getWidth()) {
                charPositionsX[i] = usedWidth;
                charPositionsY[i] = textPaint.getFontSpacing() * (lineNum) + lineShift;
                canvas.drawText(fontMapText, i, 1, usedWidth, textPaint.getFontSpacing() * (lineNum + 1), textPaint);
                usedWidth += charAdvanceWidths[i] + 1;
            } else {
                // current character would extend beyond the image bound.
                lineNum++;
                charPositionsX[i] = 0;
                charPositionsY[i] = textPaint.getFontSpacing() * (lineNum) + lineShift;
                canvas.drawText(fontMapText, i, 1, 0, textPaint.getFontSpacing() * (lineNum + 1), textPaint);
                usedWidth = charAdvanceWidths[i] + 1;
            }
        }

        // load (bind) the texture into OpenGL
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fontMapTexture[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, fontMap, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        System.out.println("Font map created. Bitmap size: " + fontMap.getWidth() + " x " + fontMap.getHeight());

    }

    public void setupTextForDrawing(){
        // find each character in the map
        // create a rectangle to draw the texture on (add verctives to the array
        // add texture coords
        float[] vertexCoords = new float[text.length()*18]; // 3 coords per vertex * 6 vertices per character
        float[] texCoords = new float[text.length()*12]; // 2 coords per vertex * 6 vertices per character

        vertexArrays.put("a_vertex", vertexCoords);
        vertexArrays.put("a_tex_coord", texCoords);

        float lineHeight = textPaint.getFontSpacing() / textSizeCoeff * aspectRatio; // in OpenGL coordinates

        float topLeftX = textPositionX;
        float topLeftY = textPositionY;

        for(int i = 0; i < text.length(); i++){
            char c = text.charAt(i);
            int charNum = (int)c - FIRST_ASCII_CHAR;

            float charWidth = charAdvanceWidths[charNum] / textSizeCoeff;
            //top left vertex
            vertexCoords[18*i] = topLeftX;
            vertexCoords[18*i + 1] = topLeftY;
            vertexCoords[18*i + 2] = 0;
            //bottom right vertex
            vertexCoords[18*i + 3] = topLeftX + charWidth;
            vertexCoords[18*i + 4] = topLeftY - lineHeight;
            vertexCoords[18*i + 5] = 0;
            //top right vertex
            vertexCoords[18*i + 6] = topLeftX + charWidth;
            vertexCoords[18*i + 7] = topLeftY;
            vertexCoords[18*i + 8] = 0;
            //top left vertex
            vertexCoords[18*i + 9] = topLeftX;
            vertexCoords[18*i + 10] = topLeftY;
            vertexCoords[18*i + 11] = 0;
            //bottom left vertex
            vertexCoords[18*i + 12] = topLeftX;
            vertexCoords[18*i + 13] = topLeftY - lineHeight;
            vertexCoords[18*i + 14] = 0;
            //bottom right vertex
            vertexCoords[18*i + 15] = topLeftX + charWidth;
            vertexCoords[18*i + 16] = topLeftY - lineHeight;
            vertexCoords[18*i + 17] = 0;

            // texture coordinates are the same as the regular android screen coordinates
            float texLeft = charPositionsX[charNum]/fontMap.getWidth();
            float texTop = charPositionsY[charNum]/fontMap.getHeight();
            float texRight = texLeft + charAdvanceWidths[charNum]/fontMap.getWidth();
            float texBottom = texTop + textPaint.getFontSpacing()/fontMap.getHeight();
            // vertex 1
            texCoords[i*12] = texLeft;
            texCoords[i*12 + 1] = texTop;
            // vertex 2
            texCoords[i*12 + 2] = texRight;
            texCoords[i*12 + 3] = texBottom;
            // vertex 3
            texCoords[i*12 + 4] = texRight;
            texCoords[i*12 + 5] = texTop;
            // vertex 4
            texCoords[i*12 + 6] = texLeft;
            texCoords[i*12 + 7] = texTop;
            // vertex 5
            texCoords[i*12 + 8] = texLeft;
            texCoords[i*12 + 9] = texBottom;
            // vertex 6
            texCoords[i*12 + 10] = texRight;
            texCoords[i*12 + 11] = texBottom;

            topLeftX += charWidth;
        }

        // create buffers for arrays
        ByteBuffer bb = ByteBuffer.allocateDirect(vertexCoords.length * 4); // (number of coordinate values * 4 bytes per float)
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertexCoords);
        vertexBuffer.position(0);
        vertexBuffers.put("a_vertex", vertexBuffer);

        ByteBuffer bb2 = ByteBuffer.allocateDirect(texCoords.length * 4); // (number of coordinate values * 4 bytes per float)
        bb2.order(ByteOrder.nativeOrder());
        FloatBuffer texBuffer = bb2.asFloatBuffer();
        texBuffer.put(texCoords);
        texBuffer.position(0);
        vertexBuffers.put("a_tex_coord", texBuffer);
    }

    public void prepareTextForDrawing(){
        if(fontMapNeedsUpdating){
            createFontMap();
            setupTextForDrawing();
            fontMapNeedsUpdating = false;
            textChanged = false;
        } else if(textChanged){
            setupTextForDrawing();
            textChanged = false;
        }
    }

    /*
    public void drawText(){

        if(shaderProgram > 0 && fontMapTexture[0] > 0) {
            if(fontMapNeedsUpdating){
                createFontMap();
                setupTextForDrawing();
                fontMapNeedsUpdating = false;
                textChanged = false;
            } else if(textChanged){
                setupTextForDrawing();
                textChanged = false;
            }

            GLES20.glUseProgram(shaderProgram);

            int vertexCoordHandle = SquareHall.linkBufferToAttribute(shaderProgram, "a_vertex", vertexBuffer, 3, GLES20.GL_FLOAT, 12); // 12 = 3 coords * 4 bytes
            int textureCoordHandle = SquareHall.linkBufferToAttribute(shaderProgram, "a_tex_coord", texBuffer, 2, GLES20.GL_FLOAT, 8); // 8 = 2 coords * 4 bytes

            int textColorHandle = GLES20.glGetUniformLocation(shaderProgram, "textColor");
            GLES20.glUniform4fv(textColorHandle, 1, textColorVector, 0);

            int backgroundColorHandle = GLES20.glGetUniformLocation(shaderProgram, "backgroundColor");
            GLES20.glUniform4fv(backgroundColorHandle, 1, backgroundColorVector, 0);

            //System.out.println("text color handle: " + textColorHandle + "; background color handle: " + backgroundColorHandle);

            int fontMapTextureHandle = GLES20.glGetUniformLocation(shaderProgram, "fontMap");
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fontMapTexture[0]);
            GLES20.glUniform1i(fontMapTextureHandle, 0);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCoords.length / 3);

            //System.out.println("vertex coord handle: " + vertexCoordHandle + "; tex coord handle: " + textureCoordHandle + "; font map texture handle: " +  fontMapTextureHandle);

            GLES20.glDisableVertexAttribArray(vertexCoordHandle);
            GLES20.glDisableVertexAttribArray(textureCoordHandle);
        }
    }
*/


    public String getText() {
        return text;
    }

    public void setText(String text) {
        oldText = this.text;
        this.text = text;
        textChanged = true;
    }

    public Paint getTextPaint() {
        return textPaint;
    }

    public void setTextPaint(Paint textPaint) {
        this.textPaint = textPaint;
        fontMapNeedsUpdating = true;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
        if(textSizeCoeff > 0) {
            textPaint.setTextSize(textSize * textSizeCoeff);
        }
        fontMapNeedsUpdating = true;
    }

    public void setWindowDimensions(int width, int height){
        this.windowWidth = width;
        this.windowHeight = height;
        textSizeCoeff = Math.min(width, height)/2;
        aspectRatio = (float)width/height;
        textPaint.setTextSize(textSize * textSizeCoeff);
        if(textSize > 0){
            textPaint.setTextSize(textSize * textSizeCoeff);
        }
    }

    public int getTextAlignment() {
        return textAlignment;
    }

    public void setTextAlignment(int textAlignment) {
        this.textAlignment = textAlignment;
    }

    public float getTextPositionX() {
        return textPositionX;
    }

    public void setTextPositionX(float textPositionX) {
        this.textPositionX = textPositionX;
        textChanged = true;
    }

    public float getTextPositionY() {
        return textPositionY;
    }

    public void setTextPositionY(float textPositionY) {
        this.textPositionY = textPositionY;
        textChanged = true;
    }

    public void setTextPosition(float x, float y, int alignment){
        this.textPositionX = x;
        this.textPositionY = y;
        this.textAlignment = alignment;
        textChanged = true;
    }

    private float[] textColorVector = new float[]{1f, 1f, 1f, 1f};
    private float[] backgroundColorVector = new float[]{0f, 0f, 0f, 0f};

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        textColorVector = colorToRGBA(textColor);
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        backgroundColorVector = colorToRGBA(backgroundColor);
    }

    public static float[] colorToRGBA(int color){
        float[] colorVector = new float[4];
        colorVector[0] = (float)((color >> 24) & 0xFF)/0xFF;
        colorVector[1] = (float)((color >> 16) & 0xFF)/0xFF;
        colorVector[2] = (float)((color >> 8) & 0xFF)/0xFF;
        colorVector[3] = (float)(color & 0xFF)/0xFF;
        return colorVector;
    }

    public FloatBuffer getVertexBuffer() {
        return (FloatBuffer)vertexBuffers.get("a_vertex");
    }

    public float[] getVertexCoords() {
        return vertexArrays.get("a_vertex");
    }

    public float[] getTexCoords() {
        return vertexArrays.get("a_tex_coords");
    }

    public FloatBuffer getTexBuffer() {
        return (FloatBuffer)vertexBuffers.get("a_tex_coord");
    }

    public int getFontMapTexture() {
        return fontMapTexture[0];
    }

    public float[] getTextColorVector() {
        return textColorVector;
    }

    public float[] getBackgroundColorVector() {
        return backgroundColorVector;
    }

    public HashMap<String, float[]> getVertexArrays() {
        return vertexArrays;
    }

    public HashMap<String, Buffer> getVertexBuffers() {
        return vertexBuffers;
    }

    /* OLD VERSION, where I draw the font map one line at a time. It results in slight overlap between letters, so I abandoned it. Starting at android version 21, I can change the letter spacing to solve this problem.

        public void createFontMap(){
        if(fontMap != null && !fontMap.isRecycled()){
            fontMap.recycle();
        }
        int widthCount = textPaint.getTextWidths(fontMapText, 0, fontMapText.length, charAdvanceWidths);
        if(widthCount != fontMapText.length){
            System.out.println("Advance width count doesn't match with the expected one!");
            try {
                throw new RuntimeException();
            } catch (RuntimeException e){
                e.printStackTrace();
            }
        }
        Rect bounds = new Rect();
        textPaint.getTextBounds(fontMapText, 0, fontMapText.length, bounds);
        int n = (int)Math.ceil(Math.sqrt(bounds.width()/bounds.height()));

        fontMap = Bitmap.createBitmap(bounds.width()/n, (int)Math.ceil(textPaint.getFontSpacing()*(n+1) + lineShift), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(fontMap);
        canvas.drawColor(0x00000000); // transparent black

        textPaint.getTextBounds("Gg", 0, 2, bounds);
        System.out.println("left: " + bounds.left + "; top: " + bounds.top + "; right: " + bounds.right + "; bottom: " + bounds.bottom);
        lineShift = bounds.bottom;

        float usedWidth = 0;
        int lineNum = 0;
        int lastDrawnChar = -1;
        for(int i = 0; i < fontMapText.length; i++){

            if (usedWidth + charAdvanceWidths[i] < fontMap.getWidth()) {
                charPositionsX[i] = usedWidth;
                charPositionsY[i] = textPaint.getFontSpacing() * (lineNum) + lineShift;
                usedWidth += charAdvanceWidths[i];
            } else {
                // current character would extend beyond the image bound. Draw the line up to this char.
                int firstChar = lastDrawnChar + 1;
                int charLength = i - firstChar;
                canvas.drawText(fontMapText, firstChar, charLength, 0, textPaint.getFontSpacing() * (lineNum + 1), textPaint);

                usedWidth = charAdvanceWidths[i];
                lastDrawnChar = i - 1;
                lineNum++;
                charPositionsX[i] = 0;
                charPositionsY[i] = textPaint.getFontSpacing() * (lineNum) + lineShift;
            }
            // draw the last line if reached the end of the loop
            if(i == fontMapText.length - 1) {
                int firstChar = lastDrawnChar + 1;
                int charLength = i - firstChar + 1;
                canvas.drawText(fontMapText, firstChar, charLength, 0, textPaint.getFontSpacing() * (lineNum + 1), textPaint);
            }
        }

        System.out.println("Font map created. Bitmap size: " + fontMap.getWidth() + " x " + fontMap.getHeight());

    }

     */
}
