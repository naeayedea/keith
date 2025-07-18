package com.naeayedea.keith.common.model.command.output;

public interface ResponseTransformer<O extends CommandOutput, T> {

    T transform(O commandOutput);
}
