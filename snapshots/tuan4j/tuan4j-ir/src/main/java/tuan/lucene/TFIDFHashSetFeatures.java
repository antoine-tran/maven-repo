package tuan.lucene;

import gnu.trove.map.hash.TObjectIntHashMap;
import tuan.ir.core.Features;

class TFIDFHashSetFeatures implements Features {

	// contains term string values in the document. Each
	// term is mapped to an internal index in the feature vector
	private TObjectIntHashMap<String> terms;
	
	// this temporary array stores frequencies of terms in the document
	// It will be released to garbage collector after the features are
	// calculated. TODO: Might revisit the necessity of this array in
	// later versions of lucenes
	private byte[] freq;
	
	// this will be the data of the feature set
	private double[] tfidf;
	
	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String dimension(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int dimensionValue(int index) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double featureValue(int index) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	// This operation is not supported here
	public Object feature(int idex) {
		return null;
	}

}
