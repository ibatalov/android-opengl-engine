package batalov.ivan.opengltest.light_parameters;

/**
 * Created by ivan on 3/10/18.
 */

public class PointDirLightParameters extends LightParameters{
	public float[] position = new float[3];
	public float[] decayCoeffs = new float[3];
	public float[] lightSpaceMatrix = new float[16];
}
