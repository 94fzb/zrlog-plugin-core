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
    <style type="text/css">#plugin {max-width: 256px;max-height: 256px;background-image:
        url(data:image/svg+xml;utf8;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iaXNvLTg4NTktMSI/Pgo8IS0tIEdlbmVyYXRvcjogQWRvYmUgSWxsdXN0cmF0b3IgMTYuMC4wLCBTVkcgRXhwb3J0IFBsdWctSW4gLiBTVkcgVmVyc2lvbjogNi4wMCBCdWlsZCAwKSAgLS0+CjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+CjxzdmcgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgdmVyc2lvbj0iMS4xIiBpZD0iQ2FwYV8xIiB4PSIwcHgiIHk9IjBweCIgd2lkdGg9IjI0cHgiIGhlaWdodD0iMjRweCIgdmlld0JveD0iMCAwIDUzNS41IDUzNS41IiBzdHlsZT0iZW5hYmxlLWJhY2tncm91bmQ6bmV3IDAgMCA1MzUuNSA1MzUuNTsiIHhtbDpzcGFjZT0icHJlc2VydmUiPgo8Zz4KCTxnIGlkPSJleHRlbnNpb24iPgoJCTxwYXRoIGQ9Ik00NzEuNzUsMjU1SDQzMy41VjE1M2MwLTI4LjA1LTIyLjk1LTUxLTUxLTUxaC0xMDJWNjMuNzVDMjgwLjUsMjguMDUsMjUyLjQ1LDAsMjE2Ljc1LDBTMTUzLDI4LjA1LDE1Myw2My43NVYxMDJINTEgICAgYy0yOC4wNSwwLTUxLDIyLjk1LTUxLDUxdjk2LjloMzguMjVjMzguMjUsMCw2OC44NSwzMC42LDY4Ljg1LDY4Ljg1Uzc2LjUsMzg3LjYsMzguMjUsMzg3LjZIMHY5Ni45YzAsMjguMDUsMjIuOTUsNTEsNTEsNTFoOTYuOSAgICB2LTM4LjI1YzAtMzguMjUsMzAuNi02OC44NSw2OC44NS02OC44NXM2OC44NSwzMC42LDY4Ljg1LDY4Ljg1djM4LjI1aDk2LjljMjguMDUsMCw1MS0yMi45NSw1MS01MXYtMTAyaDM4LjI1ICAgIGMzNS43LDAsNjMuNzUtMjguMDUsNjMuNzUtNjMuNzVTNTA3LjQ1LDI1NSw0NzEuNzUsMjU1eiIgZmlsbD0iIzAwMDAwMCIvPgoJPC9nPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+Cjwvc3ZnPgo=)}
    </style>
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
                                    <button class="btn btn-xs disabled">
                                        <i class="icon-zoom-in bigger-120"></i>
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
                                    <button class="btn btn-xs btn-primary">
                                        <i class="icon-zoom-in bigger-120"></i>
                                    </button>
                                </a>
                                <a href="${plugin.shortName}/stop">
                                    <button class="btn btn-xs btn-danger">
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
        <!-- /.table-responsive -->
        <a href="plugin_center">
            <button class="btn btn-info"><i class="icon-download"></i>下载</button>
        </a>
    </div>
</div>
</body>
</html>