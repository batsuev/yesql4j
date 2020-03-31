package com.yesql4j.reactor;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.SynchronousSink;
import reactor.core.scheduler.Scheduler;
import reactor.util.context.Context;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class Yesql4jReactor {

    private static Logger log = LoggerFactory.getLogger(Yesql4jReactor.class);

    public static Mono<RowSet<Row>> preparedQuery(MySQLPool pool, Scheduler scheduler, String query) {
        return Mono.subscriberContext().flatMap(
                (context) -> queryMono(context, pool, scheduler, query)
        );
    }

    public static Mono<RowSet<Row>> preparedQuery(MySQLPool pool, Scheduler scheduler, String query, List<Integer> paramsIndexes, Tuple params) {
        return Mono.subscriberContext().flatMap(
                (context) -> queryMono(context, pool, scheduler, query, paramsIndexes, params)
        );
    }

    public static Mono<RowSet<Row>> preparedBatch(MySQLPool pool, Scheduler scheduler, String query, List<Tuple> batch) {
        return Mono.subscriberContext().flatMap(
                (context) -> batchQueryMono(context, pool, scheduler, query, batch)
        );
    }

    public static <T> Mono<T> transactional(MySQLPool pool, Scheduler scheduler, Mono<T> mono) {
        return Mono.subscriberContext().flatMap(currentContext -> {
            if (currentContext.hasKey(Transaction.class)) {
                log.debug("already in transaction");

                return mono;
            } else {
                Mono<Transaction> tr = startTransaction(scheduler, pool);

                return Mono.usingWhen(
                        tr,
                        (Transaction transaction) -> mono.subscriberContext(ctx -> ctx.put(Transaction.class, transaction)),
                        (Transaction transaction) -> commitTransaction(scheduler, transaction),
                        (Transaction transaction, Throwable throwable) -> rollbackTransaction(scheduler, transaction, throwable),
                        (Transaction transaction) -> cancelTransaction(scheduler, transaction)
                );
            }
        });
    }

    public static void handleSingleRow(RowSet<Row> rows, SynchronousSink<Row> sink) {
        Row row = singleRow(rows);
        if (row != null) {
            sink.next(row);
        }
        sink.complete();
    }

    public static Row singleRow(RowSet<Row> rows) {
        if (rows.size() == 1) {
            return rows.iterator().next();
        }else {
            return null;
        }
    }

    private static Mono<Transaction> startTransaction(Scheduler scheduler, MySQLPool pool) {
        Mono<Transaction> transaction = Mono.create(sink -> {
            log.debug("starting transaction");

            pool.begin(transactionAsyncResult -> {
                if (transactionAsyncResult.succeeded()) {
                    sink.success(transactionAsyncResult.result());
                } else {
                    sink.error(transactionAsyncResult.cause());
                }
            });
        });
        return transaction.publishOn(scheduler);
    }

    private static Mono<Boolean> commitTransaction(Scheduler scheduler, Transaction transaction) {
        Mono<Boolean> commit = Mono.create(sink -> {
            log.debug("committing transaction");
            transaction.commit(voidAsyncResult -> {
                if (voidAsyncResult.succeeded()) {
                    sink.success(true);
                } else {
                    sink.error(voidAsyncResult.cause());
                }
            });
        });
        return commit.publishOn(scheduler);
    }

    private static Mono<Object> rollbackTransaction(Scheduler scheduler, Transaction transaction, Throwable throwable) {
        Mono<Object> res = Mono.create(sink -> {
            log.debug("rollback transaction");
            transaction.rollback(voidAsyncResult -> sink.error(throwable));
        });
        return res.publishOn(scheduler);
    }

    private static Mono<Object> cancelTransaction(Scheduler scheduler, Transaction transaction) {
        return rollbackTransaction(scheduler, transaction, new Exception("Transaction cancelled"));
    }

    private static SqlClient getCurrentTransaction(Context context, MySQLPool pool, String query) {
        if (context.hasKey(Transaction.class)) {
            log.debug("running '{}' in transaction", query);
        }else {
            log.debug("running '{}' on pool `{}`", query, pool);
        }
        return context.getOrDefault(Transaction.class, pool);
    }

    private static Mono<RowSet<Row>> queryMono(Context context, MySQLPool pool, Scheduler scheduler, String query) {
        Mono<RowSet<Row>> queryMono = Mono.create(sink -> {
            SqlClient executor = getCurrentTransaction(context, pool, query);
            SqlClient client = executor.preparedQuery(query, asyncResultMonoSinkAdapter(sink));
            sink.onCancel(client::close);
        });

        return queryMono.publishOn(scheduler);
    }

    private static Mono<RowSet<Row>> queryMono(Context context, MySQLPool pool, Scheduler scheduler, String query, List<Integer> paramsIndexes, Tuple params) {
        Mono<RowSet<Row>> queryMono = Mono.create(sink -> {
            SqlClient executor = getCurrentTransaction(context, pool, query);
            if (tupleHasList(params)) {
                String cleanQuery = addListParams(query, paramsIndexes, params);
                Tuple flattenParams = flattenTuple(params);
                SqlClient client = executor.preparedQuery(cleanQuery, flattenParams, asyncResultMonoSinkAdapter(sink));
                sink.onCancel(client::close);
            }else {
                SqlClient client = executor.preparedQuery(query, params, asyncResultMonoSinkAdapter(sink));
                sink.onCancel(client::close);
            }
        });
        return queryMono.publishOn(scheduler);
    }

    private static Mono<RowSet<Row>> batchQueryMono(Context context, MySQLPool pool, Scheduler scheduler, String query, List<Tuple> params) {
        Mono<RowSet<Row>> queryMono = Mono.create(sink -> {
            SqlClient executor = getCurrentTransaction(context, pool, query);
            SqlClient client = executor.preparedBatch(query, params, asyncResultMonoSinkAdapter(sink));
            sink.onCancel(client::close);
        });
        return queryMono.publishOn(scheduler);
    }

    private static Handler<AsyncResult<RowSet<Row>>> asyncResultMonoSinkAdapter(MonoSink<RowSet<Row>> sink) {
        return rowSetAsyncResult -> {
            if (rowSetAsyncResult.succeeded()) {
                log.debug("got result");
                sink.success(rowSetAsyncResult.result());
            } else {
                log.debug("got error");
                sink.error(rowSetAsyncResult.cause());
            }
        };
    }

    static String addListParams(String query, List<Integer> paramsIndexes, Tuple params) {
        StringBuilder updatedQuery = new StringBuilder(query);
        int offset = 0;
        for (int i = 0; i < params.size(); i++) {
            Integer index = paramsIndexes.get(i);
            if (params.getValue(i) instanceof Collection) {
                int size = ((Collection) params.getValue(i)).size();
                String updated = String.join(",", Collections.nCopies(size, "?"));
                updatedQuery.replace(index + offset, index + offset + 1, updated);
                offset += updated.length() - 1;
            }
        }
        return updatedQuery.toString();
    }

    static Tuple flattenTuple(Tuple params) {
        Tuple res = Tuple.tuple();
        for (int i = 0; i < params.size(); i++) {
            Object paramValue = params.getValue(i);
            if (paramValue instanceof Collection) {
                ((Collection) params.getValue(i)).forEach(res::addValue);
            }else {
                res.addValue(paramValue);
            }
        }
        return res;
    }

    static boolean tupleHasList(Tuple params) {
        for (int i = 0; i < params.size(); i++) {
            if (params.getValue(i) instanceof Collection) {
                return true;
            }
        }
        return false;
    }
}
