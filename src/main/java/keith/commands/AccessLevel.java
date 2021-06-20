package keith.commands;

public enum AccessLevel {

    //Only bot owner(s)
    OWNER,
    //Anyone with admin status
    ADMIN,
    //Users that haven't been banned
    USER,
    //Commands with ALL access level can be used by anyone, even banned users
    ALL
}
