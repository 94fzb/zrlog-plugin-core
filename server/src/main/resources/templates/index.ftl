<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8"/>
    <title> 后台管理 </title>
    <link href="assets/css/bootstrap.min.css" rel="stylesheet"/>
    <link rel="stylesheet" href="assets/css/font-awesome.min.css"/>

    <link rel="stylesheet" href="assets/css/ace.min.css"/>
    <link rel="stylesheet" href="assets/css/ace-rtl.min.css"/>
    <link rel="stylesheet" href="assets/css/ace-skins.min.css"/>
    <link rel="stylesheet" href="assets/css/jquery.gritter.css"/>

    <script src="assets/js/jquery-2.0.3.min.js"></script>
    <script src="assets/js/bootstrap.min.js"></script>
    <script src="assets/js/typeahead-bs2.min.js"></script>
    <script src="assets/js/ace-elements.min.js"></script>
    <script src="assets/js/ace.min.js"></script>
    <script src="assets/js/ace-extra.min.js"></script>

    <script src="assets/js/jquery.gritter.min.js"></script>
</head>

<body>
<div class="page-header">
    <h1>
        插件
        <small>
            <i class="icon-double-angle-right"></i>
            管理插件
        </small>
    </h1>
</div>
<!-- /.page-header -->

<div class="col-xs-12">

    <div class="table-responsive">
        <table class="table table-striped table-bordered table-hover" id="sample-table-1">
            <thead>
            <tr>
                <th class="hidden-480">名称</th>
                <th>作者</th>
                <th>简介</th>
                <th>版本</th>
                <th>查看</th>
                <th>安装/启动/停止/卸载</th>
            </tr>
            </thead>

            <tbody>
            <#list plugins as plugin>
                <tr>
                    <td>
                        ${plugin.name}
                    </td>
                    <td class="hidden-480">${plugin.author }</td>
                    <td>${plugin.desc }</td>
                    <td>${plugin.version }</td>
                    <td>
                        <div class="visible-md visible-lg hidden-sm hidden-xs btn-group">
                            <a href="${plugin.shortName}/">
                                <button class="btn btn-xs btn-success">
                                    <i class="icon-ok bigger-120"></i>
                                </button>
                            </a>

                        </div>
                    </td>
                    <td>
                        <div class="visible-md visible-lg hidden-sm hidden-xs btn-group">
                            <a href="${plugin.shortName}/install">
                                <button class="btn btn-xs btn-success">
                                    <i class="icon-ok bigger-120"></i>
                                </button>
                            </a>
                            <a href="${plugin.shortName}/start">
                                <button class="btn btn-xs btn-success">
                                    <i class="icon-ok bigger-120"></i>
                                </button>
                            </a>
                            <a href="${plugin.shortName}/stop">
                                <button class="btn btn-xs btn-success">
                                    <i class="icon-ok bigger-120"></i>
                                </button>
                            </a>
                            <a href="${plugin.shortName}/uninstall">
                                <button class="btn btn-xs btn-danger">
                                    <i class="icon-trash bigger-120"></i>
                                </button>
                            </a>

                        </div>
                    </td>
                </tr>
            </#list>
            </tbody>
        </table>
    </div>
    <!-- /.table-responsive -->
    <a href="plugin_center">
        <button class="btn btn-info">下载</button>
    </a>
</div>
</body>
</html>