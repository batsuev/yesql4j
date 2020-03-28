package com.yesql4j.example.reactor;

import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import reactor.core.publisher.Mono;
import data.Queries;

public class App {

    private MySQLPool createPool() {
        var opts = new MySQLConnectOptions();
        opts.setHost("localhost");
        opts.setPort(3306);
        opts.setDatabase("saas_sessions");

        var poolOpts = new PoolOptions();
        poolOpts.setMaxSize(5);

        return MySQLPool.pool(opts, poolOpts);
    }

    public void example() {
        var pool = createPool();

        Queries.ddl(pool).subscribe(System.out::println);

        Mono<Integer> updatesCount = Queries.updateEntry(pool, "Test", 21L);
    }

    public static void main(String[] args) {
        new App().example();
        //pool.close();
    }
}
