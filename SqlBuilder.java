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
     * @param columns 字段
     * @return SelectSql
     */
    public SelectSql select(String... columns) {
        return new SelectSql(columns);
    }

    /**
     * from
     *
     * @param tables 表
     * @return SqlBuilder
     */
    private SqlBuilder from(String... tables) {
        sql.append(" from ").append(concat(",", tables)).append(SP);
        return this;
    }

    /**
     * where
     *
     * @return WhereSql
     */
    public WhereSql where() {
        return new WhereSql();
    }

    /**
     * append
     *
     * @param sql    sql语句
     * @param params 参数
     * @return SqlBuilder
     */
    public SqlBuilder append(CharSequence sql, Object... params) {
        this.sql.append(SP).append(sql).append(SP);
        this.params.addAll(Arrays.asList(params));
        return this;
    }

    /**
     * append
     *
     * @param sqlBuilder SqlBuilder
     * @return SqlBuilder
     */
    public SqlBuilder append(SqlBuilder sqlBuilder) {
        return append(sqlBuilder, false);
    }

    /**
     * append
     *
     * @param sqlBuilder SqlBuilder
     * @param bracket    是否用括号包裹
     * @return SqlBuilder
     */
    public SqlBuilder append(SqlBuilder sqlBuilder, boolean bracket) {
        this.sql.append(bracket ? " (" : "").append(sqlBuilder.sql()).append(bracket ? ") " : "");
        this.params.addAll(sqlBuilder.params());
        return this;
    }

    @Override
    public String toString() {
        return sql();
    }

    /**
     * 生成sql
     *
     * @return sql语句
     */
    public String sql() {
        return sql.toString().trim().replaceAll("\\s+", " ");
    }

    /**
     * 获取参数
     *
     * @return 参数列表
     */
    public List<Object> params() {
        return params;
    }

    /**
     * 字符拼接
     *
     * @param s       用于拼接的字符
     * @param strings 字符列表
     * @return 拼接后的字符串
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
     * 别名
     *
     * @param name 别名
     * @return SqlBuilder
     */
    public SqlBuilder as(String name) {
        sql.insert(0, "(").append(") as ").append(name);
        return this;
    }

    /**
     * select 语句
     */
    public class SelectSql {
        public SelectSql(String... columns) {
            sql.append("select ").append(concat(",", columns));
        }

        /**
         * from
         *
         * @param tables 表
         * @return SqlBuilder
         */
        public SqlBuilder from(String... tables) {
            return SqlBuilder.this.from(tables);
        }

        /**
         * 子查询
         *
         * @param subSelect 子SQL
         * @return SelectSql
         */
        public SelectSql sub(SqlBuilder subSelect) {
            return sub(subSelect, false);
        }

        /**
         * 子查询
         *
         * @param subSelect 子SQL
         * @param bracket   是否用括号包裹
         * @return SelectSql
         */
        public SelectSql sub(SqlBuilder subSelect, boolean bracket) {
            sql.append(bracket ? " (" : "").append(subSelect.sql()).append(bracket ? ") " : "");
            params.addAll(subSelect.params());
            return this;
        }

        /**
         * select语句结束
         * @return SqlBuilder
         */
        public SqlBuilder end(){
            return SqlBuilder.this;
        }
    }

    /**
     * where语句
     */
    public class WhereSql {

        private boolean flag = false;

        public WhereSql() {
            sql.append(" where ");
        }

        /**
         * and
         *
         * @return WhereSql
         */
        public WhereSql and() {
            if (flag) {
                sql.append(AND);
            }
            return this;
        }

        /**
         * or
         *
         * @return WhereSql
         */
        public WhereSql or() {
            if (flag) {
                sql.append(OR);
            }
            return this;
        }

        /**
         * 比较
         *
         * @param key   键
         * @param value 值
         * @return WhereSql
         */
        public WhereSql eq(String key, Object value) {
            return eq(key, value, "=");
        }

        /**
         * 比较
         *
         * @param key      键
         * @param value    值
         * @param operator 操作符
         * @return WhereSql
         */
        public WhereSql eq(String key, Object value, String operator) {
            sql.append(key).append(operator).append("? ");
            params.add(value);
            flag = true;
            return this;
        }

        /**
         * 比较
         *
         * @param kv 集合
         * @return WhereSql
         */
        public WhereSql eq(Map<String, Object> kv) {
            return eq(kv, "=", false);
        }

        /**
         * eq
         *
         * @param kv       集合
         * @param operator 操作符
         * @param or       是否为or连接
         * @return WhereSql
         */
        public WhereSql eq(Map<String, Object> kv, String operator, boolean or) {
            if (kv.size() == 0) {
                return this;
            }
            sql.append(" (");
            String[] keys = kv.keySet().toArray(new String[0]);
            for (int i = 0; i < keys.length; i++) {
                eq(keys[i], kv.get(keys[i]), operator);
                if (i < keys.length - 1) {
                    sql.append(or ? OR : AND);
                }
            }
            sql.append(") ");
            return this;
        }

        /**
         * 字符串模糊匹配
         *
         * @param key   键
         * @param value 值
         * @return WhereSql
         */
        public WhereSql like(String key, Object value) {
            sql.append(key).append(" like concat('%',?,'%') ");
            params.add(value);
            flag = true;
            return this;
        }

        /**
         * 字符串模糊匹配
         *
         * @param kv 集合
         * @return WhereSql
         */
        public WhereSql like(Map<String, Object> kv) {
            return like(kv, false);
        }

        /**
         * 字符串模糊匹配
         *
         * @param kv 集合
         * @param or 是否为or拼接
         * @return WhereSql
         */
        public WhereSql like(Map<String, Object> kv, boolean or) {
            if (kv.size() == 0) {
                return this;
            }
            sql.append(" (");
            String[] keys = kv.keySet().toArray(new String[0]);
            for (int i = 0; i < keys.length; i++) {
                like(keys[i], kv.get(keys[i]));
                if (i < keys.length - 1) {
                    sql.append(or ? OR : AND);
                }
            }
            sql.append(") ");
            return this;
        }


        /**
         * 区间
         *
         * @param key    键
         * @param value1 区间1
         * @param value2 区间2
         * @return WhereSql
         */
        public WhereSql between(String key, Object value1, Object value2) {
            sql.append(key).append(" between ? and ? ");
            params.add(value1);
            params.add(value2);
            flag = true;
            return this;
        }

        /**
         * 区间
         *
         * @param kv String,Object[2]
         * @return WhereSql
         */
        public WhereSql between(Map<String, Object> kv) {
            return between(kv, false);
        }

        /**
         * 区间
         *
         * @param kv String,Object[2]
         * @param or 是否为or拼接
         * @return WhereSql
         */
        public WhereSql between(Map<String, Object> kv, boolean or) {
            if (kv.size() == 0) {
                return this;
            }
            sql.append(" (");
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
            sql.append(") ");
            return this;
        }

        /**
         * where语句结束
         *
         * @return SqlBuilder
         */
        public SqlBuilder end() {
            return SqlBuilder.this;
        }
    }
}
