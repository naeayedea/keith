/*
 * Copyright (C) Steven Muirhead 2025. All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the
 * copyright holder(s) in writing.
 *
 */


CREATE TABLE IF NOT EXISTS schema_history
(
    major_version    int          not null,
    minor_version    int          not null,
    patch_version    int          not null,
    version_metadata varchar(256) not null default '',
    date_updated     datetime     not null default NOW(),
    PRIMARY KEY (major_version, minor_version, patch_version, version_metadata)
);

#only insert our current version if there are no entries, otherwise expect upgrade scripts to populate
INSERT INTO schema_history (major_version, minor_version, patch_version, version_metadata)
SELECT 4, 0, 0, 'alpha'
FROM dual
WHERE NOT EXISTS (SELECT * FROM schema_history);