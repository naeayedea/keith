package com.naeayedea.keith.core.commands.lib;

import org.springframework.lang.NonNull;


public abstract class AbstractCommand implements Command {

    private final int cost;

    private final String internalName;

    private final String nameKey;

    private final String aliasKey;

    public AbstractCommand(String internalName, String nameKey, String aliasKey) {
        this(internalName, nameKey, aliasKey, 1);
    }

    public AbstractCommand(String internalName, String nameKey, String aliasKey, int cost) {
        this.internalName = internalName;
        this.nameKey = nameKey;
        this.aliasKey = aliasKey;
        this.cost = cost;

    }

    @Override
    @NonNull
    public String getNameTranslationKey() {
        return nameKey;
    }

    @Override
    @NonNull
    public String getAliasTranslationKey() {
        return aliasKey;
    }

    @Override
    @NonNull
    public String getInternalName() {
        return internalName;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public int getTimeOut() {
        return 10;
    }

    @Override
    public int getCost() {
        return cost;
    }

    @Override
    @NonNull
    //default to the max level, small safety step to reduce the chance of accidentally exposing commands
    public AccessLevel getAccessLevel() {
        return AccessLevel.MAX_LEVEL;
    }
}
