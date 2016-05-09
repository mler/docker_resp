package com.bdx.rainbow.gds.configuration;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.util.ClassUtils;

import com.alibaba.druid.pool.DruidDataSource;

/*
 * @see http://mybatis.github.io/spring/mappers.html
 */
@Configuration
@MapperScan(basePackages = {"com.bdx.rainbow.gds.mapper"},sqlSessionFactoryRef="sqlSessionFactory")
public class MybatisConfig implements EnvironmentAware{

	private static Logger log = LoggerFactory.getLogger(MybatisConfig.class);

	@Autowired
	private JdbcConnectionSettings jdbcConnectionSettings;

	@Autowired
	private Environment environment;
	
	@Bean
	@Primary
	public DataSource dataSource() {
		DruidDataSource ds = new DruidDataSource();
		ds.setDriverClassName(jdbcConnectionSettings.getDriver());
		ds.setUsername(jdbcConnectionSettings.getUsername());
		ds.setPassword(jdbcConnectionSettings.getPassword());
		ds.setUrl(jdbcConnectionSettings.getUrl());
		ds.setMaxActive(jdbcConnectionSettings.getMaxActive());
		ds.setValidationQuery(jdbcConnectionSettings.getValidationQuery());
		ds.setTestOnBorrow(jdbcConnectionSettings.getTestOnBorrow());
		ds.setTestOnReturn(jdbcConnectionSettings.getTestOnReturn());
		ds.setTestWhileIdle(jdbcConnectionSettings.getTestWhileIdle());
		ds.setTimeBetweenEvictionRunsMillis(jdbcConnectionSettings.getTimeBetweenEvictionRunsMillis());
		ds.setMinEvictableIdleTimeMillis(jdbcConnectionSettings.getMinEvictableIdleTimeMillis());
		return ds;
	}


	public Resource[] getResource( String basePackage, String pattern ) throws IOException {
		String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + ClassUtils.convertClassNameToResourcePath(new StandardEnvironment().resolveRequiredPlaceholders(basePackage)) + "/" + pattern;
		System.out.println("> packageSearchPath : "+packageSearchPath);
		Resource[] resources = new PathMatchingResourcePatternResolver().getResources(packageSearchPath);
		return resources;
	}


	@Bean
	@Primary
	public SqlSessionFactory sqlSessionFactory() throws Exception {
		log.debug("> sqlSessionFactory");
		final SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
		sqlSessionFactory.setDataSource(dataSource());
		sqlSessionFactory.setConfigLocation(new ClassPathResource("mybatis-config.xml"));
		sqlSessionFactory.setFailFast(true);
		sqlSessionFactory.setMapperLocations(getResource("gds.mapper", "**/*.xml"));
		sqlSessionFactory.setTypeHandlersPackage("com.bdx.rainbow.configuration.typehandler");
		return sqlSessionFactory.getObject();
	}


	@Bean
	@Primary
	public DataSourceTransactionManager transactionManager() {
		log.debug("> transactionManager");
		return new DataSourceTransactionManager(dataSource());
	}

	@PostConstruct
	public void postConstruct() {
		log.info("jdbc.settings={}", jdbcConnectionSettings);
	}


	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
}
