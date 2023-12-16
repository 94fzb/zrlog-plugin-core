package com.zrlog.plugin.type;

/**
 * Created by xiaochun on 2016/2/12.
 */
public enum ActionType {

    INIT_CONNECT(0, "插件初始化"), HTTP_FILE(0, "获取HTTP资源文件"), HTTP_METHOD(0, "处理HTTP请求方法"),
    GET_WEBSITE(0, "website"), SET_WEBSITE(0, "website"),
    PLUGIN_START(0, "插件启动"), PLUGIN_STOP(0, "插件停止"), PLUGIN_INSTALL(0, "安装插件"), PLUGIN_UNINSTALL(0, "卸载启动"),
    SERVICE(1, "注册服务，调用其他服务"),
    ADD_COMMENT(1, "添加文章评论"), DELETE_COMMENT(1, "删除文章评论"),
    GET_DB_PROPERTIES(1, "读取程序数据库配置文件"),
    HTTP_ATTACHMENT_FILE(0, "响应HTTP附件"), LOAD_PUBLIC_INFO(0, "读取程序公开信息"),
    CURRENT_TEMPLATE(1, ""), BLOG_RUN_TIME(1, ""), CREATE_ARTICLE(1, "创建文章");

    private final int level;
    private final String desc;

    ActionType(int level, String desc) {
        this.level = level;
        this.desc = desc;
    }
}
