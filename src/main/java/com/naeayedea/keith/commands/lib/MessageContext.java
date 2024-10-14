package com.naeayedea.keith.commands.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MessageContext<T extends Enum<T>> {

    private final List<T> options;

    private final List<Object> arguments;

    public MessageContext(List<T> options, List<Object> arguments) {
        this.options = options;
        this.arguments = arguments;
    }

    public static <T extends Enum<T>> MessageContext<T> of(List<T> options, List<Object> arguments) {
        return new MessageContext<>(options, arguments);
    }

    public static <T extends Enum<T>> MessageContext<T> of(T option, List<Object> arguments) {
        return new MessageContext<>(List.of(option), arguments);
    }

    public static <T extends Enum<T>> MessageContext<T> of(List<T> options, Object argument) {
        return new MessageContext<>(options, List.of(argument));
    }

    public static <T extends Enum<T>> MessageContext<T> of(T option, Object argument) {
        return new MessageContext<>(List.of(option), List.of(argument));
    }

    public static <T extends Enum<T>> MessageContext<T> of(T option) {
        return new MessageContext<>(List.of(option), List.of());
    }

    public static <T extends Enum<T>> MessageContext<T> of(List<T> options) {
        return new MessageContext<>(options, List.of());
    }

    public static <T extends Enum<T>> MessageContext<T> empty() {
        return new MessageContext<>(new ArrayList<T>(), List.of());
    }

    public List<T> getOptions() {
        return options;
    }

    public List<Object> getArguments() {
        return arguments;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MessageContext) obj;
        return Objects.equals(this.options, that.options) &&
            Objects.equals(this.arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(options, arguments);
    }

    @Override
    public String toString() {
        return "MessageContext[" +
            "options=" + options + ", " +
            "arguments=" + arguments + ']';
    }

}
