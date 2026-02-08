package com.infoworks.lab.controllers.rest;

import com.infoworks.sql.executor.QueryExecutor;
import com.infoworks.orm.Table;
import com.infoworks.sql.executor.SQLExecutor;
import com.infoworks.sql.query.QueryType;
import com.infoworks.sql.query.SQLQuery;
import com.infoworks.sql.query.SQLSelectQuery;
import com.infoworks.sql.query.pagination.SearchQuery;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/user/v1")
public class UserController {

    /**
     * Example of inject @Scope beans.
     * e.g. @RequestScope bean SQLExecutor to do JDBC-Calls to database.
     */
    @Resource(name = "executor")
    private QueryExecutor executor;

    private String dbName;

    public UserController(@Qualifier("AppDBNameKey") String dbName) {
        this.dbName = dbName;
    }

    @GetMapping @SuppressWarnings("Duplicates")
    ResponseEntity<List<Map>> read(@RequestParam(value = "limit", defaultValue = "10", required = false) Integer limit
            , @RequestParam(value = "page", defaultValue = "1", required = false) Integer page) {
        if (limit < 0) limit = 10;
        if (page <= 0) page = 1;
        int offset = (page - 1) * limit;
        //Do select:
        try {
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
        try {
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
