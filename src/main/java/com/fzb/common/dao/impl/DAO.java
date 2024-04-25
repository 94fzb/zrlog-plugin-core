package com.fzb.common.dao.impl;

import com.fzb.common.dao.api.IDAO;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DAO implements IDAO {

    public static final String[] ALL = "*".split(" ");
    private static DataSource dataSource;
    protected String tableName;
    protected String pk;
    protected QueryRunner queryRunner;
    private Map<String, Object> attrs = new HashMap<String, Object>();

    public DAO() {
        queryRunner = new QueryRunner(dataSource, true);
    }

    public static void setDs(DataSource ds) {
        dataSource = ds;
    }

    public Map<String, Object> getAttrs() {
        return attrs;
    }

    public DAO setAttrs(Map<String, Object> attrs) {
        this.attrs = attrs;
        return this;
    }

    protected String getAttrsKey() {
        StringBuilder sb = new StringBuilder();
        for (String lable : attrs.keySet()) {
            sb.append(lable);
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    protected String appendParams() {
        StringBuilder sb = new StringBuilder();
        int length = attrs.size();
        for (int i = 0; i < length; i++) {
            sb.append("?,");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }


    protected Object[] getMapValus(Map<String, Object> map) {

        Object[] obj = new Object[map.size()];
        int i = 0;
        for (Object value : map.values()) {
            obj[i++] = value;
        }
        return obj;
    }

    protected Object[] getAttsValus(Map<String, Object> attrs, Map<String, Object> cond) {
        Object[] obj = new Object[attrs.size() + cond.size()];
        int i = 0;
        for (Object value : attrs.values()) {
            obj[i++] = value;
        }
        for (Object value : cond.values()) {
            obj[i++] = value;
        }
        return obj;
    }


    protected String condsMap2Str(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("1=1");
        for (Entry<String, Object> m : map.entrySet()) {
            sb.append(" and ").append(m.getKey()).append("=?");
        }
        return sb.toString();
    }

    protected String attrsMap2Str(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, Object> m : map.entrySet()) {
            sb.append(" " + m.getKey() + "=?,");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public DAO set(String attr, Object value) {
        attrs.put(attr, value);
        return this;
    }

    @Override
    public boolean save() throws SQLException {
        if (attrs.isEmpty()) {
            //抛出异常信息
            return false;
        }
        String sb = "insert into " +
                tableName +
                "(" +
                getAttrsKey() +
                ") values(" +
                appendParams() +
                ")";
        return queryRunner.update(sb, getMapValus(attrs)) > 0;
    }


    @Override
    public boolean update(Map<String, Object> conditions) throws SQLException {
        String sb = "update " +
                tableName +
                " set " +
                attrsMap2Str(attrs) +
                " where " +
                condsMap2Str(conditions);
        return queryRunner.update(sb, getAttsValus(attrs, conditions)) > 0;
    }


    @Override
    public boolean delete() throws SQLException {
        String sb = "delete from " +
                tableName +
                " where " +
                condsMap2Str(attrs);
        return queryRunner.update(sb, getMapValus(attrs)) > 0;
    }


    @Override
    public boolean deleteById(int id) throws SQLException {
        String sb = "delete from " +
                tableName +
                " where id=?";
        return queryRunner.update(sb, id) > 0;
    }


    @Override
    public Map<String, Object> queryFirst(String... columns) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        for (String str : columns) {
            sb.append(str + ",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(" from ");
        sb.append(tableName);
        Map<String, Object> map = null;
        if (!attrs.isEmpty()) {
            sb.append(" where ");
            sb.append(condsMap2Str(attrs));
            map = queryRunner.query(sb.toString(), new MapHandler(), getMapValus(attrs));
        } else {
            map = queryRunner.query(sb.toString(), new MapHandler());
        }
        return map;
    }

    @Override
    public Object queryFirst(String column) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        sb.append(column).append(",");
        sb.deleteCharAt(sb.length() - 1);
        sb.append(" from ");
        sb.append(tableName);
        Map<String, Object> map;
        if (!attrs.isEmpty()) {
            sb.append(" where ");
            sb.append(condsMap2Str(attrs));
            map = queryRunner.query(sb + " order by " + column, new MapHandler(), getMapValus(attrs));
        } else {
            map = queryRunner.query(sb + " order by " + column, new MapHandler());
        }
        if (map != null) {
            return map.get(column);
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> queryList(String... columns) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        for (String str : columns) {
            sb.append(str).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(" from ");
        sb.append(tableName);
        return execution(sb);
    }

    @Override
    public List<Map<String, Object>> queryPagination(int page, int rows, String... columns) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        for (String str : columns) {
            sb.append(str).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(" from ");
        sb.append(tableName);
        sb.append(" limit ");
        if (page < 1) {
            page = 1;
        }
        sb.append((page - 1) * rows).append(",");
        sb.append(rows);
        return execution(sb);
    }

    private List<Map<String, Object>> execution(StringBuilder sb) throws SQLException {
        List<Map<String, Object>> lmap = null;
        if (!attrs.isEmpty()) {
            sb.append(" where ");
            sb.append(condsMap2Str(attrs));
            lmap = queryRunner.query(sb.toString(), new MapListHandler(), getMapValus(attrs));
        } else {
            lmap = queryRunner.query(sb.toString(), new MapListHandler());
        }
        return lmap;
    }

    @Override
    public List<Map<String, Object>> queryPagination(String sql, int page,
                                                     int rows) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append(sql);
        sb.append(" limit ");
        if (page < 1) {
            page = 1;
        }
        sb.append((page - 1) * rows).append(",");
        sb.append(rows);
        return execution(sb);
    }

    @Override
    public boolean execute(String sql, Object... params) throws SQLException {
        return queryRunner.update(sql, params) > 0;
    }

    @Override
    public boolean execCellStateMent(String sql, Object... params) {
        return false;
    }

    @Override
    public List<Map<String, Object>> queryList(String sql,
                                               Object... params) throws SQLException {
        return queryRunner.query(sql, new MapListHandler(), params);
    }

    @Override
    public Object queryFirstObj(String sql, Object... params) throws SQLException {
        return queryRunner.query(sql, new ScalarHandler<>(1), params);
    }

    @Override
    public Map<String, Object> queryFirst(String sql, Object... params)
            throws SQLException {
        return queryRunner.query(sql, new MapHandler(), params);
    }

    @Override
    public Map<String, Object> loadById(Object id) {
        try {
            if (pk != null) {
                return set(pk, id).queryFirst(ALL);
            }
            return set("id", id).queryFirst(ALL);
        } catch (SQLException e) {

        }
        return null;
    }

}