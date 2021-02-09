package com.exadel.frs;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;

@AutoConfigureEmbeddedDatabase(beanName = "dataSource")
public class EmbeddedPostgreSQLTest {
}
