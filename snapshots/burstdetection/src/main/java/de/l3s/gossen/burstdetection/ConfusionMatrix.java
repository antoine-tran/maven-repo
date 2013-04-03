package de.l3s.gossen.burstdetection;

public class ConfusionMatrix {
    private int truePositives;
    private int falsePositives;
    private int falseNegatives;
    private int trueNegatives;

    public ConfusionMatrix(int truePositives, int falsePositives,
            int falseNegatives, int trueNegatives) {
        this.truePositives = truePositives;
        this.falsePositives = falsePositives;
        this.falseNegatives = falseNegatives;
        this.trueNegatives = trueNegatives;
    }

    public ConfusionMatrix() {
        this(0, 0, 0, 0);
    }

    public int getTruePositive() {
        return truePositives;
    }

    public void addTruePositives(int n) {
        truePositives += n;
    }

    public int getTrueNegative() {
        return trueNegatives;
    }

    public void addTrueNegative(int n) {
        trueNegatives += n;
    }

    public int getFalsePositive() {
        return falsePositives;
    }

    public void addFalsePositives(int n) {
        falsePositives += n;
    }

    public int getFalseNegative() {
        return falseNegatives;
    }

    public void addFalseNegative(int n) {
        falseNegatives += n;
    }

    public void add(ConfusionMatrix other) {
        truePositives += other.truePositives;
        falsePositives += other.falsePositives;
        falseNegatives += other.falseNegatives;
        trueNegatives += other.trueNegatives;
    }

    public double getPrecision() {
        return getTruePositive()
                / ((double) getTruePositive() + getFalsePositive());
    }

    public double getRecall() {
        return getTruePositive()
                / ((double) getTruePositive() + getFalseNegative());
    }

    public double getF1() {
        double precision = getPrecision();
        double recall = getRecall();
        return 2 * ((precision * recall) / (precision + recall));
    }
}
