package dataclasses;

public class CoinAlgoPair  {
    private WhatToMineCoin wtmCoin;
    private NicehashAlgorithm nhAlgo;

    public CoinAlgoPair(WhatToMineCoin wtmCoin, NicehashAlgorithm nhAlgo) {
        this.wtmCoin = wtmCoin;
        this.nhAlgo = nhAlgo;
    }

    public NicehashAlgorithm getNhAlgo() {
        return nhAlgo;
    }

    public WhatToMineCoin getWtmCoin() {
        return wtmCoin;
    }
}
