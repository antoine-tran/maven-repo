package de.l3s.gossen.burstdetection;

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;

public final class Bursts {

    private static final class ThresholdFilter<O> implements Predicate<Burst<O>> {
        private final double threshold;

        private ThresholdFilter(double threshold) {
            this.threshold = threshold;
        }

        @Override
        public boolean apply(@Nullable Burst<O> input) {
            return input != null && input.getStrength() > threshold;
        }
    }

    private static final Comparator<Burst<?>> DESCENDING_BY_STRENGTH = new Comparator<Burst<?>>() {
        @Override
        public int compare(Burst<?> o1, Burst<?> o2) {
            return -Double.compare(o1.getStrength(), o2.getStrength());
        }
    };

    private Bursts() {}

    public static <O> Collection<Burst<O>> strongerThan(Collection<Burst<O>> bursts,
            double threshold) {
        return Collections2.filter(bursts, new ThresholdFilter<O>(threshold));
    }

    public static <O> Collection<Burst<O>> topK(Collection<Burst<O>> bursts, int k) {
        if (bursts.size() <= k) {
            return bursts;
        }
        List<Burst<O>> burstsList = bursts instanceof List<?> 
                ? (List<Burst<O>>) bursts 
                : Lists.newArrayList(bursts);
        Collections.sort(burstsList, DESCENDING_BY_STRENGTH);
        return burstsList.subList(0, k);
    }

    public static <O> boolean hasOverlappingBursts(Collection<Burst<O>> bursts,
            Collection<Burst<O>> bursts1) {
        for (Burst<O> burst : bursts) {
            for (Burst<O> burst1 : bursts1) {
                if (!burst.getDuration().intersection(burst1.getDuration()).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static ConfusionMatrix evaluate(Collection<Burst<?>> bursts,
            Collection<Burst<?>> goldBursts) {
        int truePositives = 0;
        int falsePositives = 0;
        int falseNegatives = 0;
        BitSet foundBursts = new BitSet(goldBursts.size());
        List<Range<Integer>> goldBurstIntervals = Lists.newArrayListWithExpectedSize(goldBursts.size());
        for (Burst<?> burst : goldBursts) {
            goldBurstIntervals.add(burst.getDuration());
        }

        for (Burst<?> burst : bursts) {
            int idx = findBurstIndex(goldBurstIntervals, burst);
            if (idx < 0) {
                falsePositives++;
            } else {
                foundBursts.set(idx);
            }
        }

        for (int i = 0; i < goldBursts.size(); i++) {
            if (foundBursts.get(i)) {
                truePositives++;
            } else {
                falseNegatives++;
            }
        }

        return new ConfusionMatrix(truePositives, falsePositives, falseNegatives, 0);
    }

    private static int findBurstIndex(List<Range<Integer>> burstIntervals, Burst<?> burst) {
        Range<Integer> burstInterval = burst.getDuration();
        int pos = 0;
        for (Range<Integer> interval : burstIntervals) {
            if (!interval.intersection(burstInterval).isEmpty()) {
                return pos;
            }
            pos++;
        }
        return -1;
    }

}
