-- name: unsafe-params-test
-- @param Long   id
SELECT * FROM users WHERE id > :id [:sorting];