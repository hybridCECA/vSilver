package utils;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Map;

public class Conversion {
    public static final double BTC_TO_SATOSHIS = 100E6;
    public static final Map<Character, Double> marketFactorMap = Map.of('k', 1E3, 'm', 1E6);
    public static enum HashPrefix {
        KILO, MEGA, GIGA, TERA, PETA
    }
    public static final int STRING_PRICE_TO_INT_PRICE = 10000;

    public static double unitProfitToDailyBTC(double unitProfitability, char hashPrefix) {
        return unitProfitability / BTC_TO_SATOSHIS * getMarketFactor(hashPrefix);
    }

    public static String getDisplayMarketFactor(char prefix) {
        return Character.toString(prefix).toUpperCase() + "H";
    }

    public static double getMarketFactor(char prefix) {
        return marketFactorMap.get(prefix);
    }

    public static int stringPriceToIntPrice(String price) {
        double doublePrice = Double.parseDouble(price);
        doublePrice *= STRING_PRICE_TO_INT_PRICE;
        return Math.toIntExact(Math.round(doublePrice));
    }

    public static String intPriceToStringPrice(int price) {
        double doublePrice = ((double) price) / STRING_PRICE_TO_INT_PRICE;
        DecimalFormat format = new DecimalFormat("#.####");

        return format.format(doublePrice);
    }
}
