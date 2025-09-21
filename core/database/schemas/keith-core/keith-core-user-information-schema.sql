/*
 * Copyright (C) Steven Muirhead "2025". All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the copyright holder(s) in writing.
 */

CREATE TABLE IF NOT EXISTS platform
(
    id        int          not null AUTO_INCREMENT,
    name      varchar(256) not null,
    supported bool,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS entitlement
(
    id   int          not null AUTO_INCREMENT,
    name varchar(256) not null,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS user
(
    user_id      varchar(256) not null,
    access_level int          not null default 1,
    command_count BIGINT not null default 0,
    first_seen  datetime not null default NOW(),
    PRIMARY KEY (user_id)
);

CREATE TABLE IF NOT EXISTS user_entitlements
(
    user_id        varchar(256),
    entitlement_id int,
    PRIMARY KEY (user_id, entitlement_id),
    FOREIGN KEY (user_id) REFERENCES user (user_id),
    FOREIGN KEY (entitlement_id) REFERENCES entitlement (id)
);

CREATE TABLE IF NOT EXISTS user_platform_presence
(
    user_id          varchar(256),
    platform_user_id varchar(256),
    platform_id      int,
    locale           varchar(64),
    PRIMARY KEY (user_id, platform_id),
    FOREIGN KEY (user_id) REFERENCES user (user_id),
    FOREIGN KEY (platform_id) REFERENCES platform (id)
);

