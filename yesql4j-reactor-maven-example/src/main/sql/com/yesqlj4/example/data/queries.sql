-- name: ddl!
CREATE TABLE example_table (
    id bigint unsigned auto_increment primary key,
    name varchar(255),
    stamp timestamp default now(),
    value_decimal decimal(12, 2),
    value_double double,
    value_bytes blob
);

-- name: create-entry<!
INSERT INTO example_table (name, stamp, value_decimal, value_double, value_bytes) VALUES (?, ?, ?, ?, ?);

-- name: update-entry!
-- @param String name
-- @param Long   id
UPDATE example_table SET name = :name WHERE id = :id;

-- name: select-entries
SELECT * FROM example_table;

-- name: select-entry
-- @param Long id
SELECT * FROM example_table WHERE id = :id;