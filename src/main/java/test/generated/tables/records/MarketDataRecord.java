/*
 * This file is generated by jOOQ.
 */
package test.generated.tables.records;


import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record6;
import org.jooq.Row6;
import org.jooq.impl.UpdatableRecordImpl;
import test.generated.tables.MarketData;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class MarketDataRecord extends UpdatableRecordImpl<MarketDataRecord> implements Record6<Long, Long, String, Double, Double, Integer> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.market_data.id</code>.
     */
    public void setId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.market_data.id</code>.
     */
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.market_data.algo_id</code>.
     */
    public void setAlgoId(Long value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.market_data.algo_id</code>.
     */
    public Long getAlgoId() {
        return (Long) get(1);
    }

    /**
     * Setter for <code>public.market_data.market_name</code>.
     */
    public void setMarketName(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.market_data.market_name</code>.
     */
    public String getMarketName() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.market_data.fulfill_speed</code>.
     */
    public void setFulfillSpeed(Double value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.market_data.fulfill_speed</code>.
     */
    public Double getFulfillSpeed() {
        return (Double) get(3);
    }

    /**
     * Setter for <code>public.market_data.total_speed</code>.
     */
    public void setTotalSpeed(Double value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.market_data.total_speed</code>.
     */
    public Double getTotalSpeed() {
        return (Double) get(4);
    }

    /**
     * Setter for <code>public.market_data.fulfill_price</code>.
     */
    public void setFulfillPrice(Integer value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.market_data.fulfill_price</code>.
     */
    public Integer getFulfillPrice() {
        return (Integer) get(5);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record6 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row6<Long, Long, String, Double, Double, Integer> fieldsRow() {
        return (Row6) super.fieldsRow();
    }

    @Override
    public Row6<Long, Long, String, Double, Double, Integer> valuesRow() {
        return (Row6) super.valuesRow();
    }

    @Override
    public Field<Long> field1() {
        return MarketData.MARKET_DATA.ID;
    }

    @Override
    public Field<Long> field2() {
        return MarketData.MARKET_DATA.ALGO_ID;
    }

    @Override
    public Field<String> field3() {
        return MarketData.MARKET_DATA.MARKET_NAME;
    }

    @Override
    public Field<Double> field4() {
        return MarketData.MARKET_DATA.FULFILL_SPEED;
    }

    @Override
    public Field<Double> field5() {
        return MarketData.MARKET_DATA.TOTAL_SPEED;
    }

    @Override
    public Field<Integer> field6() {
        return MarketData.MARKET_DATA.FULFILL_PRICE;
    }

    @Override
    public Long component1() {
        return getId();
    }

    @Override
    public Long component2() {
        return getAlgoId();
    }

    @Override
    public String component3() {
        return getMarketName();
    }

    @Override
    public Double component4() {
        return getFulfillSpeed();
    }

    @Override
    public Double component5() {
        return getTotalSpeed();
    }

    @Override
    public Integer component6() {
        return getFulfillPrice();
    }

    @Override
    public Long value1() {
        return getId();
    }

    @Override
    public Long value2() {
        return getAlgoId();
    }

    @Override
    public String value3() {
        return getMarketName();
    }

    @Override
    public Double value4() {
        return getFulfillSpeed();
    }

    @Override
    public Double value5() {
        return getTotalSpeed();
    }

    @Override
    public Integer value6() {
        return getFulfillPrice();
    }

    @Override
    public MarketDataRecord value1(Long value) {
        setId(value);
        return this;
    }

    @Override
    public MarketDataRecord value2(Long value) {
        setAlgoId(value);
        return this;
    }

    @Override
    public MarketDataRecord value3(String value) {
        setMarketName(value);
        return this;
    }

    @Override
    public MarketDataRecord value4(Double value) {
        setFulfillSpeed(value);
        return this;
    }

    @Override
    public MarketDataRecord value5(Double value) {
        setTotalSpeed(value);
        return this;
    }

    @Override
    public MarketDataRecord value6(Integer value) {
        setFulfillPrice(value);
        return this;
    }

    @Override
    public MarketDataRecord values(Long value1, Long value2, String value3, Double value4, Double value5, Integer value6) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached MarketDataRecord
     */
    public MarketDataRecord() {
        super(MarketData.MARKET_DATA);
    }

    /**
     * Create a detached, initialised MarketDataRecord
     */
    public MarketDataRecord(Long id, Long algoId, String marketName, Double fulfillSpeed, Double totalSpeed, Integer fulfillPrice) {
        super(MarketData.MARKET_DATA);

        setId(id);
        setAlgoId(algoId);
        setMarketName(marketName);
        setFulfillSpeed(fulfillSpeed);
        setTotalSpeed(totalSpeed);
        setFulfillPrice(fulfillPrice);
    }
}
