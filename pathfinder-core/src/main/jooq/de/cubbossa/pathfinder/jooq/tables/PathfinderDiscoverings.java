/*
 * This file is generated by jOOQ.
 */
package de.cubbossa.pathfinder.jooq.tables;


import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.jooq.DefaultSchema;
import de.cubbossa.pathfinder.jooq.Keys;
import de.cubbossa.pathfinder.jooq.tables.records.PathfinderDiscoveringsRecord;
import de.cubbossa.pathfinder.storage.misc.NamespacedKeyConverter;
import de.cubbossa.pathfinder.storage.misc.UUIDConverter;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Function;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Function3;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Records;
import org.jooq.Row3;
import org.jooq.Schema;
import org.jooq.SelectField;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class PathfinderDiscoverings extends TableImpl<PathfinderDiscoveringsRecord> {

  private static final long serialVersionUID = 1L;

  /**
   * The reference instance of <code>pathfinder_discoverings</code>
   */
  public static final PathfinderDiscoverings PATHFINDER_DISCOVERINGS = new PathfinderDiscoverings();

  /**
   * The class holding records for this type
   */
  @Override
  public Class<PathfinderDiscoveringsRecord> getRecordType() {
    return PathfinderDiscoveringsRecord.class;
  }

  /**
   * The column <code>pathfinder_discoverings.discover_key</code>.
   */
  public final TableField<PathfinderDiscoveringsRecord, NamespacedKey> DISCOVER_KEY =
      createField(DSL.name("discover_key"), SQLDataType.VARCHAR(64).nullable(false), this, "",
          new NamespacedKeyConverter());

  /**
   * The column <code>pathfinder_discoverings.player_id</code>.
   */
  public final TableField<PathfinderDiscoveringsRecord, UUID> PLAYER_ID =
      createField(DSL.name("player_id"), SQLDataType.VARCHAR(36).nullable(false), this, "",
          new UUIDConverter());

  /**
   * The column <code>pathfinder_discoverings.date</code>.
   */
  public final TableField<PathfinderDiscoveringsRecord, LocalDateTime> DATE =
      createField(DSL.name("date"), SQLDataType.LOCALDATETIME(0).nullable(false), this, "");

  private PathfinderDiscoverings(Name alias, Table<PathfinderDiscoveringsRecord> aliased) {
    this(alias, aliased, null);
  }

  private PathfinderDiscoverings(Name alias, Table<PathfinderDiscoveringsRecord> aliased,
                                 Field<?>[] parameters) {
    super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
  }

  /**
   * Create an aliased <code>pathfinder_discoverings</code> table reference
   */
  public PathfinderDiscoverings(String alias) {
    this(DSL.name(alias), PATHFINDER_DISCOVERINGS);
  }

  /**
   * Create an aliased <code>pathfinder_discoverings</code> table reference
   */
  public PathfinderDiscoverings(Name alias) {
    this(alias, PATHFINDER_DISCOVERINGS);
  }

  /**
   * Create a <code>pathfinder_discoverings</code> table reference
   */
  public PathfinderDiscoverings() {
    this(DSL.name("pathfinder_discoverings"), null);
  }

  public <O extends Record> PathfinderDiscoverings(Table<O> child,
                                                   ForeignKey<O, PathfinderDiscoveringsRecord> key) {
    super(child, key, PATHFINDER_DISCOVERINGS);
  }

  @Override
  public Schema getSchema() {
    return aliased() ? null : DefaultSchema.DEFAULT_SCHEMA;
  }

  @Override
  public UniqueKey<PathfinderDiscoveringsRecord> getPrimaryKey() {
    return Keys.PATHFINDER_DISCOVERINGS__PATHFINDER_DISCOVERINGS_PK;
  }

  @Override
  public PathfinderDiscoverings as(String alias) {
    return new PathfinderDiscoverings(DSL.name(alias), this);
  }

  @Override
  public PathfinderDiscoverings as(Name alias) {
    return new PathfinderDiscoverings(alias, this);
  }

  @Override
  public PathfinderDiscoverings as(Table<?> alias) {
    return new PathfinderDiscoverings(alias.getQualifiedName(), this);
  }

  /**
   * Rename this table
   */
  @Override
  public PathfinderDiscoverings rename(String name) {
    return new PathfinderDiscoverings(DSL.name(name), null);
  }

  /**
   * Rename this table
   */
  @Override
  public PathfinderDiscoverings rename(Name name) {
    return new PathfinderDiscoverings(name, null);
  }

  /**
   * Rename this table
   */
  @Override
  public PathfinderDiscoverings rename(Table<?> name) {
    return new PathfinderDiscoverings(name.getQualifiedName(), null);
  }

  // -------------------------------------------------------------------------
  // Row3 type methods
  // -------------------------------------------------------------------------

  @Override
  public Row3<NamespacedKey, UUID, LocalDateTime> fieldsRow() {
    return (Row3) super.fieldsRow();
  }

  /**
   * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
   */
  public <U> SelectField<U> mapping(
      Function3<? super NamespacedKey, ? super UUID, ? super LocalDateTime, ? extends U> from) {
    return convertFrom(Records.mapping(from));
  }

  /**
   * Convenience mapping calling {@link SelectField#convertFrom(Class,
   * Function)}.
   */
  public <U> SelectField<U> mapping(Class<U> toType,
                                    Function3<? super NamespacedKey, ? super UUID, ? super LocalDateTime, ? extends U> from) {
    return convertFrom(toType, Records.mapping(from));
  }
}
