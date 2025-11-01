Migracja JSP -> Thymeleaf wykonana.
Stare pliki JSP usunięte:
 - /WEB-INF/views/probe.jsp
 - /WEB-INF/views/classes/list.jsp
 - /WEB-INF/views/classes/new.jsp
Nowe szablony:
 - classpath:templates/probe.html
 - classpath:templates/classes/list.html
 - classpath:templates/classes/new.html
Konfiguracja Thymeleaf: AppConfig.templateResolver + viewResolver.
Zależności JSTL usunięte z pom.xml.
Kontrolery zwracają te same nazwy widoków, które teraz mapują się na pliki .html.

