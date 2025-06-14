package com.naeayedea.keith.core.commands.lib.output;

public interface ResponseTransformer<O extends CommandOutput, T> {

    T transform(O commandOutput);
}
