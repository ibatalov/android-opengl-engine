package batalov.ivan.opengltest;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * Created by ivan on 10/8/17.
 */

class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;
	private float rectWidth = 0.6f;
	private float rectHeight = 0.6f;
	private float rectPosX = 0.5f;
	private float rectPosY = -0.5f;

	private float[] ringRadii = new float[]{0.15f, 0.4f};
	private float[] ringColors = new float[]{
			1f, 43f/255f, 43f/255f, 1,
	//		1f, 233f/255f, 42f/255f, 1,
			105f/255f, 1, 41f/255f, 1
	};
	private float[] arrowHeadDimensions = new float[]{0.05f, 0.05f};

	private float rectThickness = 0.008f;
	private float[] rectColor = new float[]{0f, 97f/255f, 1.0f, 1.0f};

    public MyGLSurfaceView(Context context){
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 24, 0);

        mRenderer = new MyGLRenderer(context);
	    mRenderer.createMoveAreaRect(rectWidth, rectHeight, rectPosX, rectPosY, rectThickness, rectColor);
	    mRenderer.createMoveArrow(ringRadii, ringColors, rectThickness, arrowHeadDimensions);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data.
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        //setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    private float startX;
    private float startY;

    float characterX = 0.5f; // character position on the screen (normalized)
    float characterY = 0.5f;
    float characterRadius = 0.1f;

    public static final int MODE_NONE = 0;
    public static final int MODE_MOVE = 1;
    public static final int MODE_AIM = 2;
    public static final int MODE_ZOOM = 3;

    int mode1 = 0;
    int mode2 = 0;

    int pointerID1 = 0;
    int pointerID2 = 0;

    float initialScale;
    float scale;
    float initialZoomDist = 0;

    PointF moveVector = new PointF();
    float moveVectorLength;

	PointF currentPoint = new PointF();


	/**
	 *  x, y in android reference frame from 0 to getHeight() or gerWidth()
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean isWithinTouchRect(float x, float y){
		if(Math.abs(2f*x/minDim - 1f - rectPosX) < rectWidth/2 && Math.abs(maxDim/minDim*(1 - rectPosY) - 2f*y/minDim) < rectHeight/2){
			return true;
		}
		return false;
	}

	private float minDim;
	private float maxDim;

	private int currID;
	private float currX;
	private float currY;
	private float moveSpeed;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        minDim = Math.min(getWidth(), getHeight());
	    maxDim = Math.max(getWidth(), getHeight());

	    currID = e.getPointerId(e.getActionIndex());
	    currX = e.getX(e.findPointerIndex(currID));
	    currY = e.getY(e.findPointerIndex(currID));

        if(e.getActionMasked() == MotionEvent.ACTION_DOWN){
            pointerID1 = e.getPointerId(e.getActionIndex());
            //System.out.println("action_down");
            if(isWithinTouchRect(e.getX(), e.getY())){
                mode1 = MODE_MOVE;
                startX = currX;
                startY = currY;
	            mRenderer.setMoveTouchStart(2*(currX - 0.5f*getWidth())/minDim, 2*(0.5f*getHeight() - currY)/minDim);
	            setMoveSpeed(moveVector);
            } else{
                mode1 = MODE_AIM;
                mRenderer.setAiming(true);
                mRenderer.setAimPosition(2*(currX - characterX*getWidth())/minDim, 2*(characterY*getHeight() - currY)/minDim);
            }
        } else if(e.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN){
	        //System.out.println("action_pointer_down. pointer count: " + e.getPointerCount());
	        if(e.getPointerCount() == 2){
		        if(mode1 == MODE_ZOOM || mode2 == MODE_ZOOM || ((mode1 == MODE_AIM || mode2 == MODE_AIM) &&  !isWithinTouchRect(currX, currY))){
			        if(mode1 == MODE_NONE){
				        pointerID1 = currID;
			        } else if(mode2 == MODE_NONE){
				        pointerID2 = currID;
			        }
			        mode1 = MODE_ZOOM;
			        mode2 = MODE_ZOOM;
			        mRenderer.setAiming(false);
			        initialScale = mRenderer.getScale();
			        initialZoomDist = PointF.length(e.getX(0) - e.getX(1), e.getY(0) - e.getY(1));
		        } else{
			        if(mode1 == MODE_MOVE){
				        mode2 = MODE_AIM;
				        pointerID2 = currID;
				        mRenderer.setAiming(true);
				        mRenderer.setAimPosition(2*(currX - characterX*getWidth())/minDim, 2*(characterY*getHeight() - currY)/minDim);

			        } else if(mode2 == MODE_MOVE){
				        mode1 = MODE_AIM;
				        pointerID1 = currID;
				        mRenderer.setAiming(true);
				        mRenderer.setAimPosition(2*(currX - characterX*getWidth())/minDim, 2*(characterY*getHeight() - currY)/minDim);

			        } else if(isWithinTouchRect(currX, currY)){
				        setMoveSpeed(moveVector);
				        startX = currX;
				        startY = currY;
				        mRenderer.setMoveTouchStart(2*(currX - 0.5f*getWidth())/minDim, 2*(0.5f*getHeight() - currY)/minDim);

				        if(mode1 == MODE_NONE){
					        mode1 = MODE_MOVE;
					        pointerID1 = currID;
				        } else if(mode2 == MODE_NONE){
					        mode2 = MODE_MOVE;
					        pointerID2 = currID;
				        }
			        }
		        }
	        }
        } else if(e.getActionMasked() == MotionEvent.ACTION_MOVE){
            //System.out.println("action_move");
            if(mode1 == MODE_ZOOM && mode2 == MODE_ZOOM){
                scale = initialScale*initialZoomDist/PointF.length(e.getX(0) - e.getX(1), e.getY(0) - e.getY(1));
                mRenderer.setScale(scale);
                //System.out.println("mScale: " + scale);
            } else {
	            if(mode1 == MODE_MOVE || mode2 == MODE_MOVE){
		            if(mode1 == MODE_MOVE){
			            currentPoint.x = e.getX(e.findPointerIndex(pointerID1));
			            currentPoint.y = e.getY(e.findPointerIndex(pointerID1));
		            } else{
			            currentPoint.x = e.getX(e.findPointerIndex(pointerID2));
			            currentPoint.y = e.getY(e.findPointerIndex(pointerID2));
		            }
		            if(PointF.length(currentPoint.x - startX, currentPoint.y - startY) < characterRadius*minDim){
			            mRenderer.setMoving(false);
		            } else{
			            /*
			            moveVector.x = 2*(currentPoint.x - startX)/minDim;
			            moveVector.y = -2*(currentPoint.y - startY)/minDim;
			            moveVectorLength = moveVector.length();
			            moveVector.x /= moveVectorLength;
			            moveVector.y /= moveVectorLength;
			            moveVectorLength = Math.max(0, moveVectorLength - characterRadius);
			            moveVector.x *= moveVectorLength;
			            moveVector.y *= moveVectorLength;

			            mRenderer.setMoveVector(moveVector);
			            */
			            mRenderer.setMoving(true);
		            }
		            moveVector.x = 2*(currentPoint.x - startX)/minDim;
		            moveVector.y = -2*(currentPoint.y - startY)/minDim;
		            mRenderer.setMoveVector(moveVector);
		            setMoveSpeed(moveVector);
	            }
	            if(mode1 == MODE_AIM || mode2 == MODE_AIM){
		            if(mode1 == MODE_AIM){
			            currentPoint.x = e.getX(e.findPointerIndex(pointerID1));
			            currentPoint.y = e.getY(e.findPointerIndex(pointerID1));
		            } else{
			            currentPoint.x = e.getX(e.findPointerIndex(pointerID2));
			            currentPoint.y = e.getY(e.findPointerIndex(pointerID2));
		            }
		            mRenderer.setAimPosition(2*(currentPoint.x - characterX*getWidth())/minDim, 2*(characterY*getHeight() - currentPoint.y)/minDim);
	            }
            }
        } else if(e.getActionMasked() == MotionEvent.ACTION_UP || e.getActionMasked() == MotionEvent.ACTION_POINTER_UP){
            System.out.println("action_up or pointer_up");
            if(e.getPointerId(e.getActionIndex()) == pointerID1){
                // mode 1 event is processed
                if(mode1 == MODE_MOVE){
                    mRenderer.setMoving(false);
	                mRenderer.endMoveTouch();
	                moveVector.x = 0;
	                moveVector.y = 0;
	                mRenderer.setMoveVector(moveVector);

                } else if(mode1 == MODE_AIM){
	                mRenderer.addBullet();
                    mRenderer.setAiming(false);
                }
                mode1 = MODE_NONE;
                pointerID1 = 0;
            } else if(e.getPointerId(e.getActionIndex()) == pointerID2){
                // mode 2 event is processed
                if(mode2 == MODE_MOVE){
                    mRenderer.setMoving(false);
	                mRenderer.endMoveTouch();
	                moveVector.x = 0;
	                moveVector.y = 0;
	                mRenderer.setMoveVector(moveVector);
                } else if(mode2 == MODE_AIM){
	                mRenderer.addBullet();
                    mRenderer.setAiming(false);
                }
                mode2 = MODE_NONE;
                pointerID2 = 0;
            }
        }
        return true;
    }

	private static final float[] moveArrowColorSlow = new float[]{1f, 43f/255f, 43f/255f, 1};
	private static final float[] moveArrowColorMedium = new float[]{1f, 233f/255f, 42f/255f, 1};
	private static final float[] moveArrowColorFast = new float[]{105f/255f, 1, 41f/255f, 1};

	private void setMoveSpeed(PointF moveVector){
		float length = moveVector.length();
		if(length < ringRadii[0]){
			mRenderer.setMoveSpeed(0.0f);
			mRenderer.setMoveArrowColor(moveArrowColorSlow);
		} else if(length < ringRadii[1]){
			mRenderer.setMoveSpeed(0.1f);
			mRenderer.setMoveArrowColor(moveArrowColorSlow);
		} /* else if(length < ringRadii[2]){
			mRenderer.setMoveSpeed(0.2f);
			mRenderer.setMoveArrowColor(moveArrowColorMedium);
		} */ else{
			mRenderer.setMoveSpeed(0.3f);
			mRenderer.setMoveArrowColor(moveArrowColorFast);
		}
	}
}