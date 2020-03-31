package com.yesql4j.reactor;

import io.vertx.mysqlclient.MySQLClient;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Collections;

public class TransactionsTest {

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

        Yesql4jReactor.preparedQuery(pool, Schedulers.single(),
                "CREATE TABLE IF NOT EXISTS test_table (id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, value VARCHAR(255))"
        ).subscribe();
    }

    @AfterAll
    static void teardown() {
        pool.close();
    }

    @BeforeEach
    void cleanupTestTable() {
        var cleanup = Yesql4jReactor.preparedQuery(
                pool, Schedulers.single(),
                "DELETE FROM test_table WHERE 1"
        ).map(SqlResult::rowCount);

        StepVerifier.create(cleanup)
                .expectNextCount(1)
                .expectComplete()
                .verify();
    }

    @Test
    public void basicTransactionTest() {
        Mono<Long> insert = Yesql4jReactor.preparedQuery(
                pool, Schedulers.single(),
                "INSERT INTO test_table (value) VALUES (?)", Collections.emptyList(), Tuple.of("test1")
        ).map(res -> res.property(MySQLClient.LAST_INSERTED_ID));

        Mono<String> insertedValue = insert.flatMap(insertedId ->
                Yesql4jReactor.preparedQuery(
                        pool, Schedulers.single(),
                        "SELECT value FROM test_table WHERE id = ?", Collections.emptyList(), Tuple.of(insertedId)
                ).map(res -> res.iterator().next().getString("value"))
        );

        Mono<String> transactional = Yesql4jReactor.transactional(pool, Schedulers.single(), insertedValue);

        StepVerifier.create(transactional)
                .expectNext("test1")
                .expectComplete()
                .verify();
    }

    @Test
    public void rollbackOnProcessingExceptionTest() {
        Mono<Long> insert = Yesql4jReactor.preparedQuery(
                pool, Schedulers.single(),
                "INSERT INTO test_table (value) VALUES (?)", Collections.emptyList(), Tuple.of("test1")
        ).map(res -> res.property(MySQLClient.LAST_INSERTED_ID));

        Mono<Long> failed = Mono.error(new Exception("processing exception"));

        Mono<Long> transactional = Yesql4jReactor.transactional(pool, Schedulers.single(), insert.then(failed));
        StepVerifier.create(transactional)
                .expectError()
                .verify();


        Mono<Integer> allRows = Yesql4jReactor.preparedQuery(
                pool, Schedulers.single(),
                "SELECT * FROM test_table"
        ).map(SqlResult::rowCount);

        StepVerifier.create(allRows)
                .expectNext(0)
                .expectComplete()
                .verify();
    }

    @Test
    public void rollbackOnFailedQueryTest() {
        Mono<Long> insert = Yesql4jReactor.preparedQuery(
                pool, Schedulers.single(),
                "INSERT INTO test_table (value) VALUES (?)", Collections.emptyList(), Tuple.of("test1")
        ).map(res -> res.property(MySQLClient.LAST_INSERTED_ID));

        Mono<Long> failed = Yesql4jReactor.preparedQuery(
                pool, Schedulers.single(),
                "INSERT INTO adfasfsafadsfasfdfa"
        ).map(res -> res.property(MySQLClient.LAST_INSERTED_ID));

        Mono<Long> transactional = Yesql4jReactor.transactional(pool, Schedulers.single(), insert.then(failed));
        StepVerifier.create(transactional)
                .expectError()
                .verify();

        Mono<Integer> allRows = Yesql4jReactor.preparedQuery(
                pool,
                Schedulers.single(),
                "SELECT * FROM test_table"
        ).map(SqlResult::rowCount);

        StepVerifier.create(allRows)
                .expectNext(0)
                .expectComplete()
                .verify();
    }

    @Test
    public void cancellationTest() {
        Mono<Long> insert = Yesql4jReactor.preparedQuery(
                pool, Schedulers.single(),
                "INSERT INTO test_table (value) VALUES (?)", Collections.emptyList(), Tuple.of("test1")
        ).map(res -> res.property(MySQLClient.LAST_INSERTED_ID));

        Mono<Long> longMono = Mono.delay(Duration.ofSeconds(30));

        Mono<Long> transactional = Yesql4jReactor.transactional(pool, Schedulers.single(), insert.then(longMono));

        StepVerifier.create(transactional)
                .expectSubscription()
                .expectNoEvent(Duration.ofSeconds(1))
                .thenCancel()
                .verify();

        Mono<Integer> allRows = Yesql4jReactor.preparedQuery(
                pool, Schedulers.single(),
                "SELECT * FROM test_table"
        ).map(SqlResult::rowCount);

        StepVerifier.create(allRows)
                .expectNext(0)
                .expectComplete()
                .verify();
    }
}
