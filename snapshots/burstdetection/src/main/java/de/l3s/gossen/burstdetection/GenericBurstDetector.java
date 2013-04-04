package de.l3s.gossen.burstdetection;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;

import tuan.collections.IntArrayList;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

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
		if (bursts == null || bursts.isEmpty()) {
			for (int i = 0; i < values.length; i++) {
				sb.append('0');
			}
		}
		else {
			RangeSet<Integer> res = internalFindDisjointRanges(bursts);
			for (int i = 0; i < values.length; i++) {
				if (res.rangeContaining(i) != null) {
					sb.append('1');
				}
				else sb.append('0');
			}
		}
		return sb;
	}
	
	@Override
	/** 
	 * Extract disjoint sets of ranges of bursts. This can be casted to the maximal clique
	 * partitioning problem which is NP-complete, so we use a greedy algorithm here
	 */
	public <O> Collection<Range<Integer>> getDistinctBurstRanges(O object, double[] values) {
		Collection<Burst<O>> bursts = detectBursts(object, values);
		RangeSet<Integer> res = internalFindDisjointRanges(bursts);
		return res.asRanges();
	}
	
	private <O> RangeSet<Integer> internalFindDisjointRanges(Collection<Burst<O>> bursts) {
		int n = bursts.size();
		int cliqueSize = n*(n+1)/2;
		RangeSet<Integer> res = TreeRangeSet.create();
		
		// We use 3 arrays to store info about ranges, the first and second arrays are used
		// for the range end points, and the third array is for the state of the ranges: 
		// 0 - overlap unchecked, 1 - overlap checked
		IntArrayList starts = new IntArrayList(cliqueSize);
		IntArrayList ends = new IntArrayList(cliqueSize);
		BitSet flags = new BitSet(cliqueSize);
		
		boolean stop = (n == 0);
		
		// beginning index of the next round
		int lst = 0;
		
		// feed the range with original bursts
		for (Burst<O> b : bursts) {
			starts.add(b.getStart());
			ends.add(b.getEnd());
		}
		while (!stop) {	
			
			// when no more intersections found, quit
			stop = true;
			
			// mark the end of this round
			int k = starts.size();
			for (int i = lst; i < k; i++) {
				for (int j = lst; j < i; j++) {
					if (starts.get(j) > ends.get(i) || starts.get(i) > ends.get(j))
						continue;
					else if (starts.get(i) != starts.get(j) || ends.get(i) != ends.get(j)) {
						stop = false;
						flags.set(j);
						flags.set(i);
						starts.add(Math.max(starts.get(j), starts.get(i)));
						ends.add(Math.min(ends.get(j), ends.get(i)));
					}
				}
			}
			if (!stop) {
				for (int i = flags.nextClearBit(lst); i < k; ) {
					res.add(Range.closedOpen(starts.get(i), ends.get(i)));
					i = flags.nextClearBit(i);
				}
				lst = k;
			}
		}
		
		// output remaining bursts
		for (int i = lst; i < starts.size(); i++) {
			res.add(Range.closedOpen(starts.get(i), ends.get(i)));
		}
		return res;
	}
}
