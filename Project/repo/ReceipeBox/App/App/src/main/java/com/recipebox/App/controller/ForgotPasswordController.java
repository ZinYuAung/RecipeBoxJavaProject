package com.recipebox.App.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.recipebox.App.entity.User;
import com.recipebox.App.repository.UserRepository;

@Controller
public class ForgotPasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

   
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, 
                                        @RequestParam("newPassword") String newPassword, 
                                        Model model) {
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Email address not found. Please check and try again."); // ✅ Key နှင့် Value (စာသား) နှစ်ခုစလုံး ပါဝင်သည်
            return "auth/forgot-password";
        }

        User user = userOpt.get();
        user.setPassword_hash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        model.addAttribute("success", "Password changed successfully! You can now log in.");
        return "auth/forgot-password";
    }
}