package batalov.ivan.opengltest;

/**
 * Created by ivan on 3/8/18.
 */

public class BulletParameters {
	public long startTime;
	public long lastUpdateTime;

	public boolean finished = false;

	public float[] moveDirection = new float[2];
	public float[] startPosition = new float[2];
	public float[] lastPosition = new float[2];

	public float speed; // units/ms

	public float rotationAngle;

	public float[] traceTranslation = new float[2];
	public float traceLength;

	public float[] bulletModelMatrix = new float[16];
	public float[] traceModelMatrix = new float[16];

	public BulletParameters(){

	}

	public static void normalize3D(float[] v){
		float tempLength = (float)Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
		v[0] /= tempLength;
		v[1] /= tempLength;
		v[2] /= tempLength;
	}

	public static float getLength3D(float[] v){
		return (float)Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
	}

	public static void normalize2D(float[] v){
		float tempLength = (float)Math.sqrt(v[0]*v[0] + v[1]*v[1]);
		v[0] /= tempLength;
		v[1] /= tempLength;
	}

	public static float getLength2D(float[] v){
		return (float)Math.sqrt(v[0]*v[0] + v[1]*v[1]);
	}
}
