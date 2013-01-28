package tuan.math;

import java.util.Random;

import tuan.collections.IntArrayList;

/** A convenience methods for handling random numbers */
public class RandomUtils {

	/** Generate n unique integers ranging randomly uniformly from 0 to maximum,
	 * output in an ordered or unordered way.
	 * The complexity is O(n^2) though, for checking elements takes linear time.
	 * At least this method does not consume too much memory
	 * 
	 * TODO: Use int heap to reduce the complexity down to O(nlogn) .... */
	public static int[] nextInts(int n, int maximum, boolean ordered) {
		if (maximum == 0) return new int[0];
		if (maximum < n) n = maximum;
		Random gen = new Random();
		IntArrayList ints = new IntArrayList(false, n);
		while (ints.size <= n) {
			int i = gen.nextInt(maximum);
			if (!ints.contains(i)) ints.add(i);
		}		
		if (ordered) ints.sort();
		return ints.toArray();
	}
}
