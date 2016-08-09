<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8"/>
    <link href="assets/css/bootstrap.min.css" rel="stylesheet"/>
    <link rel="stylesheet" href="assets/css/bootstrap.min.css"/>
    <link rel="stylesheet" href="assets/css/font-awesome.min.css"/>

    <link rel="stylesheet" href="assets/css/jquery.gritter.css"/>

    <script src="assets/js/jquery-2.0.3.min.js"></script>
    <script src="assets/js/jquery.gritter.min.js"></script>
    <script src="assets/js/set_update.js"></script>
</head>

<body style="background:#f7f7f7">
<div class="rows" style="padding:30px;">
    <div class="col-xs-12">
        <form role="form" class="form-horizontal" checkBox="autoDownloadLostFile" id="ajaxotherMsg">
            <div class="form-group">
                <label class="col-sm-3 control-label"><b>自动获取丢失的的插件</b></label>

                <div class="col-sm-9">
                    <span class="col-sm-2"> <label class="pull-right inline">
                        <input type="hidden" id="autoDownloadLostFile" value="off">
                        <input type="checkbox"
                        <#if setting.autoDownloadLostFile == true>checked="checked"</#if>
                        name="autoDownloadLostFile" class="ace ace-switch ace-switch-5"
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
        </form>
    </div>
</div>
<div style="display:none">
    <input id="gritter-success" checked="" type="checkbox"/>
</div>
</body>
</html>