package batalov.ivan.opengltest.light_parameters;

import java.io.Serializable;

/**
 * Created by ivan on 3/10/18.
 */

public class LightParameters implements Serializable {
	public float[] color = new float[4];
	public float ambient;
	public float diffuse;
	public float specular;
	public int castShadow;
	public int depthMap;

}
