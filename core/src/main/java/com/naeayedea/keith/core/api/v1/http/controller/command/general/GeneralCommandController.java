/*
 * Copyright (C) Steven Muirhead 2025. All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the
 * copyright holder(s) in writing.
 *
 */

package com.naeayedea.keith.core.api.v1.http.controller.command.general;

import com.naeayedea.keith.common.exception.KeithInternalException;
import com.naeayedea.keith.common.model.api.v1.response.basic.TextResponse;
import com.naeayedea.keith.common.model.api.v1.response.tile.TileResponse;
import com.naeayedea.keith.common.model.user.AccessLevel;
import com.naeayedea.keith.common.model.user.KeithUser;
import com.naeayedea.keith.core.api.annotation.http.KeithHttpCommandController;
import com.naeayedea.keith.core.api.v1.exception.KeithUserFacingException;
import com.naeayedea.keith.core.api.v1.http.internal.model.request.KeithCommandContext;
import com.naeayedea.keith.core.service.HelpService;
import com.naeayedea.keith.core.service.UserService;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@KeithHttpCommandController(baseAccessLevel = AccessLevel.USER)
public class GeneralCommandController {

    private final HelpService helpService;

    private final UserService userService;

    public GeneralCommandController(
        HelpService helpService,
        UserService userService
    ) {
        this.helpService = helpService;
        this.userService = userService;
    }

    @GetMapping("/ping")
    public ResponseEntity<TextResponse> ping() {
        return ResponseEntity.ok(TextResponse.simple("pong"));
    }

    /**
     * @param keithCommandContext who's asking, resolved from the request
     * @param commandName the specific command to describe, or omitted/{@code "info"} for the info panel
     * @param prefix the calling platform's command prefix, used to format the description
     * @param inviteLink a leaf-supplied invite/install link to show on the info panel, or omitted
     * @param platformTips leaf-supplied "how to use this here" text to show on the info panel, or omitted
     * @return a {@link TileResponse} for the info panel or a single command's detail
     * @throws KeithUserFacingException with a 404 status if {@code commandName} is a real command
     *                                   name but doesn't match a registered one
     */
    @GetMapping("/help")
    public ResponseEntity<TileResponse> help(
        KeithCommandContext keithCommandContext,
        @RequestParam(required = false) @Nullable String commandName,
        @RequestParam(defaultValue = "/") String prefix,
        @RequestParam(required = false) @Nullable String inviteLink,
        @RequestParam(required = false) @Nullable String platformTips
    ) throws KeithUserFacingException, KeithInternalException {
        KeithUser user = userService.getOrCreateUser(keithCommandContext.getPlatformUserId());

        return ResponseEntity.ok(helpService.buildHelpResponse(commandName, prefix, user.getLocale(), inviteLink, platformTips));
    }

    /**
     * @param keithCommandContext who's asking, resolved from the request
     * @param prefix the calling platform's command prefix, used to format each description
     * @return a {@link TileResponse} listing every command - used only to populate a leaf app's
     *         command picker (e.g. a Discord dropdown), not shown to the user as a page of its own
     */
    @GetMapping("/help/commands")
    public ResponseEntity<TileResponse> helpCommands(
        KeithCommandContext keithCommandContext,
        @RequestParam(defaultValue = "/") String prefix
    ) throws KeithInternalException {
        KeithUser user = userService.getOrCreateUser(keithCommandContext.getPlatformUserId());

        return ResponseEntity.ok(helpService.buildCommandListResponse(prefix, user.getLocale()));
    }

    @GetMapping("/hello")
    public ResponseEntity<TextResponse> hello(KeithCommandContext keithCommandContext) {
        return ResponseEntity.ok(
            TextResponse.builder()
                .addText("Hello, ")
                .addMentionedUser(keithCommandContext.getPlatformUserId())
                .build()
        );
    }

    @GetMapping("/test-text-response")
    public ResponseEntity<TextResponse> testTextResponse(KeithCommandContext keithCommandContext) {
        return ResponseEntity.ok(
            TextResponse.builder()
                .addText("This is a test response. Your user id is: ")
                .addMentionedUser(keithCommandContext.getPlatformUserId())
                .addText(". The special replacement character will be doubled, for example: REPLACEMENT CHARACTER -> " + TextResponse.USER_REPLACEMENT_CHARACTER)
                .addText(" <-. Multiple users can be mentioned in the same command, for example: ")
                .addMentionedUser("dummy-user-id")
                .addText(".")
                .build()
        );
    }
}
