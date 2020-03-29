-- This is a simple query.
--
-- but...
-- name: test1
-- The docstring
  -- is tricksy.
SELECT CURRENT_TIMESTAMP AS time
FROM SYSIBM.SYSDUMMY1;

-- using ; in query
-- name: test-special1
-- The docstring
  -- is tricksy.
SELECT a where b = ';';

-- using ; in query
-- name: test-special2
-- The docstring
  -- is tricksy.
SELECT a
  where
    b = ';' and
    d = ';';

-- comments on footer