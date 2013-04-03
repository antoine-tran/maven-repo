package de.l3s.gossen.burstdetection;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;

/**
 * Post-process the burst after applying Kleinberg's algorithm
 * @author tuan
 */
public abstract class GenericBurstDetector implements BurstDetector {

	@Override
	/** Convert a burst series to a binary series, where 1 means in the burst
	 *  period and 0 means otherwise */
	public <O> CharSequence burstBinarySeries(O object, double[] values) {
		Collection<Burst<O>> bursts = detectBursts(object, values);
		StringBuilder sb = new StringBuilder(values.length);
		if (bursts == null || bursts.isEmpty()) 
			return sb;
		Iterator<Burst<O>> iter = checkNotNull(bursts.iterator());
		Burst<O> b = iter.next();
		for (int i = 0, s = b.getStart(), e = b.getEnd(); i < values.length; i++) {
			if (i >= s && i <= e) {
				sb.append('1');
			}
			else {
				sb.append('0');
				if (i > e && iter.hasNext()) {
					b = iter.next();
					s = b.getStart();
					e = b.getEnd();
				}
			}
		}
		return sb;
	}
	
	@Override
	/** 
	 * Extract disjoint sets of ranges of bursts. This is a classical clique partitioning
	 * problem which is NP-complete, so we use Bron-Kerbosch algorithm which is a greedy
	 * strategy
	 */
	public <O> Collection<Range<Integer>> getDistinctBurstRanges(O object, double[] values) {
		Collection<Burst<O>> bursts = detectBursts(object, values);
		List<Range<Integer>> ranges = Lists.newArrayList();
		return null;
	}
}
