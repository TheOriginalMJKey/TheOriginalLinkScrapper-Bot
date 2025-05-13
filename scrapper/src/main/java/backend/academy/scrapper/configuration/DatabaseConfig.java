package backend.academy.scrapper.configuration;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DatabaseConfig {

    private final DataSource dataSource;

    @Autowired
    public void runMigrations() {
        log.info("Running database migrations");

        try (Connection connection = dataSource.getConnection()) {
            Database database =
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

            try (Liquibase liquibase = new Liquibase(
                    "migrations/changelog/changelog-master.yml", new ClassLoaderResourceAccessor(), database)) {

                liquibase.update(new Contexts(), new LabelExpression());
                log.info("Database migrations completed successfully");
            }
        } catch (SQLException | LiquibaseException e) {
            log.error("Error running database migrations", e);
            throw new RuntimeException("Database migration failed", e);
        }
    }
}
