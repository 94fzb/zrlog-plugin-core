package com.fzb.zrlog.plugin.server.dao;

import com.fzb.common.dao.impl.DAO;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiaochun on 2016/2/14.
 */
public class WebSiteDAO extends DAO {
    public WebSiteDAO() {
        this.tableName = "website";
    }

    public boolean saveOrUpdate(String key, Object value) throws SQLException {
        Object object = new WebSiteDAO().set("name", key).queryFirst("value");
        if (object != null) {
            Map<String, Object> cond = new HashMap<>();
            cond.put("name", key);
            return new WebSiteDAO().set("value", value).update(cond);
        } else {
            return new WebSiteDAO().set("name", key).set("value", value).save();
        }
    }
}
