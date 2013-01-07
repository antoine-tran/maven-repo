package tuan.ml.data;

public class FeaturesFactory {

	public static Features newInstance(double... vals) {
		return new ArrayFeatures(vals);
	}
}
