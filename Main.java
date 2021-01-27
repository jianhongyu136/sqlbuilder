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
        SqlBuilder select = new SqlBuilder()
                .select("*").from("user", "test")
                .where().between(map, true).end()
                .append("order by id").as("ttt");
        SqlBuilder sql = new SqlBuilder()
                .select().sub(select).end();

        System.out.println(sql);
        System.out.println(sql.params());

        SqlBuilder update = new SqlBuilder()
                .update("table")
                .lb().set("name", "jhy").rb()
                .set("sex", "男")
                .where().eq("id", 50).end();

        System.out.println(update);
        System.out.println(update.params());

        SqlBuilder delete = new SqlBuilder()
                .delete("area").where().eq("id", 12).end();

        System.out.println(delete);
        System.out.println(delete.params());

        SqlBuilder insert = new SqlBuilder()
                .insert("table")
                .add(map)
                .end();
        System.out.println(insert);
        System.out.println(insert.params());
    }
}
