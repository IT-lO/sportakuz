<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!-- Prosta strona testowa -->
OK: sum=${sum}
<br/>
<c:forEach var="n" items="${names}">${fn:toUpperCase(n)} </c:forEach>
