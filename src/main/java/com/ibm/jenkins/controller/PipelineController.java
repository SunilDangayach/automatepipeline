package com.ibm.jenkins.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.jenkins.model.ResponseBean;
import com.ibm.jenkins.service.PipelineService;


@RestController
public class PipelineController {

	@Value("${version}")
	private String version;
	
	@Autowired
	PipelineService pipelineService;

	private static final Logger logger = LoggerFactory.getLogger(PipelineController.class);

	@RequestMapping(method = RequestMethod.POST, value = "/createPipeline", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<ResponseBean<Void>> createPipeline()
	{
		logger.debug("Creating pipeline");
		ResponseEntity<ResponseBean<Void>> response;
		ResponseBean<Void> rb = new ResponseBean<>();

		pipelineService.createPipeline();

		rb.setVersion(version);
		response = ResponseEntity.ok(rb);
		return response;
	}
	
}
