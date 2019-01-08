package batalov.ivan.opengltest;

import java.util.Arrays;

/**
 * Created by ivan on 5/2/18.
 */

public class IntArray {
	public int[] array;
	public IntArray(int capacity){
		array = new int[capacity];
	}

	public IntArray(int[] array){
		this.array = Arrays.copyOf(array, array.length);
	}

	/**
	 *
	 * @param intArray
	 * @return
	 */
	@Override
	public boolean equals(Object intArray){
		if(intArray instanceof IntArray){
			return Arrays.equals(this.array, ((IntArray)intArray).array);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 235;
		if(array != null) {
			for (int i = 0; i < array.length; i++) {
				hash += array[i]*i;
			}
		}
		return hash;
	}
}
