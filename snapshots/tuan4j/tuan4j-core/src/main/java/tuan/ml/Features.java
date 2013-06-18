/**
 * 
 */
package tuan.ml;

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
	public Object feature(int idex);
	
	/** update the feature value at a specific dim. The index here is the 
	 * global index, not the local one */
	public void update(int dim, double value);
		
	/** update a feature at a dimension and update the local index at the same time.
	 * Note that not all implementations support this operation */
	public void add(int dim, double value) throws UnsupportedOperationException;
	
	/** update the feature nominal value at a specific dim. The index here is the 
	 * global index, not the local one */
	public void update(int dim, Object value);
		
	/** update a nominal feature at a dimension and update the local index at the same time.
	 * Note that not all implementations support this operation */
	public void add(int dim, Object value) throws UnsupportedOperationException;
		
	@Override
	public int hashCode();
}
