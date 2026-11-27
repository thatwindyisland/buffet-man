package org.example.buffet.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.example.buffet.mapper.GoodsMapper;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class GoodsPersistenceConfig {
    @Bean
    public DataSource dataSource(Environment environment) {
        return new DriverManagerDataSource(
                environment.getProperty("BUFFET_DB_URL", "jdbc:mysql://localhost:3306/buffet"),
                environment.getProperty("BUFFET_DB_USER", "buffet"),
                environment.getProperty("BUFFET_DB_PASSWORD", "")
        );
    }

    @Bean
    public SqlSessionFactoryBean sqlSessionFactory(DataSource dataSource) {
        var factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        return factory;
    }

    @Bean
    public MapperFactoryBean<GoodsMapper> goodsMapper(SqlSessionFactory sqlSessionFactory) {
        var factory = new MapperFactoryBean<>(GoodsMapper.class);
        factory.setSqlSessionFactory(sqlSessionFactory);
        return factory;
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
