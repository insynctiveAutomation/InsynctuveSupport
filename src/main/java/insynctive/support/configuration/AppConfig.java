package insynctive.support.configuration;

import java.util.Properties;

import javax.naming.ConfigurationException;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@PropertySource("classpath:application.properties")
@Import({PropertyPlaceHolderConfiguration.class})
@ComponentScan({"insynctive.*"})
@EnableTransactionManagement
@EnableScheduling
public class AppConfig {

	@Value("${environment}")
	private Integer environment;
	
	@Value("${hibernate.auto}")
	private String hibernateAuto;
	
	@Value("${hibernate.driver.class.name}")
	private String driverClassName;
	
	@Value("${hibernate.db.uri}")
	private String dbUri;
	
	@Value("${hibernate.db.username}")
	private String dbUsername;
	
	@Value("${hibernate.db.password}")
	private String dbPassword;
	
	@Value("${hibernate.dialect}")
	private String hibernateDialect;

	@Value("${hibernate.showSQL}")
	private Boolean showSQL;
	
	@Bean
	public JdbcTemplate jdbcTemplate() throws ConfigurationException {
		return new JdbcTemplate(dataSource());
	}

	@Bean
	public DataSource dataSource() throws ConfigurationException {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();


		switch(environment){
		/*LOCAL*/
		case 1 :
			dataSource.setDriverClassName(driverClassName);
			dataSource.setUrl(dbUri);
			dataSource.setUsername(dbUsername);
			dataSource.setPassword(dbPassword);
			break;
		/*HEROKU insynctive-support*/
		case 2 :
			/*HEROKU*/
			dataSource.setDriverClassName("com.mysql.jdbc.Driver");
			dataSource.setUrl("jdbc:mysql://us-cdbr-iron-east-03.cleardb.net/heroku_71adb2241d1d2a5");
			dataSource.setUsername("beacfac291b2f1");
			dataSource.setPassword("90063b5a");
			break;
		/*HEROKU alpha-insynctive-support*/
		case 3 :
			dataSource.setDriverClassName("com.mysql.jdbc.Driver");
			dataSource.setUrl("jdbc:mysql://us-cdbr-iron-east-03.cleardb.net:3306/heroku_1725234cc46ffb2");
			dataSource.setUsername("b7076e6c3cf689");
			dataSource.setPassword("a6083623");
			break;
		default :
			throw new ConfigurationException(environment == null ? "No environment added in application.properties" : "Wrong environment added in application.properties");
	}
		
//		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
//		dataSource.setUrl("jdbc:mysql://localhost:3306/insynctive-support");
//		dataSource.setUsername("root");
//		dataSource.setPassword("");
		
		return dataSource;
	}

	@Bean(name = "sessionFactory")
	public LocalSessionFactoryBean sessionFactory() throws ConfigurationException {
		LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
		sessionFactoryBean.setDataSource(dataSource());
		sessionFactoryBean.setPackagesToScan("insynctive.support.model");
		sessionFactoryBean.setHibernateProperties(hibProperties());
		return sessionFactoryBean;
	}

	@Bean
	public HibernateTransactionManager transactionManager() throws ConfigurationException {
		HibernateTransactionManager transactionManager = new HibernateTransactionManager();
		transactionManager.setSessionFactory(sessionFactory().getObject());
		return transactionManager;
	}
	
	private Properties hibProperties() {
		Properties properties = new Properties();
		properties.put("hibernate.hbm2ddl.auto", "update");
		properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
		properties.put("hibernate.show_sql", true);
		return properties;
	}
}
