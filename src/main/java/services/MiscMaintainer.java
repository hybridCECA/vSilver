package services;

import database.Connection;
import nicehash.NHApi;
import utils.*;

public class MiscMaintainer implements vService {
    private final static VLogger LOGGER = Logging.getLogger(MiscMaintainer.class);

    @Override
    public int getRunPeriodSeconds() {
        return Config.getConfigInt(Consts.MISC_MAINTAINER_PERIOD_SECONDS);
    }

    @Override
    public void run() {
        int numToDelete = Connection.getNumToClean();
        LOGGER.info("Cleaning " + numToDelete + " database row(s)");
        Connection.cleanDatabase();
        LOGGER.info("Invalidating buy info cache");
        NHApi nhApi = SingletonFactory.getInstance(NHApi.class);
        nhApi.invalidateBuyInfoCache();
        LOGGER.info("Invalidating config cache");
        Config.invalidateCache();
    }
}
