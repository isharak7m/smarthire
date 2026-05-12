package com.smarthire.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables @Async for fire-and-forget email notifications.
 */
@Configuration
@EnableAsync
public class AsyncConfig {}
