/*
 * This file is generated by jOOQ.
 */
package test.generated.tables;


import org.jooq.Record;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import test.generated.Keys;
import test.generated.Public;
import test.generated.tables.records.ConfigRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Config extends TableImpl<ConfigRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.config</code>
     */
    public static final Config CONFIG = new Config();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ConfigRecord> getRecordType() {
        return ConfigRecord.class;
    }

    /**
     * The column <code>public.config.id</code>.
     */
    public final TableField<ConfigRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.config.key</code>.
     */
    public final TableField<ConfigRecord, String> KEY = createField(DSL.name("key"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.config.value</code>.
     */
    public final TableField<ConfigRecord, String> VALUE = createField(DSL.name("value"), SQLDataType.CLOB.nullable(false), this, "");

    private Config(Name alias, Table<ConfigRecord> aliased) {
        this(alias, aliased, null);
    }

    private Config(Name alias, Table<ConfigRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.config</code> table reference
     */
    public Config(String alias) {
        this(DSL.name(alias), CONFIG);
    }

    /**
     * Create an aliased <code>public.config</code> table reference
     */
    public Config(Name alias) {
        this(alias, CONFIG);
    }

    /**
     * Create a <code>public.config</code> table reference
     */
    public Config() {
        this(DSL.name("config"), null);
    }

    public <O extends Record> Config(Table<O> child, ForeignKey<O, ConfigRecord> key) {
        super(child, key, CONFIG);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<ConfigRecord, Integer> getIdentity() {
        return (Identity<ConfigRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<ConfigRecord> getPrimaryKey() {
        return Keys.CONFIG_PKEY;
    }

    @Override
    public Config as(String alias) {
        return new Config(DSL.name(alias), this);
    }

    @Override
    public Config as(Name alias) {
        return new Config(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Config rename(String name) {
        return new Config(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Config rename(Name name) {
        return new Config(name, null);
    }

    // -------------------------------------------------------------------------
    // Row3 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row3<Integer, String, String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }
}
