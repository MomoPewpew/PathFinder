/*
 * This file is generated by jOOQ.
 */
package de.cubbossa.pathfinder.jooq;


import de.cubbossa.pathfinder.jooq.tables.PathfinderDiscoverings;
import de.cubbossa.pathfinder.jooq.tables.PathfinderEdges;
import de.cubbossa.pathfinder.jooq.tables.PathfinderGroupModifierRelation;
import de.cubbossa.pathfinder.jooq.tables.PathfinderNodeTypeRelation;
import de.cubbossa.pathfinder.jooq.tables.PathfinderNodegroupNodes;
import de.cubbossa.pathfinder.jooq.tables.PathfinderNodegroups;
import de.cubbossa.pathfinder.jooq.tables.PathfinderPathVisualizer;
import de.cubbossa.pathfinder.jooq.tables.PathfinderSearchTerms;
import de.cubbossa.pathfinder.jooq.tables.PathfinderWaypoints;
import de.cubbossa.pathfinder.jooq.tables.SqliteSequence;


/**
 * Convenience access to all tables in the default schema.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tables {

    /**
     * The table <code>pathfinder_discoverings</code>.
     */
    public static final PathfinderDiscoverings PATHFINDER_DISCOVERINGS = PathfinderDiscoverings.PATHFINDER_DISCOVERINGS;

    /**
     * The table <code>pathfinder_edges</code>.
     */
    public static final PathfinderEdges PATHFINDER_EDGES = PathfinderEdges.PATHFINDER_EDGES;

    /**
     * The table <code>pathfinder_group_modifier_relation</code>.
     */
    public static final PathfinderGroupModifierRelation PATHFINDER_GROUP_MODIFIER_RELATION = PathfinderGroupModifierRelation.PATHFINDER_GROUP_MODIFIER_RELATION;

    /**
     * The table <code>pathfinder_node_type_relation</code>.
     */
    public static final PathfinderNodeTypeRelation PATHFINDER_NODE_TYPE_RELATION = PathfinderNodeTypeRelation.PATHFINDER_NODE_TYPE_RELATION;

    /**
     * The table <code>pathfinder_nodegroup_nodes</code>.
     */
    public static final PathfinderNodegroupNodes PATHFINDER_NODEGROUP_NODES = PathfinderNodegroupNodes.PATHFINDER_NODEGROUP_NODES;

    /**
     * The table <code>pathfinder_nodegroups</code>.
     */
    public static final PathfinderNodegroups PATHFINDER_NODEGROUPS = PathfinderNodegroups.PATHFINDER_NODEGROUPS;

    /**
     * The table <code>pathfinder_path_visualizer</code>.
     */
    public static final PathfinderPathVisualizer PATHFINDER_PATH_VISUALIZER = PathfinderPathVisualizer.PATHFINDER_PATH_VISUALIZER;

    /**
     * The table <code>pathfinder_search_terms</code>.
     */
    public static final PathfinderSearchTerms PATHFINDER_SEARCH_TERMS = PathfinderSearchTerms.PATHFINDER_SEARCH_TERMS;

    /**
     * The table <code>pathfinder_waypoints</code>.
     */
    public static final PathfinderWaypoints PATHFINDER_WAYPOINTS = PathfinderWaypoints.PATHFINDER_WAYPOINTS;

    /**
     * The table <code>sqlite_sequence</code>.
     */
    public static final SqliteSequence SQLITE_SEQUENCE = SqliteSequence.SQLITE_SEQUENCE;
}
