<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getHeader("host")+path+"/";
request.setAttribute("url", request.getScheme()+"://"+request.getHeader("host")+request.getContextPath());
request.setAttribute("suburl", request.getRequestURL().substring(basePath.length()));
%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script src="${url}/admin/js/set_update.js"></script>
<div class="page-header">
	<h1>
		多说设置 <small> <i class="icon-double-angle-right"></i> 信息设置
		</small>
	</h1>
</div>
<div class="tabbable tabs-left">
	<ul class="nav nav-tabs">
		<li class="active"><a href="#duoshuoBind" data-toggle="tab">绑定</a></li>
		<li><a href="#input" data-toggle="tab">手动添加</a></li>
	</ul>

	<div class="tab-content">
		<div class="tab-pane in active" id="duoshuoBind" style="padding: 10px">
			<iframe
				src="http://duoshuo.com/connect-site/?system=zrlog&callback=${url}/admin/black?include=plugins/duoshuo/callback&user_key=${session.user.userId}&user_name=${session.user.userName}"
				scrolling="no" style="border: 0px;" width="100%" height="600px">
			</iframe>
		</div>

		<div class="tab-pane" id="input" style="padding: 10px">
			<h4 class="header blue">输入</h4>
			<form role="form" id="ajaxduoshuo" checkBox="user_comment_plugin"
				class="form-horizontal">
				<input type="hidden" id="user_comment_plugin" value="off">
				<div class="form-group">
					<label for="form-field-1"
						class="col-sm-3 control-label no-padding-right"> 多说短域名 </label>

					<div class="col-sm-9">
						<input type="text" name="duoshuo_short_name"
							value="${webs.duoshuo_short_name}" class="col-xs-10 col-sm-5"
							placeholder="" id="form-field-1">
					</div>
				</div>

				<div class="form-group">
					<label for="form-field-1"
						class="col-sm-3 control-label no-padding-right"> 多说密钥 </label>

					<div class="col-sm-9">
						<input type="text" name="duoshuo_secret"
							value="${webs.duoshuo_secret}" class="col-xs-10 col-sm-5"
							placeholder="" id="form-field-1">

					</div>
				</div>
				<div class="form-group">
					<label for="form-field-1"
						class="col-sm-3 control-label no-padding-right"> 是否启用 </label>
					<div class="col-sm-9">
						<label> <input class="ace ace-switch ace-switch-6"
							type="checkbox"
							<c:if test="${webs.user_comment_plugin eq 'on'}">checked="checked"</c:if>
							name="user_comment_plugin"> <span class="lbl">&nbsp;</span>
						</label>
					</div>
				</div>

				<div class="space-4"></div>
				<div class="clearfix form-actions">
					<div class="col-md-offset-3 col-md-9">
						<button id="duoshuo" type="button" class="btn btn-info">
							<i class="icon-ok bigger-110"></i> 提交
						</button>
					</div>
				</div>

			</form>
		</div>
	</div>
</div>