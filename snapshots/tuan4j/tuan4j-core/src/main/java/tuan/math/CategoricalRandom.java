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
 * Generate a random number based on a categorical distribution,
 * i.e. a distribution defined by a limited number of magnitudes
 * @author tuan
 * @version 0.1
 * 
 */
public class CategoricalRandom extends DiscreteRandom {

	private double[] d;
	private float[] f;

	public CategoricalRandom() {
		super();
	}

	public CategoricalRandom(long seed) {
		super(seed);
	}

	@Override
	public int nextInt() throws ParameterNotDeclaredException {
		int i;
		if (d != null) {
			if (d.length == 0) throw new ParameterNotDeclaredException();
			double random = generator.nextDouble();
			double sum = 0.0d;
			for(i = 0; i < d.length; i++) {		
				sum += d[i];
				if (sum >= random) break;
			}
		}
		else if (f != null) {
			if (f.length == 0) throw new ParameterNotDeclaredException();
			float random = generator.nextFloat();
			float sum = 0.0f;
			for (i = 0; i < f.length; i++) {
				sum += f[i];
				if (sum >= random) break;
			}
		}
		else throw new ParameterNotDeclaredException();
		return 0;
	}

	/** 
	 * fully declare a bounded discrete random with the probabilities 
	 * of its possible outcomes at the double precision scale (indexed 
	 * from 1 to k). IMPORTANT NOTE: The performance of the genration 
	 * is highly sensitive to the order of the probabilities. It is 
	 * fastest when the probabilities are sorted in descendant order */
	@Override
	public void parameterize(double... par) 
			throws ParameterAlreadyDeclaredException, InvalidParameterException{
		if (f != null && f.length > 0)
			throw new ParameterAlreadyDeclaredException();
		if (par == null || par.length == 0)
			throw new InvalidParameterException();
		this.d = par;
	}

	/** 
	 * fully declare a bounded discrete random with the probabilities 
	 * of its possible outcomes at the float precision scale (indexed from
	 *  1 to k). IMPORTANT NOTE: The performance of the genration is highly
	 *  sensitive to the order of the probabilities. It is fastest when the 
	 * probabilities are sorted in descendant order */
	@Override
	public void parameterize(float... par) 
			throws ParameterAlreadyDeclaredException, InvalidParameterException{
		if (d != null && d.length > 0)
			throw new ParameterAlreadyDeclaredException();
		if (par == null || par.length == 0)
			throw new InvalidParameterException();
		this.f = par;
	}
}
