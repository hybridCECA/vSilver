/*
 * This file is generated by jOOQ.
 */
package test.generated.tables;


import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row3;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import test.generated.Keys;
import test.generated.Public;
import test.generated.tables.records.OrderLimitsRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OrderLimits extends TableImpl<OrderLimitsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.order_limits</code>
     */
    public static final OrderLimits ORDER_LIMITS = new OrderLimits();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<OrderLimitsRecord> getRecordType() {
        return OrderLimitsRecord.class;
    }

    /**
     * The column <code>public.order_limits.id</code>.
     */
    public final TableField<OrderLimitsRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.order_limits.order_id</code>.
     */
    public final TableField<OrderLimitsRecord, String> ORDER_ID = createField(DSL.name("order_id"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.order_limits.order_limit</code>.
     */
    public final TableField<OrderLimitsRecord, Double> ORDER_LIMIT = createField(DSL.name("order_limit"), SQLDataType.DOUBLE.nullable(false), this, "");

    private OrderLimits(Name alias, Table<OrderLimitsRecord> aliased) {
        this(alias, aliased, null);
    }

    private OrderLimits(Name alias, Table<OrderLimitsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.order_limits</code> table reference
     */
    public OrderLimits(String alias) {
        this(DSL.name(alias), ORDER_LIMITS);
    }

    /**
     * Create an aliased <code>public.order_limits</code> table reference
     */
    public OrderLimits(Name alias) {
        this(alias, ORDER_LIMITS);
    }

    /**
     * Create a <code>public.order_limits</code> table reference
     */
    public OrderLimits() {
        this(DSL.name("order_limits"), null);
    }

    public <O extends Record> OrderLimits(Table<O> child, ForeignKey<O, OrderLimitsRecord> key) {
        super(child, key, ORDER_LIMITS);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<OrderLimitsRecord, Integer> getIdentity() {
        return (Identity<OrderLimitsRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<OrderLimitsRecord> getPrimaryKey() {
        return Keys.ORDER_LIMITS_PKEY;
    }

    @Override
    public OrderLimits as(String alias) {
        return new OrderLimits(DSL.name(alias), this);
    }

    @Override
    public OrderLimits as(Name alias) {
        return new OrderLimits(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public OrderLimits rename(String name) {
        return new OrderLimits(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public OrderLimits rename(Name name) {
        return new OrderLimits(name, null);
    }

    // -------------------------------------------------------------------------
    // Row3 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row3<Integer, String, Double> fieldsRow() {
        return (Row3) super.fieldsRow();
    }
}
