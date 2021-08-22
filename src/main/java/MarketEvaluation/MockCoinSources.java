package MarketEvaluation;

import coinsources.CoinSources;
import database.Connection;
import dataclasses.Coin;
import dataclasses.NicehashAlgorithmBuyInfo;
import nicehash.NHApi;
import nicehash.NHApiFactory;
import org.json.JSONException;
import utils.Config;
import utils.Consts;
import utils.Conversions;

import java.io.IOException;
import java.util.List;

public class MockCoinSources implements CoinSources {
    private double profitability;
    private String algoName;

    public MockCoinSources(String algoName) {
        this.algoName = algoName;
    }

    public void setProfitability(double profitability) {
        this.profitability = profitability;
    }

    @Override
    public List<Coin> getCoinList() throws IOException, JSONException {
        return null;
    }

    @Override
    public Coin getCoin(String coinName) throws IOException, JSONException {
        NHApi nhApi = NHApiFactory.getInstance();
        NicehashAlgorithmBuyInfo buyInfo = nhApi.getAlgoBuyInfo(algoName);
        char hashPrefix = Conversions.speedTextToHashPrefix(buyInfo.getSpeedText());
        double factor = Conversions.getMarketFactor(hashPrefix);

        Coin coin = new Coin();
        coin.setName(coinName);
        coin.setAlgorithm(algoName);
        double unitProfitabilityFactor = 1.0 / 10000.0 * 100E6 / factor * Config.getConfigDouble(Consts.ORDER_BOT_MIN_PROFIT_MARGIN);
        coin.setUnitProfitability(profitability * unitProfitabilityFactor);

        return coin;
    }
}
