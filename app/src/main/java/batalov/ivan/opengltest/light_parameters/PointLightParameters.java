package batalov.ivan.opengltest.light_parameters;

import batalov.ivan.opengltest.light_parameters.LightParameters;

/**
 * Created by ivan on 3/10/18.
 */

public class PointLightParameters extends LightParameters {
	public float[] position = new float[3];
	public float[] decayCoeffs = new float[3];
	public float farPlane;
}
