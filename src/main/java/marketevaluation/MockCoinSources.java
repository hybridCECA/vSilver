package marketevaluation;

import coinsources.CoinSources;
import dataclasses.Coin;
import dataclasses.NicehashAlgorithmBuyInfo;
import nicehash.NHApi;
import org.json.JSONException;
import utils.Config;
import utils.Consts;
import utils.Conversions;
import utils.SingletonFactory;

import java.util.List;

public class MockCoinSources implements CoinSources {
    private int coinRevenue;

    public void setCoinRevenue(int coinRevenue) {
        this.coinRevenue = coinRevenue;
    }

    @Override
    public List<Coin> getCoinList() {
        return null;
    }

    @Override
    public Coin getCoin(String coinName, String algoName) throws JSONException {
        NHApi nhApi = SingletonFactory.getInstance(NHApi.class);
        NicehashAlgorithmBuyInfo buyInfo = nhApi.getAlgoBuyInfo(algoName);
        char hashPrefix = Conversions.speedTextToHashPrefix(buyInfo.getSpeedText());
        double unitProfit = Conversions.intPriceToUnitProfit(coinRevenue, hashPrefix);

        Coin coin = new Coin();
        coin.setName(coinName);
        coin.setAlgorithm(algoName);

        double minProfitMargin = Config.getConfigDouble(Consts.ORDER_BOT_MIN_PROFIT_MARGIN);
        coin.setUnitProfitability(unitProfit * minProfitMargin);

        return coin;
    }
}
