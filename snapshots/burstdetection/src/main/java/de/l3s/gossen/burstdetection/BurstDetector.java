package de.l3s.gossen.burstdetection;

import java.util.Collection;

import com.google.common.collect.Range;

public interface BurstDetector {
    public <O> Collection<Burst<O>> detectBursts(O object, double[] values);
    public <O> Collection<Range<Integer>> getDistinctBurstRanges(O object, double[] values);
    public <O> CharSequence burstBinarySeries(O object, double[] values, boolean strict);
}
