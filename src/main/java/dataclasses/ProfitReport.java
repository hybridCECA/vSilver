package dataclasses;

import java.util.Objects;

public class ProfitReport implements Comparable<ProfitReport> {
    final double temporalProfitRatio;
    final double totalProfitRatio;
    final double activeRatio;
    final double marketSpeedRatio;
    final double coinSpeedRatio;
    final double totalProfit;
    double evaluationScore;

    public ProfitReport(double temporalProfitRatio, double totalProfitRatio, double activeRatio, double marketSpeedRatio, double coinSpeedRatio, double totalProfit) {
        this.temporalProfitRatio = temporalProfitRatio;
        this.totalProfitRatio = totalProfitRatio;
        this.activeRatio = activeRatio;
        this.marketSpeedRatio = marketSpeedRatio;
        this.coinSpeedRatio = coinSpeedRatio;
        this.totalProfit = totalProfit;

        calculateEvaluationScore();
    }

    public double getEvaluationScore() {
        return evaluationScore;
    }

    private void calculateEvaluationScore() {
        //evaluationScore = temporalProfitRatio;
        evaluationScore = totalProfit;
        evaluationScore *= powerScale(activeRatio, 4);
        evaluationScore *= powerScale(Math.log10(marketSpeedRatio) / 2, 3);
        //evaluationScore *= powerScale(Math.log10(coinSpeedRatio) / 2, 3);
    }

    private double powerScale(double x, double power) {
        return Math.max(Math.min(-Math.pow(-(x - 1), power) + 1, 1), 0);
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
                "temporalProfitRatio=" + temporalProfitRatio +
                ", totalProfitRatio=" + totalProfitRatio +
                ", activeRatio=" + activeRatio +
                ", marketSpeedRatio=" + marketSpeedRatio +
                ", coinSpeedRatio=" + coinSpeedRatio +
                ", totalProfit=" + totalProfit +
                ", evaluationScore=" + evaluationScore +
                '}';
    }
}
