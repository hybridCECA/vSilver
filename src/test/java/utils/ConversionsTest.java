package utils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConversionsTest {

    @org.junit.jupiter.api.Test
    void testStringPriceToIntPrice() {
        Map<String, Integer> solutions = Map.of(
                "0.004", 40,
                "0.0005", 5,
                "-0.003", -30,
                "0.1", 1000,
                "1.1", 11000,
                "0.00005", 1,
                "1E-3", 10
        );

        for (String input : solutions.keySet()) {
            int answer = solutions.get(input);
            int intPrice = Conversions.stringPriceToIntPrice(input);
            assertEquals(answer, intPrice);
        }
    }

    @org.junit.jupiter.api.Test
    void testIntPriceToStringPrice() {
        Map<Integer, String> solutions = Map.of(
                40, "0.004",
                5, "0.0005",
                -30, "-0.003",
                1000, "0.1",
                11000, "1.1",
                1, "0.0001",
                10, "0.001"
        );

        for (int input : solutions.keySet()) {
            String answer = solutions.get(input);
            String intPrice = Conversions.intPriceToStringPrice(input);
            assertEquals(answer, intPrice);
        }
    }

    @org.junit.jupiter.api.Test
    void testDoublePriceToStringPrice() {
        Map<Double, String> solutions = Map.of(
                0.001, "0.001",
                0.005, "0.005",
                0.01, "0.01",
                0.5, "0.5",
                1.5, "1.5"
        );

        for (double input : solutions.keySet()) {
            String answer = solutions.get(input);
            String intPrice = Conversions.doublePriceToStringPrice(input);
            assertEquals(answer, intPrice);
        }
    }
}