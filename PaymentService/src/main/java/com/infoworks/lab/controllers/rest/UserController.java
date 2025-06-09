package com.infoworks.lab.controllers.rest;

import com.infoworks.lab.jsql.ExecutorType;
import com.infoworks.lab.jsql.JsqlConfig;
import com.infoworks.lab.rest.models.SearchQuery;
import com.it.soul.lab.sql.QueryExecutor;
import com.it.soul.lab.sql.SQLExecutor;
import com.it.soul.lab.sql.query.QueryType;
import com.it.soul.lab.sql.query.SQLQuery;
import com.it.soul.lab.sql.query.SQLSelectQuery;
import com.it.soul.lab.sql.query.models.Table;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/user/v1")
public class UserController {

    private String dbName;
    private JsqlConfig config;

    public UserController(@Qualifier("AppDBNameKey") String dbName, JsqlConfig config) {
        this.dbName = dbName;
        this.config = config;
    }

    @GetMapping @SuppressWarnings("Duplicates")
    ResponseEntity<List<Map>> read(@RequestParam(value = "limit", defaultValue = "10", required = false) Integer limit
            , @RequestParam(value = "page", defaultValue = "1", required = false) Integer page) {
        if (limit < 0) limit = 10;
        if (page <= 0) page = 1;
        int offset = (page - 1) * limit;
        //Do select:
        try (QueryExecutor executor = config.create(ExecutorType.SQL, dbName)) {
            SQLSelectQuery query = new SQLQuery.Builder(QueryType.SELECT)
                    .columns("id", "client_ref", "tenant_ref"
                            , "account_ref", "amount", "currency")
                    .from("account")
                    .addLimit(limit, offset).build();
            ResultSet set = ((SQLExecutor) executor).executeSelect(query);
            Table accountTbl = ((SQLExecutor) executor).collection(set);
            List<Map> transformed = accountTbl.getRows().stream()
                    .flatMap(row -> Stream.of(row.keyObjectMap()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(transformed);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(new ArrayList<>());
    }

    @PostMapping("/search") @SuppressWarnings("Duplicates")
    ResponseEntity<List<Map>> search(@RequestBody SearchQuery searchQuery) {
        int limit = searchQuery.getSize();
        int page = searchQuery.getPage();
        if (limit < 0) limit = 10;
        if (page <= 0) page = 1;
        int offset = (page - 1) * limit;
        //Do select:
        try (QueryExecutor executor = config.create(ExecutorType.SQL, dbName)) {
            SQLSelectQuery query = new SQLQuery.Builder(QueryType.SELECT)
                    .columns("id", "client_ref", "tenant_ref"
                            , "account_ref", "amount", "currency")
                    .from("account")
                    .where(searchQuery.getPredicate())
                    .addLimit(limit, offset).build();
            ResultSet set = ((SQLExecutor) executor).executeSelect(query);
            Table accountTbl = ((SQLExecutor) executor).collection(set);
            List<Map> transformed = accountTbl.getRows().stream()
                    .flatMap(row -> Stream.of(row.keyObjectMap()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(transformed);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(new ArrayList<>());
    }

}
