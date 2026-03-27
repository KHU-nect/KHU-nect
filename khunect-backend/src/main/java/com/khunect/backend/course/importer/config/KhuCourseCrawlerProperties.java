package com.khunect.backend.course.importer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.course-import.khu")
public class KhuCourseCrawlerProperties {

	private boolean enabled;
	private String baseUrl;
	private String searchPath;
	private String keywordParamName;
	private String yearParamName;
	private String termParamName;
	private String rowSelector;
	private String courseCodeSelector;
	private String courseNameSelector;
	private String professorNameSelector;
	private String departmentNameSelector;
	private String scheduleTextSelector;
	private String classroomSelector;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getSearchPath() {
		return searchPath;
	}

	public void setSearchPath(String searchPath) {
		this.searchPath = searchPath;
	}

	public String getKeywordParamName() {
		return keywordParamName;
	}

	public void setKeywordParamName(String keywordParamName) {
		this.keywordParamName = keywordParamName;
	}

	public String getYearParamName() {
		return yearParamName;
	}

	public void setYearParamName(String yearParamName) {
		this.yearParamName = yearParamName;
	}

	public String getTermParamName() {
		return termParamName;
	}

	public void setTermParamName(String termParamName) {
		this.termParamName = termParamName;
	}

	public String getRowSelector() {
		return rowSelector;
	}

	public void setRowSelector(String rowSelector) {
		this.rowSelector = rowSelector;
	}

	public String getCourseCodeSelector() {
		return courseCodeSelector;
	}

	public void setCourseCodeSelector(String courseCodeSelector) {
		this.courseCodeSelector = courseCodeSelector;
	}

	public String getCourseNameSelector() {
		return courseNameSelector;
	}

	public void setCourseNameSelector(String courseNameSelector) {
		this.courseNameSelector = courseNameSelector;
	}

	public String getProfessorNameSelector() {
		return professorNameSelector;
	}

	public void setProfessorNameSelector(String professorNameSelector) {
		this.professorNameSelector = professorNameSelector;
	}

	public String getDepartmentNameSelector() {
		return departmentNameSelector;
	}

	public void setDepartmentNameSelector(String departmentNameSelector) {
		this.departmentNameSelector = departmentNameSelector;
	}

	public String getScheduleTextSelector() {
		return scheduleTextSelector;
	}

	public void setScheduleTextSelector(String scheduleTextSelector) {
		this.scheduleTextSelector = scheduleTextSelector;
	}

	public String getClassroomSelector() {
		return classroomSelector;
	}

	public void setClassroomSelector(String classroomSelector) {
		this.classroomSelector = classroomSelector;
	}
}
