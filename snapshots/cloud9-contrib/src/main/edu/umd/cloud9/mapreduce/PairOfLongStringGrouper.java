package edu.umd.cloud9.mapreduce;

import edu.umd.cloud9.io.pair.PairOfLongString;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

/**
 * Created by tuan on 21/07/15.
 */
public class PairOfLongStringGrouper extends WritableComparator {

    protected PairOfLongStringGrouper() {
        super(PairOfLongString.class, true);
    }

    @Override
    public int compare(WritableComparable a, WritableComparable b) {
        PairOfLongString k1 = (PairOfLongString) a;
        PairOfLongString k2 = (PairOfLongString) b;

        return Long.compare(k1.getKey(), k2.getKey());
    }
}
