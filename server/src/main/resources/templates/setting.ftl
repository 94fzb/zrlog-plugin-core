<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8"/>
    <link href="assets/css/bootstrap.min.css" rel="stylesheet"/>
    <link rel="stylesheet" href="assets/css/bootstrap.min.css"/>
    <link rel="stylesheet" href="assets/css/font-awesome.min.css"/>

    <link rel="stylesheet" href="assets/css/ace.min.css"/>
    <link rel="stylesheet" href="assets/css/ace-rtl.min.css"/>
    <link rel="stylesheet" href="assets/css/ace-skins.min.css"/>
    <link rel="stylesheet" href="assets/css/jquery.gritter.css"/>
    <style>
        .rows {
        margin-left: -12px;
        margin-top: 20px;
        }
    </style>

    <script src="assets/js/jquery-2.0.3.min.js"></script>
    <script src="assets/js/jquery.gritter.min.js"></script>
    <script src="assets/js/set_update.js"></script>
</head>

<body>
<div class="main-container">
    <div class="rows">
        <div class="col-xs-12">
            <form role="form" class="form-horizontal" checkBox="autoDownloadLostFile" id="ajaxotherMsg">
                <div class="form-group">
                    <label class="col-sm-3 control-label"><b>自动获取丢失的的插件</b></label>
                    <div class="col-sm-9">
                    <span class="col-sm-2"> <label class="pull-right inline">
                        <input type="hidden" id="autoDownloadLostFile" value="off">
                        <input type="checkbox" <#if setting.autoDownloadLostFile == true>checked="checked"</#if> name="autoDownloadLostFile" class="ace ace-switch ace-switch-5"
                               id="gritter-light"> <span class="lbl"></span>
                    </label>
                    </span>
                    </div>
                </div>
                <div class="clearfix form-actions">
                    <div class="col-md-offset-6">
                        <button id="otherMsg" type="button" class="btn btn-info">
                            <i class="icon-ok bigger-110"></i> 提交
                        </button>
                    </div>
                </div>
                <input id="gritter-success" checked="" type="checkbox" class="ace ace-switch ace-switch-5"/>
            </form>
        </div>
    </div>
</div>
</body>
</html>