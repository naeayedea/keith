package com.naeayedea.keith.core.model.channel;

import com.naeayedea.keith.core.model.message.KeithMessage;

import java.util.List;

public interface KeithMessageChannel extends KeithChannel {

    List<KeithMessage> getLastNMessages(int N);

}
