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

/**
 * This class factories all implemented distributions
 * in the package tuan.math
 * 
 * @author tuan
 * @version 1.0
 * @since 27.04.2012
 *
 */
public class Distributions {

	/** the distribution defined by a bounded array of magnitudes */
	public static final int BOUNDED_DISCRETE_DISTRIBUTION = 1;
	
	public static DiscreteRandom discreteDistribution(int type) {
		switch (type) {
		case BOUNDED_DISCRETE_DISTRIBUTION:	
			return new BoundedDiscreteRandom();
		default:
			return null;
		}
	}
	public static DiscreteRandom discreteDistribution(int type, long seed) {
		switch (type) {
		case BOUNDED_DISCRETE_DISTRIBUTION:	
			return new BoundedDiscreteRandom(seed);
		default:
			return null;
		}
	}
}
