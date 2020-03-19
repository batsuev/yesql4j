package com.yesql4j.reactor;

import io.vertx.mysqlclient.MySQLPool;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ConcurrentHashMap;

public final class Yesql4jSchedulers {

    private static ConcurrentHashMap<MySQLPool, Scheduler> schedulers = new ConcurrentHashMap<>();

    public static Scheduler scheduler(MySQLPool pool) {
        return schedulers.computeIfAbsent(pool, k -> Schedulers.boundedElastic());
    }
}
