package com.naeayedea.keith.platform.discord.command.lib.output;

import com.naeayedea.keith.common.model.command.output.ResponseTransformer;
import com.naeayedea.keith.common.model.command.output.basic.TextCommandOutput;
import org.springframework.stereotype.Component;

@Component
public class DiscordTextCommandOutputResponseTransformer implements ResponseTransformer<TextCommandOutput, String> {

    @Override
    public String transform(TextCommandOutput commandOutput) {
        return commandOutput.getText();
    }
}
