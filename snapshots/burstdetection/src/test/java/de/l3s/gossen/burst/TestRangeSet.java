package de.l3s.gossen.burst;

import org.junit.Test;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

public class TestRangeSet {

	@Test
	public void testDisconnectedRanges() {
		RangeSet<Integer> rs = TreeRangeSet.create();
		rs.add(Range.closed(1, 10));
		rs.add(Range.closed(6, 17));
		rs.add(Range.closed(8,9));
		rs.add(Range.closed(15, 19));
		rs.add(Range.closed(22, 32));
		rs.add(Range.closed(27, 30));
		rs.add(Range.closed(34, 40));
		rs.add(Range.closed(35, 37));
		rs.add(Range.closed(43, 47));
		rs.add(Range.closedOpen(45, 47));
		
		for (Range r : rs.asRanges()) {
			System.out.println(r);
		}
	}
}
