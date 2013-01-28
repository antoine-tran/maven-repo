/**
 * ==================================
 * 
 * Copyright (c) 2010 Anh Tuan Tran
 *
 * URL: http://www.mpi-inf.mpg.de/~attran,
 *          http://www.l3s.de/~ttran
 *
 * Email: tranatuan24@gmail.com
 * ==================================
 * 
 * This source code is provided with AS IF - it does not guarantee the
 * or compatibilities with older or newer version of third-parties. In any
 * cases, if you have problems regarding using libraries delivered with
 * the project, feel free to write to the above email. Also, we would like
 * to get feedbacks from all of you
 */
package tuan.math;

import java.util.Random;

/**
 * This class provides a number of utility methods for
 * generating random numbers with a discrete distribution
 * 
 * @version 0.1
 * @author tuan 
 * @since 27.04.2011
 *
 */
public abstract class DiscreteRandom {
	
	protected Random generator;
	
	protected DiscreteRandom() {
		generator = new Random();
	}
	
	protected DiscreteRandom(long seed) {
		generator = new Random(seed);
	}
	
	/**
	 * All discrete distribution can be characterized by the 
	 * probabilities Pr(X = k), where k is an integer from a countable set.
	 * This method generates randomly an integer number based on a
	 * particular distribution. 
	 */
	public abstract int nextInt() throws ParameterNotDeclaredException;
	
	/**
	 * parameterize the distribution at the double scale
	 */
	public abstract void parameterize(double... par) throws 
		ParameterAlreadyDeclaredException, InvalidParameterException;
	
	/**
	 * parameterize the distribution at the float scale
	 */
	public abstract void parameterize(float... par) throws 
		ParameterAlreadyDeclaredException, InvalidParameterException;
}
