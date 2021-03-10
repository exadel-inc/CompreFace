package com.exadel.frs.core.trainservice;

import com.exadel.frs.core.trainservice.config.IntegrationTest;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@IntegrationTest
@ExtendWith(SpringExtension.class)
@AutoConfigureEmbeddedDatabase(beanName = "dsPg")
public class EmbeddedPostgreSQLTest {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void initDatabase() {
        try {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(dataSource.getConnection()));
            Liquibase liquibase = new Liquibase("db/changelog/db.changelog-master.yaml", new ClassLoaderResourceAccessor(), database);
            liquibase.update(new Contexts(), new LabelExpression());
        } catch (Exception e) {
            //manage exception
            e.printStackTrace();
        }
    }
}
