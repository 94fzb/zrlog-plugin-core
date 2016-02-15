<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getHeader("host")+path+"/";
request.setAttribute("url", request.getScheme()+"://"+request.getHeader("host")+request.getContextPath());
request.setAttribute("suburl", request.getRequestURL().substring(basePath.length()));
%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script src="js/set_update.js"></script>
<form role="form" id="ajaxduoshuo" checkBox="user_comment_plugin" class="form-horizontal">
	<div class="form-group">
		<label for="form-field-1"
			class="col-sm-3 control-label no-padding-right"> 多说短域名 </label>

		<div class="col-sm-9">
			<input type="text" name="duoshuo_short_name"
				value="${param.short_name}" class="col-xs-10 col-sm-5"
				placeholder="" id="form-field-1">
		</div>
	</div>

	<div class="form-group">
		<label for="form-field-1"
			class="col-sm-3 control-label no-padding-right"> 多说密钥 </label>

		<div class="col-sm-9">
			<input type="text" name="duoshuo_secret" value="${param.secret}"
				class="col-xs-10 col-sm-5" placeholder="" id="form-field-1">

		</div>
	</div>
	<div class="form-group">
		<label for="form-field-1"
			class="col-sm-3 control-label no-padding-right"> 是否启用 </label>
		<div class="col-sm-9">
			<input type="hidden" id="user_comment_plugin" value="off">
			<label> <input class="ace ace-switch ace-switch-6"
				type="checkbox" value="duoshuo" <c:if test="${webs.user_comment_plugin eq 'on'}">checked="checked"</c:if>
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

			&nbsp; &nbsp; &nbsp;
			<button type="reset" class="btn">
				<i class="icon-undo bigger-110"></i> 重置
			</button>
		</div>
	</div>
</form>