package com.ibm.jenkins.configuration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.ibm.jenkins.controller.PipelineController;
import com.ibm.jenkins.filter.FolderFilter;

@Component
public class StartupApplicationListener implements ApplicationListener<ContextRefreshedEvent> {
 
	@Autowired
	private CredentialsProvider cp;
	
	@Value("${git.repo}")
	private String repo;
	
	private String[] foldersArray;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PipelineController.class);
  
    @Override 
    public void onApplicationEvent(ContextRefreshedEvent event) {
    	Git result = null;
		try {
			File localPath = File.createTempFile("base101", "");
			if (!localPath.delete()) {
				LOGGER.info("Could not delete temporary file " + localPath);
			}
			LOGGER.info("Cloning from " + repo + " to " + localPath);
			result = Git.cloneRepository().setURI(repo).setDirectory(localPath).setCredentialsProvider(cp).call();

			foldersArray = localPath.list(new FolderFilter());
			LOGGER.info("Result is {}",result);
			
		}catch (InvalidRemoteException | TransportException e) {
			LOGGER.error("failed while cloning remote repo", e);
		} catch (IOException | GitAPIException e) {
			LOGGER.error("failed while cloning remote repo",e);
		}
    }
		
    
    public List<String> getRepoFolders() {
    	return Arrays.asList(foldersArray);
    }
    
    
}