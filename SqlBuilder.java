package com.cyitce.sqlbuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author jianhongyu
 * @version 1.0
 * @className SqlBuilder
 * @description SQL语句生成
 * @date 2021/1/22 10:36
 */
public class SqlBuilder {

    private final static String OR = " or ";
    private final static String AND = " and ";
    private final static String SP = " ";

    private final StringBuilder sql;
    private final List<Object> params;

    public SqlBuilder() {
        this(new StringBuilder());
    }

    public SqlBuilder(StringBuilder sql) {
        this.sql = sql;
        this.params = new ArrayList<>();
    }


    /**
     * select
     *
     * @param columns
     * @return
     */
    public SelectSql select(String... columns) {
        return new SelectSql(columns);
    }

    /**
     * from
     *
     * @param tables
     * @return
     */
    private SqlBuilder from(String... tables) {
        sql.append(" from ").append(concat(",", tables)).append(SP);
        return this;
    }

    /**
     * where
     * @return
     */
    public WhereSql where() {
        return new WhereSql();
    }

    /**
     * append
     * @param sql
     * @param params
     * @return
     */
    public SqlBuilder append(CharSequence sql, Object... params) {
        this.sql.append(SP).append(sql).append(SP);
        this.params.addAll(Arrays.asList(params));
        return this;
    }

    @Override
    public String toString() {
        return sql();
    }

    /**
     * 生成sql
     * @return
     */
    public String sql() {
        return sql.toString().trim().replaceAll("\\s+", " ");
    }

    /**
     * 获取参数
     * @return
     */
    public List<Object> params() {
        return params;
    }

    /**
     * 字符拼接
     *
     * @param s
     * @param strings
     * @return
     */
    private String concat(String s, String... strings) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            sb.append(strings[i]);
            if (i < strings.length - 1) {
                sb.append(s);
            }
        }
        sb.append(SP);
        return sb.toString();
    }

    /**
     * select 语句
     */
    public class SelectSql {
        public SelectSql(String... columns) {
            sql.append("select ").append(concat(",", columns));
        }

        public SqlBuilder from(String... tables) {
            return SqlBuilder.this.from(tables);
        }
    }

    /**
     * where语句
     */
    public class WhereSql {

        public WhereSql() {
            sql.append(" where ");
        }

        public WhereSql and() {
            sql.append(AND);
            return this;
        }

        public WhereSql or() {
            sql.append(OR);
            return this;
        }

        public WhereSql eq(String key, Object value) {
            return eq(key, value, "=");
        }

        public WhereSql eq(String key, Object value, String operator) {
            sql.append(key).append(operator).append("? ");
            params.add(value);
            return this;
        }

        public WhereSql eq(Map<String, Object> kv) {
            return eq(kv, "=", false);
        }

        public WhereSql eq(Map<String, Object> kv, String operator, boolean or) {
            if (kv.size() == 0) {
                return this;
            }
            sql.append("(");
            String[] keys = kv.keySet().toArray(new String[0]);
            for (int i = 0; i < keys.length; i++) {
                eq(keys[i], kv.get(keys[i]), operator);
                if (i < keys.length - 1) {
                    sql.append(or ? OR : AND);
                }
            }
            sql.append(")");
            return this;
        }

        public WhereSql like(String key, Object value) {
            sql.append(key).append(" like concat('%',?,'%') ");
            params.add(value);
            return this;
        }

        public WhereSql like(Map<String, Object> kv) {
            return like(kv, false);
        }

        public WhereSql like(Map<String, Object> kv, boolean or) {
            if (kv.size() == 0) {
                return this;
            }
            sql.append("(");
            String[] keys = kv.keySet().toArray(new String[0]);
            for (int i = 0; i < keys.length; i++) {
                like(keys[i], kv.get(keys[i]));
                if (i < keys.length - 1) {
                    sql.append(or ? OR : AND);
                }
            }
            sql.append(")");
            return this;
        }


        public WhereSql between(String key, Object value1, Object value2) {
            sql.append(key).append(" between ? and ? ");
            params.add(value1);
            params.add(value2);
            return this;
        }

        public WhereSql between(Map<String, Object> kv) {
            return between(kv, false);
        }

        public WhereSql between(Map<String, Object> kv, boolean or) {
            if (kv.size() == 0) {
                return this;
            }
            sql.append("(");
            String[] keys = kv.keySet().toArray(new String[0]);
            for (int i = 0; i < keys.length; i++) {
                Object v = kv.get(keys[i]);
                if (v instanceof List) {
                    List vl = ((List) v);
                    if (vl.size() >= 2) {
                        between(keys[i], vl.get(0), vl.get(1));
                        if (i < keys.length - 1) {
                            sql.append(or ? OR : AND);
                        }
                    }
                }
            }
            sql.append(")");
            return this;
        }


        public SqlBuilder end() {
            return SqlBuilder.this;
        }
    }
}
