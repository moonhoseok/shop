<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- /shop1/src/main/webapp/WEB-INF/view/chat/chat.jsp -->
<%@ include file="/WEB-INF/view/jspHeader.jsp" %>
<!-- 포트번호 8080 -->
<c:set var="port" value="${pageContext.request.localPort}"/>
<!-- IP주소 : localhost -->
<c:set var="server" value="${pageContext.request.serverName}"/>
<!--  -->
<c:set var="path" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>web socket client</title>
<script type="text/javascript">
	$(function(){ // 첫문장이 실행하자마자 
		let ws = new WebSocket("ws://${server}:${port}${path}/chatting")
		ws.onopen = function(){ // 서버접속 완료
			$("#chatStatus").text("info:connection opened")
			$("input[name=chatInput]").on("keydown",function(evt){
				if(evt.keyCode == 13){ // enter key
					let msg = $("input[name=chatInput]").val()
					ws.send(msg) //서버로 데이터 전송
					$("input[name=chatInput]").val("")
				}
			})
		}
		// 서버로부터 메세지를 수신한 경우 
		ws.onmessage = function(event){
			//event.data : 수신된 메세지 정보
			//prepend() : 앞쪽에 추가
			//ap pend() : 뒤쪽에 추가
			$("textarea").eq(0).prepend(event.data+"\n")
		}
		// 서버연결 해제 
		ws.onclose = function(event){
			$("#chatStatus").text("info:connection close")
		}
	})
</script>
</head>
<body>
<p><div id="chatStatus"></div>
<textarea rows="15" cols="40" name="chatMsg"></textarea><br>
메시지입력 : <input type="text" name="chatInput">
</body>
</html>