package services;

public class MaxProfitFactory {
    private static MaxProfit maxProfit = null;

    // Singleton
    public static MaxProfit getInstance() {
        if (maxProfit == null) {
            maxProfit = new MaxProfitImpl();
        }

        return maxProfit;
    }
}
