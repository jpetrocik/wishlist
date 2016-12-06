package org.psoft.wishlist;


import javax.servlet.Filter;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	@Bean
	public DataSource dataSource(){
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://petrocik.net:3306/personal");
		dataSource.setUsername("john");
		dataSource.setPassword("oropez");
		
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
	
    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);
    }

}