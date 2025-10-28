<!-- src/main/webapp/WEB-INF/views/classes/list.jsp -->
<%@ taglib prefix="c"   uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"  %>

<html>
<head><title>Lista zajęć</title></head>
<body>
<h1>Lista zajęć</h1>

<c:if test="${not empty success}">
    <div class="flash-success">${success}</div>
</c:if>

<p><a href="<c:url value='/classes/new'/>">+ Dodaj zajęcia</a></p>

<table border="1" cellpadding="6">
    <thead>
    <tr>
        <th>ID</th>
        <th>Typ</th>
        <th>Instruktor</th>
        <th>Sala</th>
        <th>Start</th>
        <th>Koniec</th>
        <th>Pojemność</th>
        <th>Status</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="c" items="${classes}">
        <tr>
            <td>${c.id}</td>
            <td>${c.type.name}</td>
            <td>${c.instructor.firstName} ${c.instructor.lastName}</td>
            <td>${c.room.name}</td>
            <td>${c.startTime}</td>
            <td>${c.endTime}</td>
            <td>${c.capacity}</td>
            <td>${c.status}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>
</body>
</html>
