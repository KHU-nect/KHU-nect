package com.khunect.backend.course.importer.dto;

public record CourseImportResult(
	int createdCount,
	int updatedCount,
	int skippedCount
) {
}
