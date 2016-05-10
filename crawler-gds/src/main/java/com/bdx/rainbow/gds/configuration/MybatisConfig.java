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
import com.alibaba.dubbo.common.utils.StringUtils;

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
	
	
//	============Docker Mysql==========
//	BSI_HZDB_PASSWORD=348be7b71b6d387
//	BSI_HZDB_HOST=mysqlshare.servicebroker.dataos.io
//	BSI_HZDB_URI=mysql://74e2a25af66413a:348be7b71b6d387@mysqlshare.servicebroker.dataos.io:3306/4be652d45e2d594
//	BSI_HZDB_PORT=3306
//	BSI_HZDB_VHOST=
//	BSI_HZDB_NAME=4be652d45e2d594
//	BSI_HZDB_USERNAME=74e2a25af66413a
//	========Docker Zookeeper========
//	BSI_HZZK_PASSWORD=28740d50b399fc17e364f2c18537176b
//	BSI_HZZK_HOST=sb-wjmsyfulyvrlc-zk.service-brokers.svc.cluster.local
//	BSI_HZZK_URI=
//	BSI_HZZK_PORT=2181
//	BSI_HZZK_VHOST=
//	BSI_HZZK_NAME=
//	BSI_HZZK_USERNAME=super
	
	/**
	 * 如果是docker部署的话，数据库参数是从环境变量里面传递过来的，
	 * 因此在生成datasource之前先对jdbcsetting做个预处理，将环境
	 * 变量里面的数据库参数绑定到setting中
	 */
	private void preProcessJdbcConnectionSettings()
	{
		if(environment == null)
			return;
		
		String env_db_uri = environment.getProperty("BSI_HZDB_URI");
		if(StringUtils.isBlank(env_db_uri) ==false)
		{
			env_db_uri = "jdbc:"+env_db_uri+"?useUnicode=true&characterEncoding=utf8";
			jdbcConnectionSettings.setDriver(env_db_uri);
		}
		
		String env_db_username = environment.getProperty("BSI_HZDB_USERNAME");
		if(StringUtils.isBlank(env_db_username) ==false)
			jdbcConnectionSettings.setUsername(env_db_username);
		
		String env_db_password = environment.getProperty("BSI_HZDB_PASSWORD");
		if(StringUtils.isBlank(env_db_password) ==false)
			jdbcConnectionSettings.setPassword(env_db_password);
		
	}
	
	@Bean
	@Primary
	public DataSource dataSource() {

		preProcessJdbcConnectionSettings();
		
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
