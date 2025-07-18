package com.naeayedea.keith.common.model.channel;

import com.naeayedea.keith.common.model.message.KeithMessage;

import java.util.List;

public interface KeithMessageChannel extends KeithChannel {

    List<KeithMessage> getLastNMessages(int N);

}
