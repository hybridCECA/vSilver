package dataclasses;

import java.util.Objects;

public class ProfitReport implements Comparable<ProfitReport> {
    final double temporalProfits;
    final double totalProfits;
    final double activePercentage;
    final double totalSpeedRatio;
    double evaluationScore;

    public ProfitReport(double temporalProfits, double totalProfits, double activePercentage, double totalSpeedRatio) {
        this.temporalProfits = temporalProfits;
        this.totalProfits = totalProfits;
        this.activePercentage = activePercentage;
        this.totalSpeedRatio = totalSpeedRatio;

        calculateEvaluationScore();
    }

    private void calculateEvaluationScore() {
        evaluationScore = temporalProfits;
        evaluationScore *= powerScale(activePercentage, 4);
        evaluationScore *= powerScale(Math.log10(totalSpeedRatio) / 2, 3);
    }

    private double powerScale(double x, double power) {
        return Math.min(-Math.pow(-(x - 1), power) + 1, 1);
    }

    @Override
    public int compareTo(ProfitReport o) {
        return Double.compare(this.evaluationScore, o.evaluationScore);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProfitReport)) return false;
        ProfitReport that = (ProfitReport) o;
        return Double.compare(that.evaluationScore, evaluationScore) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(evaluationScore);
    }

    @Override
    public String toString() {
        return "ProfitReport{" +
                "temporalProfits=" + temporalProfits +
                ", totalProfits=" + totalProfits +
                ", activePercentage=" + activePercentage +
                ", totalSpeedRatio=" + totalSpeedRatio +
                ", evaluationScore=" + evaluationScore +
                '}';
    }
}
