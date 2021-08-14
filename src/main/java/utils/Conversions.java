package utils;

import java.text.DecimalFormat;
import java.util.Map;

public class Conversions {
    public static final double BTC_TO_SATOSHIS = 100E6;
    public static final Map<Character, Double> marketFactorMap = Map.of('k', 1E3, 'm', 1E6, 'g', 1E9, 't', 1e12, 'p', 1e15);
    public static final int STRING_PRICE_TO_INT_PRICE = 10000;

    public static char speedTextToHashPrefix(String speedText) {
        return speedText.toLowerCase().charAt(0);
    }

    public static double unitProfitToDoublePrice(double unitProfitability, char hashPrefix) {
        return unitProfitability / BTC_TO_SATOSHIS * getMarketFactor(hashPrefix);
    }

    public static int unitProfitToIntPrice(double unitProfitability, char hashPrefix) {
        double dailyBtc = unitProfitToDoublePrice(unitProfitability, hashPrefix);
        return stringPriceToIntPrice(Double.toString(dailyBtc));
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

    public static String doublePriceToStringPrice(double price) {
        DecimalFormat format = new DecimalFormat("#.####");

        return format.format(price);
    }

    public static String intPriceToStringPrice(int price) {
        double doublePrice = ((double) price) / STRING_PRICE_TO_INT_PRICE;
        return doublePriceToStringPrice(doublePrice);
    }
}
