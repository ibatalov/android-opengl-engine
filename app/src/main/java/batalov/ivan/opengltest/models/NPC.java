package batalov.ivan.opengltest.models;

import android.graphics.PointF;
import android.opengl.Matrix;
import android.os.Bundle;

import java.util.ArrayList;

import batalov.ivan.opengltest.FloatArray;
import batalov.ivan.opengltest.MyUtilities;

/**
 * Created by ivan on 4/6/18.
 */

public class NPC {

	public static class Behavior{
		public static final int STAND = 0;
		public static final int PATROL = 1;
		public static final int MOVE_TO_POINT = 2;
		public static final int PURSUIT = 3;
		public static final int SHOOT_AND_COVER = 4;

	}

	public class PatrolingState{
		public float[][] movementPattern;
		public int nextDestination;
		public float[] moveDirection = new float[3];
		public int upOrDown = 1;
	}

	public class MovingState{
		public float[] destination;
		public ArrayList<FloatArray> path;
	}

	public class PursuitState{
		public float[] targetLocation;
		public ArrayList<FloatArray> path;
	}

	public class ShootAndCoverState{
		// TODO
	}

	public int behavior = Behavior.STAND;
	private PatrolingState patrolingState;
	private MovingState movingState;
	private PursuitState pursuitState;
	private ShootAndCoverState shootAndCoverState;

	public Model3D model;
	public float size = 0.1f;
	public float[] position = new float[3];
	public float[] orientation = new float[]{0, 1, 0};
	private float[] modelMatrix = new float[16];
	private float angle;
	private float moveSpeed = 0.1f;

	public NPC(Model3D model, float[] position){
		this.model = model;
		this.position = position;
	}

	public float[] getModelMatrix(){
		Matrix.setIdentityM(modelMatrix, 0);
		//System.out.println("NPC position: (" + position[0] + ", " + position[1] + ", " + position[2] + ");");

		Matrix.translateM(modelMatrix, 0, position[0], position[1], size/2);
		angle = getRotationAngle(orientation[0], orientation[1]);
		Matrix.rotateM(modelMatrix, 0, angle*180/(float)Math.PI, 0, 0, 1);
		Matrix.scaleM(modelMatrix, 0, size/2, size/2, size/2); //original model is 2x2x2
		return modelMatrix;
	}

	private static float getRotationAngle(float x, float y){
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

	public void addMovementPattern(PatrolingState patrolingState){
		this.patrolingState = patrolingState;
		behavior = Behavior.PATROL;
	}

	public void setMovementTarget(float[] destination, ArrayList<FloatArray> path){
		if(movingState == null) {
			movingState = new MovingState();
		}
		movingState.destination = destination;
		movingState.path = path;
		behavior = Behavior.MOVE_TO_POINT;
	}

	public void setPursuitState(PursuitState pursuitState){
		this.pursuitState = pursuitState;
		behavior = Behavior.PURSUIT;
	}

	PointF tempPoint1 = new PointF();
	PointF tempPoint2 = new PointF();
	public void updatePosition(float travelTime, LevelMap levelMap){
		if(movementPattern != null && travelTime > 0){
			float maxDistance = Math.min(travelTime*moveSpeed, 0.2f);

			moveDirection[0] = movementPattern[nextDestination][0] - position[0];
			moveDirection[1] = movementPattern[nextDestination][1] - position[1];
			moveDirection[2] = movementPattern[nextDestination][2] - position[2];
			float distLeft = MyUtilities.vectorLength(moveDirection, 3);

			moveDirection[0] /= distLeft;
			moveDirection[1] /= distLeft;
			moveDirection[2] /= distLeft;

			float[] desiredPosition = new float[3];
			if(distLeft < maxDistance){
				System.arraycopy(movementPattern[nextDestination], 0, position, 0, 3);
				travelTime *= (maxDistance - distLeft)/maxDistance;
				maxDistance = travelTime*moveSpeed;

				if(nextDestination + upOrDown >= movementPattern.length || nextDestination + upOrDown < 0){
					upOrDown *= -1;
				}
				nextDestination += upOrDown;

				moveDirection[0] = movementPattern[nextDestination][0] - position[0];
				moveDirection[1] = movementPattern[nextDestination][1] - position[1];
				moveDirection[2] = movementPattern[nextDestination][2] - position[2];
				distLeft = MyUtilities.vectorLength(moveDirection, 3);

				moveDirection[0] /= distLeft;
				moveDirection[1] /= distLeft;
				moveDirection[2] /= distLeft;
			}

			desiredPosition[0] = position[0] + moveDirection[0]*maxDistance;
			desiredPosition[1] = position[1] + moveDirection[1]*maxDistance;
			desiredPosition[2] = position[2] + moveDirection[2]*maxDistance;

			tempPoint1.x = position[0];
			tempPoint1.y = position[1];

			tempPoint2.x = desiredPosition[0];
			tempPoint2.y = desiredPosition[1];

			tempPoint1 = levelMap.getCorrectedDestination(tempPoint1, tempPoint2, 10, 1);
			position[0] = tempPoint1.x;
			position[1] = tempPoint1.y;
		}
	}

	public void updatePosition(float timeIncrement, LevelMap levelMap){
		if(timeIncrement > 0) {
			switch (behavior) {
				case Behavior.STAND:
					return;
				case Behavior.MOVE_TO_POINT:
					break;
				case Behavior.PATROL:
					break;
				case Behavior.PURSUIT:
					break;
				case Behavior.SHOOT_AND_COVER:
					break;
			}
		}
	}

}
