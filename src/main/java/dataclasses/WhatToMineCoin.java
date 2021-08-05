package dataclasses;

import dataclasses.NicehashAlgorithm;

public class WhatToMineCoin extends NicehashAlgorithm {
    private String name;
    private double exchangeRate;
    private double profitability24;

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

    public double getProfitability24() {
        return profitability24;
    }

    public void setProfitability24(double profitability24) {
        this.profitability24 = profitability24;
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
