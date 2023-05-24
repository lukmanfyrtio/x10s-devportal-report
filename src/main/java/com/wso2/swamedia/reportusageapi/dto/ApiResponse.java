package com.wso2.swamedia.reportusageapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
public class ApiResponse<T> {
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String message;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String status;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private T data;

	// Constructors, getters, and setters

	// Helper methods for creating different types of responses
	public static <T> ApiResponse<T> success(String message, T data) {
		ApiResponse<T> response = new ApiResponse<>();
		response.setMessage(message);
		response.setStatus("success");
		response.setData(data);
		return response;
	}

	public static <T> ApiResponse<T> error(String message) {
		ApiResponse<T> response = new ApiResponse<>();
		response.setMessage(message);
		response.setStatus("error");
		return response;
	}
}
