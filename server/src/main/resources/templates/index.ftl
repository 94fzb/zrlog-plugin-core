<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8"/>
    <title> 插件管理 </title>
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
    <script src="js/plugin.js"></script>
</head>

<body>
<div class="main-container">
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

    <div class="tabbable">
        <ul id="plugintab" class="nav nav-tabs padding-12 tab-color-blue background-blue">
            <li class="active">
                <a href="#all" data-toggle="tab">所有</a>
            </li>
            <li class="">
                <a href="#used" data-toggle="tab">使用中</a>
            </li>
            <li class="">
                <a href="#unused" data-toggle="tab">未使用</a>
            </li>
        </ul>

        <div class="tab-content">
            <div class="tab-pane active" id="all">
                <div class="table-responsive">
                    <table class="table table-striped table-bordered table-hover" id="sample-table-1">
                        <thead>
                        <tr>
                            <th class="hidden-480">名称</th>
                            <th>作者</th>
                            <th>简介</th>
                            <th>版本</th>
                            <th>查看</th>
                            <th>设置/启动|停止/卸载</th>
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
                                    <div class="btn-group">
                                        <a href="${plugin.shortName}/">
                                            <button class="btn btn-xs btn-light">
                                                <i class="icon-zoom-in bigger-120"></i>
                                            </button>
                                        </a>

                                    </div>
                                </td>
                                <td>
                                    <div class="btn-group">
                                        <a href="${plugin.shortName}/install">
                                            <button class="btn btn-xs btn-gray">
                                                <i class="icon-cogs bigger-120"></i>
                                            </button>
                                        </a>
                                        <a href="${plugin.shortName}/start">
                                            <button class="btn btn-xs btn-primary">
                                                <i class="icon-play bigger-120"></i>
                                            </button>
                                        </a>
                                        <a>
                                            <button class="btn btn-xs btn-danger stop" name="${plugin.shortName}">
                                                <i class="icon-stop bigger-120"></i>
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
            </div>
            <div class="tab-pane" id="used">
                <div class="table-responsive">
                    <table class="table table-striped table-bordered table-hover" id="sample-table-1">
                        <thead>
                        <tr>
                            <th class="hidden-480">名称</th>
                            <th>作者</th>
                            <th>简介</th>
                            <th>版本</th>
                            <th>查看</th>
                            <th>停止/卸载</th>
                        </tr>
                        </thead>

                        <tbody>
                        <#list usedPlugins as plugin>
                            <tr>
                                <td>
                                    ${plugin.name}
                                </td>
                                <td class="hidden-480">${plugin.author }</td>
                                <td>${plugin.desc }</td>
                                <td>${plugin.version }</td>
                                <td>
                                    <div class="btn-group">
                                        <a href="${plugin.shortName}/">
                                            <button class="btn btn-xs btn-light">
                                                <i class="icon-zoom-in bigger-120"></i>
                                            </button>
                                        </a>

                                    </div>
                                </td>
                                <td>
                                    <div class="btn-group">
                                        <a>
                                            <button class="btn btn-xs btn-danger stop" name="${plugin.shortName}">
                                                <i class="icon-stop bigger-120"></i>
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
            </div>
            <div class="tab-pane" id="unused">
                <div class="table-responsive">
                    <table class="table table-striped table-bordered table-hover" id="sample-table-1">
                        <thead>
                        <tr>
                            <th class="hidden-480">名称</th>
                            <th>作者</th>
                            <th>简介</th>
                            <th>版本</th>
                            <th>设置/启动/卸载</th>
                        </tr>
                        </thead>

                        <tbody>
                        <#list unusedPlugins as plugin>
                            <tr>
                                <td>
                                    ${plugin.name}
                                </td>
                                <td class="hidden-480">${plugin.author }</td>
                                <td>${plugin.desc }</td>
                                <td>${plugin.version }</td>

                                <td>
                                    <div class="btn-group">
                                        <a href="${plugin.shortName}/install">
                                            <button class="btn btn-xs btn-gray">
                                                <i class="icon-cogs bigger-120"></i>
                                            </button>
                                        </a>
                                        <a href="${plugin.shortName}/start">
                                            <button class="btn btn-xs btn-primary">
                                                <i class="icon-play bigger-120"></i>
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
            </div>
        </div>
    </div>
    <!-- /.table-responsive -->
    <a href="center">
        <button class="btn btn-info"><i class="icon-download"></i>下载</button>
    </a>
</div>
<input id="gritter-light" checked="" type="checkbox" class="ace ace-switch ace-switch-5" />
</body>
</html>