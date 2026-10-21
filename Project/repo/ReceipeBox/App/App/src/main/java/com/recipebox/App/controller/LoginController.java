package com.recipebox.App.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.recipebox.App.entity.User;
import com.recipebox.App.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping({"/", "/home"})
    public String home() { 
        return "dashboard/home"; 
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !isAnonymousUser(auth)) {
            return redirectBasedOnRole(auth);
        }

       
        HttpSession session = request.getSession(false);
        if (session != null) {
            if (session.getAttribute("emailError") != null) {
                model.addAttribute("emailError", session.getAttribute("emailError"));
                session.removeAttribute("emailError");
            }
            if (session.getAttribute("passwordError") != null) {
                model.addAttribute("passwordError", session.getAttribute("passwordError"));
                session.removeAttribute("passwordError");
            }
            if (session.getAttribute("enteredEmail") != null) {
                model.addAttribute("enteredEmail", session.getAttribute("enteredEmail"));
                session.removeAttribute("enteredEmail");
            }
        }

        return "auth/login";
    }
    @PostMapping("/login")
    public String processLogin(@RequestParam("email") String email,
                               @RequestParam("password") String password,
                               HttpSession session,
                               Model model) {
        
        boolean hasError = false;
        
        // Retain entered email on reload
        model.addAttribute("enteredEmail", email);

        // 1. Check if email is empty
        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("emailError", "Email is required.");
            hasError = true;
        }

        // 2. Check if password is empty
        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("passwordError", "Password is required.");
            hasError = true;
        }

        if (hasError) {
            return "auth/login";
        }

        // 3. Check if email exists in database
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            model.addAttribute("emailError", "Invalid email address. Please check and try again.");
            return "auth/login";
        }
        
        User user = userOpt.get();
   
        if (!passwordEncoder.matches(password, user.getPassword_hash())) {
            model.addAttribute("passwordError", "Invalid password. Please check and try again.");
            return "auth/login";
        }

        session.setAttribute("loginUser", user);

        if ("ADMIN".equals(user.getRole())) {
            return "redirect:/admin/dashboard";
        } else {
            return "redirect:/user/dashboard";
        }
    }

    @GetMapping("/signup")
    public String signup() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !isAnonymousUser(auth)) {
            return redirectBasedOnRole(auth);
        }
        return "auth/signup";
    }

    private boolean isAnonymousUser(Authentication auth) {
        return auth == null || auth.getPrincipal().toString().equals("anonymousUser");
    }

    private String redirectBasedOnRole(Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isUser = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));

        if (isAdmin) {
            return "redirect:/admin/dashboard";
        } else if (isUser) {
            return "redirect:/user/dashboard";
        }
        return "redirect:/home";
    }
}