/*
 * This file is generated by jOOQ.
 */
package de.cubbossa.pathfinder.jooq;


import de.cubbossa.pathfinder.jooq.tables.PathfinderDiscoverings;
import de.cubbossa.pathfinder.jooq.tables.PathfinderNodegroups;
import de.cubbossa.pathfinder.jooq.tables.PathfinderNodegroupsNodes;
import de.cubbossa.pathfinder.jooq.tables.PathfinderNodes;
import de.cubbossa.pathfinder.jooq.tables.PathfinderPathVisualizer;
import de.cubbossa.pathfinder.jooq.tables.PathfinderSearchTerms;
import de.cubbossa.pathfinder.jooq.tables.records.PathfinderDiscoveringsRecord;
import de.cubbossa.pathfinder.jooq.tables.records.PathfinderNodegroupsNodesRecord;
import de.cubbossa.pathfinder.jooq.tables.records.PathfinderNodegroupsRecord;
import de.cubbossa.pathfinder.jooq.tables.records.PathfinderNodesRecord;
import de.cubbossa.pathfinder.jooq.tables.records.PathfinderPathVisualizerRecord;
import de.cubbossa.pathfinder.jooq.tables.records.PathfinderSearchTermsRecord;

import org.jooq.ForeignKey;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables in the
 * default schema.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<PathfinderDiscoveringsRecord> PATHFINDER_DISCOVERINGS__PATHFINDER_DISCOVERINGS_PK = Internal.createUniqueKey(PathfinderDiscoverings.PATHFINDER_DISCOVERINGS, DSL.name("pathfinder_discoverings_PK"), new TableField[] { PathfinderDiscoverings.PATHFINDER_DISCOVERINGS.DISCOVER_KEY, PathfinderDiscoverings.PATHFINDER_DISCOVERINGS.PLAYER_ID }, true);
    public static final UniqueKey<PathfinderNodegroupsRecord> PATHFINDER_NODEGROUPS__PATHFINDER_NODEGROUPS__PK = Internal.createUniqueKey(PathfinderNodegroups.PATHFINDER_NODEGROUPS, DSL.name("pathfinder_nodegroups__PK"), new TableField[] { PathfinderNodegroups.PATHFINDER_NODEGROUPS.KEY }, true);
    public static final UniqueKey<PathfinderNodegroupsNodesRecord> PATHFINDER_NODEGROUPS_NODES__PATHFINDER_NODEGROUPS_NODES_PK = Internal.createUniqueKey(PathfinderNodegroupsNodes.PATHFINDER_NODEGROUPS_NODES, DSL.name("PATHFINDER_NODEGROUPS_NODES_PK"), new TableField[] { PathfinderNodegroupsNodes.PATHFINDER_NODEGROUPS_NODES.GROUP_KEY, PathfinderNodegroupsNodes.PATHFINDER_NODEGROUPS_NODES.NODE_ID }, true);
    public static final UniqueKey<PathfinderNodesRecord> PATHFINDER_NODES__NEWTABLE_PK = Internal.createUniqueKey(PathfinderNodes.PATHFINDER_NODES, DSL.name("NewTable_PK"), new TableField[] { PathfinderNodes.PATHFINDER_NODES.ID }, true);
    public static final UniqueKey<PathfinderPathVisualizerRecord> PATHFINDER_PATH_VISUALIZER__PK_PATHFINDER_PATH_VISUALIZER = Internal.createUniqueKey(PathfinderPathVisualizer.PATHFINDER_PATH_VISUALIZER, DSL.name("pk_pathfinder_path_visualizer"), new TableField[] { PathfinderPathVisualizer.PATHFINDER_PATH_VISUALIZER.KEY }, true);
    public static final UniqueKey<PathfinderSearchTermsRecord> PATHFINDER_SEARCH_TERMS__PK_PATHFINDER_SEARCH_TERMS = Internal.createUniqueKey(PathfinderSearchTerms.PATHFINDER_SEARCH_TERMS, DSL.name("pk_pathfinder_search_terms"), new TableField[] { PathfinderSearchTerms.PATHFINDER_SEARCH_TERMS.GROUP_KEY, PathfinderSearchTerms.PATHFINDER_SEARCH_TERMS.SEARCH_TERM }, true);

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<PathfinderNodegroupsNodesRecord, PathfinderNodegroupsRecord> PATHFINDER_NODEGROUPS_NODES__FK_PATHFINDER_NODEGROUPS_NODES_PATHFINDER_NODEGROUPS = Internal.createForeignKey(PathfinderNodegroupsNodes.PATHFINDER_NODEGROUPS_NODES, DSL.name("FK_pathfinder_nodegroups_nodes_pathfinder_nodegroups"), new TableField[] { PathfinderNodegroupsNodes.PATHFINDER_NODEGROUPS_NODES.GROUP_KEY }, Keys.PATHFINDER_NODEGROUPS__PATHFINDER_NODEGROUPS__PK, new TableField[] { PathfinderNodegroups.PATHFINDER_NODEGROUPS.KEY }, true);
    public static final ForeignKey<PathfinderNodegroupsNodesRecord, PathfinderNodesRecord> PATHFINDER_NODEGROUPS_NODES__FK_PATHFINDER_NODEGROUPS_NODES_PATHFINDER_NODES_2 = Internal.createForeignKey(PathfinderNodegroupsNodes.PATHFINDER_NODEGROUPS_NODES, DSL.name("FK_pathfinder_nodegroups_nodes_pathfinder_nodes_2"), new TableField[] { PathfinderNodegroupsNodes.PATHFINDER_NODEGROUPS_NODES.NODE_ID }, Keys.PATHFINDER_NODES__NEWTABLE_PK, new TableField[] { PathfinderNodes.PATHFINDER_NODES.ID }, true);
    public static final ForeignKey<PathfinderSearchTermsRecord, PathfinderNodegroupsRecord> PATHFINDER_SEARCH_TERMS__FK_PATHFINDER_SEARCH_TERMS_PATHFINDER_NODEGROUPS__PK = Internal.createForeignKey(PathfinderSearchTerms.PATHFINDER_SEARCH_TERMS, DSL.name("fk_pathfinder_search_terms_pathfinder_nodegroups__PK"), new TableField[] { PathfinderSearchTerms.PATHFINDER_SEARCH_TERMS.GROUP_KEY }, Keys.PATHFINDER_NODEGROUPS__PATHFINDER_NODEGROUPS__PK, new TableField[] { PathfinderNodegroups.PATHFINDER_NODEGROUPS.KEY }, true);
}
