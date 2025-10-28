<%@ taglib prefix="c"   uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"  %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<html>
<head><title>Dodaj zajęcia</title></head>
<body>
<h1>Dodaj zajęcia</h1>

<form:form method="post" modelAttribute="form">
    <div>
        <label>Typ:</label>
        <form:select path="classTypeId">
            <form:option value="" label="-- wybierz --"/>
            <c:forEach var="t" items="${types}">
                <form:option value="${t.id}" label="${t.name}"/>
            </c:forEach>
        </form:select>
        <form:errors path="classTypeId" cssClass="error"/>
    </div>

    <div>
        <label>Instruktor:</label>
        <form:select path="instructorId">
            <form:option value="" label="-- wybierz --"/>
            <c:forEach var="i" items="${instructors}">
                <form:option value="${i.id}" label="${i.firstName} ${i.lastName}"/>
            </c:forEach>
        </form:select>
        <form:errors path="instructorId" cssClass="error"/>
    </div>

    <div>
        <label>Sala:</label>
        <form:select path="roomId">
            <form:option value="" label="-- wybierz --"/>
            <c:forEach var="r" items="${rooms}">
                <form:option value="${r.id}" label="${r.name} (cap: ${r.capacity})"/>
            </c:forEach>
        </form:select>
        <form:errors path="roomId" cssClass="error"/>
    </div>

    <div>
        <label>Data:</label>
        <form:input path="date" type="date"/>
        <form:errors path="date" cssClass="error"/>
    </div>

    <div>
        <label>Start:</label>
        <form:input path="startTime" type="time"/>
        <form:errors path="startTime" cssClass="error"/>
    </div>

    <div>
        <label>Koniec:</label>
        <form:input path="endTime" type="time"/>
        <form:errors path="endTime" cssClass="error"/>
    </div>

    <div>
        <label>Pojemność:</label>
        <form:input path="capacity" type="number" min="1"/>
        <form:errors path="capacity" cssClass="error"/>
    </div>

    <div>
        <label>Notatka:</label>
        <form:textarea path="note" rows="3"/>
        <form:errors path="note" cssClass="error"/>
    </div>

    <div>
        <button type="submit">Zapisz</button>
        <a href="<c:url value='/classes'/>">Anuluj</a>
    </div>
</form:form>

</body>
</html>
