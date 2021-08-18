package services;

import dataclasses.SXBalance;
import nicehash.MaxProfit;
import southxchange.SXApi;
import utils.Config;
import utils.Consts;

import java.util.List;

public class TransferBot extends vService {
    private static final String REFERENCE_CURRENCY = "BTC";

    @Override
    public int getRunPeriodSeconds() {
        return Config.getConfigInt(Consts.TRANSFER_BOT_PERIOD_SECONDS);
    }

    @Override
    public void run() {
        try {
            System.out.println("Transfer bot start");
            transfer();
            System.out.println("Transfer bot done");
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void transfer() throws Exception {
        List<SXBalance> balances = SXApi.listBalances();

        for (SXBalance balance : balances) {
            if (balance.getCurrency().equals(REFERENCE_CURRENCY)) {
                continue;
            }

            System.out.println("Selling " + balance.getAvailable() + " " + balance.getCurrency());
            SXApi.sell(balance.getCurrency(), REFERENCE_CURRENCY, balance.getAvailable());
        }
    }
}
