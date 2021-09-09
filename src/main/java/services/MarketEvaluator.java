package services;

import dataclasses.CryptoInvestment;
import dataclasses.ProfitReport;
import dataclasses.TriplePair;
import marketevaluation.MarketEvaluation;
import utils.*;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class MarketEvaluator implements vService {
    private static Map<CryptoInvestment, ProfitReport> profitReportMap = null;
    private static final VLogger LOGGER = Logging.getLogger(MarketEvaluator.class);

    @Override
    public int getRunPeriodSeconds() {
        return Config.getConfigInt(Consts.MARKET_EVALUATOR_PERIOD_SECONDS);
    }

    @Override
    public void run() {
        try {
            LOGGER.info("Refreshing profit reports");
            profitReportMap = MarketEvaluation.getProfitReports();
            LOGGER.info("Done refreshing profit reports");
        } catch (SQLException e) {
            LOGGER.error(e);
        }
    }

    public static boolean hasProfitReport(CryptoInvestment investment) {
        if (profitReportMap == null) {
            return false;
        }

        if (profitReportMap.containsKey(investment)) {
            return true;
        }

        getPairs();
        TriplePair searchFor = new TriplePair(investment);
        Set<TriplePair> triplePairs = getPairs();

        return triplePairs.contains(searchFor);
    }

    private static Set<TriplePair> getPairs() {
        return profitReportMap
                .keySet()
                .stream()
                .map(TriplePair::new)
                .collect(Collectors.toSet());
    }

    public static ProfitReport getProfitReport(CryptoInvestment investment) {
        if (!hasProfitReport(investment)) {
            throw new RuntimeException("Profit report is not available yet");
        }

        if (profitReportMap.containsKey(investment)) {
            return profitReportMap.get(investment);
        }

        TriplePair searchFor = new TriplePair(investment);
        double searchLimit = investment.getFulfillSpeed();

        // Get profit report with min diff in limit
        // Diff -> Profit report
        SortedMap<Double, ProfitReport> reportsByLimitDiff = Collections.synchronizedSortedMap(new TreeMap<>());
        profitReportMap.entrySet()
                .stream()
                .filter(entry -> new TriplePair(entry.getKey()).equals(searchFor))
                .forEach(entry -> {
                    double limit = entry.getKey().getFulfillSpeed();
                    double diff = Math.abs(limit - searchLimit);
                    reportsByLimitDiff.put(diff, entry.getValue());
                });

        double minDiff = reportsByLimitDiff.firstKey();
        return reportsByLimitDiff.get(minDiff);
    }
}
