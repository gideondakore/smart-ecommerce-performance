package com.amalitech.smartshop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class AsyncConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService asyncExecutorService(
            @Value("${app.async.core-pool-size:8}") int corePoolSize,
            @Value("${app.async.max-pool-size:16}") int maxPoolSize,
            @Value("${app.async.queue-capacity:200}") int queueCapacity,
            @Value("${app.async.keep-alive-seconds:60}") int keepAliveSeconds
    ) {
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveSeconds,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
