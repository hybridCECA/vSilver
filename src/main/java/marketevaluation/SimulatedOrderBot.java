package marketevaluation;

import nicehash.OrderBot;

public class SimulatedOrderBot {
    private final OrderBot orderBot;
    private final MockNHApi nhApi;
    private final MockCoinSources coinSources;
    private final MockMaxProfit maxProfit;
    private final MockPriceTools priceTools;

    public SimulatedOrderBot(String orderId, double limit, String coinName, String algoName, String marketName) {
        nhApi = new MockNHApi(limit);
        coinSources = new MockCoinSources();
        maxProfit = new MockMaxProfit();
        priceTools = new MockPriceTools();

        orderBot = new OrderBot(orderId, limit, coinName, algoName, marketName);
        orderBot.setNhApi(nhApi);
        orderBot.setCoinSources(coinSources);
        orderBot.setMaxProfit(maxProfit);
        orderBot.setPriceTools(priceTools);
        orderBot.disableLogging();
    }

    public void setFulfillPrice(int price) {
        priceTools.setFulfillPrice(price);
    }

    public void setCoinRevenue(int revenue) {
        coinSources.setCoinRevenue(revenue);
    }

    public void setMaxProfit(int price) {
        maxProfit.setMaxProfit(price);
    }

    public void setCurrentPrice(int price) {
        nhApi.setCurrentPrice(price);
    }

    public int run() {
        orderBot.run();
        return nhApi.getSubmitPrice();
    }
}
