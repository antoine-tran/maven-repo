package de.l3s.gossen.burstdetection;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import de.l3s.gossen.burstdetection.Burst.Direction;
import de.l3s.gossen.burstdetection.impl.Cell;

import static com.google.common.base.Preconditions.checkArgument;
import static de.l3s.gossen.burstdetection.KleinbergBurstDetector.*;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;

public class KleinbergDeviationBurstDetector extends GenericBurstDetector {

    private static final Logger logger = LoggerFactory.getLogger(KleinbergDeviationBurstDetector.class);
    private final int inputStates;
    private final double gamma;
    private final double densityScaling;

    public KleinbergDeviationBurstDetector(int inputStates, double gamma, double densityScaling) {
        this.inputStates = inputStates;
        this.gamma = gamma;
        this.densityScaling = densityScaling;
    }

    public KleinbergDeviationBurstDetector() {
        this(DEFAULT_STATES, DEFAULT_GAMMA, DEFAULT_DENSITY_SCALING);
    }

    @Override
    public <O> Collection<Burst<O>> detectBursts(O object, double[] values) {
        return findBursts(object, values, inputStates, gamma, densityScaling);
    }

    private static Cell[] computeCosts(int levels, double[] objectValues, double densityScaling) {
        double expected = computeExpected(objectValues);
        double standardDeviation = standardDeviation(objectValues, expected);

        // double[] fRate = initializeFRate(expected, levels, densityScaling);
        NormalDistribution[] distributions = createLevels(levels, expected, standardDeviation);
        double interval = .5 * standardDeviation;

        Cell[] cells = new Cell[objectValues.length];

        for (int j = 0; j < cells.length; j++) {
            Cell cell = new Cell(2 * levels + 1);
            cells[j] = cell;
            for (int k = -levels; k <= levels; k++) {
                double value = objectValues[j];
                double lowerBound = k != -levels ? value - interval : NEGATIVE_INFINITY;
                double upperBound = k != +levels ? value + interval : POSITIVE_INFINITY;
                double cost = distributions[k + levels].cumulativeProbability(lowerBound,
                        upperBound);
                logger.trace("Cost for cell {}: {}", j, cost);
                cell.setCost(k + levels, cost);
            }
        }
        return cells;
    }

    private static Cell[] computeStates(double[] objectValues, int burstStates, double gamma,
            double densityScaling) {

        double transCost = computeTransCost(objectValues.length, gamma);

        int levels = burstStates + 1;

        Cell[] cells = computeCosts(levels, objectValues, densityScaling);

        int q = computeTotals(cells, transCost, levels);
        computePathAndMark(cells, levels, q);

        int[] leftBarrier = new int[levels];

        for (int k = 0; k < levels; k++) {
            leftBarrier[k] = -1;
        }

        for (int j = 0; j < cells.length; j++) {
            Cell currentCell = cells[j];

            for (int k = 0; k < levels - 1; k++) {
                if (currentCell.getMark(k)) {
                    leftBarrier[k] = j;
                }
            }

            for (int k = 0; k < currentCell.getPath(); k++) {
                if (leftBarrier[k] >= 0) {
                    Cell barrierCell = cells[leftBarrier[k]];
                    barrierCell.setBreakpoint(k, j);
                    barrierCell.setCandidate(k, true);
                    currentCell.setEndCandidate(k, 1);
                    leftBarrier[k] = -1;
                }
            }

            for (int k = currentCell.getPath(); k < levels - 1; k++) {
                if (leftBarrier[k] >= 0) {
                    Cell barrierCell = cells[leftBarrier[k]];
                    barrierCell.addToPower(k, currentCell.getCost(k + 1) - currentCell.getCost(k));
                    barrierCell.addToTotalPower(k,
                            currentCell.getCost(levels - 1) - currentCell.getCost(k));
                }
            }
        }
        Cell lastCell = cells[cells.length - 1];
    
        for (int k = 0; k < levels - 1; k++) {
            if (leftBarrier[k] >= 0) {
                Cell barrierCell = cells[leftBarrier[k]];
                barrierCell.setBreakpoint(k, cells.length - 1);
                barrierCell.setCandidate(k, true);
                lastCell.setEndCandidate(k, 1);
                leftBarrier[k] = -1;
            }
        }

        for (int j = 0; j < cells.length - 1; j++) {
            Cell currentCell = cells[j];

            int p = -1;
            q = -1;
            for (int k = 0; k < levels - 1; k++) {
                if (currentCell.getCandidate(k)) {
                    p = k;
                    if (q < 0) {
                        q = k;
                    }
                }
            }
            if (p < 0) {
                continue;
            }
    
            currentCell.setMinRateClass(q);
            for (int k = 0; k < p; ++k) {
                if (currentCell.getCandidate(k)) {
                    /*
                     * This try to accumulate all level's weight into the lower
                     * burst level. This have created double standard of
                     * total_power value of lower level with the higher levels.
                     * Based on the paper, total_power (weight) is the rectangle
                     * area of the time period with the level.
                     * currentCell.totalPower[p] += currentCell.power[k];
                     */
                    currentCell.setSubordinate(k, true);
                }
            }
        }

        return cells;
    }

    private static <O> List<Burst<O>> findBursts(O object, double[] objectValues, int inputStates,
            double gamma, double densityScaling) {

        checkArgument(object != null && !object.equals(""));
        double expected = computeExpected(objectValues);
        double standardDeviation = standardDeviation(objectValues, expected);
        if (Math.abs(standardDeviation) < 0.01) {
            logger.debug("Standard deviation of expected values ({}) is too small",
                    standardDeviation);
            return Collections.emptyList();
        }

        List<Burst<O>> bursts = Lists.newArrayList();

        int n = objectValues.length;
        Cell[] cells = computeStates(objectValues, inputStates, gamma, densityScaling);

        for (int i = 0; i < n; i++) {
            Cell currentCell = cells[i];
            for (int level = inputStates - 1; level >= 0; level--) {
                if (isValidBurstCandidate(currentCell, level, i)) {
                    int actualLevel = level - inputStates;
                    Burst<O> burst = toBurst(object, currentCell, level, i, n,
                            actualLevel < 0 ? Direction.LOWER : Direction.HIGHER);
                    bursts.add(burst);
                }
            }
        }
        return bursts;
    }

    private static NormalDistribution[] createLevels(int levels, double mean,
            double standardDeviation) {
        NormalDistribution[] distributions = new NormalDistribution[2 * levels + 1];
        for (int i = -levels; i <= levels; i++) {
            double shiftedMean = mean + (i * standardDeviation);
            distributions[i + levels] = new NormalDistribution(shiftedMean, standardDeviation);
        }
        return distributions;
    }

    private static double standardDeviation(double[] objectValues, double expected) {
        double accumulator = 0;
        for (double value : objectValues) {
            accumulator += (value - expected) * (value - expected);
        }
        return Math.sqrt(accumulator / objectValues.length);
    }

    private static double computeExpected(double[] entry) {
        double binK = 0;

        for (double val : entry) {
            binK += val;
        }

        return binK / entry.length;
    }
}
