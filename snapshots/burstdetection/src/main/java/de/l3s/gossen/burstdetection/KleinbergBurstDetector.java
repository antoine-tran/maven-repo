package de.l3s.gossen.burstdetection;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import de.l3s.gossen.burstdetection.Burst.Direction;
import de.l3s.gossen.burstdetection.impl.Cell;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class KleinbergBurstDetector extends GenericBurstDetector {

	public static final double DEFAULT_DENSITY_SCALING = 1.5;
	public static final double DEFAULT_GAMMA = 1.0;
	public static final int DEFAULT_STATES = 3;

	private static <O> List<Burst<O>> findBursts(O object, int[] objectFrequencies,
			int[] documentFrequencies, int inputStates, double gamma, double densityScaling) {

		checkArgument(objectFrequencies.length == documentFrequencies.length,
				"#objectFrequencies (%s) != #documentFrequencies (%s)", objectFrequencies.length,
				documentFrequencies.length);
		checkNotNull(object);

		if ("".equals(object)) {
			return Collections.emptyList();
		}
		List<Burst<O>> bursts = Lists.newArrayList();

		int n = documentFrequencies.length;
		Cell[] cells = computeStates(objectFrequencies, documentFrequencies, inputStates, gamma,
				densityScaling);

		for (int i = 0; i < n; i++) {
			Cell currentCell = cells[i];
			for (int level = inputStates - 1; level >= 0; level--) {
				if (isValidBurstCandidate(currentCell, level, i)) {
					bursts.add(toBurst(object, currentCell, level, i, n, Direction.HIGHER));
				}
			}
		}
		return bursts;
	}

	static <O> Burst<O> toBurst(O object, Cell cell, int level, int i, int n, Direction direction) {
		int burstStart = i;

		int endCell = cell.getBreakpoint(level);

		double strength = cell.getTotalPower(level);

		return Burst.of(object, burstStart, endCell, level, strength, direction);
	}

	private static final int MIN_SLENGTH = 0;
	private static final double POWER_THRESH = 0;

	static boolean isValidBurstCandidate(Cell currentCell, int level, int binIndex) {
		return (currentCell.getCandidate(level)
				&& currentCell.getBreakpoint(level) - binIndex + 1 >= MIN_SLENGTH && currentCell
				.getTotalPower(level) >= POWER_THRESH);
	}

	/* Constant parameters of the burst detection algorithm */
	//    private static final int MIN_SLENGTH = 0;
	//    private static final double POWER_THRESH = 0;
	//    private static final double HUGE_N = 1000000.0;
	private static final double DTRANS = 1.0;

	/* Updated parameters */
	//private final double gamma = 1.0;         // Parameter that controls the ease with which the 
	// automaton can change states 'trans' in C code.
	//private final int inputStates = 1;        // The higher  bursting states.
	//  private final double densityScaling = 2;    // Density scaling which will affect the probability of 
	// the burst happens .

	private static Cell[] computeStates(int[] entry, int[] binBase, int burstStates, double gamma,
			double densityScaling) {

		double transCost = computeTransCost(entry.length, gamma);

		int levels = burstStates + 1;

		Cell[] cells = computeCosts(levels, entry, binBase, densityScaling);

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

	private static Cell[] computeCosts(int levels, int[] entry, int[] binBase, double densityScaling) {
		double expected = computeExpected(entry, binBase);

		double[] fRate = initializeFRate(expected, levels, densityScaling);

		Cell[] cells = new Cell[entry.length];

		for (int j = 0; j < cells.length; j++) {
			Cell cell = new Cell(levels);
			cells[j] = cell;
			for (int k = 0; k < levels; k++) {
				double cost = binomW(1.0 / fRate[k], entry[j], binBase[j]);
				//                logger.debug("Cost for cell {}: {}", j, cost);
				cell.setCost(k, cost);
			}
		}
		return cells;
	}

	static int computeTotals(Cell[] cells, double transCost, int levels) {

		Cell firstCell = cells[0];
		Cell lastCell = cells[cells.length - 1];

		// first bucket
		for (int k = 0; k < levels; k++) {
			firstCell.setTotal(k, firstCell.getCost(k) + transCost * (levels - 1 - k));
		}

		for (int j = 1; j < cells.length; j++) {
			Cell currentCell = cells[j];
			Cell previousCell = cells[j - 1];
			for (int k = 0; k < levels; k++) {
				double d = currentCell.getCost(k) + previousCell.getTotal(0);
				int q = 0;
				double tmpD;
				for (int m = 1; m < levels; m++) {
					/*
					 * The '< d' have changed to '<= d' due to we are interested
					 * on lower burst level that give the same cost. Ideally,
					 * there will not exist two levels that contains the same
					 * cost. It only happens if all costs is zero where there is
					 * not data in this bin.
					 */
					tmpD = currentCell.getCost(k) + previousCell.getTotal(m);
					if (m > k && (tmpD + transCost * (m - k)) <= d) {
						d = tmpD + transCost * (m - k);
						q = m;
					} else if (m <= k && tmpD <= d) {
						d = tmpD;
						q = m;
					}
				}
				currentCell.setTotal(k, d);
				currentCell.setPreviousPath(k, q);

			}
		}

		int q = 0;
		for (int k = 0; k < levels; k++) {
			double d = lastCell.getTotal(0);
			q = 0;
			for (int m = 1; m < levels; m++) {
				if (lastCell.getTotal(m) < d) {
					d = lastCell.getTotal(m);
					q = m;
				}
			}
		}
		return q;
	}

	static void computePathAndMark(Cell[] cells, int levels, int q) {

		Cell firstCell = cells[0];
		Cell lastCell = cells[cells.length - 1];

		lastCell.setPath(q);

		for (int j = cells.length - 2; j >= 0; j--) {
			Cell nextCell = cells[j + 1];
			Cell currentCell = cells[j];
			currentCell.setPath(nextCell.getPreviousPath(nextCell.getPath()));
		}

		for (int k = firstCell.getPath(); k < levels - 1; k++) {
			firstCell.setMark(k, true);
		}

		for (int j = 1; j < cells.length; j++) {
			Cell currentCell = cells[j];
			Cell previousCell = cells[j - 1];
			for (int k = currentCell.getPath(); k < previousCell.getPath(); k++) {
				currentCell.setMark(k, true);
			}
		}
	}

	private static double[] initializeFRate(double expected, int levels, double densityScaling) {
		double[] fRate = new double[levels];

		fRate[levels - 1] = expected;

		/*
		 * Change it to the same. Based on the paper. It didn't make sense to
		 * have first level ratio different from other level. This sound
		 * cheating.
		 */
		for (int j = levels - 2; j >= 0; j--) {
			fRate[j] = fRate[j + 1] / densityScaling;
		}
		return fRate;
	}

	/**
	 * Compute the TODO what is transCost
	 * 
	 * @param n
	 *            number of buckets
	 * @param gamma
	 *            transition rate
	 * @return the transCost
	 */
	static double computeTransCost(int n, double gamma) {
		double transCost = gamma * Math.log(n + 1) - Math.log(DTRANS);

		if (transCost < 0.0) {
			transCost = 0.0;
		}
		return transCost;
	}

	private static double computeExpected(int[] entry, int[] binBase) {
		int binN = 0;
		int binK = 0;

		for (int i = 0; i < entry.length; i++) {
			binK += entry[i];
			binN += binBase[i];
		}

		if (binN == 0 || binK == 0) {
			throw new RuntimeException("A word bursted on is never used");
		}

		return (double) binN / (double) binK;
	}

	/** Compute the logarithm of choose(n,k) */
	private static double logChoose(int n, int k) {
		int index;
		double value = 0.0;

		for (index = n; index > n - k; --index) {
			value += Math.log(index);
		}

		for (index = 1; index <= k; ++index) {
			value -= Math.log(index);
		}
		return value;
	}

	private static double binomW(double probability, int k, int n) {
		if (probability >= 1.0) {
			throw new IllegalArgumentException("probability >= 1.0, got " + probability);
		}
		return -1
				* (logChoose(n, k) + k * Math.log(probability) + (n - k)
						* Math.log(1.0 - probability));
	}

	private final int[] documentFrequencies;
	private final int inputStates;
	private final double gamma;
	private final double densityScaling;

	public KleinbergBurstDetector(int[] documentFrequencies, int inputStates, double gamma,
			double densityScaling) {
		this.documentFrequencies = documentFrequencies;
		this.inputStates = inputStates;
		this.gamma = gamma;
		this.densityScaling = densityScaling;
	}

	public KleinbergBurstDetector(int[] documentFrequencies) {
		this(documentFrequencies, DEFAULT_STATES, DEFAULT_GAMMA, DEFAULT_DENSITY_SCALING);
	}

	@Override
	public <O> Collection<Burst<O>> detectBursts(O object, double[] values) {
		int[] intValues = new int[values.length];
		for (int i = 0; i < values.length; i++) {
			intValues[i] = (int) values[i];
		}
		return findBursts(object, intValues, documentFrequencies, inputStates, gamma,
				densityScaling);
	}
}
