package com.ibm.jenkins.configuration;

import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GitHubConfiguration {

	@Value("${git.username}")
	private String userName;
	
	@Value("${git.password}")
	private String password;
	
	private static final Logger logger = LoggerFactory.getLogger(JenkinsConfiguration.class);
	
	@Bean
	public CredentialsProvider credentialsProvider() throws Exception
	{
		logger.debug("Creating CredentialsProvider object");
		return new UsernamePasswordCredentialsProvider(userName,password);
	}

}
