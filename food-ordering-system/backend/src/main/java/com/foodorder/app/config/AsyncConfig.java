package com.foodorder.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configures the thread pool used by @Async methods (see OrderService).
 *
 * This is where the "multithreading for concurrent orders" requirement lives:
 * order placement/notification work is handed off to this bounded pool so
 * that many customers can place orders at the same time without blocking
 * each other's HTTP request threads.
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Value("${app.async.core-pool-size:5}")
    private int corePoolSize;

    @Value("${app.async.max-pool-size:15}")
    private int maxPoolSize;

    @Value("${app.async.queue-capacity:100}")
    private int queueCapacity;

    @Override
    @Bean(name = "orderTaskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("order-worker-");
        executor.initialize();
        return executor;
    }
}
