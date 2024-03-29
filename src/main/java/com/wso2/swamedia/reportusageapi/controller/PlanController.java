package com.wso2.swamedia.reportusageapi.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wso2.swamedia.reportusageapi.Utils;
import com.wso2.swamedia.reportusageapi.dto.ApiResponse;
import com.wso2.swamedia.reportusageapi.dto.SubPoliciesResponse;

@RestController
@RequestMapping("/plan")
public class PlanController {

	private static final Logger LOGGER = LoggerFactory.getLogger(PlanController.class);

	private static final int POST_PAID_SUBS_TYPE = 2;
	private static final int PRE_PAID_SUBS_TYPE = 1;
	private static final int FREE_SUBS_TYPE = 3;

	@Autowired
	private Utils utils;

	@GetMapping
	public ResponseEntity<?> getPlanByPaidType(@RequestParam(value = "subsTypeId", required = true) Integer subsTypeId,
			@RequestParam(value = "apiId", required = true) String apiId, Authentication authentication) {
		LOGGER.info("Received request for get plan by payment type");
		try {
			validateSubsTypeId(subsTypeId);

			List<SubPoliciesResponse> resData = utils.getSubscriptionThrottlingPoliciesPublisher(apiId);
			ApiResponse<?> response = ApiResponse.success("Get plan by payment type retrieval successful.",
					subsTypeId.equals(PRE_PAID_SUBS_TYPE) ? Utils.getTierAttributesOfTypeTimeAndQuota(resData)
							: subsTypeId.equals(POST_PAID_SUBS_TYPE) ? Utils.getTierAttributesOfTypeTime(resData)
									: Utils.getTierAttributesOfTypeTimeAndFree(resData));
			LOGGER.info("Get plan by payment type retrieval completed");

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			ApiResponse<?> responseError = ApiResponse.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
		}
	}

	private void validateSubsTypeId(int subsTypeId) {
		if (subsTypeId != POST_PAID_SUBS_TYPE && subsTypeId != PRE_PAID_SUBS_TYPE && subsTypeId != FREE_SUBS_TYPE) {
			throw new IllegalArgumentException(
					"Invalid subsTypeId. Allowed values: 1 (pre-paid), 2 (post-paid) or 3 (free)");
		}
	}
}