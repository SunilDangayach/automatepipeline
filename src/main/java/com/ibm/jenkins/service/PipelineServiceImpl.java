package com.ibm.jenkins.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ibm.jenkins.configuration.StartupApplicationListener;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.client.JenkinsHttpClient;

@Service
public class PipelineServiceImpl implements PipelineService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PipelineServiceImpl.class);

	@Autowired
	private CredentialsProvider crendentailsProvider;

	@Value("${git.repo}")
	private String repo;

	@Value("${jenkins.base-jobs}")
	private String baseJenkinJobs;

	@Value("${jenkins.base-pipeline-view}")
	private String basePipelineView;

	@Autowired
	private JenkinsServer jenkinsServer;
	
	@Autowired
	private JenkinsHttpClient jenkinsClient;

	@Autowired
	private StartupApplicationListener startup;

	private static final String TEMP_REPO_NAME = "temp_repo";

	@Override
	public void createPipeline() {
		Git result = null;
		try {
			File localPath = File.createTempFile(TEMP_REPO_NAME, "");
			if (!localPath.delete()) {
				LOGGER.info("Could not delete temporary file " + localPath);
			}
			LOGGER.info("Cloning from " + repo + " to " + localPath);
			result = Git.cloneRepository().setURI(repo).setDirectory(localPath)
					.setCredentialsProvider(crendentailsProvider).call();

			LOGGER.info("Result is {}", result);

			List<String> listChanges = getNewChanges(result);
			List<String> newPipelines = null;
			List<String> existingMicroservices = startup.getRepoFolders();
			if (CollectionUtils.isNotEmpty(existingMicroservices) && CollectionUtils.isNotEmpty(listChanges)) {
				newPipelines = listChanges.stream().filter(change -> !existingMicroservices.contains(change))
						.map(change -> change).collect(Collectors.toList());
				LOGGER.info(newPipelines.toString());
			}

			if (CollectionUtils.isNotEmpty(newPipelines)) {
				newPipelines.forEach(microservice -> {
					createJenkinsPipeline(microservice);
				});
			}

		} catch (InvalidRemoteException | TransportException e) {
			LOGGER.error("failed while cloning remote repo", e);
		} catch (IOException | GitAPIException e) {
			LOGGER.error("failed while cloning remote repo", e);
		}
	}

	private void createJenkinsPipeline(String microservice) {
		String baseJobsArray[];
		if (baseJenkinJobs != null) {
			baseJobsArray = baseJenkinJobs.split(",");

			IntStream.range(0, baseJobsArray.length).forEach(index->{
				try {
					String jobXmlFile = jenkinsServer.getJobXml(baseJobsArray[index]);
					if(baseJobsArray.length>index+1) {
						jobXmlFile=jobXmlFile.replaceAll(baseJobsArray[index+1], microservice+"_"+(index+1));
					}
					jenkinsServer.createJob(microservice+"_"+index, jobXmlFile,true);
				}catch(IOException e) {
					LOGGER.error("Exception during jobs creation",e);
				}
			});

			try {
				String viewXmlFile = jenkinsClient.get("/view/"+basePipelineView+"/config.xml");
				viewXmlFile = viewXmlFile.replaceAll(baseJobsArray[0], microservice+"_"+0);
				jenkinsServer.createView(microservice+"_view", viewXmlFile,true);
				
			} catch (IOException e) {
				LOGGER.error("Exception during views creation",e);
			}
			
		}

	}

	private List<String> getNewChanges(Git result) throws RevisionSyntaxException, AmbiguousObjectException,
			IncorrectObjectTypeException, IOException, GitAPIException {
		List<String> changes = null;
		try (Repository repository = result.getRepository()) {
			ObjectId oldHead = repository.resolve("HEAD^^{tree}");
			ObjectId head = repository.resolve("HEAD^{tree}");
			LOGGER.info("Printing diff between tree: " + oldHead + " and " + head);

			try (ObjectReader reader = repository.newObjectReader()) {
				CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
				oldTreeIter.reset(reader, oldHead);
				CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
				newTreeIter.reset(reader, head);

				try (Git git = new Git(repository)) {
					List<DiffEntry> diffs = git.diff().setNewTree(newTreeIter).setOldTree(oldTreeIter).call();
					if (CollectionUtils.isNotEmpty(diffs)) {
						changes = diffs.stream().map(diff -> {
							String newPath = diff.getNewPath().replaceAll("//", "/");
							String[] entryArray = newPath.split("/");
							if (entryArray != null && entryArray.length > 0) {
								return entryArray[0];
							} else {
								return newPath;
							}
						}).collect(Collectors.toList());
					}
				}
			}
		}
		return changes;
	}

}
