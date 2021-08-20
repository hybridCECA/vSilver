package dataclasses;

import nicehash.NHApi;
import org.json.JSONException;
import utils.Conversions;

public class NicehashAlgorithm {
    private String algorithm;
    private double unitProfitability;

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public double getUnitProfitability() {
        return unitProfitability;
    }

    public void setUnitProfitability(double unitProfitability) {
        this.unitProfitability = unitProfitability;
    }

    public int getIntProfitability() throws JSONException {
        return Conversions.unitProfitToIntPrice(unitProfitability, getHashPrefix());
    }

    public double getDoubleProfitability() throws JSONException {
        return Conversions.unitProfitToDoublePrice(unitProfitability, getHashPrefix());
    }

    private char getHashPrefix() throws JSONException {
        NicehashAlgorithmBuyInfo algoBuyInfo = NHApi.getAlgoBuyInfo(algorithm);
        return Conversions.speedTextToHashPrefix(algoBuyInfo.getSpeedText());
    }

    @Override
    public String toString() {
        return "NicehashAlgorithm{" +
                "algorithm='" + algorithm + '\'' +
                ", profitability=" + unitProfitability +
                '}';
    }
}
