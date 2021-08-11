package dataclasses;

public class WhatToMineCoin extends NicehashAlgorithm {
    private String name;
    private double exchangeRate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    @Override
    public String toString() {
        return "WhatToMineCoin{" +
                "name='" + name + '\'' +
                ", algorithm='" + getAlgorithm() + '\'' +
                ", profitability=" + getProfitability() +
                '}';
    }
}
