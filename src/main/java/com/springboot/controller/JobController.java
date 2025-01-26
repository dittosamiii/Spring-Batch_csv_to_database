package com.springboot.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class JobController {

	private JobLauncher jobLauncher;
	private Job job;

	@GetMapping("/run-job")
	public ResponseEntity<String> runJob() {
		try {
			JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis())
					.toJobParameters();

			jobLauncher.run(job, jobParameters);
			return ResponseEntity.ok("Batch Job has been launch");
		} catch (Exception e) {
			return ResponseEntity.status(500).body("Job Execution failed " + e.getMessage());
		}
	}

}
