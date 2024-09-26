package com.naeayedea.model.chat;

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class Chat {

        ChatAgent agentOne;
        ChatAgent agentTwo;

        public Chat(ChatAgent one, ChatAgent two) {
            agentOne = one;
            agentTwo = two;
        }

        public MessageChannel getDestination(String id) {
            return getTarget(id).getChannel();
        }

        public ChatAgent getTarget(String id) {
            if (agentOne.getChannel().getId().equals(id)) {
                return agentTwo;
            } else {
                return agentOne;
            }
        }

        public ChatAgent getSelf(String id) {
            if (agentOne.getChannel().getId().equals(id)) {
                return agentOne;
            } else {
                return agentTwo;
            }
        }

        public void close() {
            agentOne.getChannel().sendMessage("Chat connection closed").queue();
            agentTwo.getChannel().sendMessage("Chat connection closed").queue();
        }
    }