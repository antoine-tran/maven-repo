package tuan.ml;

public class DataLoader {

	public static Document createDocument(String key, String features, String delimeter) {
		String[] featureVals = features.split(delimeter);
		int n = featureVals.length;
		double[] vals = new double[n];
		for (int i = 0; i < n; i++) {
			vals[i] = Double.valueOf(featureVals[i]);
		}
		Features f = FeaturesFactory.newInstance(vals);
		return new Document(key, f, n);
	}
}
