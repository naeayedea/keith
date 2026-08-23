/*
 * Copyright (C) Steven Muirhead 2025. All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the
 * copyright holder(s) in writing.
 *
 */

package com.naeayedea.keith.core.api.v1;

import com.naeayedea.keith.core.api.v1.exception.KeithUserFacingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice(basePackages = "com.naeayedea.keith.core.api.v1.http.controller")
public class KeithV1ControllerAdvice {

    @ExceptionHandler(KeithUserFacingException.class)
    public ResponseEntity<Map<String, Object>> handleException(KeithUserFacingException e) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("status", e.getStatus().value());
        response.put("error", e.getStatus().getReasonPhrase());
        response.put("message", e.getMessage());
        response.put("timestamp", Instant.now().toString());

        return ResponseEntity.status(e.getStatus()).body(response);
    }

}
