package com.khunect.backend.course.importer.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KhuCourseCrawlerProperties.class)
public class CourseImportConfig {
}
