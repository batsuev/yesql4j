package yesql4j.reactor;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.util.context.Context;

import java.util.List;

public final class Yesql4jReactor {

    private static Logger log = LoggerFactory.getLogger(Yesql4jReactor.class);

    public static Mono<RowSet<Row>> preparedQuery(MySQLPool pool, String query) {
        return Mono.subscriberContext().flatMap(
                (context) -> queryMono(context, pool, query).subscribeOn(Yesql4jSchedulers.scheduler(pool))
        );
    }

    public static Mono<RowSet<Row>> preparedQuery(MySQLPool pool, String query, Tuple params) {
        return Mono.subscriberContext().flatMap(
                (context) -> queryMono(context, pool, query, params).subscribeOn(Yesql4jSchedulers.scheduler(pool))
        );
    }

    public static Mono<RowSet<Row>> preparedBatch(MySQLPool pool, String query, List<Tuple> batch) {
        return Mono.subscriberContext().flatMap(
                (context) -> batchQueryMono(context, pool, query, batch).subscribeOn(Yesql4jSchedulers.scheduler(pool))
        );
    }

    public static <T> Mono<T> transactional(MySQLPool pool, Mono<T> mono) {
        return Mono.subscriberContext().flatMap(currentContext -> {
            if (currentContext.hasKey(Transaction.class)) {
                log.debug("already in transaction");

                return mono;
            } else {
                Mono<Transaction> tr = startTransaction(pool);

                return Mono.usingWhen(
                        tr,
                        (Transaction transaction) -> mono.subscriberContext(ctx -> ctx.put(Transaction.class, transaction)),
                        Yesql4jReactor::commitTransaction,
                        Yesql4jReactor::rollbackTransaction,
                        Yesql4jReactor::cancelTransaction
                );
            }
        });
    }

    private static Mono<Transaction> startTransaction(MySQLPool pool) {
        return Mono.create(sink -> {
            log.debug("starting transaction");

            pool.begin(transactionAsyncResult -> {
                if (transactionAsyncResult.succeeded()) {
                    sink.success(transactionAsyncResult.result());
                } else {
                    sink.error(transactionAsyncResult.cause());
                }
            });
        });
    }

    private static Mono<Void> commitTransaction(Transaction transaction) {
        return Mono.create(sink -> {
            log.debug("committing transaction");
            transaction.commit(voidAsyncResult -> {
                if (voidAsyncResult.succeeded()) {
                    sink.success();
                } else {
                    sink.error(voidAsyncResult.cause());
                }
            });
        });
    }

    private static Mono<Object> rollbackTransaction(Transaction transaction, Throwable throwable) {
        return Mono.create(sink -> {
            log.debug("rollback transaction");
            transaction.rollback(voidAsyncResult -> sink.error(throwable));
        });
    }

    private static Mono<Object> cancelTransaction(Transaction transaction) {
        return rollbackTransaction(transaction, new Exception("Transaction cancelled"));
    }

    private static SqlClient getCurrentTransaction(Context context, MySQLPool pool, String query) {
        if (context.hasKey(Transaction.class)) {
            log.debug("running '{}' in transaction", query);
        }else {
            log.debug("running '{}' on pool `{}`", query, pool);
        }
        return context.getOrDefault(Transaction.class, pool);
    }

    private static Mono<RowSet<Row>> queryMono(Context context, MySQLPool pool, String query) {
        return Mono.create(sink -> {
            SqlClient executor = getCurrentTransaction(context, pool, query);
            SqlClient client = executor.preparedQuery(query, asyncResultMonoSinkAdapter(sink));
            sink.onCancel(client::close);
        });
    }

    private static Mono<RowSet<Row>> queryMono(Context context, MySQLPool pool, String query, Tuple params) {
        return Mono.create(sink -> {
            SqlClient executor = getCurrentTransaction(context, pool, query);
            SqlClient client = executor.preparedQuery(query, params, asyncResultMonoSinkAdapter(sink));
            sink.onCancel(client::close);
        });
    }

    private static Mono<RowSet<Row>> batchQueryMono(Context context, MySQLPool pool, String query, List<Tuple> params) {
        return Mono.create(sink -> {
            SqlClient executor = getCurrentTransaction(context, pool, query);
            SqlClient client = executor.preparedBatch(query, params, asyncResultMonoSinkAdapter(sink));
            sink.onCancel(client::close);
        });
    }

    private static Handler<AsyncResult<RowSet<Row>>> asyncResultMonoSinkAdapter(MonoSink<RowSet<Row>> sink) {
        return rowSetAsyncResult -> {
            if (rowSetAsyncResult.succeeded()) {
                sink.success(rowSetAsyncResult.result());
            } else {
                sink.error(rowSetAsyncResult.cause());
            }
        };
    }
}
