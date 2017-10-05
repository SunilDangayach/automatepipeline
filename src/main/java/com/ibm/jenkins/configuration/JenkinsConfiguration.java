package com.ibm.jenkins.configuration;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.client.JenkinsHttpClient;

@Configuration
public class JenkinsConfiguration {
	
	@Value("${jenkins.url}")
	private String uri;

	@Value("${jenkins.username}")
	private String userName;
	
	@Value("${jenkins.password}")
	private String password;
	
	private static final Logger logger = LoggerFactory.getLogger(JenkinsConfiguration.class);
	
	@Bean
	public JenkinsServer jenkinsServer() throws Exception
	{
		logger.debug("Creating jenkins server object");
		return new JenkinsServer(new URI(uri), userName, password);
	}


	@Bean
	public JenkinsHttpClient jenkinsHttpClient() throws Exception
	{
		logger.debug("Creating jenkins Http Client object");
		return new JenkinsHttpClient(new URI(uri), userName, password);
	}
}
