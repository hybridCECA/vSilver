package whattomine;

import nicehash.NicehashAlgorithm;

public class WhatToMineCoin extends NicehashAlgorithm {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
