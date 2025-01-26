package com.springboot.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.springboot.model.User;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
public class SpringBatchConfiguration {

	private JobRepository jobRepository;
	private PlatformTransactionManager transactionManager;
	private DataSource dataSource;

	
	@Bean
	FlatFileItemReader<User> reader() {
		return new FlatFileItemReaderBuilder<User>().name("csvReader").resource(new ClassPathResource("users.csv"))
				.linesToSkip(1).delimited().names("firstName", "lastName")
				.fieldSetMapper(new BeanWrapperFieldSetMapper<>() {
					{
						setTargetType(User.class);
					}
				}).build();

	}

	@Bean
	ItemProcessor<User, User> processor() {
		return user -> {
			user.setFirstName(user.getFirstName().toUpperCase());
			user.setLastName(user.getLastName().toUpperCase());
			return user;
		};
	}

	@Bean
	JdbcBatchItemWriter<User> writer() {
		return new JdbcBatchItemWriterBuilder<User>().dataSource(dataSource)
				.sql("INSERT INTO user (first_name, last_name) VALUES (:firstName, :lastName)").beanMapped().build();
	}

	@Bean
	Step step() {
		return new StepBuilder("csv-to-database-step", jobRepository).<User, User>chunk(10, transactionManager)
				.reader(reader()).processor(processor()).writer(writer()).build();
	}

	@Bean
	Job job() {
		return new JobBuilder("csv-to-database-job", jobRepository).start(step()).build();
	}
}
