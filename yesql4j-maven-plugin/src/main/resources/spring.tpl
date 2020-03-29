{{#if packageName}}
package {{packageName}};
{{/if}}

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Arrays;
import com.yesql4j.spring.InParameters;

@Component
public final class {{className}} {

    private final JdbcTemplate jdbcTemplate;

    public {{className}}(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
{{#each selects}}{{#if hasParams}}
    @NonNull
    public <T> List<T> {{name}}({{paramsSignature}}, RowMapper<T> rowMapper) {
        List<Object> params = Arrays.asList({{paramsBindings}});
        if (InParameters.hasListParam(params)) {
            String updatedQuery = InParameters.addListParams("{{query}}", Arrays.asList({{paramsIndexes}}), params);
            List<Object> updatedParams = InParameters.flattenParams(params);
            return jdbcTemplate.query(updatedQuery, updatedParams.toArray(), rowMapper);
        }else {
            return jdbcTemplate.query("{{query}}", params.toArray(), rowMapper);
        }
    }{{else}}
    @NonNull
    public <T> Lst<T> {{name}}(RowMapper<T> rowMapper) {
        return jdbcTemplate.query(pool, "{{query}}", rowMapper);
    }{{/if}}
{{/each}}
{{#each updates}}{{#if hasParams}}
    @NonNull
    public int {{name}}({{paramsSignature}}) {
        List<Object> params = Arrays.asList({{paramsBindings}});
        if (InParameters.hasListParam(params)) {
            String updatedQuery = InParameters.addListParams("{{query}}", Arrays.asList({{paramsIndexes}}), params);
            List<Object> updatedParams = InParameters.flattenParams(params);
            return jdbcTemplate.update(updatedQuery, updatedParams.toArray());
        }else {
            return jdbcTemplate.update("{{query}}", {{paramsBindings}});
        }
    }{{/if}}
{{/each}}{{#each inserts}}{{#if hasParams}}
    @NonNull
    public Long {{name}}({{paramsSignature}}) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("{{query}}");
            {{psParamsBindings}}
            return ps;
        }, keyHolder);
        return (long) keyHolder.getKey();
    }{{else}}
    public Long {{name}}({{paramsSignature}}) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> connection.prepareStatement("{{query}}"), keyHolder);
        return (long) keyHolder.getKey();
    }{{/if}}
{{/each}}
}