package com.netlex.demo.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfiguration {

	@Bean
    Executor asyncTaskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(4);
		taskExecutor.setQueueCapacity(150);
		taskExecutor.setMaxPoolSize(4);
		taskExecutor.setThreadNamePrefix("AsyncTaskTreak-");
		taskExecutor.initialize();
		return taskExecutor;
	}
}
