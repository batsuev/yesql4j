# Installation
Update project properties:
```$xml
<properties>
  <yesql4j.version>0.1.7-beta</yesql4j.version>
</properties>
```

Update your project maven dependencies:
```$xml
<dependency>
    <groupId>yesql4j</groupId>
    <artifactId>yesql4j-vertx-mysql-reactor</artifactId>
    <version>${yesql4j.version}</version>
</dependency>
```

Add plugin for building sql to `build/plugins` section:
```$xml
<plugin>
    <groupId>yesql4j</groupId>
    <artifactId>yesql4j-maven-plugin</artifactId>
    <version>${yesql4j.version}</version>
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

# Basic usage
Create sql queries file in `src/sql` folder, for example:
`src/sql/yesq4j/example/data/queries.sql`:
```$mysql
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
UPDATE example_table SET name = :name WHERE id = :id;

-- name: select-entries
SELECT * FROM example_table;

-- name: select-entry
SELECT * FROM example_table WHERE id = :id;
```

This sql file will be translated to java class `yesqlj4.example.data.Queries`. 
Now you can use all entries as simple java calls:
```$mysql
Queries.ddl(pool).subscribe(System.out::println);
Mono<Integer> updatesCount = Queries.updateEntry(pool, "Test", 21);
```