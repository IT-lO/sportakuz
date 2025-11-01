<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
OK: 2+2=${2+2}
<c:forEach var="n" items="${['Ala','Ola','Ela']}">${fn:toUpperCase(n)} </c:forEach>
