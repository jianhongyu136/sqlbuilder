package com.cyitce.sqlbuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jianhongyu
 * @version 1.0
 * @className Main
 * @description
 * @date 2021/1/22 14:27
 */
public class Main {
    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", Arrays.asList("abc", "def"));
        map.put("count", Arrays.asList(123, 565));
        SqlBuilder sqlBuilder = new SqlBuilder()
                .select("*").from("user", "test")
                .where().between(map, true).end()
                .append("order by id").as("ttt");
        SqlBuilder sql = new SqlBuilder()
                .select().sub(sqlBuilder).end();
        System.out.println(sql);
        System.out.println(sql.params());
    }
}
