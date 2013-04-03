package de.l3s.gossen.burstdetection.impl;

public class Cell {
	private final double[] costs;
	private final double[] totals;
	private final int[] previousPaths;
	private int path = 0;
	private int minRateClass = 0;
	private final boolean[] candidates;
	private final int[] endCandidates;
	private final boolean[] marks;
	private final int[] breakpoints;
	private final boolean[] subordinates;
	private final double[] powers;
	private final double[] totalPowers;
	
	public Cell(int levels) {
		costs = new double[levels];
		totals = new double[levels];
		powers = new double[levels];
		totalPowers = new double[levels];
		previousPaths = new int[levels];
		endCandidates = new int[levels];
		breakpoints = new int[levels];
		subordinates = new boolean[levels];
		candidates = new boolean[levels];
		marks = new boolean[levels];
	}
	
    public double getCost(int level) {
        return costs[level];
	}

    public double getTotal(int level) {
        return totals[level];
	}

    public int getPreviousPath(int level) {
        return previousPaths[level];
	}

	public int getPath() {
		return path;
	}

	public int getMinRateClass() {
		return minRateClass;
	}

    public boolean getCandidate(int level) {
        return candidates[level];
	}

    public int getEndCandidate(int level) {
        return endCandidates[level];
	}

    public boolean getMark(int level) {
        return marks[level];
	}

    public int getBreakpoint(int level) {
        return breakpoints[level];
	}

    public boolean getSubordinate(int level) {
        return subordinates[level];
	}

    public double getPower(int level) {
        return powers[level];
	}

    public double getTotalPower(int level) {
        return totalPowers[level];
	}

	public void setMinRateClass(int minRateClass) {
		this.minRateClass = minRateClass;
	}

	public void setPath(int path) {
		this.path = path;
	}

    public void setBreakpoint(int level, int val) {
        breakpoints[level] = val;
    }

    public void setCandidate(int level, boolean val) {
        candidates[level] = val;
    }

    public void setCost(int level, double val) {
        costs[level] = val;
    }

    public void setTotal(int level, double val) {
        totals[level] = val;
    }

    public void setPreviousPath(int level, int val) {
        previousPaths[level] = val;
    }

    public void setEndCandidate(int level, int val) {
        endCandidates[level] = val;
    }

    public void setSubordinate(int level, boolean val) {
        subordinates[level] = val;
    }

    public void setMark(int level, boolean val) {
        marks[level] = val;
    }

    public void addToPower(int level, double increment) {
        powers[level] += increment;

    }

    public void addToTotalPower(int level, double increment) {
        totalPowers[level] += increment;
    }
}
