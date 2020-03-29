-- name: update-entry!
-- @param String name
-- @param Long   id
UPDATE example_table
SET name = :name
WHERE id = :id;

-- name: select-entry
-- @param Long id
SELECT *
FROM example_table
WHERE id = :id;