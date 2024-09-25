package com.naeayedea.keith.commands;

public enum AccessLevel {

    //Only bot owner(s)
    OWNER (3),
    //Anyone with admin status
    ADMIN (2),
    //Users that haven't been banned
    USER (1),
    //Commands with ALL access level can be used by anyone, even banned users
    ALL (0);

    public final int num;
    AccessLevel(int num){
        this.num = num;
    }

    public static AccessLevel getLevel(String num) {
        switch (num) {
            case "0":
                return ALL;
            case "2":
                return ADMIN;
            case "3":
                return OWNER;
            default:
                return USER;
        }
    }

    public String toString() {
        switch (num) {
            case 0:
                return "All";
            case 2:
                return "Admin";
            case 3:
                return "Owner";
            default:
                return "User";
        }
    }
}
