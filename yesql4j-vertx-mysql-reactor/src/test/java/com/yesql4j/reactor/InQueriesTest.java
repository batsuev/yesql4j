package com.yesql4j.reactor;

import io.vertx.sqlclient.Tuple;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InQueriesTest {

    @Test
    void listParamsOk() {
        String query = "SELECT * FROM users WHERE a = ? AND id IN (?) AND name IN (?);";
        Tuple params = Tuple.of("t1", Arrays.asList(1,2,3), Arrays.asList("test1", "test2"));
        List<Integer> paramsIndexes = Arrays.asList(30, 43, 59);

        assertTrue(Yesql4jReactor.tupleHasList(params));
        assertEquals(
                "SELECT * FROM users WHERE a = ? AND id IN (?,?,?) AND name IN (?,?);",
                Yesql4jReactor.addListParams(query, paramsIndexes, params)
        );
        assertEquals(
                Tuple.of("t1", 1, 2, 3, "test1", "test2"),
                Yesql4jReactor.flattenTuple(params)
        );
    }
}
