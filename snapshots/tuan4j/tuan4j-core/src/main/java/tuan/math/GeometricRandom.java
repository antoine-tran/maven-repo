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
 * A random number generator that is based on the geometric
 * distribution, i.e. the distribution defined by the
 * probability Pr(X = k) = c(1-c)^k, k = 0, 1, 2, ..... where
 * c is a constant in [0,1]
 * @author tuan
 * @version 0.1
 * @since 27.04.2012
 *
 */
public class GeometricRandom extends DiscreteRandom {

	private double dc = -1d;
	private float fc = -1f;
		
	public GeometricRandom() {
		super();
	}
	
	public GeometricRandom(long seed) {
		super(seed);
	}
	
	@Override
	public int nextInt() throws ParameterNotDeclaredException {
		int i = 0;
		if (dc >= 0d) {
			double random = generator.nextDouble();
			double sum = 0.0d;
			while (true) {
				sum += dc * Math.pow(1 - dc, i);
				if (sum >= random) break;
			}
		}
		else if (fc >= 0f) {
			float random = generator.nextFloat();
			float sum = 0.0f;
			while (true) {
				sum += fc * ((float)Math.pow(1 - fc, i));
				if (sum >= random) break;
			}
		}
		else throw new ParameterNotDeclaredException();
		return i;
	}

	/**
	 * specify the parameter c of the distribution at the double precision 
	 * scale
	 */
	@Override
	public void parameterize(double... par)
			throws ParameterAlreadyDeclaredException, InvalidParameterException{
		if (fc >= 0f) 
			throw new ParameterAlreadyDeclaredException();
		else if (par == null || par[0] < 0d) 
			throw new InvalidParameterException();
		dc = par[0];
	}

	/**
	 * specify the parameter c of the distribution at the float precision 
	 * scale
	 */
	@Override
	public void parameterize(float... par)
			throws ParameterAlreadyDeclaredException, InvalidParameterException{
		if (dc >= 0d)
			throw new ParameterAlreadyDeclaredException();
		else if (par == null || par[0] < 0f)
			throw new InvalidParameterException();
		fc = par[0];
	}

}
