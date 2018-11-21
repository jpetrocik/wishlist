package org.psoft.wishlist;


import java.util.Timer;

import javax.servlet.Filter;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.google.common.eventbus.EventBus;
import com.plivo.api.Plivo;
import com.plivo.api.PlivoClient;

@SpringBootApplication
public class Application {

	@Value("${jdbc.url}")
	String dbUrl;

	@Value("${database.user}")
	String dbUser;

	@Value("${database.password}")
	String dbPassword;

	@Value("${sms.authId}")
	String authId;

	@Value("${sms.authToken}")
	String authToken;

	@Value("${sms.enable}")
	boolean smsEnable;

	@Bean
	public EventBus eventBus() {
		return new EventBus();
	}

	@Bean
	public Timer timer() {
		return new Timer();
	}

	@Bean
	public DataSource dataSource(){
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl(dbUrl);
		dataSource.setUsername(dbUser);
		dataSource.setPassword(dbPassword);
		dataSource.setMinIdle(2);
		dataSource.setMaxActive(10);
		dataSource.setInitialSize(2);
		dataSource.setValidationQuery("/* ping */");
		dataSource.setPoolPreparedStatements(true);
		dataSource.setMaxOpenPreparedStatements(5);

		return dataSource;
	}

	@Bean
	public FilterRegistrationBean someFilterRegistration() {

	    FilterRegistrationBean registration = new FilterRegistrationBean();
	    registration.setFilter(loginFilter());
	    registration.addUrlPatterns("/*");
	    registration.addInitParameter("paramName", "paramValue");
	    registration.setName("loginFilter");
	    registration.setOrder(1);
	    return registration;
	}

	@Bean(name = "loginFilter")
	public Filter loginFilter() {
		return new LoginFilter();
	}

	@Bean
	public PlivoClient AuthyApiClient() {
		PlivoClient plivoClient = Plivo.init(authId, authToken);
		plivoClient.setTesting(!smsEnable);
		return plivoClient;

	}

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);
    }

}
