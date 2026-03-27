package com.khunect.backend.course.importer.service;

import com.khunect.backend.course.entity.CourseSourceType;
import com.khunect.backend.course.importer.dto.CourseImportResult;
import com.khunect.backend.course.importer.dto.CourseImportRow;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CsvCourseImportService {

	private final CourseImportService courseImportService;

	public CsvCourseImportService(CourseImportService courseImportService) {
		this.courseImportService = courseImportService;
	}

	public CourseImportResult importCsv(InputStream inputStream) throws IOException {
		List<CourseImportRow> rows = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			String line = reader.readLine();
			if (line == null) {
				return new CourseImportResult(0, 0, 0);
			}

			while ((line = reader.readLine()) != null) {
				if (line.isBlank()) {
					continue;
				}
				List<String> parts = parseCsvLine(line);
				if (parts.size() < 8) {
					rows.add(new CourseImportRow("", "", "", "", "", "", 0, "FIRST", "CSV"));
					continue;
				}
				rows.add(new CourseImportRow(
					parts.get(0),
					parts.get(1),
					parts.get(2),
					parts.get(3),
					parts.get(4),
					parts.get(5),
					Integer.parseInt(parts.get(6).trim()),
					parts.get(7),
					parts.size() > 8 ? parts.get(8) : "CSV"
				));
			}
		}

		return courseImportService.importRows(rows, CourseSourceType.IMPORTED);
	}

	private List<String> parseCsvLine(String line) {
		List<String> parts = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean inQuotes = false;

		for (int i = 0; i < line.length(); i++) {
			char ch = line.charAt(i);
			if (ch == '"') {
				inQuotes = !inQuotes;
				continue;
			}
			if (ch == ',' && !inQuotes) {
				parts.add(current.toString());
				current.setLength(0);
				continue;
			}
			current.append(ch);
		}
		parts.add(current.toString());
		return parts;
	}
}
