package de.cubbossa.pathfinder.storage.implementation;

import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.node.NodeTypeRegistry;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import de.cubbossa.pathfinder.storage.DataStorageException;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;
import org.jooq.ConnectionProvider;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.sqlite.SQLiteConfig;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqliteStorage extends SqlStorage {

  private final File file;
  private Connection connection;

  public SqliteStorage(File file, NodeTypeRegistry nodeTypeRegistry,
                       ModifierRegistry modifierRegistry,
                       VisualizerTypeRegistry visualizerTypeRegistry) {
    super(SQLDialect.SQLITE, nodeTypeRegistry, modifierRegistry, visualizerTypeRegistry);
    this.file = file;
  }

  public void init() throws Exception {
    if (!file.exists()) {
      file.getParentFile().mkdirs();
      file.createNewFile();
    }
    try {
      ConnectionProvider fac = getConnectionProvider();
      Connection con = fac.acquire();
      con.prepareStatement("PRAGMA ignore_check_constraints = true;").execute();
      fac.release(con);

      super.init();

    } catch (SQLException e) {
      throw new DataStorageException("Could not connect to Sqlite database.", e);
    }
  }

  public void shutdown() {
    try {
      if (connection != null && !connection.isClosed()) {
        connection.close();
        connection = null;
      }
    } catch (SQLException e) {
      throw new DataStorageException("Could not disconnect Sqlite database", e);
    }
  }

  ConnectionProvider connectionProvider = new ConnectionProvider() {
    @Override
    public synchronized @Nullable Connection acquire() throws DataAccessException {
      if (connection != null) {
        return connection;
      }
      try {
        SQLiteConfig config = new SQLiteConfig();
        config.setJournalMode(SQLiteConfig.JournalMode.WAL);
        config.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL);

        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath(), config.toProperties());
        connection.setAutoCommit(false);

        return connection;
      } catch (ClassNotFoundException | SQLException e) {
        throw new DataStorageException("Could not connect to Sqlite database.", e);
      }
    }

    @SneakyThrows
    @Override
    public void release(Connection con) throws DataAccessException {
      con.commit();
    }
  };

  public ConnectionProvider getConnectionProvider() {
    return connectionProvider;
  }
}
