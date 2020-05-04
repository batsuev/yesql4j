# Yesql4j
Maven plugin for Java code generation from sql files.
Currently supported frameworks:
- Spring (jdbc template)
- Vert.x MySQL + reactor

Inspired by https://github.com/krisajenkins/yesql

## Installation
```xml
<plugin>
    <groupId>com.yesql4j</groupId>
    <artifactId>yesql4j-maven-plugin</artifactId>
    <version>0.1.8-beta</version>
    <configuration>
        <generator>SPRING</generator>
    </configuration>
    <executions>
        <execution>
            <phase>generate-sources</phase>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
## Writing sql files
SQL files should be placed under `src/main/sql`.
Example sql file (`src/main/sql/com/example/queries.sql`):
```mysql
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
```
Base folder for sql files can be changed using `sqlSourceRoot` configuration option (default `"${basedir}/src/main/sql"`)

These files will be translated to java classes, placed in `${project.build.directory}/generated-sources/sql/java` (`javaOutputDirectory` configuration option)

### Naming conventions
* Use `!` postfix for update queries (example: `-- name: update-entry!`).
* Use `<!` postfix for returning generated key (example: `-- name: create-entry<!`)
* All other cases will return rows from database.

### Parameters
* You can use both named (like `:prop`) and `?` parameters.
* Param types can be specified in comment: `-- @param Type name`. Base java types from `java.lang`, `java.util` and `java.math` can be specified without package, full class name with package required otherwise.

## Spring usage
TO BE ADDED
## Reactor + Vert.x MySQL
TO BE ADDED