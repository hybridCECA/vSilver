package marketevaluation;

import database.AllData;
import dataclasses.*;
import nicehash.NHApi;
import org.json.JSONException;
import services.MarketEvaluator;
import services.MaxProfitImpl;
import utils.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class MarketEvaluation {
    public static final String ORDER_ID = "order_id";
    private static final VLogger LOGGER = Logging.getLogger(MarketEvaluator.class);

    public static void workshop() throws SQLException {
        Map<CryptoInvestment, ProfitReport> map = getProfitReports();
        SortedMap<ProfitReport, CryptoInvestment> sortedMap = new TreeMap<>();
        for (Map.Entry<CryptoInvestment, ProfitReport> entry : map.entrySet()) {
            sortedMap.put(entry.getValue(), entry.getKey());
        }

        sortedMap.entrySet().forEach(System.out::println);
    }

    public static Map<CryptoInvestment, ProfitReport> getProfitReports() throws SQLException {
        int maxProfitAnalyzeMinutes = Config.getConfigInt(Consts.MAX_PROFIT_ANALYZE_MINUTES);
        int analyzeMinutes = Config.getConfigInt(Consts.MARKET_EVALUATION_ANALYZE_MINUTES);
        int dataWindow = maxProfitAnalyzeMinutes + analyzeMinutes;

        Map<CryptoInvestment, ProfitReport> profitReports = new HashMap<>();
        Integer medianLength = null;
        double entriesTolerance = Config.getConfigDouble(Consts.MARKET_EVALUATION_ENTRIES_TOLERANCE);
        do {
            LOGGER.info("Getting alldata batch");
            List<List<AllDataRecord>> allDataRecords = AllData.getNextData(dataWindow);

            if (medianLength == null) {
                medianLength = getMedianListSize(allDataRecords);
                LOGGER.info("Median length is " + medianLength);
            }

            int finalMedianLength = medianLength;
            allDataRecords.parallelStream().forEach(list -> {
                // Skip if size is outside tolerance
                if (list.size() < finalMedianLength * (1.0 - entriesTolerance)
                    || list.size() > finalMedianLength * (1.0 + entriesTolerance)) {
                    LOGGER.info("Skipping list with length " + list.size());
                    return;
                }

                AllDataRecord record = list.get(0);
                CryptoInvestment investment = new CryptoInvestment(record);

                try {
                    ProfitReport profitReport = getProfitReport(list, maxProfitAnalyzeMinutes);
                    profitReports.put(investment, profitReport);
                } catch (JSONException e) {
                    LOGGER.error(e);
                }
            });
        } while (AllData.hasData());

        return profitReports;
    }

    private static <T> int getMedianListSize(List<List<T>> lists) {
        List<Integer> sizesList = lists.stream()
                .mapToInt(List::size)
                .sorted()
                .boxed()
                .collect(Collectors.toList());

        return sizesList.get(sizesList.size() / 2);
    }

    private static ProfitReport getProfitReport(List<AllDataRecord> allDataRecords, int maxProfitAnalyzeMinutes) throws JSONException {
        NHApi nhApi = SingletonFactory.getInstance(NHApi.class);

        // Setup order bot
        AllDataRecord firstRecord = allDataRecords.get(0);
        double limit = allDataRecords.get(0).getFulfillSpeed();

        SimulatedOrderBot simulatedOrderBot = new SimulatedOrderBot(ORDER_ID, limit, firstRecord.getCoinName(), firstRecord.getAlgoName(), firstRecord.getMarketName());

        LocalDateTime startingTime = firstRecord.getTimestamp();
        LocalDateTime lastDownAdjustTime = startingTime.minusMinutes(15);
        int currentPrice = 0;
        double profitRatioTotal = 0;
        int numPaid = 0;
        double adjustedProfit = 0;
        double minProfitMargin = Config.getConfigDouble(Consts.ORDER_BOT_MIN_PROFIT_MARGIN);

        // Main simulation loop
        int startingIndex = getStartingIndex(allDataRecords, maxProfitAnalyzeMinutes);
        for (int i = startingIndex; i < allDataRecords.size(); i++) {
            AllDataRecord record = allDataRecords.get(i);

            simulatedOrderBot.setFulfillPrice(record.getFulfillPrice());
            simulatedOrderBot.setCoinRevenue(record.getCoinRevenue());

            LocalDateTime currentTime = record.getTimestamp();
            LocalDateTime analyzeStartTime = currentTime.minusMinutes(maxProfitAnalyzeMinutes);
            List<PriceRecord> priceRecords = getPriceRecordsList(allDataRecords, analyzeStartTime, currentTime);
            int maxProfitPrice = new MaxProfitImpl().getMaxProfitPrice(priceRecords, record.getCoinRevenue());
            simulatedOrderBot.setMaxProfit(maxProfitPrice);

            simulatedOrderBot.setCurrentPrice(currentPrice);

            int submitPrice = simulatedOrderBot.run();

            /*
            double hour = currentTime.getHour();
            hour += currentTime.getMinute() / 60.0;
            System.out.println("(" + hour + ", " + submitPrice + ")");

             */

            if (submitPrice < currentPrice) {
                if (currentTime.isBefore(lastDownAdjustTime.plusMinutes(10))) {
                    submitPrice = currentPrice;
                } else {
                    lastDownAdjustTime = currentTime;
                }
            }

            currentPrice = submitPrice;

            NicehashAlgorithmBuyInfo buyInfo = nhApi.getAlgoBuyInfo(record.getAlgoName());
            String speedText = buyInfo.getSpeedText();
            char hashPrefix = Conversions.speedTextToHashPrefix(speedText);
            double factor = Conversions.getMarketFactor(hashPrefix);

            if (currentPrice > record.getFulfillPrice()) {
                profitRatioTotal += (double) (record.getCoinRevenue() - currentPrice) / currentPrice;
                adjustedProfit += record.getCoinRevenue() * record.getNethash() / (record.getNethash() + limit * factor) - currentPrice * minProfitMargin;
                numPaid++;
            }
        }

        AllDataRecord lastRecord = allDataRecords.get(allDataRecords.size() - 1);

        int numRuns = allDataRecords.size() - startingIndex;
        double temporalProfitRatio = profitRatioTotal / numRuns;
        double totalProfitRatio = profitRatioTotal / numPaid;
        double activeRatio = (double) numPaid / numRuns;
        double marketSpeedRatio = lastRecord.getTotalSpeed() / lastRecord.getFulfillSpeed();
        double totalProfit = adjustedProfit * limit;

        NicehashAlgorithmBuyInfo buyInfo = nhApi.getAlgoBuyInfo(lastRecord.getAlgoName());
        String speedText = buyInfo.getSpeedText();
        char hashPrefix = Conversions.speedTextToHashPrefix(speedText);
        double factor = Conversions.getMarketFactor(hashPrefix);
        double coinSpeedRatio = lastRecord.getNethash() / lastRecord.getFulfillSpeed() / factor;

        return new ProfitReport(temporalProfitRatio, totalProfitRatio, activeRatio, marketSpeedRatio, coinSpeedRatio, totalProfit);
    }

    private static int getStartingIndex(List<AllDataRecord> allDataRecords, int maxProfitAnalyzeMinutes) {
        AllDataRecord firstRecord = allDataRecords.get(0);
        LocalDateTime startingTime = firstRecord.getTimestamp();
        for (int i = 0; i < allDataRecords.size(); i++) {
            AllDataRecord record = allDataRecords.get(i);
            LocalDateTime time = record.getTimestamp();
            if (time.isAfter(startingTime.plusMinutes(maxProfitAnalyzeMinutes))) {
                return i;
            }
        }

        throw new RuntimeException("Could not get starting index");
    }

    private static List<PriceRecord> getPriceRecordsList(List<AllDataRecord> allDataRecords, LocalDateTime startTime, LocalDateTime endTime) {
        // Fullfill price -> count
        SortedMap<Integer, Integer> counter = new TreeMap<>();
        for (AllDataRecord record : allDataRecords) {
            LocalDateTime time = record.getTimestamp();
            if (time.isAfter(endTime)) {
                break;
            }

            if (time.isAfter(startTime)) {
                int fulfillPrice = record.getFulfillPrice();

                if (counter.containsKey(fulfillPrice)) {
                    int currentCount = counter.get(fulfillPrice);
                    currentCount++;
                    counter.put(fulfillPrice, currentCount);
                } else {
                    counter.put(fulfillPrice, 1);
                }
            }
        }

        List<PriceRecord> list = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : counter.entrySet()) {
            int fulfillPrice = entry.getKey();
            int count = entry.getValue();

            PriceRecord priceRecord = new PriceRecord(fulfillPrice, count);
            list.add(priceRecord);
        }

        return list;
    }
}
