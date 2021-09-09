package database;

import dataclasses.AllDataRecord;
import dataclasses.CryptoInvestment;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.types.DayToSecond;
import utils.Config;
import utils.Consts;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static test.generated.Tables.*;

public class AllData {
    private static java.sql.Connection connection = null;
    private static Cursor<Record> cursor = null;

    private static void startCursor(int analyzeMinutes, int stop) throws SQLException {
        connection = Connection.getConnection();
        connection.setAutoCommit(false);
        DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

        cursor = create.selectFrom(
                        ALGO_DATA.join(MARKET_DATA).on(ALGO_DATA.ID.eq(MARKET_DATA.ALGO_ID))
                                .join(COIN_DATA).on((ALGO_DATA.ID.eq(COIN_DATA.ALGO_ID)))
                                .join(MARKET_PRICE_DATA).on(MARKET_DATA.ID.eq(MARKET_PRICE_DATA.MARKET_ID))
                ).where(ALGO_DATA.TIMESTAMP.ge(DSL.currentLocalDateTime().minus((new DayToSecond(0, 0, analyzeMinutes)))))
                .orderBy(COIN_DATA.COIN_NAME.asc(), MARKET_DATA.MARKET_NAME.asc(), ALGO_DATA.ALGO_NAME.asc(), MARKET_PRICE_DATA.FULFILL_SPEED.asc(), ALGO_DATA.TIMESTAMP.asc())
                .fetchSize(stop)
                .fetchLazy();
    }

    private static void finishCursor() throws SQLException {
        cursor.close();
        cursor = null;
        connection.close();
    }

    public static boolean hasData() {
        return cursor != null;
    }

    public static List<List<AllDataRecord>> getNextData(int analyzeMinutes) throws SQLException {
        int stop = Config.getConfigInt(Consts.ALL_DATA_FETCH_STOP);
        int count = 0;

        if (!hasData()) {
            startCursor(analyzeMinutes, stop);
        }

        List<List<AllDataRecord>> list = new ArrayList<>();
        List<AllDataRecord> subList = null;
        CryptoInvestment investment = null;

        while (cursor.hasNext()) {
            Record record = cursor.fetchNext();
            AllDataRecord allDataRecord = new AllDataRecord(record);
            CryptoInvestment currentInvestment = new CryptoInvestment(allDataRecord);

            if (currentInvestment.equals(investment)) {
                subList.add(allDataRecord);
            } else {
                if (subList != null) {
                    list.add(subList);
                    subList = null;
                }

                if (count > stop) {
                    break;
                }

                subList = new ArrayList<>();
                investment = currentInvestment;
            }

            count++;
        }

        if (subList != null) {
            list.add(subList);
        }

        if (!cursor.hasNext()) {
            finishCursor();
        }

        return list;
    }

}
