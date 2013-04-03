package de.l3s.gossen.burst;

import java.util.Collection;

import org.junit.Test;

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

}
