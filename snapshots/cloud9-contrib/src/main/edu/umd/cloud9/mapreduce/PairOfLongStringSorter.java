package edu.umd.cloud9.mapreduce;

import edu.umd.cloud9.io.pair.PairOfLongString;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

/**
 * Created by tuan on 21/07/15.
 */
public class PairOfLongStringSorter extends WritableComparator {

    protected PairOfLongStringSorter() {
        super(PairOfLongString.class, true);
    }

    @Override
    public int compare(WritableComparable a, WritableComparable b) {
        PairOfLongString k1 = (PairOfLongString) a;
        PairOfLongString k2 = (PairOfLongString) b;

        int compare = Long.compare(k1.getKey(), k2.getKey());
        if (compare == 0) {
            return (k2.getValue().compareTo(k2.getValue()));
        } else return compare;
    }
}
