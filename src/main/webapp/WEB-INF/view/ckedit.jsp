<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%-- /shop1/src/main/webapp/WEB-INF/view/ckedit.jsp--%>
<script type="text/javascript">
	window.parent.CKEDITOR.tools.callFunction
		(${param.CKEditorFuncNum},'${fileName}','이미지등록')
</script>
