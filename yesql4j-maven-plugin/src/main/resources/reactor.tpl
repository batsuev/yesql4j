{{#if packageName}}
package {{packageName}};
{{/if}}

import reactor.core.publisher.Mono;
import com.yesql4j.reactor.Yesql4jReactor;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class {{className}} {
{{#each selects}}{{#if hasParams}}
    @NotNull
    public static Mono<RowSet<Row>> {{name}}(MySQLPool pool, {{paramsSignature}}) {
        return Yesql4jReactor.preparedQuery(pool, "{{query}}", Arrays.asList({{paramsIndexes}}), Tuple.wrap({{paramsBindings}}));
    }{{else}}
    @NotNull
    public static Mono<RowSet<Row>> {{name}}(MySQLPool pool) {
        return Yesql4jReactor.preparedQuery(pool, "{{query}}");
    }{{/if}}
{{/each}}
{{#each updates}}{{#if hasParams}}
    @NotNull
    public static Mono<Integer> {{name}}(MySQLPool pool, {{paramsSignature}}) {
        return Yesql4jReactor.preparedQuery(pool, "{{query}}", Arrays.asList({{paramsIndexes}}), Tuple.wrap({{paramsBindings}})).map(SqlResult::rowCount);
    }

    @NotNull
    public static Mono<Integer> {{name}}(MySQLPool pool, List<Map<String, Object>> params) {
        List<Tuple> wrappedParams = params.stream()
            .map(row -> Tuple.wrap(row))
            .collect(Collectors.toList());
        return Yesql4jReactor.preparedBatch(pool, "{{query}}", wrappedParams).map(SqlResult::rowCount);
    }{{else}}
    @NotNull
    public static Mono<Integer> {{name}}(MySQLPool pool) {
        return Yesql4jReactor.preparedQuery(pool, "{{query}}").map(SqlResult::rowCount);
    }{{/if}}
{{/each}}{{#each inserts}}{{#if hasParams}}
    @NotNull
    public static Mono<Long> {{name}}(MySQLPool pool, {{paramsSignature}}) {
        return Yesql4jReactor.preparedQuery(pool, "{{query}}", Arrays.asList({{paramsIndexes}}), Tuple.wrap({{paramsBindings}}))
            .map(res -> res.property(MySQLClient.LAST_INSERTED_ID));
    }{{else}}

    @NotNull
    public static Mono<Long> {{name}}(MySQLPool pool) {
        return Yesql4jReactor.preparedQuery(pool, "{{query}}")
            .map(res -> res.property(MySQLClient.LAST_INSERTED_ID));
    }{{/if}}
{{/each}}
}