package com.naeayedea.keith.core.commands.lib;

public enum AccessLevel {

    //Only bot owner(s)
    OWNER(3),
    //Anyone with admin status
    ADMIN(2),
    //Users that haven't been banned
    USER(1),
    //Commands with ALL access level can be used by anyone, even banned users
    BANNED(0);

    public final int num;

    public static final AccessLevel MAX_LEVEL = OWNER;

    AccessLevel(int num) {
        this.num = num;
    }

    public static AccessLevel getLevel(String num) {
        return switch (num) {
            case "0" -> BANNED;
            case "2" -> ADMIN;
            case "3" -> OWNER;
            default -> USER;
        };
    }

    public String toString() {
        return switch (num) {
            case 0 -> "All";
            case 2 -> "Admin";
            case 3 -> "Owner";
            default -> "User";
        };
    }

    public boolean isSameOrLowerPermissionLevel(AccessLevel level) {
        return this.num <= level.num;
    }

}
