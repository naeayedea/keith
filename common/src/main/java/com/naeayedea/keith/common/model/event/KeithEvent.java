package com.naeayedea.keith.common.model.event;

import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

import java.util.Objects;

public final class KeithEvent<T> extends EventBase<T> implements ResolvableTypeProvider {
    public KeithEvent(T source) {
        this.source = source;
    }

    public T source() {
        return source;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (KeithEvent) obj;
        return Objects.equals(this.source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source);
    }

    @Override
    public String toString() {
        return "KeithEvent[" +
            "source=" + source + ']';
    }

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(
            getClass(),
            ResolvableType.forInstance(this.source)
        );
    }
}
