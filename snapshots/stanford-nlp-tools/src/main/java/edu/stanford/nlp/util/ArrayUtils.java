package edu.stanford.nlp.util;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.NumberFormat;
import java.util.*;

import edu.stanford.nlp.math.SloppyMath;


/**
 * Static utility methods for operating on arrays.
 *
 * @author Huy Nguyen (htnguyen@cs.stanford.edu)
 * @author Michel Galley (mgalley@stanford.edu)
 */
public class ArrayUtils {

	/**
	 * Should not be instantiated
	 */
	private ArrayUtils() {}

	public static void main(String[] args) {

		int[] orig = new int[args.length];
		for (int i = 0; i < orig.length; i++) {
			orig[i] = Integer.parseInt(args[i]);
		}

		for (int i : gapDecode(gapEncode(orig))) {
			System.err.print(i+" ");
		}
		System.err.println();

		for (int i : deltaDecode(deltaEncode(orig))) {
			System.err.print(i+" ");
		}
		System.err.println();

	}

	//ARITHMETIC FUNCTIONS

	public static double[] add(double[] a, double c) {
		double[] result = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			result[i] = a[i] + c;
		}
		return result;
	}

	public static double[] add(double[] a, double[] b) {
		double[] result = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			result[i] = a[i] + b[i];
		}
		return result;
	}

	public static int[] add(int[] a, int c) {
		int[] result = new int[a.length];
		for (int i = 0; i < a.length; i++) {
			result[i] = a[i] + c;
		}
		return result;
	}

	public static float[] add(float[] a, float[] b) {
		float[] result = new float[a.length];
		for (int i = 0; i < a.length; i++) {
			result[i] = a[i] + b[i];
		}
		return result;
	}

	public static void addInPlace(double[] a, double b) {
		for (int i = 0; i < a.length; i++) {
			a[i] += b;
		}
	}

	public static void addInPlace(double[] a, double[] b) {
		for (int i = 0; i < a.length; i++) {
			a[i] += b[i];
		}
	}

	public static void addInPlace(double[][] a, double[][] b) {
		if (a == null || b == null)
			return;
		if (a.length != b.length)
			return;
		for (int i = 0; i < a.length; ++i) {
			if (a[i] == null || b[i] == null)
				continue;
			addInPlace(a[i], b[i]);

		}
	}

	public static void addInPlace(double[][][] a, double[][][] b) {
		if (a == null || b == null)
			return;
		if (a.length != b.length)
			return;
		for (int i = 0; i < a.length; ++i) {

			addInPlace(a[i], b[i]);

		}
	}

	public static void addInPlace(double[][][][] a, double[][][][] b) {
		if (a == null || b == null)
			return;
		if (a.length != b.length)
			return;
		for (int i = 0; i < a.length; ++i) {

			addInPlace(a[i], b[i]);

		}
	}

	public static void addInPlace(int[] a, int[] b) {
		if (a == null || b == null)
			return;
		if (a.length != b.length)
			return;
		for (int i = 0; i < a.length; ++i) {
			a[i] += b[i];
		}
	}

	public static void addInPlace(int[][] a, int[][] b) {
		if (a == null || b == null)
			return;
		if (a.length != b.length)
			return;
		for (int i = 0; i < a.length; ++i) {
			addInPlace(a[i], b[i]);
		}
	}

	public static void addInPlace(long[] a, long[] b) {
		if (a == null || b == null)
			return;
		if (a.length != b.length)
			return;
		for (int i = 0; i < a.length; ++i) {
			a[i] += b[i];
		}
	}

	public static double approxLogSum(double[] logInputs, int leng) {

		if (leng == 0) {
			throw new IllegalArgumentException();
		}
		int maxIdx = 0;
		double max = logInputs[0];
		for (int i = 1; i < leng; i++) {
			if (logInputs[i] > max) {
				maxIdx = i;
				max = logInputs[i];
			}
		}
		boolean haveTerms = false;
		double intermediate = 0.0;
		double cutoff = max - SloppyMath.LOGTOLERANCE;
		// we avoid rearranging the array and so test indices each time!
		for (int i = 0; i < leng; i++) {
			if (i != maxIdx && logInputs[i] > cutoff) {
				haveTerms = true;
				intermediate += SloppyMath.approxExp(logInputs[i] - max);
			}
		}
		if (haveTerms) {
			return max + SloppyMath.approxLog(1.0 + intermediate);
		} else {
			return max;
		}
	}

	/**
	 * @return the index of the max value; if max is a tie, returns the first
	 *         one.
	 */
	 public static int argmax(double[] a) {
		 double max = Double.NEGATIVE_INFINITY;
		 int argmax = 0;
		 for (int i = 0; i < a.length; i++) {
			 if (a[i] > max) {
				 max = a[i];
				 argmax = i;
			 }
		 }
		 return argmax;
	 }

	 /**
	  * @return the index of the max value; if max is a tie, returns the first
	  *         one.
	  */
	  public static int argmax(float[] a) {
		  float max = Float.NEGATIVE_INFINITY;
		  int argmax = 0;
		  for (int i = 0; i < a.length; i++) {
			  if (a[i] > max) {
				  max = a[i];
				  argmax = i;
			  }
		  }
		  return argmax;
	  }

	  /**
	   * @return the index of the max value; if max is a tie, returns the first
	   *         one.
	   */
	  public static int argmax(short[] a) {
		  float max = Short.MIN_VALUE;
		  int argmax = 0;
		  for (int i = 0; i < a.length; i++) {
			  if (a[i] > max) {
				  max = a[i];
				  argmax = i;
			  }
		  }
		  return argmax;
	  }

	  /**
	   * @return the index of the max value; if max is a tie, returns the first
	   *         one.
	   */
	  public static int argmin(double[] a) {
		  double min = Double.POSITIVE_INFINITY;
		  int argmin = 0;
		  for (int i = 0; i < a.length; i++) {
			  if (a[i] < min) {
				  min = a[i];
				  argmin = i;
			  }
		  }
		  return argmin;
	  }

	  /**
	   * @return the index of the max value; if max is a tie, returns the first
	   *         one.
	   */
	  public static int argmin(float[] a) {
		  float min = Float.POSITIVE_INFINITY;
		  int argmin = 0;
		  for (int i = 0; i < a.length; i++) {
			  if (a[i] < min) {
				  min = a[i];
				  argmin = i;
			  }
		  }
		  return argmin;
	  }

	  /**
	   * @return the index of the max value; if max is a tie, returns the first
	   *         one.
	   */
	  public static int argmin(int[] a) {
		  int min = Integer.MAX_VALUE;
		  int argmin = 0;
		  for (int i = 0; i < a.length; i++) {
			  if (a[i] < min) {
				  min = a[i];
				  argmin = i;
			  }
		  }
		  return argmin;
	  }

	  // CASTS

	  public static double average(double[] a) {
		  double total = sum(a);
		  return total / a.length;
	  }

	  public static void booleanAndInPlace(boolean[] array, boolean[] other) {
		  if (array == null)
			  return;
		  for (int i = 0; i < array.length; ++i) {
			  array[i] &= other[i];
		  }
	  }

	  public static void booleanAndInPlace(boolean[][] array, boolean[][] other) {
		  if (array == null)
			  return;
		  for (int i = 0; i < array.length; ++i) {
			  booleanAndInPlace(array[i], other[i]);
		  }
	  }

	  public static void booleanAndInPlace(boolean[][][] array,
			  boolean[][][] other) {
		  if (array == null)
			  return;
		  for (int i = 0; i < array.length; ++i) {
			  booleanAndInPlace(array[i], other[i]);
		  }
	  }

	  public static boolean[][] clone(boolean[][] a) {
		  boolean[][] res = new boolean[a.length][];
		  for (int i = 0; i < a.length; i++) {
			  if (a[i] != null)
				  res[i] = a[i].clone();
		  }
		  return res;
	  }

	  public static boolean[][][] clone(boolean[][][] a) {
		  boolean[][][] res = new boolean[a.length][][];
		  for (int i = 0; i < a.length; i++) {
			  if (a[i] != null)
				  res[i] = clone(a[i]);
		  }
		  return res;
	  }

	  public static boolean[][][][] clone(boolean[][][][] a) {
		  boolean[][][][] res = new boolean[a.length][][][];
		  for (int i = 0; i < a.length; i++) {
			  res[i] = clone(a[i]);
		  }
		  return res;
	  }

	  public static double[][] clone(double[][] a) {
		  double[][] res = new double[a.length][];
		  for (int i = 0; i < a.length; i++) {
			  if (a[i] != null)
				  res[i] = a[i].clone();
		  }
		  return res;
	  }

	  // LINEAR ALGEBRAIC FUNCTIONS

	  public static int[][][] clone(int[][][] a) {
		  int[][][] res = new int[a.length][][];
		  for (int i = 0; i < a.length; i++) {
			  if (a[i] != null)
				  res[i] = clone(a[i]);
		  }
		  return res;
	  }

	  public static double[][][] clone(double[][][] a) {
		  double[][][] res = new double[a.length][][];
		  for (int i = 0; i < a.length; i++) {
			  if (a[i] != null)
				  res[i] = clone(a[i]);
		  }
		  return res;
	  }

	  public static double[][][][] clone(double[][][][] a) {
		  double[][][][] res = new double[a.length][][][];
		  for (int i = 0; i < a.length; i++) {
			  res[i] = clone(a[i]);
		  }
		  return res;
	  }

	  public static int[] clone(int[] original) {
		  // TODO Sort out which of these we should use.
		  // return Arrays.copyOf(original, original.length);
		  // return original.clone();
		  int[] copy = new int[original.length];
		  System.arraycopy(original, 0, copy, 0, original.length);
		  return copy;
	  }

	  public static int[][] clone(int[][] a) {
		  int[][] res = new int[a.length][];
		  for (int i = 0; i < a.length; i++) {
			  if (a[i] != null)
				  res[i] = clone(a[i]);
		  }
		  return res;
	  }

	  public static short[] clone(short[] original) {
		  // TODO Sort out which of these we should use.
		  // return Arrays.copyOf(original, original.length);
		  // return original.clone();
		  short[] copy = new short[original.length];
		  System.arraycopy(original, 0, copy, 0, original.length);
		  return copy;
	  }

	  public static double[] clone(double[] original) {
		  double[] copy = new double[original.length];
		  System.arraycopy(original, 0, copy, 0, original.length);
		  return copy;
	  }

	  // PRINTING FUNCTIONS

	  public static double[][][][] copy(double[][][][] mat) {
		  int m = mat.length;
		  double[][][][] newMat = new double[m][][][];
		  for (int r = 0; r < m; r++)
			  newMat[r] = copy(mat[r]);
		  return newMat;
	  }

	  public static float[] doubleArrayToFloatArray(double a[]) {
		  float[] result = new float[a.length];
		  for (int i = 0; i < a.length; i++) {
			  result[i] = (float) a[i];
		  }
		  return result;
	  }

	  public static float[][] doubleArrayToFloatArray(double a[][]) {
		  float[][] result = new float[a.length][];
		  for (int i = 0; i < a.length; i++) {
			  result[i] = new float[a[i].length];
			  for (int j = 0; j < a[i].length; j++) {
				  result[i][j] = (float) a[i][j];
			  }
		  }
		  return result;
	  }

	  public static double[] exp(double[] a) {
		  double[] result = new double[a.length];
		  for (int i = 0; i < a.length; i++) {
			  result[i] = Math.exp(a[i]);
		  }
		  return result;
	  }

	  public static void expInPlace(double[] a) {
		  for (int i = 0; i < a.length; i++) {
			  a[i] = Math.exp(a[i]);
		  }
	  }

	  // SAMPLE ANALYSIS

	  public static void fill(boolean[][] a, boolean val) {
		  for (int i = 0; i < a.length; i++) {
			  if (a[i] != null)
				  Arrays.fill(a[i], val);
		  }
	  }

	  public static void fill(boolean[][] a, int until1, int until2, boolean val) {
		  for (int i = 0; i < until1; ++i) {
			  Arrays.fill(a[i], 0, until2 == Integer.MAX_VALUE ? a[i].length
					  : until2, val);
		  }
	  }

	  public static void fill(boolean[][][] a, boolean val) {
		  for (int i = 0; i < a.length; i++) {
			  if (a[i] != null)
				  fill(a[i], val);
		  }
	  }

	  public static void fill(boolean[][][] a, int until, boolean val) {
		  for (int i = 0; i < until; i++) {
			  fill(a[i], val);
		  }
	  }

	  public static void fill(boolean[][][] a, int until1, int until2, boolean val) {
		  for (int i = 0; i < until1; i++) {
			  fill(a[i], until2, Integer.MAX_VALUE, val);
		  }
	  }

	  public static void fill(boolean[][][] a, int until1, int until2,
			  int until3, boolean val) {
		  for (int i = 0; i < until1; i++) {
			  fill(a[i], until2, until3, val);
		  }
	  }

	  public static void fill(boolean[][][][] a, boolean val) {
		  for (int i = 0; i < a.length; i++) {
			  fill(a[i], val);
		  }
	  }

	  public static void fill(boolean[][][][] a, int until1, int until2,
			  int until3, int until4, boolean val) {
		  for (int i = 0; i < until1; i++) {
			  fill(a[i], until2, until3, until4, val);
		  }
	  }

	  public static void fill(double[][] a, double val) {
		  for (int i = 0; i < a.length; i++) {
			  Arrays.fill(a[i], val);
		  }
	  }

	  public static void fill(double[][] a, int until1, int until2, double val) {
		  for (int i = 0; i < until1; ++i) {
			  Arrays.fill(a[i], 0, until2 == Integer.MAX_VALUE ? a[i].length
					  : until2, val);
		  }
	  }

	  public static void fill(double[][][] a, double val) {
		  for (int i = 0; i < a.length; i++) {
			  fill(a[i], val);
		  }
	  }

	  public static void fill(double[][][] a, int until1, int until2, double val) {
		  for (int i = 0; i < until1; i++) {
			  fill(a[i], until2, Integer.MAX_VALUE, val);
		  }
	  }

	  public static void fill(double[][][] a, int until1, int until2, int until3,
			  double val) {
		  for (int i = 0; i < until1; i++) {
			  fill(a[i], until2, until3, val);
		  }
	  }

	  public static void fill(double[][][][] a, double val) {
		  for (int i = 0; i < a.length; i++) {
			  fill(a[i], val);
		  }
	  }

	  public static void fill(double[][][][] a, int until1, int until2,
			  int until3, int until4, double val) {
		  for (int i = 0; i < until1; i++) {
			  fill(a[i], until2, until3, until4, val);
		  }
	  }

	  public static void fill(double[][][][][] a, double val) {
		  for (int i = 0; i < a.length; i++) {
			  fill(a[i], val);
		  }
	  }

	  public static void fill(float[][] a, float val) {
		  for (int i = 0; i < a.length; i++) {
			  Arrays.fill(a[i], val);
		  }
	  }

	  public static void fill(float[][][] a, float val) {
		  for (int i = 0; i < a.length; i++) {
			  fill(a[i], val);
		  }
	  }

	  public static void fill(float[][][][] a, float val) {
		  for (int i = 0; i < a.length; i++) {
			  fill(a[i], val);
		  }
	  }

	  public static void fill(float[][][][][] a, float val) {
		  for (int i = 0; i < a.length; i++) {
			  fill(a[i], val);
		  }
	  }

	  public static void fill(int[][] a, int val) {
		  fill(a, a.length, a[0].length, val);
	  }

	  public static void fill(int[][] a, int until1, int until2, int val) {
		  for (int i = 0; i < until1; ++i) {
			  Arrays.fill(a[i], 0, until2, val);
		  }
	  }

	  public static void fill(int[][][] a, int until1, int until2, int until3,
			  int val) {
		  for (int i = 0; i < until1; i++) {
			  fill(a[i], until2, until3, val);
		  }
	  }

	  public static void fill(double[][][] a, int until, double val) {
		  for (int i = 0; i < until; i++) {
			  fill(a[i], val);
		  }
	  }

	  public static void fill(int[][][] a, int until, int val) {
		  for (int i = 0; i < until; i++) {
			  fill(a[i], val);
		  }
	  }

	  public static void fill(int[][][] a, int val) {
		  for (int i = 0; i < a.length; i++) {
			  fill(a[i], val);
		  }
	  }

	  public static void fill(int[][][][] a, int until1, int until2, int until3,
			  int until4, int val) {
		  for (int i = 0; i < until1; i++) {
			  fill(a[i], until2, until3, until4, val);
		  }
	  }

	  public static void fill(Object[][] a, int until1, int until2, Object val) {
		  for (int i = 0; i < until1; ++i) {
			  Arrays.fill(a[i], 0, until2 == Integer.MAX_VALUE ? a[i].length
					  : until2, val);
		  }
	  }

	  public static void fill(Object[][][] a, int until1, int until2, int until3,
			  Object val) {
		  for (int i = 0; i < until1; ++i) {
			  fill(a[i], until2 == Integer.MAX_VALUE ? a[i].length : until2,
					  until3, val);
		  }
	  }

	  public static void fill(Object[][][][] a, int until1, int until2,
			  int until3, int until4, Object val) {
		  for (int i = 0; i < until1; ++i) {
			  fill(a[i], until2 == Integer.MAX_VALUE ? a[i].length : until2,
					  until3, until4, val);
		  }
	  }

	  public static double[] floatArrayToDoubleArray(float a[]) {
		  double[] result = new double[a.length];
		  for (int i = 0; i < a.length; i++) {
			  result[i] = a[i];
		  }
		  return result;
	  }

	  public static double[][] floatArrayToDoubleArray(float a[][]) {
		  double[][] result = new double[a.length][];
		  for (int i = 0; i < a.length; i++) {
			  result[i] = new double[a[i].length];
			  for (int j = 0; j < a[i].length; j++) {
				  result[i][j] = a[i][j];
			  }
		  }
		  return result;
	  }

	  public static boolean hasInfinite(double[] a) {
		  for (int i = 0; i < a.length; i++) {
			  if (Double.isInfinite(a[i]))
				  return true;
		  }
		  return false;
	  }

	  public static boolean hasNaN(double[] a) {
		  for (int i = 0; i < a.length; i++) {
			  if (Double.isNaN(a[i]))
				  return true;
		  }
		  return false;
	  }

	  // UTILITIES

	  public static double innerProduct(double[] a, double[] b) {
		  double result = 0.0;
		  for (int i = 0; i < a.length; i++) {
			  result += a[i] * b[i];
		  }
		  return result;
	  }

	  public static double innerProduct(float[] a, float[] b) {
		  double result = 0.0;
		  for (int i = 0; i < a.length; i++) {
			  result += a[i] * b[i];
		  }
		  return result;
	  }

	  public static double[] inverse(double[] a) {

		  double[] retVal = new double[a.length];
		  for (int i = 0; i < a.length; ++i) {

			  retVal[i] = (a[i] == 0.0) ? 0 : // Double.POSITIVE_INFINITY :
				  1.0 / a[i];
		  }

		  return retVal;
	  }

	  public static double klDivergence(double[] from, double[] to) {
		  double kl = 0.0;
		  double tot = sum(from);
		  double tot2 = sum(to);
		  // System.out.println("tot is " + tot + " tot2 is " + tot2);
		  for (int i = 0; i < from.length; i++) {
			  if (from[i] == 0.0) {
				  continue;
			  }
			  double num = from[i] / tot;
			  double num2 = to[i] / tot2;
			  // System.out.println("num is " + num + " num2 is " + num2);
			  kl += num * (Math.log(num / num2) / Math.log(2.0));
		  }
		  return kl;

	  }

	  public static double[][] load2DMatrixFromFile(String filename)
			  throws IOException {
		  String s = FileUtility.slurpFile(filename);
		  String[] rows = s.split("[\r\n]+");
		  double[][] result = new double[rows.length][];
		  for (int i = 0; i < result.length; i++) {
			  String[] columns = rows[i].split("\\s+");
			  result[i] = new double[columns.length];
			  for (int j = 0; j < result[i].length; j++) {
				  result[i][j] = Double.parseDouble(columns[j]);
			  }
		  }
		  return result;
	  }

	  public static double[] log(double[] a) {
		  double[] result = new double[a.length];
		  for (int i = 0; i < a.length; i++) {
			  result[i] = Math.log(a[i]);
		  }
		  return result;
	  }

	  public static void logInPlace(double[] a) {
		  for (int i = 0; i < a.length; i++) {
			  a[i] = Math.log(a[i]);
		  }
	  }

	  /**
	   * Makes the values in this array sum to 1.0. Does it in place. If the total
	   * is 0.0, sets a to the uniform distribution.
	   */
	  public static void logNormalize(double[] a) {
		  double logTotal = logSum(a);
		  if (logTotal == Double.NEGATIVE_INFINITY) {
			  // to avoid NaN values
			  double v = -Math.log(a.length);
			  for (int i = 0; i < a.length; i++) {
				  a[i] = v;
			  }
			  return;
		  }
		  shift(a, -logTotal); // subtract log total from each value
	  }

	  /**
	   * Returns the log of the sum of an array of numbers, which are themselves
	   * input in log form. This is all natural logarithms. Reasonable care is
	   * taken to do this as efficiently as possible (under the assumption that
	   * the numbers might differ greatly in magnitude), with high accuracy, and
	   * without numerical overflow.
	   * 
	   * @param logInputs
	   *            An array of numbers [log(x1), ..., log(xn)]
	   * @return log(x1 + ... + xn)
	   */
	  public static double logSum(double[] logInputs) {
		  return logSum(logInputs, logInputs.length);
		  // int leng = logInputs.length;
		  // if (leng == 0) {
		  // throw new IllegalArgumentException();
		  // }
		  // int maxIdx = 0;
		  // double max = logInputs[0];
		  // for (int i = 1; i < leng; i++) {
		  // if (logInputs[i] > max) {
		  // maxIdx = i;
		  // max = logInputs[i];
		  // }
		  // }
		  // boolean haveTerms = false;
		  // double intermediate = 0.0;
		  // double cutoff = max - SloppyMath.LOGTOLERANCE;
		  // // we avoid rearranging the array and so test indices each time!
		  // for (int i = 0; i < leng; i++) {
		  // if (i != maxIdx && logInputs[i] > cutoff) {
		  // haveTerms = true;
		  // intermediate += Math.exp(logInputs[i] - max);
		  // }
		  // }
		  // if (haveTerms) {
		  // return max + Math.log(1.0 + intermediate);
		  // } else {
		  // return max;
		  // }
	  }

	  public static double logSum(double[] logInputs, int leng) {

		  if (leng == 0) {
			  throw new IllegalArgumentException();
		  }
		  int maxIdx = 0;
		  double max = logInputs[0];
		  for (int i = 1; i < leng; i++) {
			  if (logInputs[i] > max) {
				  maxIdx = i;
				  max = logInputs[i];
			  }
		  }
		  boolean haveTerms = false;
		  double intermediate = 0.0;
		  double cutoff = max - SloppyMath.LOGTOLERANCE;
		  // we avoid rearranging the array and so test indices each time!
		  for (int i = 0; i < leng; i++) {
			  if (i != maxIdx && logInputs[i] > cutoff) {
				  haveTerms = true;
				  intermediate += Math.exp(logInputs[i] - max);
			  }
		  }
		  if (haveTerms) {
			  return max + Math.log(1.0 + intermediate);
		  } else {
			  return max;
		  }
	  }

	  /**
	   * Returns the log of the sum of an array of numbers, which are themselves
	   * input in log form. This is all natural logarithms. Reasonable care is
	   * taken to do this as efficiently as possible (under the assumption that
	   * the numbers might differ greatly in magnitude), with high accuracy, and
	   * without numerical overflow.
	   * 
	   * @param logInputs
	   *            An array of numbers [log(x1), ..., log(xn)]
	   * @return log(x1 + ... + xn)
	   */
	  public static float logSum(float[] logInputs) {
		  int leng = logInputs.length;
		  if (leng == 0) {
			  throw new IllegalArgumentException();
		  }
		  int maxIdx = 0;
		  float max = logInputs[0];
		  for (int i = 1; i < leng; i++) {
			  if (logInputs[i] > max) {
				  maxIdx = i;
				  max = logInputs[i];
			  }
		  }
		  boolean haveTerms = false;
		  double intermediate = 0.0f;
		  float cutoff = (float) (max - SloppyMath.LOGTOLERANCE);
		  // we avoid rearranging the array and so test indices each time!
		  for (int i = 0; i < leng; i++) {
			  if (i != maxIdx && logInputs[i] > cutoff) {
				  haveTerms = true;
				  intermediate += Math.exp(logInputs[i] - max);
			  }
		  }
		  if (haveTerms) {
			  return max + (float) Math.log(1.0 + intermediate);
		  } else {
			  return max;
		  }
	  }

	  public static double max(double[] a) {
		  return a[argmax(a)];
	  }

	  public static float max(float[] a) {
		  return a[argmax(a)];
	  }

	  public static float max(short[] a) {
		  return a[argmax(a)];
	  }

	  public static int max(int[] a) {
		  int max = Integer.MIN_VALUE;
		  for (int i = 0; i < a.length; i++) {
			  if (a[i] > max) {
				  max = a[i];
			  }
		  }
		  return max;
	  }

	  public static int max(Integer[] a) {
		  int max = Integer.MIN_VALUE;
		  for (int i = 0; i < a.length; i++) {
			  if (a[i] > max) {
				  max = a[i];
			  }
		  }
		  return max;
	  }

	  public static double mean(double[] a) {
		  return sum(a) / a.length;
	  }

	  public static double min(double[] a) {
		  return a[argmin(a)];
	  }

	  public static float min(float[] a) {
		  return a[argmin(a)];
	  }

	  public static int min(int[] a) {
		  return a[argmin(a)];
	  }

	  /**
	   * Scales the values in this array by c.
	   */
	   public static double[] multiply(double[] a, double c) {
		  double[] result = new double[a.length];
		  for (int i = 0; i < a.length; i++) {
			  result[i] = a[i] * c;
		  }
		  return result;
	   }

	   /**
	    * Scales the values in this array by c.
	    */
	   public static double[][] multiply(double[][] a, double c) {
		   double[][] result = new double[a.length][a[0].length];
		   for (int i = 0; i < a.length; i++) {
			   for (int j = 0; j < a[0].length; j++) {
				   result[i][j] = a[i][j] * c;
			   }
		   }
		   return result;
	   }

	   /**
	    * Scales the values in this array by c.
	    */
	   public static float[] multiply(float[] a, float c) {
		   float[] result = new float[a.length];
		   for (int i = 0; i < a.length; i++) {
			   result[i] = a[i] * c;
		   }
		   return result;
	   }

	   /**
	    * Scales in place the values in this array by c.
	    */
	   public static void multiplyInPlace(double[] a, double c) {
		   for (int i = 0; i < a.length; i++) {
			   a[i] = a[i] * c;
		   }
	   }

	   /**
	    * Computes 2-norm of vector
	    * 
	    * @param a
	    * @return Euclidean norm of a
	    */
	   public static double norm(double[] a) {
		   double squaredSum = 0;
		   for (int i = 0; i < a.length; i++) {
			   squaredSum += a[i] * a[i];
		   }
		   return Math.sqrt(squaredSum);
	   }

	   // PRINTING FUNCTIONS

	   /**
	    * Computes 2-norm of vector
	    * 
	    * @param a
	    * @return Euclidean norm of a
	    */
	   public static double norm(float[] a) {
		   double squaredSum = 0;
		   for (int i = 0; i < a.length; i++) {
			   squaredSum += a[i] * a[i];
		   }
		   return Math.sqrt(squaredSum);
	   }

	   /**
	    * Computes 1-norm of vector
	    * 
	    * @param a
	    * @return 1-norm of a
	    */
	   public static double norm_1(double[] a) {
		   double sum = 0;
		   for (int i = 0; i < a.length; i++) {
			   sum += (a[i] < 0 ? -a[i] : a[i]);
		   }
		   return sum;
	   }

	   /**
	    * Computes 1-norm of vector
	    * 
	    * @param a
	    * @return 1-norm of a
	    */
	   public static double norm_1(float[] a) {
		   double sum = 0;
		   for (int i = 0; i < a.length; i++) {
			   sum += (a[i] < 0 ? -a[i] : a[i]);
		   }
		   return sum;
	   }

	   /**
	    * Computes inf-norm of vector
	    * 
	    * @param a
	    * @return inf-norm of a
	    */
	   public static double norm_inf(double[] a) {
		   double max = Double.NEGATIVE_INFINITY;
		   for (int i = 0; i < a.length; i++) {
			   if (Math.abs(a[i]) > max) {
				   max = Math.abs(a[i]);
			   }
		   }
		   return max;
	   }

	   /**
	    * Computes inf-norm of vector
	    * 
	    * @param a
	    * @return inf-norm of a
	    */
	   public static double norm_inf(float[] a) {
		   double max = Double.NEGATIVE_INFINITY;
		   for (int i = 0; i < a.length; i++) {
			   if (Math.abs(a[i]) > max) {
				   max = Math.abs(a[i]);
			   }
		   }
		   return max;
	   }

	   /**
	    * Makes the values in this array sum to 1.0. Does it in place. If the total
	    * is 0.0, sets a to the uniform distribution.
	    */
	   public static void normalize(double[] a) {
		   double total = sum(a);
		   if (total == 0.0) {
			   throw new RuntimeException("Can't normalize an array with sum 0.0");
		   }
		   scale(a, 1.0 / total); // divide each value by total
	   }

	   public static void normalize(double[][] a) {
		   double total = sum(a);
		   if (total == 0.0) {
			   throw new RuntimeException("Can't normalize an array with sum 0.0");
		   }
		   for (int i = 0; i < a.length; i++) {
			   scale(a[i], 1.0 / total); // divide each value by total
		   }
	   }

	   /**
	    * Makes the values in this array sum to 1.0. Does it in place. If the total
	    * is 0.0, sets a to the uniform distribution.
	    */
	   public static void normalize(float[] a) {
		   float total = sum(a);
		   if (total == 0.0) {
			   throw new RuntimeException("Can't normalize an array with sum 0.0");
		   }
		   scale(a, 1.0F / total); // divide each value by total
	   }

	   public static double[][] outerProduct(double[] a, double[] b) {
		   if (a.length != b.length) {
			   return null;
		   }
		   double[][] retVal = new double[a.length][a.length];
		   for (int i = 0; i < a.length; ++i) {
			   for (int j = 0; j < a.length; ++j) {
				   retVal[i][j] = a[i] * b[j];
			   }
		   }
		   return retVal;
	   }

	   public static double[] pairwiseAdd(double[] a, double[] b) {
		   if (a.length != b.length) {
			   throw new RuntimeException();
		   }
		   double[] result = new double[a.length];
		   for (int i = 0; i < result.length; i++) {
			   result[i] = a[i] + b[i];
		   }
		   return result;
	   }

	   /**
	    * Assumes that both arrays have same length.
	    */
	   public static double[] pairwiseMultiply(double[] a, double[] b) {
		   if (a.length != b.length) {
			   throw new RuntimeException();
		   }
		   double[] result = new double[a.length];
		   for (int i = 0; i < result.length; i++) {
			   result[i] = a[i] * b[i];
		   }
		   return result;
	   }

	   /**
	    * Puts the result in the result array. Assumes that all arrays have same
	    * length.
	    */
	   public static void pairwiseMultiply(double[] a, double[] b, double[] result) {
		   if (a.length != b.length) {
			   throw new RuntimeException();
		   }
		   for (int i = 0; i < result.length; i++) {
			   result[i] = a[i] * b[i];
		   }
	   }

	   /**
	    * Assumes that both arrays have same length.
	    */
	   public static float[] pairwiseMultiply(float[] a, float[] b) {
		   if (a.length != b.length) {
			   throw new RuntimeException();
		   }
		   float[] result = new float[a.length];
		   for (int i = 0; i < result.length; i++) {
			   result[i] = a[i] * b[i];
		   }
		   return result;
	   }

	   /**
	    * Puts the result in the result array. Assumes that all arrays have same
	    * length.
	    */
	   public static void pairwiseMultiply(float[] a, float[] b, float[] result) {
		   if (a.length != b.length) {
			   throw new RuntimeException();
		   }
		   for (int i = 0; i < result.length; i++) {
			   result[i] = a[i] * b[i];
		   }
	   }

	   /**
	    * Scales the values in this array by c.
	    */
	   public static double[] pow(double[] a, double c) {
		   double[] result = new double[a.length];
		   for (int i = 0; i < a.length; i++) {
			   result[i] = Math.pow(a[i], c);
		   }
		   return result;
	   }

	   /**
	    * Scales the values in this array by c.
	    */
	   public static float[] pow(float[] a, float c) {
		   float[] result = new float[a.length];
		   for (int i = 0; i < a.length; i++) {
			   result[i] = (float) Math.pow(a[i], c);
		   }
		   return result;
	   }

	   /**
	    * Scales the values in this array by c.
	    */
	   public static void powInPlace(double[] a, double c) {
		   for (int i = 0; i < a.length; i++) {
			   a[i] = Math.pow(a[i], c);
		   }
	   }

	   /**
	    * Scales the values in this array by c.
	    */
	   public static void powInPlace(float[] a, float c) {
		   for (int i = 0; i < a.length; i++) {
			   a[i] = (float) Math.pow(a[i], c);
		   }
	   }

	   public static double product(double[] a) {
		   double retVal = 1.0;

		   for (double d : a) {

			   retVal *= d;

		   }

		   return retVal;
	   }

	   public static boolean[] reallocArray(boolean[] array, int minLength) {
		   if (array == null || array.length < minLength) {
			   return new boolean[minLength];
		   }
		   return array;

	   }

	   public static boolean[] reallocArray(boolean[] array, int minLength,
			   boolean fillVal) {
		   boolean[] newArray = reallocArray(array, minLength);
		   Arrays.fill(newArray, fillVal);
		   return newArray;

	   }

	   public static boolean[][] reallocArray(boolean[][] array, int minLength1,
			   int minLength2) {
		   if (array == null || array.length < minLength1) {
			   return new boolean[minLength1][minLength2];
		   }
		   return array;

	   }

	   public static boolean[][] reallocArray(boolean[][] array, int minLength1,
			   int minLength2, boolean fillVal) {
		   boolean[][] newArray = reallocArray(array, minLength1, minLength2);
		   fill(newArray, minLength1, minLength2, fillVal);
		   return newArray;

	   }

	   public static boolean[][][] reallocArray(boolean[][][] array,
			   int minLength1, int minLength2, int minLength3) {
		   if (array == null || array.length < minLength1) {
			   return new boolean[minLength1][minLength2][minLength3];
		   }
		   return array;

	   }

	   public static boolean[][][] reallocArray(boolean[][][] array,
			   int minLength1, int minLength2, int minLength3, boolean fillVal) {
		   boolean[][][] newArray = reallocArray(array, minLength1, minLength2,
				   minLength3);
		   fill(newArray, minLength1, minLength2, minLength3, fillVal);
		   return newArray;

	   }

	   public static boolean[][][][] reallocArray(boolean[][][][] array,
			   int minLength1, int minLength2, int minLength3, int minLength4) {
		   if (array == null || array.length < minLength1
				   || array[0].length < minLength2
				   || array[0][0].length < minLength3
				   || array[0][0][0].length < minLength4) {
			   return new boolean[minLength1][minLength2][minLength3][minLength4];
		   }
		   return array;

	   }

	   public static boolean[][][][] reallocArray(boolean[][][][] array,
			   int minLength1, int minLength2, int minLength3, int minLength4,
			   boolean fillVal) {
		   boolean[][][][] newArray = reallocArray(array, minLength1, minLength2,
				   minLength3, minLength4);
		   fill(newArray, minLength1, minLength2, minLength3,
				   minLength4, fillVal);
		   return newArray;

	   }

	   /**
	    * If array is less than the minimum length (or null), a new array is
	    * allocated with length minLength; Otherwise, the argument is returned
	    * 
	    * @param array
	    * @param minLength
	    * @return
	    */
	    public static double[] reallocArray(double[] array, int minLength) {
	    	if (array == null || array.length < minLength) {
	    		return new double[minLength];
	    	}
	    	return array;

	    }

	    public static double[] reallocArray(double[] array, int minLength,
	    		double fillVal) {
	    	double[] newArray = reallocArray(array, minLength);
	    	Arrays.fill(newArray, fillVal);
	    	return newArray;

	    }

	    public static double[][] reallocArray(double[][] array, int minLength1,
	    		int minLength2) {
	    	if (array == null || array.length < minLength1) {
	    		return new double[minLength1][minLength2];
	    	}
	    	return array;

	    }

	    public static double[][] reallocArray(double[][] array, int minLength1,
	    		int minLength2, double fillVal) {
	    	double[][] newArray = reallocArray(array, minLength1, minLength2);
	    	fill(newArray, minLength1, minLength2, fillVal);
	    	return newArray;

	    }

	    public static double[][][] reallocArray(double[][][] array, int minLength1,
	    		int minLength2, int minLength3) {
	    	if (array == null || array.length < minLength1) {
	    		return new double[minLength1][minLength2][minLength3];
	    	}
	    	return array;

	    }

	    public static double[][][] reallocArray(double[][][] array, int minLength1,
	    		int minLength2, int minLength3, double fillVal) {
	    	double[][][] newArray = reallocArray(array, minLength1, minLength2,
	    			minLength3);
	    	fill(newArray, minLength1, minLength2, minLength3, fillVal);
	    	return newArray;

	    }

	    public static double[][][][] reallocArray(double[][][][] array,
	    		int minLength1, int minLength2, int minLength3, int minLength4) {
	    	if (array == null || array.length < minLength1
	    			|| array[0].length < minLength2
	    			|| array[0][0].length < minLength3
	    			|| array[0][0][0].length < minLength4) {
	    		return new double[minLength1][minLength2][minLength3][minLength4];
	    	}
	    	return array;

	    }

	    public static double[][][][] reallocArray(double[][][][] array,
	    		int minLength1, int minLength2, int minLength3, int minLength4,
	    		double fillVal) {
	    	double[][][][] newArray = reallocArray(array, minLength1, minLength2,
	    			minLength3, minLength4);
	    	fill(newArray, minLength1, minLength2, minLength3,
	    			minLength4, fillVal);
	    	return newArray;

	    }

	    public static int[] reallocArray(int[] array, int minLength) {
	    	if (array == null || array.length < minLength) {
	    		return new int[minLength];
	    	}
	    	return array;

	    }

	    public static int[] reallocArray(int[] array, int minLength, int fillVal) {
	    	int[] newArray = reallocArray(array, minLength);
	    	Arrays.fill(newArray, fillVal);
	    	return newArray;

	    }

	    public static int[][] reallocArray(int[][] array, int minLength1,
	    		int minLength2) {
	    	if (array == null || array.length < minLength1) {
	    		return new int[minLength1][minLength2];
	    	}
	    	return array;

	    }

	    public static int[][] reallocArray(int[][] array, int minLength1,
	    		int minLength2, int fillVal) {
	    	int[][] newArray = reallocArray(array, minLength1, minLength2);
	    	fill(newArray, minLength1, minLength2, fillVal);
	    	return newArray;

	    }

	    public static int[][][] reallocArray(int[][][] array, int minLength1,
	    		int minLength2, int minLength3) {
	    	if (array == null || array.length < minLength1) {
	    		return new int[minLength1][minLength2][minLength3];
	    	}
	    	return array;

	    }

	    public static int[][][] reallocArray(int[][][] array, int minLength1,
	    		int minLength2, int minLength3, int fillVal) {
	    	int[][][] newArray = reallocArray(array, minLength1, minLength2,
	    			minLength3);
	    	fill(newArray, minLength1, minLength2, minLength3, fillVal);
	    	return newArray;

	    }

	    public static int[][][][] reallocArray(int[][][][] array, int minLength1,
	    		int minLength2, int minLength3, int minLength4) {
	    	if (array == null || array.length < minLength1
	    			|| array[0].length < minLength2
	    			|| array[0][0].length < minLength3
	    			|| array[0][0][0].length < minLength4) {
	    		return new int[minLength1][minLength2][minLength3][minLength4];
	    	}
	    	return array;

	    }

	    public static int[][][][] reallocArray(int[][][][] array, int minLength1,
	    		int minLength2, int minLength3, int minLength4, int fillVal) {
	    	int[][][][] newArray = reallocArray(array, minLength1, minLength2,
	    			minLength3, minLength4);
	    	fill(newArray, minLength1, minLength2, minLength3,
	    			minLength4, fillVal);
	    	return newArray;

	    }

	    /**
	     * Scales the values in this array by b. Does it in place.
	     */
	     public static void scale(double[] a, double b) {
	    	for (int i = 0; i < a.length; i++) {
	    		a[i] = a[i] * b;
	    	}
	    }

	    /**
	     * Scales the values in this array by b. Does it in place.
	     */
	     public static void scale(float[] a, double b) {
	    	for (int i = 0; i < a.length; i++) {
	    		a[i] = (float) (a[i] * b);
	    	}
	    }

	    public static void setToLogDeterministic(double[] a, int i) {
	    	for (int j = 0; j < a.length; j++) {
	    		if (j == i) {
	    			a[j] = 0.0;
	    		} else {
	    			a[j] = Double.NEGATIVE_INFINITY;
	    		}
	    	}
	    }

	    public static void setToLogDeterministic(float[] a, int i) {
	    	for (int j = 0; j < a.length; j++) {
	    		if (j == i) {
	    			a[j] = 0.0F;
	    		} else {
	    			a[j] = Float.NEGATIVE_INFINITY;
	    		}
	    	}
	    }

	    /**
	     * Shifts the values in this array by b. Does it in place.
	     */
	     public static void shift(double[] a, double b) {
	    	 for (int i = 0; i < a.length; i++) {
	    		 a[i] = a[i] + b;
	    	 }
	     }

	     public static double standardErrorOfMean(double[] a) {
	    	 return stdev(a) / Math.sqrt(a.length);
	     }

	     public static double stdev(double[] a) {
	    	 return Math.sqrt(variance(a));
	     }

	     public static int[] subArray(int[] a, int from, int to) {
	    	 int[] result = new int[to - from];
	    	 System.arraycopy(a, from, result, 0, to - from);
	    	 return result;
	     }

	     public static double[] subArray(double[] a, int from, int to) {
	    	 double[] result = new double[to - from];
	    	 System.arraycopy(a, from, result, 0, to - from);
	    	 return result;
	     }

	     public static double[][] subMatrix(double[][] ds, int i, int ni, int j,
	    		 int nj) {
	    	 double[][] retVal = new double[ni][nj];
	    	 for (int k = i; k < i + ni; ++k) {
	    		 for (int l = j; l < j + nj; ++l) {
	    			 retVal[k - i][l - j] = ds[k][l];
	    		 }
	    	 }
	    	 return retVal;
	     }

	     public static double[] subtract(double[] a, double[] b) {
	    	 double[] c = new double[a.length];

	    	 for (int i = 0; i < a.length; i++) {
	    		 c[i] = a[i] - b[i];
	    	 }
	    	 return c;
	     }

	     public static void subtract(double[] a, double[] b, double[] scratch) {
	    	 assert b != null;
	    	 assert a.length == b.length;
	    	 for (int i = 0; i < a.length; ++i) {
	    		 scratch[i] = a[i] - b[i];
	    	 }
	     }

	     public static void multiply(double[] a, double b, double[] scratch) {
	    	 assert a != null;
	    	 for (int i = 0; i < a.length; ++i) {
	    		 scratch[i] = a[i] * b;
	    	 }
	     }

	     public static float[] subtract(float[] a, float[] b) {
	    	 float[] c = new float[a.length];

	    	 for (int i = 0; i < a.length; i++) {
	    		 c[i] = a[i] - b[i];
	    	 }
	    	 return c;
	     }

	     public static double sum(double[] a) {
	    	 if (a == null) {
	    		 return 0.0;
	    	 }
	    	 double result = 0.0;
	    	 for (int i = 0; i < a.length; i++) {
	    		 result += a[i];
	    	 }
	    	 return result;
	     }

	     public static double sum(double[][] a) {
	    	 if (a == null) {
	    		 return 0.0;
	    	 }
	    	 double result = 0.0;
	    	 for (int i = 0; i < a.length; i++) {
	    		 result += sum(a[i]);
	    	 }
	    	 return result;
	     }

	     public static double sum(double[][][] a) {
	    	 if (a == null) {
	    		 return 0.0;
	    	 }
	    	 double result = 0.0;
	    	 for (int i = 0; i < a.length; i++) {
	    		 result += sum(a[i]);
	    	 }
	    	 return result;
	     }

	     public static double sum(double[] a, int len) {
	    	 double result = 0.0;
	    	 for (int i = 0; i < len; i++) {
	    		 result += a[i];
	    	 }
	    	 return result;
	     }

	     public static float sum(float[] a) {
	    	 float result = 0.0F;
	    	 for (int i = 0; i < a.length; i++) {
	    		 result += a[i];
	    	 }
	    	 return result;
	     }

	     public static int sum(int[] a) {
	    	 int result = 0;
	    	 for (int i = 0; i < a.length; i++) {
	    		 result += a[i];
	    	 }
	    	 return result;
	     }

	     public static long sum(long[] a) {
	    	 int result = 0;
	    	 for (int i = 0; i < a.length; i++) {
	    		 result += a[i];
	    	 }
	    	 return result;
	     }

	     public static double sumSquaredError(double[] a) {
	    	 double mean = mean(a);
	    	 double result = 0.0;
	    	 for (int i = 0; i < a.length; i++) {
	    		 double diff = a[i] - mean;
	    		 result += (diff * diff);
	    	 }
	    	 return result;
	     }



	     public static String toString(byte[] a) {
	    	 return toString(a, null);
	     }

	     public static String toString(byte[] a, NumberFormat nf) {
	    	 if (a == null)
	    		 return null;
	    	 if (a.length == 0)
	    		 return "[]";
	    	 StringBuffer b = new StringBuffer();
	    	 b.append("[");
	    	 for (int i = 0; i < a.length - 1; i++) {
	    		 String s;
	    		 if (nf == null) {
	    			 s = String.valueOf(a[i]);
	    		 } else {
	    			 s = nf.format(a[i]);
	    		 }
	    		 b.append(s);
	    		 b.append(", ");
	    	 }
	    	 String s;
	    	 if (nf == null) {
	    		 s = String.valueOf(a[a.length - 1]);
	    	 } else {
	    		 s = nf.format(a[a.length - 1]);
	    	 }
	    	 b.append(s);
	    	 b.append(']');
	    	 return b.toString();
	     }

	     public static String toString(double[] a) {
	    	 return toString(a, null);
	     }

	     public static String toString(double[] a, NumberFormat nf) {
	    	 if (a == null)
	    		 return null;
	    	 if (a.length == 0)
	    		 return "[]";
	    	 StringBuffer b = new StringBuffer();
	    	 b.append("[");
	    	 for (int i = 0; i < a.length - 1; i++) {
	    		 String s;
	    		 if (nf == null) {
	    			 s = String.valueOf(a[i]);
	    		 } else {
	    			 s = nf.format(a[i]);
	    		 }
	    		 b.append(s);
	    		 b.append(", ");
	    	 }
	    	 String s;
	    	 if (nf == null) {
	    		 s = String.valueOf(a[a.length - 1]);
	    	 } else {
	    		 s = nf.format(a[a.length - 1]);
	    	 }
	    	 b.append(s);
	    	 b.append(']');
	    	 return b.toString();
	     }

	     public static String toString(double[][] counts) {
	    	 return toString(counts, 10, null, null, NumberFormat.getInstance(),
	    			 false);
	     }

	     public static String toString(double[][] counts, int cellSize,
	    		 Object[] rowLabels, Object[] colLabels, NumberFormat nf,
	    		 boolean printTotals) {
	    	 if (counts == null)
	    		 return null;
	    	 // first compute row totals and column totals
	    	 double[] rowTotals = new double[counts.length];
	    	 double[] colTotals = new double[counts[0].length]; // assume it's square
	    	 double total = 0.0;
	    	 for (int i = 0; i < counts.length; i++) {
	    		 for (int j = 0; j < counts[i].length; j++) {
	    			 rowTotals[i] += counts[i][j];
	    			 colTotals[j] += counts[i][j];
	    			 total += counts[i][j];
	    		 }
	    	 }
	    	 StringBuffer result = new StringBuffer();
	    	 // column labels
	    	 if (colLabels != null) {
	    		 result.append(StringUtils.padLeft("", cellSize));
	    		 for (int j = 0; j < counts[0].length; j++) {
	    			 String s = colLabels[j].toString();
	    			 if (s.length() > cellSize - 1) {
	    				 s = s.substring(0, cellSize - 1);
	    			 }
	    			 s = StringUtils.padLeft(s, cellSize);
	    			 result.append(s);
	    		 }
	    		 if (printTotals) {
	    			 result.append(StringUtils.padLeft("Total", cellSize));
	    		 }
	    		 result.append("\n\n");
	    	 }
	    	 for (int i = 0; i < counts.length; i++) {
	    		 // row label
	    		 if (rowLabels != null) {
	    			 String s = rowLabels[i].toString();
	    			 s = StringUtils.pad(s, cellSize); // left align this guy only
	    			 result.append(s);
	    		 }
	    		 // value
	    		 for (int j = 0; j < counts[i].length; j++) {
	    			 result.append(StringUtils.padLeft(nf.format(counts[i][j]),
	    					 cellSize));
	    		 }
	    		 // the row total
	    		 if (printTotals) {
	    			 result.append(StringUtils.padLeft(nf.format(rowTotals[i]),
	    					 cellSize));
	    		 }
	    		 result.append("\n");
	    	 }
	    	 result.append("\n");
	    	 // the col totals
	    	 if (printTotals) {
	    		 result.append(StringUtils.pad("Total", cellSize));
	    		 for (int j = 0; j < colTotals.length; j++) {
	    			 result.append(StringUtils.padLeft(nf.format(colTotals[j]),
	    					 cellSize));
	    		 }
	    		 result.append(StringUtils.padLeft(nf.format(total), cellSize));
	    	 }
	    	 result.append("\n");
	    	 return result.toString();
	     }

	     public static String toString(double[][][] a) {
	    	 String s = "[";
	    	 for (int i = 0; i < a.length; i++) {
	    		 s = s.concat(toString(a[i]) + ", ");
	    	 }
	    	 return s + "]";
	     }

	     public static String toString(float[] a) {
	    	 return toString(a, null);
	     }

	     public static String toString(float[] a, NumberFormat nf) {
	    	 if (a == null)
	    		 return null;
	    	 if (a.length == 0)
	    		 return "[]";
	    	 StringBuffer b = new StringBuffer();
	    	 b.append("[");
	    	 for (int i = 0; i < a.length - 1; i++) {
	    		 String s;
	    		 if (nf == null) {
	    			 s = String.valueOf(a[i]);
	    		 } else {
	    			 s = nf.format(a[i]);
	    		 }
	    		 b.append(s);
	    		 b.append(", ");
	    	 }
	    	 String s;
	    	 if (nf == null) {
	    		 s = String.valueOf(a[a.length - 1]);
	    	 } else {
	    		 s = nf.format(a[a.length - 1]);
	    	 }
	    	 b.append(s);
	    	 b.append(']');
	    	 return b.toString();
	     }

	     public static String toString(float[][] counts) {
	    	 return toString(counts, 10, null, null,
	    			 NumberFormat.getIntegerInstance(), false);
	     }

	     // CASTS

	     public static String toString(float[][] counts, int cellSize,
	    		 Object[] rowLabels, Object[] colLabels, NumberFormat nf,
	    		 boolean printTotals) {
	    	 // first compute row totals and column totals
	    	 double[] rowTotals = new double[counts.length];
	    	 double[] colTotals = new double[counts[0].length]; // assume it's square
	    	 double total = 0.0;
	    	 for (int i = 0; i < counts.length; i++) {
	    		 for (int j = 0; j < counts[i].length; j++) {
	    			 rowTotals[i] += counts[i][j];
	    			 colTotals[j] += counts[i][j];
	    			 total += counts[i][j];
	    		 }
	    	 }
	    	 StringBuffer result = new StringBuffer();
	    	 // column labels
	    	 if (colLabels != null) {
	    		 result.append(StringUtils.padLeft("", cellSize));
	    		 for (int j = 0; j < counts[0].length; j++) {
	    			 String s = colLabels[j].toString();
	    			 if (s.length() > cellSize - 1) {
	    				 s = s.substring(0, cellSize - 1);
	    			 }
	    			 s = StringUtils.padLeft(s, cellSize);
	    			 result.append(s);
	    		 }
	    		 if (printTotals) {
	    			 result.append(StringUtils.padLeft("Total", cellSize));
	    		 }
	    		 result.append("\n\n");
	    	 }
	    	 for (int i = 0; i < counts.length; i++) {
	    		 // row label
	    		 if (rowLabels != null) {
	    			 String s = rowLabels[i].toString();
	    			 s = StringUtils.pad(s, cellSize); // left align this guy only
	    			 result.append(s);
	    		 }
	    		 // value
	    		 for (int j = 0; j < counts[i].length; j++) {
	    			 result.append(StringUtils.padLeft(nf.format(counts[i][j]),
	    					 cellSize));
	    		 }
	    		 // the row total
	    		 if (printTotals) {
	    			 result.append(StringUtils.padLeft(nf.format(rowTotals[i]),
	    					 cellSize));
	    		 }
	    		 result.append("\n");
	    	 }
	    	 result.append("\n");
	    	 // the col totals
	    	 if (printTotals) {
	    		 result.append(StringUtils.pad("Total", cellSize));
	    		 for (int j = 0; j < colTotals.length; j++) {
	    			 result.append(StringUtils.padLeft(nf.format(colTotals[j]),
	    					 cellSize));
	    		 }
	    		 result.append(StringUtils.padLeft(nf.format(total), cellSize));
	    	 }
	    	 result.append("\n");
	    	 return result.toString();
	     }

	     public static String toString(float[][][] a) {
	    	 String s = "[";
	    	 for (int i = 0; i < a.length; i++) {
	    		 s = s.concat(toString(a[i]) + ", ");
	    	 }
	    	 return s + "]";
	     }

	     public static String toString(int[] a) {
	    	 return toString(a, null);
	     }

	     public static String toString(int[] a, NumberFormat nf) {
	    	 if (a == null)
	    		 return null;
	    	 if (a.length == 0)
	    		 return "[]";
	    	 StringBuffer b = new StringBuffer();
	    	 b.append("[");
	    	 for (int i = 0; i < a.length - 1; i++) {
	    		 String s;
	    		 if (nf == null) {
	    			 s = String.valueOf(a[i]);
	    		 } else {
	    			 s = nf.format(a[i]);
	    		 }
	    		 b.append(s);
	    		 b.append(", ");
	    	 }
	    	 String s;
	    	 if (nf == null) {
	    		 s = String.valueOf(a[a.length - 1]);
	    	 } else {
	    		 s = nf.format(a[a.length - 1]);
	    	 }
	    	 b.append(s);
	    	 b.append(']');
	    	 return b.toString();
	     }

	     // ARITHMETIC FUNCTIONS

	     public static String toString(int[][] counts) {
	    	 return toString(counts, 10, null, null, NumberFormat.getInstance(),
	    			 false);
	     }

	     public static String toString(int[][] counts, int cellSize,
	    		 Object[] rowLabels, Object[] colLabels, NumberFormat nf,
	    		 boolean printTotals) {
	    	 // first compute row totals and column totals
	    	 int[] rowTotals = new int[counts.length];
	    	 int[] colTotals = new int[counts[0].length]; // assume it's square
	    	 int total = 0;
	    	 for (int i = 0; i < counts.length; i++) {
	    		 for (int j = 0; j < counts[i].length; j++) {
	    			 rowTotals[i] += counts[i][j];
	    			 colTotals[j] += counts[i][j];
	    			 total += counts[i][j];
	    		 }
	    	 }
	    	 StringBuilder result = new StringBuilder();
	    	 // column labels
	    	 if (colLabels != null) {
	    		 result.append(StringUtils.padLeft("", cellSize));
	    		 for (int j = 0; j < counts[0].length; j++) {
	    			 String s = colLabels[j].toString();
	    			 if (s.length() > cellSize - 1) {
	    				 s = s.substring(0, cellSize - 1);
	    			 }
	    			 s = StringUtils.padLeft(s, cellSize);
	    			 result.append(s);
	    		 }
	    		 if (printTotals) {
	    			 result.append(StringUtils.padLeft("Total", cellSize));
	    		 }
	    		 result.append("\n\n");
	    	 }
	    	 for (int i = 0; i < counts.length; i++) {
	    		 // row label
	    		 if (rowLabels != null) {
	    			 String s = rowLabels[i].toString();
	    			 s = StringUtils.padOrTrim(s, cellSize); // left align this guy
	    			 // only
	    			 result.append(s);
	    		 }
	    		 // value
	    		 for (int j = 0; j < counts[i].length; j++) {
	    			 result.append(StringUtils.padLeft(nf.format(counts[i][j]),
	    					 cellSize));
	    		 }
	    		 // the row total
	    		 if (printTotals) {
	    			 result.append(StringUtils.padLeft(nf.format(rowTotals[i]),
	    					 cellSize));
	    		 }
	    		 result.append("\n");
	    	 }
	    	 result.append("\n");
	    	 // the col totals
	    	 if (printTotals) {
	    		 result.append(StringUtils.pad("Total", cellSize));
	    		 for (int j = 0; j < colTotals.length; j++) {
	    			 result.append(StringUtils.padLeft(nf.format(colTotals[j]),
	    					 cellSize));
	    		 }
	    		 result.append(StringUtils.padLeft(nf.format(total), cellSize));
	    	 }
	    	 result.append("\n");
	    	 return result.toString();
	     }

	     public static double variance(double[] a) {
	    	 return sumSquaredError(a) / (a.length - 1);
	     }

	     private static void printMatrix(double[][] a) {
	    	 final int len = 5;
	    	 for (int i = 0; i < len; ++i) {
	    		 System.out.print("[");
	    		 for (int j = 0; j < len; ++j) {
	    			 System.out.print(a[i][j] + "\t,");
	    		 }
	    		 System.out.println("]");
	    	 }
	     }

	     public static <T> boolean hasNull(T[] array) {
	    	 for (T t : array) {
	    		 if (t == null)
	    			 return true;
	    	 }
	    	 return false;
	     }

	     public static byte[] gapEncode(int[] orig) {
	    	 List<Byte> encodedList = gapEncodeList(orig);
	    	 byte[] arr = new byte[encodedList.size()];
	    	 int i = 0;
	    	 for (byte b : encodedList) { arr[i++] = b; }
	    	 return arr;
	     }

	     public static List<Byte> gapEncodeList(int[] orig) {
	    	 for (int i = 1; i < orig.length; i++) {
	    		 if (orig[i] < orig[i-1]) { 
	    			 throw new IllegalArgumentException("Array must be sorted!"); 
	    		 }
	    	 }

	    	 List<Byte> bytes = new ArrayList<Byte>();

	    	 int index = 0;    
	    	 int prevNum = 0;
	    	 byte currByte = 0 << 8;

	    	 for (int f : orig) {
	    		 String n = (f == prevNum ? "" : Integer.toString(f-prevNum, 2));
	    		 for (int ii = 0; ii < n.length(); ii++) {
	    			 if (index == 8) {
	    				 bytes.add(currByte);
	    				 currByte = 0 << 8;
	    				 index = 0;
	    			 }
	    			 currByte <<= 1;
	    			 currByte++;
	    			 index++;
	    		 }

	    		 if (index == 8) {
	    			 bytes.add(currByte);
	    			 currByte = 0 << 8;
	    			 index = 0;
	    		 }
	    		 currByte <<= 1;
	    		 index++;

	    		 for (int i = 1; i < n.length(); i++) {
	    			 if (index == 8) {
	    				 bytes.add(currByte);
	    				 currByte = 0 << 8;
	    				 index = 0;
	    			 }
	    			 currByte <<= 1;
	    			 if (n.charAt(i) == '1') {
	    				 currByte++;
	    			 }
	    			 index++;
	    		 }
	    		 prevNum = f;
	    	 }

	    	 while (index > 0 && index < 9) {
	    		 if (index == 8) {
	    			 bytes.add(currByte);
	    			 break;
	    		 }
	    		 currByte <<= 1;
	    		 currByte++;
	    		 index++;
	    	 }

	    	 return bytes;
	     }

	     public static int[] gapDecode(byte[] gapEncoded) {
	    	 return gapDecode(gapEncoded, 0, gapEncoded.length);
	     }

	     public static int[] gapDecode(byte[] gapEncoded, int startIndex, int endIndex) {
	    	 List<Integer> ints = gapDecodeList(gapEncoded, startIndex, endIndex);
	    	 int[] arr = new int[ints.size()];
	    	 int index = 0;
	    	 for (int i : ints) { arr[index++] = i; }
	    	 return arr;
	     }

	     public static List<Integer> gapDecodeList(byte[] gapEncoded) {
	    	 return gapDecodeList(gapEncoded, 0, gapEncoded.length);
	     }

	     public static List<Integer> gapDecodeList(byte[] gapEncoded, int startIndex, int endIndex) {

	    	 boolean gettingSize = true;
	    	 int size = 0;
	    	 List<Integer> ints = new ArrayList<Integer>();
	    	 int gap = 0;
	    	 int prevNum = 0;

	    	 for (int i = startIndex; i < endIndex; i++) {
	    		 byte b = gapEncoded[i];
	    		 for (int index = 7; index >= 0; index--) {
	    			 boolean value = ((b >> index) & 1) == 1;

	    			 if (gettingSize) {
	    				 if (value) { size++; }
	    				 else {
	    					 if (size == 0) {
	    						 ints.add(prevNum);
	    					 } else if (size == 1) {
	    						 prevNum++;
	    						 ints.add(prevNum);
	    						 size = 0;
	    					 } else {
	    						 gettingSize = false;
	    						 gap = 1;
	    						 size--;
	    					 }
	    				 }
	    			 } else {
	    				 gap <<= 1;
	    				 if (value) { gap++; }
	    				 size--;
	    				 if (size == 0) {
	    					 prevNum += gap;
	    					 ints.add(prevNum);
	    					 gettingSize = true;
	    				 }
	    			 }
	    		 }
	    	 }

	    	 return ints;
	     }

	     public static byte[] deltaEncode(int[] orig) {
	    	 List<Byte> encodedList = deltaEncodeList(orig);
	    	 byte[] arr = new byte[encodedList.size()];
	    	 int i = 0;
	    	 for (byte b : encodedList) { arr[i++] = b; }
	    	 return arr;
	     }

	     public static List<Byte> deltaEncodeList(int[] orig) {

	    	 for (int i = 1; i < orig.length; i++) {
	    		 if (orig[i] < orig[i-1]) { 
	    			 throw new IllegalArgumentException("Array must be sorted!"); 
	    		 }
	    	 }

	    	 List<Byte> bytes = new ArrayList<Byte>();

	    	 int index = 0;    
	    	 int prevNum = 0;
	    	 byte currByte = 0 << 8;

	    	 for (int f : orig) {
	    		 String n = (f == prevNum ? "" : Integer.toString(f-prevNum, 2));
	    		 String n1 = (n.length() == 0 ? "" : Integer.toString(n.length(), 2));
	    		 for (int ii = 0; ii < n1.length(); ii++) {
	    			 if (index == 8) {
	    				 bytes.add(currByte);
	    				 currByte = 0 << 8;
	    				 index = 0;
	    			 }
	    			 currByte <<= 1;
	    			 currByte++;
	    			 index++;
	    		 }

	    		 if (index == 8) {
	    			 bytes.add(currByte);
	    			 currByte = 0 << 8;
	    			 index = 0;
	    		 }
	    		 currByte <<= 1;
	    		 index++;

	    		 for (int i = 1; i < n1.length(); i++) {
	    			 if (index == 8) {
	    				 bytes.add(currByte);
	    				 currByte = 0 << 8;
	    				 index = 0;
	    			 }
	    			 currByte <<= 1;
	    			 if (n1.charAt(i) == '1') {
	    				 currByte++;
	    			 }
	    			 index++;
	    		 }

	    		 for (int i = 1; i < n.length(); i++) {
	    			 if (index == 8) {
	    				 bytes.add(currByte);
	    				 currByte = 0 << 8;
	    				 index = 0;
	    			 }
	    			 currByte <<= 1;
	    			 if (n.charAt(i) == '1') {
	    				 currByte++;
	    			 }
	    			 index++;
	    		 }

	    		 prevNum = f;

	    	 }

	    	 while (index > 0 && index < 9) {
	    		 if (index == 8) {
	    			 bytes.add(currByte);
	    			 break;
	    		 }
	    		 currByte <<= 1;
	    		 currByte++;
	    		 index++;
	    	 }

	    	 return bytes;
	     }


	     public static int[] deltaDecode(byte[] deltaEncoded) {
	    	 return deltaDecode(deltaEncoded, 0, deltaEncoded.length);
	     }

	     public static int[] deltaDecode(byte[] deltaEncoded, int startIndex, int endIndex) {
	    	 List<Integer> ints = deltaDecodeList(deltaEncoded);
	    	 int[] arr = new int[ints.size()];
	    	 int index = 0;
	    	 for (int i : ints) { arr[index++] = i; }
	    	 return arr;
	     }

	     public static List<Integer> deltaDecodeList(byte[] deltaEncoded) {
	    	 return deltaDecodeList(deltaEncoded, 0, deltaEncoded.length);
	     }

	     public static List<Integer> deltaDecodeList(byte[] deltaEncoded, int startIndex, int endIndex) {

	    	 boolean gettingSize1 = true;
	    	 boolean gettingSize2 = false;
	    	 int size1 = 0;
	    	 List<Integer> ints = new ArrayList<Integer>();
	    	 int gap = 0;
	    	 int size2 = 0;
	    	 int prevNum = 0;

	    	 for (int i = startIndex; i < endIndex; i++) {
	    		 byte b = deltaEncoded[i];
	    		 for (int index = 7; index >= 0; index--) {
	    			 boolean value = ((b >> index) & 1) == 1;

	    			 if (gettingSize1) {
	    				 if (value) { size1++; }
	    				 else {
	    					 if (size1 == 0) {
	    						 ints.add(prevNum);
	    					 } else if (size1 == 1) {
	    						 prevNum++;
	    						 ints.add(prevNum);
	    						 size1 = 0;
	    					 } else {
	    						 gettingSize1 = false;
	    						 gettingSize2 = true;
	    						 size2 = 1;
	    						 size1--;
	    					 }
	    				 }
	    			 } else if (gettingSize2) {
	    				 size2 <<= 1;
	    				 if (value) { size2++; }
	    				 size1--;
	    				 if (size1 == 0) {
	    					 gettingSize2 = false;
	    					 gap = 1;
	    					 size2--;
	    				 }
	    			 } else {
	    				 gap <<= 1;
	    				 if (value) { gap++; }
	    				 size2--;
	    				 if (size2 == 0) {
	    					 prevNum += gap;
	    					 ints.add(prevNum);
	    					 gettingSize1 = true;
	    				 }
	    			 }
	    		 }
	    	 }

	    	 return ints;
	     }                            

	     /** helper for gap encoding. */
	     private static byte[] bitSetToByteArray(BitSet bitSet) {

	    	 while (bitSet.length() % 8 != 0) { bitSet.set(bitSet.length(), true); }

	    	 byte[] array = new byte[bitSet.length()/8];

	    	 for (int i = 0; i < array.length; i++) {
	    		 int offset = i * 8;

	    		 int index = 0;
	    		 for (int j = 0; j < 8; j++) {
	    			 index <<= 1;        
	    			 if (bitSet.get(offset+j)) { index++; }
	    		 }

	    		 array[i] = (byte)(index - 128);
	    	 }

	    	 return array;
	     }

	     /** helper for gap encoding. */
	     private static BitSet byteArrayToBitSet(byte[] array) {

	    	 BitSet bitSet = new BitSet();
	    	 int index = 0;
	    	 for (byte b : array) {
	    		 int b1 = ((int)b) + 128;

	    		 bitSet.set(index++, (b1 >> 7) % 2 == 1);
	    		 bitSet.set(index++, (b1 >> 6) % 2 == 1);
	    		 bitSet.set(index++, (b1 >> 5) % 2 == 1);
	    		 bitSet.set(index++, (b1 >> 4) % 2 == 1);
	    		 bitSet.set(index++, (b1 >> 3) % 2 == 1);
	    		 bitSet.set(index++, (b1 >> 2) % 2 == 1);
	    		 bitSet.set(index++, (b1 >> 1) % 2 == 1);
	    		 bitSet.set(index++, b1 % 2 == 1);
	    	 }

	    	 return bitSet;
	     }

	     //     for (int i = 1; i < orig.length; i++) {
	     //       if (orig[i] < orig[i-1]) { throw new RuntimeException("Array must be sorted!"); }

	     //       StringBuilder bits = new StringBuilder(); 
	     //       int prevNum = 0;
	     //       for (int f : orig) {
	     //         StringBuilder bits1 = new StringBuilder();
	     //               System.err.print(f+"\t");
	     //               String n = Integer.toString(f-prevNum, 2);
	     //               String n1 = Integer.toString(n.length(), 2);
	     //               for (int ii = 0; ii < n1.length(); ii++) {
	     //                 bits1.append("1");                
	     //               }
	     //               bits1.append("0");
	     //               bits1.append(n1.substring(1));
	     //               bits1.append(n.substring(1));
	     //               System.err.print(bits1+"\t");
	     //               bits.append(bits1);
	     //               prevNum = f;
	     //             }



	     public static double[] flatten(double[][] array) {
	    	 int size = 0;
	    	 for (double[] a : array) {
	    		 size += a.length;
	    	 }
	    	 double[] newArray = new double[size];
	    	 int i = 0;
	    	 for (double[] a : array) {
	    		 for (double d : a) {
	    			 newArray[i++] = d;
	    		 }
	    	 }
	    	 return newArray;
	     }

	     public static double[][] to2D(double[] array, int dim1Size) {
	    	 int dim2Size = array.length/dim1Size;
	    	 return to2D(array, dim1Size, dim2Size);
	     }

	     public static double[][] to2D(double[] array, int dim1Size, int dim2Size) {
	    	 double[][] newArray = new double[dim1Size][dim2Size];
	    	 int k = 0;
	    	 for (int i = 0; i < newArray.length; i++) {
	    		 for (int j = 0; j < newArray[i].length; j++) {
	    			 newArray[i][j] = array[k++];
	    		 }
	    	 }
	    	 return newArray;
	     }

	     /**
	      * Removes the element at the specified index from the array, and returns
	      * a new array containing the remaining elements.  If <tt>index</tt> is
	      * invalid, returns <tt>array</tt> unchanged.
	      */
	     public static double[] removeAt(double[] array, int index) {
	    	 if (array == null) {
	    		 return null;
	    	 }
	    	 if (index < 0 || index >= array.length) {
	    		 return array;
	    	 }

	    	 double[] retVal = new double[array.length - 1];
	    	 for (int i = 0; i < array.length; i++) {
	    		 if (i < index) {
	    			 retVal[i] = array[i];
	    		 } else if (i > index) {
	    			 retVal[i - 1] = array[i];
	    		 }
	    	 }
	    	 return retVal;
	     }

	     /**
	      * Removes the element at the specified index from the array, and returns
	      * a new array containing the remaining elements.  If <tt>index</tt> is
	      * invalid, returns <tt>array</tt> unchanged.  Uses reflection to determine
	      * the type of the array and returns an array of the appropriate type.
	      */
	     public static Object[] removeAt(Object[] array, int index) {
	    	 if (array == null) {
	    		 return null;
	    	 }
	    	 if (index < 0 || index >= array.length) {
	    		 return array;
	    	 }

	    	 Object[] retVal = (Object[]) Array.newInstance(array[0].getClass(), array.length - 1);
	    	 for (int i = 0; i < array.length; i++) {
	    		 if (i < index) {
	    			 retVal[i] = array[i];
	    		 } else if (i > index) {
	    			 retVal[i - 1] = array[i];
	    		 }
	    	 }
	    	 return retVal;
	     }

	     /* public static String toString(int[][] a) {
    StringBuilder result = new StringBuilder("[");
    for (int i = 0; i < a.length; i++) {
      result.append(Arrays.toString(a[i]));
      if(i < a.length-1)
        result.append(',');
      }
    result.append(']');
    return result.toString();
  } */

	     /**
	      * Tests two int[][] arrays for having equal contents.
	      * @return true iff for each i, <code>equalContents(xs[i],ys[i])</code> is true
	      */
	     public static boolean equalContents(int[][] xs, int[][] ys) {
	    	 if(xs ==null)
	    		 return ys == null;
	    	 if(ys == null)
	    		 return false;
	    	 if(xs.length != ys.length)
	    		 return false;
	    	 for(int i = xs.length-1; i >= 0; i--) {
	    		 if(! equalContents(xs[i],ys[i]))
	    			 return false;
	    	 }
	    	 return true;
	     }

	     /**
	      * Tests two double[][] arrays for having equal contents.
	      * @return true iff for each i, <code>equals(xs[i],ys[i])</code> is true
	      */
	     public static boolean equals(double[][] xs, double[][] ys) {
	    	 if(xs == null)
	    		 return ys == null;
	    	 if(ys == null)
	    		 return false;
	    	 if(xs.length != ys.length)
	    		 return false;
	    	 for(int i = xs.length-1; i >= 0; i--) {
	    		 if(!Arrays.equals(xs[i],ys[i]))
	    			 return false;
	    	 }
	    	 return true;
	     }


	     /**
	      * tests two int[] arrays for having equal contents
	      * @return true iff xs and ys have equal length, and for each i, <code>xs[i]==ys[i]</code>
	      */
	     public static boolean equalContents(int[] xs, int[] ys) {
	    	 if(xs.length != ys.length)
	    		 return false;
	    	 for(int i = xs.length-1; i >= 0; i--) {
	    		 if(xs[i] != ys[i])
	    			 return false;
	    	 }
	    	 return true;
	     }

	     /**
	      * Tests two boolean[][] arrays for having equal contents.
	      * @return true iff for each i, <code>Arrays.equals(xs[i],ys[i])</code> is true
	      */
	     @SuppressWarnings("null")
	     public static boolean equals(boolean[][] xs, boolean[][] ys) {
	    	 if(xs == null && ys != null)
	    		 return false;
	    	 if(ys == null)
	    		 return false;
	    	 if(xs.length != ys.length)
	    		 return false;
	    	 for(int i = xs.length-1; i >= 0; i--) {
	    		 if(! Arrays.equals(xs[i],ys[i]))
	    			 return false;
	    	 }
	    	 return true;
	     }


	     /** Returns true iff object o equals (not ==) some element of array a. */
	     public static <T> boolean contains(T[] a, T o) {
	    	 for (T item : a) {
	    		 if (item.equals(o)) return true;
	    	 }
	    	 return false;
	     }

	     /** Return a set containing the same elements as the specified array.
	      */
	     public static <T> Set<T> asSet(T[] a) {
	    	 return new HashSet<T>(Arrays.asList(a));
	     }

	     /**
	      * Casts to a double array
	      */
	     public static double[] toDouble(float[] a) {
	    	 double[] d = new double[a.length];
	    	 for (int i = 0; i < a.length; i++) {
	    		 d[i] = a[i];
	    	 }
	    	 return d;
	     }

	     /**
	      * Casts to a double array.
	      */
	     public static double[] toDouble(int[] array) {
	    	 double[] rv = new double[array.length];
	    	 for (int i = 0; i < array.length; i++) {
	    		 rv[i] = array[i];
	    	 }
	    	 return rv;
	     }

	     /** needed because Arrays.asList() won't to autoboxing,
	      * so if you give it a primitive array you get a
	      * singleton list back with just that array as an element.
	      */
	     public static List<Integer> asList(int[] array) {
	    	 List<Integer> l = new ArrayList<Integer>();
	    	 for (int i : array) {
	    		 l.add(i);
	    	 }
	    	 return l;
	     }


	     public static double[] asPrimitiveDoubleArray(Collection<Double> d) {
	    	 double[] newD = new double[d.size()];
	    	 int i = 0;
	    	 for (Double j : d) {
	    		 newD[i++] = j;
	    	 }
	    	 return newD;
	     }


	     public static int[] asPrimitiveIntArray(Collection<Integer> d) {
	    	 int[] newI = new int[d.size()];
	    	 int i = 0;
	    	 for (Integer j : d) {
	    		 newI[i++] = j;
	    	 }
	    	 return newI;
	     }


	     public static int[] copy(int[] i) {
	    	 if (i == null) { return null; }
	    	 int[] newI = new int[i.length];
	    	 System.arraycopy(i, 0, newI, 0, i.length);
	    	 return newI;
	     }

	     public static int[][] copy(int[][] i) {
	    	 if (i == null) { return null; }
	    	 int[][] newI = new int[i.length][];
	    	 for (int j = 0; j < newI.length; j++) {
	    		 newI[j] = copy(i[j]);
	    	 }
	    	 return newI;
	     }


	     public static double[] copy(double[] d) {
	    	 if (d == null) { return null; }
	    	 double[] newD = new double[d.length];
	    	 System.arraycopy(d, 0, newD, 0, d.length);
	    	 return newD;
	     }

	     public static double[][] copy(double[][] d) {
	    	 if (d == null) { return null; }
	    	 double[][] newD = new double[d.length][];
	    	 for (int i = 0; i < newD.length; i++) {
	    		 newD[i] = copy(d[i]);
	    	 }
	    	 return newD;
	     }

	     public static double[][][] copy(double[][][] d) {
	    	 if (d == null) { return null; }
	    	 double[][][] newD = new double[d.length][][];
	    	 for (int i = 0; i < newD.length; i++) {
	    		 newD[i] = copy(d[i]);
	    	 }
	    	 return newD;
	     }

	     public static float[] copy(float[] d) {
	    	 if (d == null) { return null; }
	    	 float[] newD = new float[d.length];
	    	 System.arraycopy(d, 0, newD, 0, d.length);
	    	 return newD;
	     }

	     public static float[][] copy(float[][] d) {
	    	 if (d == null) { return null; }
	    	 float[][] newD = new float[d.length][];
	    	 for (int i = 0; i < newD.length; i++) {
	    		 newD[i] = copy(d[i]);
	    	 }
	    	 return newD;
	     }

	     public static float[][][] copy(float[][][] d) {
	    	 if (d == null) { return null; }
	    	 float[][][] newD = new float[d.length][][];
	    	 for (int i = 0; i < newD.length; i++) {
	    		 newD[i] = copy(d[i]);
	    	 }
	    	 return newD;
	     }


	     public static String toString(boolean[][] b) {
	    	 StringBuilder result = new StringBuilder("[");
	    	 for (int i = 0; i < b.length; i++) {
	    		 result.append(Arrays.toString(b[i]));
	    		 if(i < b.length-1)
	    			 result.append(',');
	    	 }
	    	 result.append(']');
	    	 return result.toString();
	     }

	     public static long[] toPrimitive(Long[] in) {
	    	 return toPrimitive(in,0L);
	     }

	     public static int[] toPrimitive(Integer[] in) {
	    	 return toPrimitive(in,0);
	     }

	     public static short[] toPrimitive(Short[] in) {
	    	 return toPrimitive(in,(short)0);
	     }

	     public static char[] toPrimitive(Character[] in) {
	    	 return toPrimitive(in,(char)0);
	     }

	     public static double[] toPrimitive(Double[] in) {
	    	 return toPrimitive(in,0.0);
	     }

	     public static long[] toPrimitive(Long[] in, long valueForNull) {
	    	 if (in == null)
	    		 return null;
	    	 final long[] out = new long[in.length];
	    	 for (int i = 0; i < in.length; i++) {
	    		 Long b = in[i];
	    		 out[i] = (b == null ? valueForNull : b);
	    	 }
	    	 return out;
	     }

	     public static int[] toPrimitive(Integer[] in, int valueForNull) {
	    	 if (in == null)
	    		 return null;
	    	 final int[] out = new int[in.length];
	    	 for (int i = 0; i < in.length; i++) {
	    		 Integer b = in[i];
	    		 out[i] = (b == null ? valueForNull : b);
	    	 }
	    	 return out;
	     }

	     public static short[] toPrimitive(Short[] in, short valueForNull) {
	    	 if (in == null)
	    		 return null;
	    	 final short[] out = new short[in.length];
	    	 for (int i = 0; i < in.length; i++) {
	    		 Short b = in[i];
	    		 out[i] = (b == null ? valueForNull : b);
	    	 }
	    	 return out;
	     }

	     public static char[] toPrimitive(Character[] in, char valueForNull) {
	    	 if (in == null)
	    		 return null;
	    	 final char[] out = new char[in.length];
	    	 for (int i = 0; i < in.length; i++) {
	    		 Character b = in[i];
	    		 out[i] = (b == null ? valueForNull : b);
	    	 }
	    	 return out;
	     }

	     public static double[] toPrimitive(Double[] in, double valueForNull) {
	    	 if (in == null)
	    		 return null;
	    	 final double[] out = new double[in.length];
	    	 for (int i = 0; i < in.length; i++) {
	    		 Double b = in[i];
	    		 out[i] = (b == null ? valueForNull : b);
	    	 }
	    	 return out;
	     }

	     /**
	      * Provides a consistent ordering over arrays. First compares by the
	      * first element. If that element is equal, the next element is
	      * considered, and so on. This is the array version of
	      * {@link edu.stanford.nlp.util.CollectionUtils#compareLists} 
	      * and uses the same logic when the arrays are of different lengths.
	      */
	     public static <T extends Comparable<T>> int compareArrays(T[] first, T[] second) {
	    	 List<T> firstAsList = Arrays.asList(first);
	    	 List<T> secondAsList = Arrays.asList(second);
	    	 return CollectionUtils.compareLists(firstAsList, secondAsList);
	     }
}
