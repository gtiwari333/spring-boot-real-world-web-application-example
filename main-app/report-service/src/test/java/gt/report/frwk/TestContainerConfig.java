package gt.report.frwk;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mysql.MySQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestContainerConfig {

    static final MySQLContainer mysql = new MySQLContainer("mysql:9.7")
        .withCommand(
            "mysqld",
            "--lower_case_table_names=1",
            "--character_set_server=utf8mb4",
            "--explicit_defaults_for_timestamp"
        );

    static {
        mysql.start();
    }

    @Bean
    @ServiceConnection
    static MySQLContainer mysql() {     //mysql is lightweight
        return mysql;
    }

}
