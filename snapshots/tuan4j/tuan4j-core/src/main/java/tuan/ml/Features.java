/**
 * 
 */
package tuan.ml;

import javax.annotation.Nullable;

/**
 * This contains features of a document in all dimension
 * @author tuan
 *
 */
public interface Features {
	
	/** the "internal" dimension size (i.e. the number of positive features) */
	public int size();

	/** get the dimension names at a specific position */
	public String dimension(int index);

	/** normally in high-dimension computing, dimensions are compressed to
	 * contiguous integers. This method returns the mapping from one
	 * dimension at a specific position to its global integer values in the 
	 * dimension index */
	int dimensionValue(int index);
	
	/** get feature quantified values of a document at a specific position */
	public double featureValue(int index);
	
	/** get feature values of a document at a position. Note that not
	 * all implementations support this operation */
	public @Nullable Object feature(int idex);
	
	@Override
	public int hashCode();
}
