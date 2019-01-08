package batalov.ivan.opengltest;

import java.util.Arrays;

/**
 * Created by ivan on 5/2/18.
 */

public class FloatArray {
	public float[] array;
	public FloatArray(int capacity){
		array = new float[capacity];
	}

	public FloatArray(float[] array){
		this.array = Arrays.copyOf(array, array.length);
	}

	@Override
	public boolean equals(Object floatArray){
		if(floatArray instanceof FloatArray){
			return Arrays.equals(this.array, ((FloatArray)floatArray).array);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 235;
		if(array != null) {
			for (int i = 0; i < array.length; i++) {
				hash += 987*array[i] +  i * i;
			}
		}
		return hash;
	}

	public float distance(FloatArray a, int length){
		double result = 0;
		for(int i = 0; i < length; i++){
			result += (a.array[i] - array[i])*(a.array[i] - array[i]);
		}
		return (float)Math.sqrt(result);
	}

	public float distance(float[] a, int length){
		double result = 0;
		for(int i = 0; i < length; i++){
			result += (a[i] - array[i])*(a[i] - array[i]);
		}
		return (float)Math.sqrt(result);
	}
}
