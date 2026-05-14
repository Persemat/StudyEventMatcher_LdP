-- Run this on existing dev databases before creating events with structured tags.
-- Hibernate ddl-auto=update adds new columns, but it does not remove the old tag column.

ALTER TABLE event_tags
    ADD COLUMN macro_tag VARCHAR(255) NULL,
    ADD COLUMN micro_tag VARCHAR(255) NULL;

UPDATE event_tags
SET macro_tag = tag,
    micro_tag = NULL
WHERE macro_tag IS NULL;

ALTER TABLE event_tags
    MODIFY macro_tag VARCHAR(255) NOT NULL,
    DROP COLUMN tag;
