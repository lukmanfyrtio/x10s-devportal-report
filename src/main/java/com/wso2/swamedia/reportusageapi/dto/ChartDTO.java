package com.wso2.swamedia.reportusageapi.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartDTO {
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private LocalDateTime xHour;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private LocalDate x;
	private int totalUsage;
	private String apiName;
	private String applicationName;
}
