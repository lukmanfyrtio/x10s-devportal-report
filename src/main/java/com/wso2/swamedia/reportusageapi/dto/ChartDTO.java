package com.wso2.swamedia.reportusageapi.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartDTO {
	private LocalDateTime xHour;
	private LocalDate x;
	private int totalUsage;
	private String apiName;
}
