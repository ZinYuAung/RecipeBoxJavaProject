package com.recipebox.App.component;

import java.io.IOException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.recipebox.App.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomLoginFailureHandler implements AuthenticationFailureHandler {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        request.getSession().setAttribute("enteredEmail", email);

        boolean hasError = false;

      
        if (email == null || email.trim().isEmpty()) {
            request.getSession().setAttribute("emailError", "Email is required.");
            hasError = true;
        }

        if (password == null || password.trim().isEmpty()) {
            request.getSession().setAttribute("passwordError", "Password is required.");
            hasError = true;
        }

        if (!hasError) {
            if (userRepository.findByEmail(email).isEmpty()) {
                request.getSession().setAttribute("emailError", "Invalid email address. Please check and try again.");
            } else if (exception instanceof BadCredentialsException) {
                request.getSession().setAttribute("passwordError", "Invalid password. Please check and try again.");
            } else {
                request.getSession().setAttribute("passwordError", "Invalid email or password.");
            }
        }

        response.sendRedirect("/login?error=true");
    }
}