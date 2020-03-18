package yesql4j.reactor;

import io.vertx.mysqlclient.MySQLClient;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;

public class SimpleQueriesTest {

    private static MySQLPool pool;

    @BeforeAll
    static void setup() {
        var opts = new MySQLConnectOptions();
        opts.setHost("localhost");
        opts.setPort(3306);
        opts.setDatabase("saas_sessions");

        var poolOpts = new PoolOptions();
        poolOpts.setMaxSize(5);

        pool = MySQLPool.pool(opts, poolOpts);
        Yesql4jReactor.preparedQuery(pool,
                "CREATE TABLE IF NOT EXISTS test_table (id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, value VARCHAR(255))"
        ).subscribe();
    }

    @AfterAll
    static void teardown() {
        pool.close();
    }

    @Test
    public void selectTest() {
        Mono<Long> insert = Yesql4jReactor.preparedQuery(
                pool,
                "INSERT INTO test_table (value) VALUES (?)", Tuple.of("test1")
        ).map(res -> res.property(MySQLClient.LAST_INSERTED_ID));

        Mono<String> insertedValue = insert.flatMap(insertedId ->
                Yesql4jReactor.preparedQuery(
                        pool,
                        "SELECT value FROM test_table WHERE id = ?", Tuple.of(insertedId)
                ).map(res -> res.iterator().next().getString("value"))
        );

        StepVerifier.create(insertedValue)
                .expectNext("test1")
                .expectComplete()
                .verify();
    }

    @Test
    public void updateTest() {
        Mono<Long> insert = Yesql4jReactor.preparedQuery(
                pool,
                "INSERT INTO test_table (value) VALUES (?)", Tuple.of("test1")
        ).map(res -> res.property(MySQLClient.LAST_INSERTED_ID));

        Mono<Integer> updatesCount = insert.flatMap(insertedId ->
                Yesql4jReactor.preparedQuery(
                        pool,
                        "UPDATE test_table SET value = ? WHERE id = ?", Tuple.of("newvalue", insertedId)
                ).map(SqlResult::rowCount)
        );

        StepVerifier.create(updatesCount)
                .expectNext(1)
                .expectComplete()
                .verify();
    }

    @Test
    public void batchInsertTest() {
        Mono<Integer> insert = Yesql4jReactor.preparedBatch(
                pool,
                "INSERT INTO test_table (value) VALUES (?)", Arrays.asList(Tuple.of("test2"), Tuple.of("test3"), Tuple.of("test4"))
        ).map(SqlResult::rowCount);

        StepVerifier.create(insert)
                .expectNext(1)
                .expectComplete()
                .verify();
    }
}
