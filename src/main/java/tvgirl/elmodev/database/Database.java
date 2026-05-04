package tvgirl.elmodev.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class Database {

    private final FileConfiguration pConfig;
    private HikariDataSource source;

    public Database(FileConfiguration pConfig) {
        this.pConfig = pConfig;
    }

    public void init() {
        HikariConfig hConfig = new HikariConfig();
        hConfig.setPoolName("e1M0dEv");

        String type = pConfig.getString("Database.type").toLowerCase();
        String host = pConfig.getString("Database.host");
        int port = pConfig.getInt("Database.port");
        String database = pConfig.getString("Database.name");

        String user = pConfig.getString("Database.user");
        String pass = pConfig.getString("Database.pass");

        String jdbcUrl;

        switch (type) {
            case "postgresql" -> jdbcUrl =
                    "jdbc:postgresql://" + host + ":" + port + "/" + database +
                            "?sslmode=disable";

            case "mysql" -> jdbcUrl =
                    "jdbc:mysql://" + host + ":" + port + "/" + database +
                            "?useSSL=false&serverTimezone=UTC";

            case "mariadb" -> jdbcUrl =
                    "jdbc:mariadb://" + host + ":" + port + "/" + database +
                            "?useSSL=false&serverTimezone=UTC";

            default -> throw new IllegalArgumentException("Unsupported DB type: " + type);
        }

        Bukkit.getLogger().info("DB URL: " + jdbcUrl);
        Bukkit.getLogger().info("USER: " + user);


        hConfig.setJdbcUrl(jdbcUrl);
        hConfig.setUsername(user);
        hConfig.setPassword(pass);

        hConfig.setMaximumPoolSize(10);
        hConfig.setMinimumIdle(2);
        hConfig.setConnectionTimeout(10000);
        hConfig.setIdleTimeout(60000);
        hConfig.setMaxLifetime(1700000);

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL нет. ", e);
        }

        this.source = new HikariDataSource(hConfig);
    }

    public HikariDataSource getSource() {
        if(this.source != null) {
            return this.source;
        } else {
            Bukkit.getLogger().info("ERROR SOURCE");
            return null;
        }
    }

    public void shutdown() {
        if (source != null && !source.isClosed()) {
            source.close();
        }
    }
}
