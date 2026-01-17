package com.icio.sportakuz.config.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RecaptchaFilter extends OncePerRequestFilter {

    private final RecaptchaService recaptchaService;
    private final SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler("/login?error");

    public RecaptchaFilter(RecaptchaService recaptchaService) {
        this.recaptchaService = recaptchaService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (request.getMethod().equals("POST") && request.getServletPath().equals("/login")) {

            String recaptchaResponse = request.getParameter("g-recaptcha-response");

            if (!recaptchaService.verify(recaptchaResponse)) {
                response.sendRedirect(request.getContextPath() + "/login?captchaError");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}