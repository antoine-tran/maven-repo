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
 * generate a random number based on a bounded discrete distribution,
 * i.e. a distribution defined by a limited number of magnitudes
 * @author tuan
 * @version 0.1
 * 
 */
public class BoundedDiscreteRandom extends DiscreteRandom {

	private double[] d;
	private float[] f;

	public BoundedDiscreteRandom() {
		super();
	}

	public BoundedDiscreteRandom(long seed) {
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
				if (sum + d[i] >= random) {
					break;
				}
				sum += d[i];
			}
		}
		else if (f != null) {
			if (f.length == 0) throw new ParameterNotDeclaredException();
			float random = generator.nextFloat();
			float sum = 0.0f;
			for (i = 0; i < f.length; i++) {
				if (sum + f[i] >= random) {
					break;
				}
				sum += f[i];
			}
		}
		else throw new ParameterNotDeclaredException();
		return 0;
	}

	@Override
	public void parameterize(double... par) 
			throws ParameterAlreadyDeclaredException {
		if (f != null && f.length > 0)
			throw new ParameterAlreadyDeclaredException();
		this.d = par;
	}

	@Override
	public void parameterize(float... par) 
			throws ParameterAlreadyDeclaredException {
		if (d != null && d.length > 0)
			throw new ParameterAlreadyDeclaredException();
		this.f = par;
	}
}
