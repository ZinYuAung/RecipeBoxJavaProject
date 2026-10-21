package com.recipebox.App.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

import com.recipebox.App.entity.User;
import com.recipebox.App.enums.UserRole;
import com.recipebox.App.repository.UserRepository;

import java.util.regex.Pattern;

@Controller
public class RegisterController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private static final String PASSWORD_REGEX = "^(?=.*[!@#$%^&*(),.?\":{}|<>]).{6,}$";

	@PostMapping("/signup")
	public String saveUser(@RequestParam("firstName") String firstName, 
	                       @RequestParam("lastName") String lastName,
			               @RequestParam("email") String email, 
			               @RequestParam("password") String password, 
			               @RequestParam("confirmPassword") String confirmPassword,
			               Model model) {

		boolean hasError = false;

		// Retain values on reload
		model.addAttribute("firstName", firstName);
		model.addAttribute("lastName", lastName);
		model.addAttribute("email", email);

		// 1. Check if First Name is empty
		if (firstName == null || firstName.trim().isEmpty()) {
			model.addAttribute("firstNameError", "First name cannot be left blank.");
			hasError = true;
		}

		String fullName = (firstName != null ? firstName.trim() : "") + " " + (lastName != null ? lastName.trim() : "");

		// 2. Check if Email is already signed up
		if (email == null || email.trim().isEmpty()) {
			model.addAttribute("emailError", "Email is required.");
			hasError = true;
		} else if (userRepository.findByEmail(email).isPresent()) {
			model.addAttribute("emailError", "Gmail is already signed up! Please use another email.");
			hasError = true;
		}

		// 3. Check if Full Name already exists
		if (!hasError && userRepository.existsByName(fullName)) {
			model.addAttribute("firstNameError", "Name is already used! Please choose another name.");
			hasError = true;
		}

		// 4. Password Validation
		if (password == null || password.trim().isEmpty()) {
			model.addAttribute("passwordError", "Password is required.");
			hasError = true;
		} else if (!Pattern.matches(PASSWORD_REGEX, password)) {
			model.addAttribute("passwordError", "Password must be at least 6 characters long and contain at least one special character.");
			hasError = true;
		}
		// 💡 5. Confirm Password Validation
				if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
					model.addAttribute("confirmPasswordError", "Confirm password is required.");
					hasError = true;
				} else if (!password.equals(confirmPassword)) {
					model.addAttribute("confirmPasswordError", "Passwords do not match.");
					hasError = true;
				}

		if (hasError) {
			return "auth/signup";
		}

		// 5. Save New User
		User user = new User();
		user.setName(fullName);
		user.setEmail(email);
		user.setPassword_hash(passwordEncoder.encode(password));
		user.setRole(UserRole.USER);
		user.setIsActive(true);

		userRepository.save(user);

		return "redirect:/login?signupSuccess=true";
	}
}