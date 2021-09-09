package utils;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ConversionsTest {

    @Test
    public void speedTextToHashPrefix() {
        Map<String, Character> solutionMap = Map.of(
                "KH", 'k',
                "msol", 'm',
                "gh", 'g',
                "TSOL", 't',
                "P", 'p'
        );

        for (Map.Entry<String, Character> entry : solutionMap.entrySet()) {
            String input = entry.getKey();
            char output = Conversions.speedTextToHashPrefix(input);

            char solution = entry.getValue();

            assertEquals(solution, output);
        }
    }

    @Test
    public void tripleCyclicalTest() {
        List<Integer> testInts = List.of(0, 1, 5, 10, 13, 15, 20, 100, 1000);
        List<Character> hashPrefixes = List.of('k', 'g', 'm', 't', 'p');
        List<Double> testDoubles = List.of(0.001, 0.01, 0.005, 0.05);
        List<Double> testUnits = List.of(1.0, 10.0, 100.0, 1000.0, 5.0, 500.0, 150.0);
        final double delta = 1E-15;
        for (Character hashPrefix : hashPrefixes) {
            for (int intPrice : testInts) {
                double doublePrice1 = Conversions.intPriceToDoublePrice(intPrice);
                double unitProfit1 = Conversions.intPriceToUnitProfit(intPrice, hashPrefix);

                double doublePrice2 = Conversions.unitProfitToDoublePrice(unitProfit1, hashPrefix);
                double unitProfit2 = Conversions.doublePriceToUnitProfit(doublePrice1, hashPrefix);

                assertEquals(doublePrice1, doublePrice2, delta);
                assertEquals(unitProfit1, unitProfit2, delta);
            }

            for (double doublePrice : testDoubles) {
                int intPrice1 = Conversions.doublePriceToIntPrice(doublePrice);
                double unitPrice1 = Conversions.doublePriceToUnitProfit(doublePrice, hashPrefix);

                int intPrice2 = Conversions.unitProfitToIntPrice(unitPrice1, hashPrefix);
                double unitPrice2 = Conversions.intPriceToUnitProfit(intPrice1, hashPrefix);

                assertEquals(intPrice1, intPrice2);
                assertEquals(unitPrice1, unitPrice2, delta);
            }
        }

        char hashPrefix = 'm';
        for (double unitProfit : testUnits) {
            int intPrice1 = Conversions.unitProfitToIntPrice(unitProfit, hashPrefix);
            double doublePrice1 = Conversions.unitProfitToDoublePrice(unitProfit, hashPrefix);

            int intPrice2 = Conversions.doublePriceToIntPrice(doublePrice1);
            double doublePrice2 = Conversions.intPriceToDoublePrice(intPrice1);

            assertEquals(intPrice1, intPrice2);
            assertEquals(doublePrice1, doublePrice2, delta);
        }
    }

    @Test
    public void getDisplayMarketFactor() {
        Map<Character, String> solutionMap = Map.of(
                'k', "KH",
                'm', "MH",
                'G', "GH",
                'T', "TH",
                'p', "PH"
        );

        for (Map.Entry<Character, String> entry : solutionMap.entrySet()) {
            char input = entry.getKey();
            String output = Conversions.getDisplayMarketFactor(input);

            String solution = entry.getValue();

            assertEquals(solution, output);
        }
    }

    @Test
    public void stringIntCyclicalTest() {
        List<String> testStrings = List.of("0.0015", "0.0032", "0.01", "0.1");
        List<Integer> testInts = List.of(0, 1, 5, 10, 13, 15, 20, 100, 1000);

        for (String testString : testStrings) {
            int convert = Conversions.stringPriceToIntPrice(testString);
            String output = Conversions.intPriceToStringPrice(convert);

            assertEquals(testString, output);
        }

        for (int testInt : testInts) {
            String convert = Conversions.intPriceToStringPrice(testInt);
            int output = Conversions.stringPriceToIntPrice(convert);

            assertEquals(testInt, output);
        }
    }

    @Test
    public void doublePriceToStringPrice() {
        Map<Double, String> solutionMap = Map.of(
                0.001, "0.001",
                0.015, "0.015",
                0.1, "0.1",
                0.5, "0.5"
        );

        for (Map.Entry<Double, String> entry : solutionMap.entrySet()) {
            double input = entry.getKey();
            String output = Conversions.doublePriceToStringPrice(input);

            String solution = entry.getValue();

            assertEquals(solution, output);
        }
    }
}