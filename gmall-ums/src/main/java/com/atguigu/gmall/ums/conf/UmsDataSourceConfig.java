package com.atguigu.gmall.ums.conf;

import io.shardingjdbc.core.api.MasterSlaveDataSourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import javax.sql.DataSource;

/**
 * @author Brodie
 * @date 2020/1/4 - 14:21
 */

@Configuration
public class UmsDataSourceConfig {
    @Bean
    public DataSource dataSource() throws Exception {
        //使用sharding-jdbc创建出具有主从库的数据源
        DataSource dataSource = MasterSlaveDataSourceFactory
                .createDataSource(ResourceUtils.getFile("classpath:sharding-jdbc.yaml"));
        return  dataSource;

    }
}
