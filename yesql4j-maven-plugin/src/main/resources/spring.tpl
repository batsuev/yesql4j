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
import com.yesql4j.spring.InParameters;

@Component
public final class {{className}} {

    private final JdbcTemplate jdbcTemplate;

    public {{className}}(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
{{#each selects}}{{#if hasParams}}
    @NotNull
    public <T> List<T> {{name}}({{paramsSignature}}) {
        return jdbcTemplate.query("{{query}}", new Object[]{ {{paramsBindings}} });
    }{{else}}
    @NotNull
    public <T> Lst<T> {{name}}() {
        return jdbcTemplate.query(pool, "{{query}}");
    }{{/if}}
{{/each}}
{{#each updates}}{{#if hasParams}}
    @NotNull
    public int {{name}}({{paramsSignature}}) {
        return jdbcTemplate.update("{{query}}", {{paramsBindings}});
    }{{/if}}
{{/each}}{{#each inserts}}{{#if hasParams}}
    @NotNull
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