package tuan.math;

/**
 * 
 * @author tuan
 *
 */
public class Primes {

	public static boolean isPrime(int n) {
		if (n <= 2)
			return n == 2;
		else if (n % 2 == 0)
			return false;
		for (int i = 3, end = (int) Math.sqrt(n); i <= end; i += 2)			
			if (n % i == 0)
				return false;
		return true;
	}
	
	/** get the next prime number that is greater than n */
	public static int nextPrime(int n) {
		while (!isPrime(n))
			n++;
		return n;
	}
}
