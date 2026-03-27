package com.khunect.backend.timetable.controller;

import com.khunect.backend.common.response.ApiResponse;
import com.khunect.backend.timetable.dto.request.AddTimetableEntryRequest;
import com.khunect.backend.timetable.dto.response.TimetableEntryResponse;
import com.khunect.backend.timetable.service.TimetableService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/timetable")
public class TimetableController {

	private final TimetableService timetableService;

	public TimetableController(TimetableService timetableService) {
		this.timetableService = timetableService;
	}

	@PostMapping
	public ApiResponse<TimetableEntryResponse> addEntry(
		Authentication authentication,
		@Valid @RequestBody AddTimetableEntryRequest request
	) {
		return ApiResponse.success(timetableService.addEntry(authentication.getName(), request));
	}

	@GetMapping("/me")
	public ApiResponse<List<TimetableEntryResponse>> getMyTimetable(Authentication authentication) {
		return ApiResponse.success(timetableService.getMyTimetable(authentication.getName()));
	}

	@DeleteMapping("/{entryId}")
	public ApiResponse<Void> deleteEntry(Authentication authentication, @PathVariable Long entryId) {
		timetableService.deleteEntry(authentication.getName(), entryId);
		return ApiResponse.successWithoutData();
	}
}
