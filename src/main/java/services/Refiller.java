package services;

import database.Connection;
import dataclasses.CryptoInvestment;
import dataclasses.ProfitReport;
import nicehash.NHApi;
import nicehash.OrderBot;
import org.json.JSONException;
import utils.*;

import java.util.Map;
import java.util.Set;

public class Refiller implements vService {
    private final static VLogger LOGGER = Logging.getLogger(Refiller.class);

    @Override
    public int getRunPeriodSeconds() {
        return Config.getConfigInt(Consts.REFILLER_PERIOD_SECONDS);
    }

    @Override
    public void run() {
        try {
            refill();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void refill() throws JSONException {
        NHApi nhApi = SingletonFactory.getInstance(NHApi.class);
        Map<String, Double> remainingAmounts = nhApi.getOrderRemainingAmounts();

        double maxRemainingAmount = Config.getConfigDouble(Consts.REFILLER_MAX_REMAINING_AMOUNT);
        double minEvaluationRatio = Config.getConfigDouble(Consts.REFILLER_MIN_EVALUATION_RATIO);
        double refillAmount = Config.getConfigDouble(Consts.REFILLER_REFILL_AMOUNT);

        Set<OrderBot> orderBots = AdjustBot.getOrderBots();
        for (OrderBot bot : orderBots) {
            double remainingAmount = remainingAmounts.get(bot.getOrderId());
            if (remainingAmount > maxRemainingAmount) {
                // Not refilling yet
                continue;
            }

            CryptoInvestment investment = new CryptoInvestment(bot);
            if (!MarketEvaluator.hasProfitReport(investment)) {
                LOGGER.info("Due to no profit report, skipping the following " + investment);
                continue;
            }
            ProfitReport profitReport = MarketEvaluator.getProfitReport(investment);
            double currentEvaluationScore = profitReport.getEvaluationScore();
            double initialEvaluationScore = Connection.getOrderInitialEvaluationScore(bot.getOrderId());
            double evaluationRatio = currentEvaluationScore / initialEvaluationScore;
            if (evaluationRatio < minEvaluationRatio) {
                LOGGER.info("Due to low evaluation ratio, not refilling the following " + investment);
                continue;
            }

            double availableBTC = nhApi.getAvailableBTC();
            if (availableBTC < refillAmount) {
                LOGGER.info("Not refilling due to insufficient funds");
                continue;
            }

            LOGGER.info("Refilling the following: " + investment);
        }
    }
}
