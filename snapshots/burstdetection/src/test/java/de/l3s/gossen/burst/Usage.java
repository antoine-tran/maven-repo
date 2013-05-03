package de.l3s.gossen.burst;

import java.util.Collection;

import org.junit.Test;

import com.google.common.collect.Range;

import de.l3s.gossen.burstdetection.Burst;
import de.l3s.gossen.burstdetection.KleinbergBurstDetector;

import static de.l3s.gossen.burstdetection.KleinbergBurstDetector.DEFAULT_DENSITY_SCALING;
import static de.l3s.gossen.burstdetection.KleinbergBurstDetector.DEFAULT_GAMMA;
import static org.junit.Assert.assertEquals;

public class Usage {

    @Test
    public void testBurstDetector() {
        KleinbergBurstDetector burstDetector = new KleinbergBurstDetector(new int[] { 100, 100,
                100, 100, 100, 100, 100 }, 1, DEFAULT_GAMMA, DEFAULT_DENSITY_SCALING);
        Collection<Burst<String>> bursts = burstDetector.detectBursts("foo", new double[] { 1, 10,
                1, 1, 100, 10, 1 });
//        assertEquals(1, bursts.size());
//        Burst<String> burst = bursts.iterator().next();
//        assertEquals(4, burst.getStart());
//        assertEquals(6, burst.getEnd());
        
        System.out.println(bursts.size());
        for (Burst<String> burst : bursts) {
        	System.out.println("[" + burst.getStart() + ", " + burst.getEnd() + "]");
        }
    }
    
    @Test
    public void testDistinctBurstDetector() {
        KleinbergBurstDetector burstDetector = new KleinbergBurstDetector(new int[] { 13874, 13194,
        		13961, 13481, 14189, 15722, 15140, 16104, 15970, 14910, 15689, 15043, 16017, 18037,
        		16710, 15923, 15274, 14797, 14245, 15646, 14054, 13364, 13602 }, 3, 
        		DEFAULT_GAMMA, DEFAULT_DENSITY_SCALING);
        Collection<Burst<String>> bursts = burstDetector.detectBursts("foo", new double[] { 1, 4,
                1, 6, 5, 1, 10, 0, 6, 8, 18, 0, 0, 2, 8, 1, 0, 0, 0, 0, 1, 0, 0});
        
        System.out.println(bursts.size());
        for (Burst<String> burst : bursts) {
        	System.out.println("[" + burst.getStart() + ", " + burst.getEnd() + "]");
        }
        
        Collection<Range<Integer>> ranges = burstDetector.getDistinctBurstRanges("foo", 
        		new double[] { 1, 4, 1, 6, 5, 1, 10, 0, 6, 8, 18, 0, 0, 2, 8, 1, 0, 0, 0, 0, 1,
        		0, 0});
        
        for (Range<Integer> r : ranges) {
        	System.out.println(r);
        }
    }
    
    @Test
    public void testBinarySeries() {
        KleinbergBurstDetector burstDetector = new KleinbergBurstDetector(new int[] { 13874, 13194,
        		13961, 13481, 14189, 15722, 15140, 16104, 15970, 14910, 15689, 15043, 16017, 18037,
        		16710, 15923, 15274, 14797, 14245, 15646, 14054, 13364, 13602 }, 3, 
        		DEFAULT_GAMMA, DEFAULT_DENSITY_SCALING);
        
        CharSequence ranges = burstDetector.burstBinarySeries("foo0", 
        		new double[] { 1, 4, 1, 6, 5, 1, 10, 0, 6, 8, 18, 0, 0, 2, 8, 1, 0, 0, 0, 0, 1,
        		0, 0}, false);
        
        System.out.println(ranges);
    }

    @Test
    public void testDistinctDetector1() {
        KleinbergBurstDetector burstDetector = new KleinbergBurstDetector(new int[] { 13874, 13194,
        		13961, 13481, 14189, 15722, 15140, 16104, 15970, 14910, 15689, 15043, 16017, 18037,
        		16710, 15923, 15274, 14797, 14245, 15646, 14054, 13364, 13602 }, 3, 
        		DEFAULT_GAMMA, DEFAULT_DENSITY_SCALING);
        
        CharSequence ranges = burstDetector.burstBinarySeries("foo1", 
        		new double[] {0, 0, 0, 1, 2, 2, 0, 0, 7, 17, 19, 19, 0, 5, 4, 1, 34, 2, 0, 0, 0, 0, 0}, false);
        
        System.out.println(ranges);
    }
    
    @Test
    public void testDistinctDetector2() {
        KleinbergBurstDetector burstDetector = new KleinbergBurstDetector(new int[] { 13874, 13194,
        		13961, 13481, 14189, 15722, 15140, 16104, 15970, 14910, 15689, 15043, 16017, 18037,
        		16710, 15923, 15274, 14797, 14245, 15646, 14054, 13364, 13602 }, 3, 
        		DEFAULT_GAMMA, DEFAULT_DENSITY_SCALING);
        
        CharSequence ranges = burstDetector.burstBinarySeries("foo2", 
        		new double[] {9,8,2,8,0,3,0,0,5,9,0,1,0,5,2,0,8,0,2,1,0,1,3}, false);
        
        System.out.println(ranges);
    }
    
    @Test
    public void testDistinctDetector3() {
        KleinbergBurstDetector burstDetector = new KleinbergBurstDetector(new int[] { 13874, 13194,
        		13961, 13481, 14189, 15722, 15140, 16104, 15970, 14910, 15689, 15043, 16017, 18037,
        		16710, 15923, 15274, 14797, 14245, 15646, 14054, 13364, 13602 }, 3, 
        		DEFAULT_GAMMA, DEFAULT_DENSITY_SCALING);
        
        CharSequence ranges = burstDetector.burstBinarySeries("foo3", 
        		new double[] {1, 0, 15, 2, 4, 3, 1, 5, 11, 6, 1, 1, 7, 0, 1, 0, 2, 4, 0, 0, 0, 1, 0}, true);
        
        System.out.println(ranges);
    }
}
