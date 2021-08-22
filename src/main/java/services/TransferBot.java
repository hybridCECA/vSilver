package services;

import dataclasses.SXBalance;
import southxchange.SXApi;
import utils.Config;
import utils.Consts;
import utils.Conversions;
import utils.Logging;

import java.util.List;
import java.util.logging.Logger;

public class TransferBot implements vService {
    private static final String REFERENCE_CURRENCY = "BTC";
    private final static Logger LOGGER = Logging.getLogger(TransferBot.class);

    @Override
    public int getRunPeriodSeconds() {
        return Config.getConfigInt(Consts.TRANSFER_BOT_PERIOD_SECONDS);
    }

    @Override
    public void run() {
        try {
            LOGGER.info("Transfer bot start");
            transfer();
            LOGGER.info("Transfer bot done");
        } catch (Exception e) {
            LOGGER.severe(Conversions.exceptionToString(e));
        }
    }

    public static void transfer() throws Exception {
        List<SXBalance> balances = SXApi.listBalances();

        for (SXBalance balance : balances) {
            if (balance.getCurrency().equals(REFERENCE_CURRENCY)) {
                // Transfer btc to nicehash if above threshold
                /*
                double btcAmount = balance.getAvailable();
                if (btcAmount > Config.getConfigDouble(Consts.TRANSFER_BOT_BTC_MIN_AMOUNT)) {
                    LOGGER.info("Transferring " + btcAmount + " BTC to Nicehash");
                    String address = NHApi.getLightningAddress(btcAmount);
                    SXApi.withdrawLightning(REFERENCE_CURRENCY, address);
                }

                 */
            } else {
                // Convert all to btc
                LOGGER.info("Selling " + balance.getAvailable() + " " + balance.getCurrency());
                SXApi.sell(balance.getCurrency(), REFERENCE_CURRENCY, balance.getAvailable());
            }
        }
    }
}
