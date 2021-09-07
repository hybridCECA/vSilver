package nicehash;

import coinsources.CoinSources;
import dataclasses.Coin;
import dataclasses.NicehashAlgorithmBuyInfo;
import dataclasses.NicehashOrder;
import dataclasses.TriplePair;
import org.json.JSONException;
import services.AdjustBot;
import services.MaxProfit;
import utils.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class OrderBot implements Comparable<OrderBot> {
    private final String orderId;
    private final String coinName;
    private final String algoName;
    private final String marketName;
    private final VLogger logger;
    private double limit;
    private NHApi nhApi;
    private CoinSources coinSources;
    private MaxProfit maxProfit;
    private PriceTools priceTools;

    public OrderBot(String orderId, double limit, String coinName, String algoName, String marketName) {
        this.orderId = orderId;
        this.limit = limit;
        this.coinName = coinName;
        this.algoName = algoName;
        this.marketName = marketName;

        nhApi = SingletonFactory.getInstance(NHApi.class);
        coinSources = SingletonFactory.getInstance(CoinSources.class);
        maxProfit = SingletonFactory.getInstance(MaxProfit.class);
        priceTools = SingletonFactory.getInstance(PriceTools.class);
        logger = Logging.getLogger(AdjustBot.class);
    }

    public void setNhApi(NHApi nhApi) {
        this.nhApi = nhApi;
    }

    public void setCoinSources(CoinSources coinSources) {
        this.coinSources = coinSources;
    }

    public void setMaxProfit(MaxProfit maxProfit) {
        this.maxProfit = maxProfit;
    }

    public void setPriceTools(PriceTools priceTools) {
        this.priceTools = priceTools;
    }

    public void run() {
        try {
            nhApi.invalidateOrderbookCache(algoName);

            List<NicehashOrder> orderbook = nhApi.getOrderbook(algoName, marketName);
            int price = priceTools.getSweepPrice(orderbook, limit, orderId);
            logger.info("Target price: " + price);

            int profitabilityBound = getProfitabilityBound();
            logger.info("Profitability bound: " + profitabilityBound);
            price = Math.min(price, profitabilityBound);

            if (!maxProfit.hasMaxProfit(this)) {
                logger.info("No max profit yet, waiting...");
                return;
            }
            int maxProfitabilityBound = maxProfit.getMaxProfit(this);
            logger.info("Max profitability bound: " + maxProfitabilityBound);
            price = Math.min(price, maxProfitabilityBound);

            int decraseBound = getPriceDecreaseBound();
            logger.info("Decrease bound: " + decraseBound);
            price = Math.max(price, decraseBound);

            NicehashAlgorithmBuyInfo algoBuyInfo = nhApi.getAlgoBuyInfo(algoName);
            char hashPrefix = Conversions.speedTextToHashPrefix(algoBuyInfo.getSpeedText());

            double submitLimit = limit;
            if (price > profitabilityBound) {
                submitLimit = algoBuyInfo.getMinLimit();
            }

            nhApi.updateOrder(orderId, price, Conversions.getDisplayMarketFactor(hashPrefix), Conversions.getMarketFactor(hashPrefix), submitLimit);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public TriplePair getTriplePair() {
        return new TriplePair(algoName, marketName, coinName);
    }

    public int getProfitabilityBound() throws IOException, JSONException {
        Coin coin = coinSources.getCoin(coinName, algoName);
        double minProfitMargin = Config.getConfigDouble(Consts.ORDER_BOT_MIN_PROFIT_MARGIN);

        double profitabilityBound = coin.getIntProfitability() / minProfitMargin;

        return Math.toIntExact(Math.round(profitabilityBound));
    }

    public String getCoinName() {
        return coinName;
    }

    public int getPriceDecreaseBound() throws JSONException {
        NicehashOrder order = nhApi.getOrder(orderId, algoName, marketName);
        int downStep = nhApi.getAlgoBuyInfo(algoName).getDownStep();

        return order.getPrice() + downStep;
    }

    public String getOrderId() {
        return orderId;
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(double limit) {
        this.limit = limit;
    }

    public String getMarketName() {
        return marketName;
    }

    public String getAlgoName() {
        return algoName;
    }

    public void disableLogging() {
        logger.disable();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderBot)) return false;
        OrderBot orderBot = (OrderBot) o;
        return orderId.equals(orderBot.orderId);
    }

    @Override
    public int compareTo(OrderBot o) {
        return this.orderId.compareTo(o.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }
}
