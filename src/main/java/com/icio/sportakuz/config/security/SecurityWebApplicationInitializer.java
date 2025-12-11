package com.icio.sportakuz.config.security;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

// Ta pusta klasa to "magiczny przycisk", który aktywuje SecurityConfig na serwerze Tomcat.
// Bez niej filtry Security (w tym obsługa logowania) w ogóle nie działają!
public class SecurityWebApplicationInitializer extends AbstractSecurityWebApplicationInitializer {
}