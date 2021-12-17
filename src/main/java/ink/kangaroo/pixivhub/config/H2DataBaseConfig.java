package ink.kangaroo.pixivhub.config;

import org.h2.tools.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;

/**
 * h2数据库服务器 H2DataBaseConfig
 */
@Configuration
public class H2DataBaseConfig {
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server inMemoryH2DataBaseServer() throws SQLException {
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9090");
    }
}