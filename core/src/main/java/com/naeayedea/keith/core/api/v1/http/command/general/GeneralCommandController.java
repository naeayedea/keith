/*
 * Copyright (C) Steven Muirhead 2025. All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the
 * copyright holder(s) in writing.
 *
 */

package com.naeayedea.keith.core.api.v1.http.command.general;

import com.naeayedea.keith.common.model.user.AccessLevel;
import com.naeayedea.keith.core.api.annotation.auth.RequireAccessLevel;
import com.naeayedea.keith.core.api.annotation.http.KeithHttpCommandController;
import com.naeayedea.keith.core.api.v1.http.internal.model.request.KeithCommandContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@KeithHttpCommandController(baseAccessLevel = AccessLevel.USER)
public class GeneralCommandController {

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    @GetMapping("/hello")
    @RequireAccessLevel(baseAccessLevel = AccessLevel.ADMIN)
    public ResponseEntity<String> hello(KeithCommandContext keithCommandContext) {
        return ResponseEntity.ok("Hello, " + keithCommandContext.getUserId());
    }
}
