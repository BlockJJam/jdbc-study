package hello.jdbc.connection;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ConnectionTest {
    @Test
    void driverManager() throws SQLException {
        // 직접사용
        Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        log.info("connection={}, class = {}", con1, con1.getClass());
        log.info("connection={}, class = {}", con2, con2.getClass());
    }

    @Test
    void dataSrouceDriverManager() throws SQLException {
        // DriverManagerDataSource - 항상 새로운 커넥션을 획득
        DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        useDataSource(driverManagerDataSource);
    }

    @Test
    void connectionPool() throws SQLException, InterruptedException {
        // connection pooling
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setMaximumPoolSize(10);
        dataSource.setPoolName("MyPool");

        useDataSource(dataSource); // 이 상태에서는 안된다
        Thread.sleep(1000);
    }

    private void useDataSource(DataSource dataSource) throws SQLException {
        // 사용시점(즉 client시점): dataSource에서 getConnection만 하면된다. 필요한 파라미터는 DriverManagerDataSource 설정에 분리해놨다.
        // 주입 설정과 사용 부분이 서로 분리됨
        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection();
        log.info("connection={}, class = {}", con1, con1.getClass());
        log.info("connection={}, class = {}", con2, con2.getClass());
    }
}
