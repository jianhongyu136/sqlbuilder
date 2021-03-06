package com.cyitce.sqlbuilder;

import java.util.*;

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

    public static boolean isBlank(CharSequence var0) {
        int var1;
        if (var0 != null && (var1 = var0.length()) != 0) {
            for (int var2 = 0; var2 < var1; ++var2) {
                if (!Character.isWhitespace(var0.charAt(var2))) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }

    public static boolean isNotBlank(CharSequence var0) {
        return !isBlank(var0);
    }

    private static <T> T[] fill(T[] a, T val) {
        for (int i = 0, len = a.length; i < len; i++) {
            a[i] = val;
        }
        return a;
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
     * append
     *
     * @param sql    sql语句
     * @param params 参数
     * @return SqlBuilder
     */
    public SqlBuilder append(CharSequence sql, Object... params) {
        this.sql.append(SP).append(sql).append(SP);
        //校验参数不为空
        if (params != null) {
            this.params.addAll(Arrays.asList(params));
        }
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
        this.sql.append(bracket ? " (" : SP).append(sqlBuilder.sql()).append(bracket ? ") " : SP);
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
        return sql.toString().trim().replaceAll("\\s+", SP);
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
    private String join(String s, String... strings) {
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
     * update语句
     *
     * @param table 表
     * @return UpdateSql
     */
    public UpdateSql update(String table) {
        return new UpdateSql(table);
    }

    /**
     * delete语句
     *
     * @param table 表
     * @return SqlBuilder
     */
    public DeleteSql delete(String table) {
        return new DeleteSql(table);
    }

    /**
     * insert语句
     *
     * @param table 表
     * @return InsertSql
     */
    public InsertSql insert(String table) {
        return new InsertSql(table);
    }

    /**
     * select 语句
     */
    public class SelectSql extends Common<SelectSql> {

        private boolean flag = false;

        public SelectSql(String... columns) {
            if (columns.length > 0) {
                flag = true;
            }
            sql.append("select ").append(join(",", columns));
        }

        /**
         * from
         *
         * @param tables 表
         * @return SqlBuilder
         */
        public SelectSql from(String... tables) {
            sql.append(" from ").append(join(",", tables)).append(SP);
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
            if (flag) {
                sql.append(", ");
            }
            sql.append(bracket ? "(" : "").append(subSelect.sql()).append(bracket ? ") " : "");
            params.addAll(subSelect.params());
            flag = true;
            return this;
        }

        /**
         * select语句结束
         *
         * @return SqlBuilder
         */
        public SqlBuilder end() {
            return SqlBuilder.this;
        }
    }

    /**
     * where语句
     */
    public class WhereSql extends Common<WhereSql> {

        private final StringBuilder sql;
        private final List<Object> params = new ArrayList<>();
        private int flag = 0;

        public WhereSql() {
            sql = new StringBuilder();
        }

        /**
         * and
         *
         * @return WhereSql
         */
        public WhereSql and() {
            if (flag > 0) {
                sql.append(AND);
            }
            return this;
        }

        /**
         * in
         *
         * @param key
         * @param subSql
         * @return
         */
        public WhereSql in(String key, SqlBuilder subSql) {
            if (subSql == null || isBlank(subSql.sql)) {
                back();
                return this;
            }
            sql.append(" (").append(key)
                    .append(" in (")
                    .append(subSql.sql())
                    .append(")) ");
            this.params.addAll(subSql.params());
            flag++;
            return this;
        }

        /**
         * in
         *
         * @param key
         * @param sub
         * @return
         */
        public WhereSql in(String key, CharSequence sub) {
            if (sub == null || isBlank(sub)) {
                back();
                return this;
            }
            sql.append(" (").append(key)
                    .append(" in (")
                    .append(sub)
                    .append(")) ");
            flag++;
            return this;
        }

        /**
         * in
         *
         * @param key
         * @param params
         * @return
         */
        public WhereSql in(String key, Object... params) {
            if (params == null || params.length == 0) {
                back();
                return this;
            }
            sql.append(" (").append(key)
                    .append(" in (")
                    .append(join(",", fill(new String[params.length], "?")))
                    .append(")) ");
            this.params.addAll(Arrays.asList(params));
            flag++;
            return this;
        }

        /**
         * or
         *
         * @return WhereSql
         */
        public WhereSql or() {
            if (flag > 0) {
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
            return eq(key, value, null);
        }


        public WhereSql eq(String key, Object value, String tableName) {
            return eq(key, value, "=", tableName);
        }

        /**
         * 比较
         *
         * @param key      键
         * @param value    值
         * @param operator 操作符
         * @return WhereSql
         */
        public WhereSql eq(String key, Object value, String operator, String tableName) {
            if (isBlank(operator)) {
                operator = "=";
            }
            if (isNotBlank(tableName)) {
                sql.append(tableName).append(".");
            }
            sql.append(key).append(operator).append("? ");
            this.params.add(value);
            flag++;
            return this;
        }

        /**
         * 比较
         *
         * @param kv 集合
         * @return WhereSql
         */
        public WhereSql eq(Map<String, Object> kv) {
            return eq(kv, null);
        }


        public WhereSql eq(Map<String, Object> kv, boolean or) {
            return eq(kv, "=", or, null);
        }

        public WhereSql eq(Map<String, Object> kv, String operator, String tableName) {
            return eq(kv, operator, false, tableName);
        }

        /**
         * 比较
         *
         * @param kv 集合
         * @return WhereSql
         */
        public WhereSql eq(Map<String, Object> kv, String tableName) {
            return eq(kv, "=", false, tableName);
        }

        /**
         * eq
         *
         * @param kv       集合
         * @param operator 操作符
         * @param or       是否为or连接
         * @return WhereSql
         */
        public WhereSql eq(Map<String, Object> kv, String operator, boolean or, String tableName) {
            if (kv == null || kv.size() == 0) {
                back();
                return this;
            }
            sql.append(" (");
            String[] keys = kv.keySet().toArray(new String[0]);
            for (int i = 0; i < keys.length; i++) {
                eq(keys[i], kv.get(keys[i]), operator, tableName);
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
            return like(key, value, null);
        }

        public WhereSql like(String key, Object value, String tableName) {
            //如果value为空则不匹配,并去除and等连词
            if (value == null) {
                back();
                return this;
            }
            if (isNotBlank(tableName)) {
                sql.append(tableName).append(".");
            }
            sql.append(key).append(" like concat('%',?,'%') ");
            this.params.add(value);
            flag++;
            return this;
        }

        /**
         * 字符串模糊匹配
         *
         * @param kv 集合
         * @return WhereSql
         */
        public WhereSql like(Map<String, Object> kv) {
            return like(kv, false, null);
        }

        /**
         * 字符串模糊匹配
         *
         * @param kv        集合
         * @param tableName 表名
         * @return WhereSql
         */
        public WhereSql like(Map<String, Object> kv, String tableName) {
            return like(kv, false, tableName);
        }

        /**
         * 字符串模糊匹配
         *
         * @param kv 集合
         * @param or 是否用or连接
         * @return WhereSql
         */
        public WhereSql like(Map<String, Object> kv, boolean or) {
            return like(kv, or, null);
        }

        /**
         * 字符串模糊匹配
         *
         * @param kv 集合
         * @param or 是否为or拼接
         * @return WhereSql
         */
        public WhereSql like(Map<String, Object> kv, boolean or, String tableName) {
            if (kv == null || kv.size() == 0) {
                back();
                return this;
            }
            sql.append(" (");
            String[] keys = kv.keySet().toArray(new String[0]);
            for (int i = 0; i < keys.length; i++) {
                like(keys[i], kv.get(keys[i]), tableName);
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
        public WhereSql between(String key, Object value1, Object value2, String tableName) {
            if (isBlank(key)) {
                return this;
            }
            if (isNotBlank(tableName)) {
                sql.append(tableName).append(".");
            }
            sql.append(key).append(" between ? and ? ");
            this.params.add(value1);
            this.params.add(value2);
            flag++;
            return this;
        }

        public WhereSql between(String key, Object... params) {
            if (params == null || params.length < 2) {
                back();
                return this;
            }
            return between(key, params[0], params[1], null);
        }

        /**
         * 区间
         *
         * @param kv String,Object[2]
         * @return WhereSql
         */
        public WhereSql between(Map<String, Object> kv) {
            return between(kv, false, null);
        }

        public WhereSql between(Map<String, Object> kv, String tableName) {
            return between(kv, false, tableName);
        }

        public WhereSql between(Map<String, Object> kv, boolean or) {
            return between(kv, or, null);
        }

        /**
         * 区间
         *
         * @param kv String,Object[2]
         * @param or 是否为or拼接
         * @return WhereSql
         */
        public WhereSql between(Map<String, Object> kv, boolean or, String tableName) {
            if (kv == null || kv.size() == 0) {
                back();
                return this;
            }
            sql.append(" (");
            String[] keys = kv.keySet().toArray(new String[0]);
            for (int i = 0; i < keys.length; i++) {
                Object v = kv.get(keys[i]);
                if (v instanceof List) {
                    List vl = ((List) v);
                    between(keys[i], vl.get(0), vl.get(1), tableName);
                    if (i < keys.length - 1) {
                        sql.append(or ? OR : AND);
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
            String whereSql = sql.toString().trim();
            if (isNotBlank(whereSql)) {
                SqlBuilder.this.sql.append(" where ").append(whereSql);
                SqlBuilder.this.params.addAll(params);
            }
            return SqlBuilder.this;
        }

        /**
         * 返回之前的状态，移除and,or,where
         */
        private void back() {
            if (flag > 0) {
                sql.deleteCharAt(sql.length() - 1).delete(sql.lastIndexOf(SP), sql.length());
            }
        }

        @Override
        public WhereSql append(SqlBuilder sqlBuilder) {
            sql.append(sqlBuilder);
            this.params.addAll(sqlBuilder.params);
            return this;
        }

        @Override
        public WhereSql append(CharSequence sql, Object... params) {
            this.sql.append(sql);
            this.params.addAll(Arrays.asList(params));
            return this;
        }

        @Override
        public WhereSql append(SqlBuilder sqlBuilder, boolean bracket) {
            sql.append(bracket ? "(" : "").append(sqlBuilder).append(bracket ? ")" : "");
            this.params.addAll(sqlBuilder.params);
            return this;
        }

        @Override
        public WhereSql lb() {
            sql.append("(");
            return this;
        }

        @Override
        public WhereSql rb() {
            sql.append(")");
            return this;
        }
    }

    /**
     * update语句
     */
    public class UpdateSql extends Common<UpdateSql> {

        private boolean flag = false;

        public UpdateSql(String table) {
            sql.append("update ").append(table).append(SP);
        }

        /**
         * set
         *
         * @param key   键
         * @param value 值
         * @return UpdateSql
         */
        public UpdateSql set(String key, Object value) {
            if (flag) {
                sql.append(", ");
            } else {
                sql.append(" set ");
            }
            sql.append(SP).append(key).append("=? ");
            params.add(value);
            flag = true;
            return this;
        }


        /**
         * set
         *
         * @param kv 集合
         * @return UpdateSql
         */
        public UpdateSql set(Map<String, Object> kv) {
            if (kv == null || kv.size() == 0) {
                return this;
            }
            String[] keys = kv.keySet().toArray(new String[0]);
            for (String key : keys) {
                set(key, kv.get(key));
            }
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
         * update语句结束
         *
         * @return SqlBuilder
         */
        public SqlBuilder end() {
            return SqlBuilder.this;
        }

    }

    /**
     * delete语句
     */
    public class DeleteSql extends Common<DeleteSql> {

        public DeleteSql(String table) {
            sql.append("delete from ").append(table).append(SP);
        }

        public WhereSql where() {
            return new WhereSql();
        }
    }

    /**
     * insert语句
     */
    public class InsertSql extends Common<InsertSql> {
        private final Map<String, Object> map;

        public InsertSql(String table) {
            map = new HashMap<>();
            sql.append("insert into ").append(table);
        }

        /**
         * add
         *
         * @param key   键
         * @param value 值
         * @return InsertSql
         */
        public InsertSql add(String key, Object value) {
            map.put(key, value);
            return this;
        }

        /**
         * add
         *
         * @param kv 集合
         * @return InsertSql
         */
        public InsertSql add(Map<String, Object> kv) {
            map.putAll(kv);
            return this;
        }

        /**
         * insert语句结束
         *
         * @return SqlBuilder
         */
        public SqlBuilder end() {
            String[] keys = map.keySet().toArray(new String[0]);
            sql.append("(");
            for (int i = 0; i < keys.length; i++) {
                sql.append(keys[i]);
                if (i < keys.length - 1) {
                    sql.append(",");
                }
                params.add(map.get(keys[i]));
            }
            sql.append(") values(");
            for (int i = 0; i < keys.length; i++) {
                sql.append("?");
                if (i < keys.length - 1) {
                    sql.append(",");
                }
            }
            sql.append(")");
            return SqlBuilder.this;
        }
    }


    private class Common<T extends Common> {

        /**
         * append
         *
         * @param sql    sql语句
         * @param params 参数
         * @return this
         */
        public T append(CharSequence sql, Object... params) {
            SqlBuilder.this.append(sql, params);
            return (T) this;
        }

        /**
         * append
         *
         * @param sqlBuilder SqlBuilder
         * @return this
         */
        public T append(SqlBuilder sqlBuilder) {
            return append(sqlBuilder, false);
        }

        /**
         * append
         *
         * @param sqlBuilder SqlBuilder
         * @param bracket    是否用括号包裹
         * @return this
         */
        public T append(SqlBuilder sqlBuilder, boolean bracket) {
            SqlBuilder.this.append(sqlBuilder, bracket);
            return (T) this;
        }


        /**
         * 左括号 (
         *
         * @return this
         */
        public T lb() {
            sql.append(" (");
            return (T) this;
        }

        /**
         * 右括号 )
         *
         * @return this
         */
        public T rb() {
            sql.append(") ");
            return (T) this;
        }
    }
}
