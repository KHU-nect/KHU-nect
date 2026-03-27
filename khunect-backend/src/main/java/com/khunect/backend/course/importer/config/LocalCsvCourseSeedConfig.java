package com.khunect.backend.course.importer.config;

import com.khunect.backend.course.importer.service.CsvCourseImportService;
import java.io.InputStream;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

@Configuration
@Profile({"local", "dev"})
public class LocalCsvCourseSeedConfig {

	@Bean
	public CommandLineRunner sampleCourseCsvImportRunner(CsvCourseImportService csvCourseImportService) {
		return args -> {
			ClassPathResource resource = new ClassPathResource("sample-data/courses-sample.csv");
			if (!resource.exists()) {
				return;
			}
			try (InputStream inputStream = resource.getInputStream()) {
				csvCourseImportService.importCsv(inputStream);
			}
		};
	}
}
