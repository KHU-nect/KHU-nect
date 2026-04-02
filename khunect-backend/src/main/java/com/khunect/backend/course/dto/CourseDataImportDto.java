package com.khunect.backend.course.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CourseDataImportDto {

	@JsonProperty("id")
	private Integer jsonId;

	@JsonProperty("college")
	private String college;

	@JsonProperty("department")
	private String department;

	@JsonProperty("course_code")
	private String courseCode;

	@JsonProperty("course_name")
	private String courseName;

	@JsonProperty("professor")
	private String professor;

	@JsonProperty("lecture_cd")
	private String lectureCd;

	@JsonProperty("is_online")
	private Boolean isOnline;

	@JsonProperty("schedule_raw")
	private String scheduleRaw;

	@JsonProperty("schedules")
	private List<ScheduleSlotDto> schedules;

	@Getter
	@NoArgsConstructor
	public static class ScheduleSlotDto {

		@JsonProperty("day")
		private String day;

		@JsonProperty("start_time")
		private String startTime;

		@JsonProperty("end_time")
		private String endTime;

		@JsonProperty("start_minutes")
		private Integer startMinutes;

		@JsonProperty("end_minutes")
		private Integer endMinutes;

		@JsonProperty("classroom")
		private String classroom;
	}
}
