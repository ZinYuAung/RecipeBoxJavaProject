package com.recipebox.App.controller;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.recipebox.App.entity.User;
import com.recipebox.App.enums.UserRole;
import com.recipebox.App.repository.AllergyRepository;
import com.recipebox.App.repository.IngredientRepository;
import com.recipebox.App.repository.RecipeRepository;
import com.recipebox.App.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RecipeRepository recipeRepository;

	@Autowired
	private AllergyRepository allergyRepository;

	@Autowired
	private IngredientRepository ingredientRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private static final String PASSWORD_REGEX = "^(?=.*[!@#$%^&*(),.?\":{}|<>]).{6,}$";

	@GetMapping("/dashboard")
	public String dashboard(HttpSession session, Model model) {
		User currentAdmin = (User) session.getAttribute("loginUser");

		if (currentAdmin == null) {
			return "redirect:/login";
		}

		model.addAttribute("adminUser", currentAdmin);
		model.addAttribute("totalRecipes", recipeRepository.count());
		model.addAttribute("totalIngredients", ingredientRepository.count());
		model.addAttribute("totalAllergies", allergyRepository.count());
		model.addAttribute("totalUsers", userRepository.count());
		model.addAttribute("recentRecipes", recipeRepository.findTop4ByOrderByRecipeIdDesc());

		return "dashboard/admin-dashboard";
	}

	@GetMapping("/users")
	public String listUsers(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size, HttpSession session, Model model) {

		User currentAdmin = (User) session.getAttribute("loginUser");
		if (currentAdmin == null) {
			return "redirect:/login";
		}

		org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
		org.springframework.data.domain.Page<User> userPage;

		List<UserRole> allowedRoles = List.of(UserRole.USER, UserRole.ADMIN);

		// Exclude SUPERADMIN and the currently logged-in admin's own ID
		if (keyword != null && !keyword.trim().isEmpty()) {
			userPage = userRepository
					.findByRoleInAndUserIdNotAndNameContainingIgnoreCaseOrRoleInAndUserIdNotAndEmailContainingIgnoreCase(
							allowedRoles, currentAdmin.getUserId(), keyword, allowedRoles, currentAdmin.getUserId(),
							keyword, pageable);
		} else {
			userPage = userRepository.findByRoleInAndUserIdNot(allowedRoles, currentAdmin.getUserId(), pageable);
		}

		model.addAttribute("users", userPage.getContent());
		model.addAttribute("userPage", userPage);
		model.addAttribute("keyword", keyword);
		model.addAttribute("loginUser", currentAdmin);

		return "user/list";
	}

	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable int id, HttpSession session, Model model) {
		User currentAdmin = (User) session.getAttribute("loginUser");
		if (currentAdmin == null) {
			return "redirect:/login";
		}

		User user = userRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + id));
		model.addAttribute("user", user);
		model.addAttribute("loginUser", currentAdmin);

		return "admin/adminUserEdit";
	}

	@PostMapping("/update")
	public String updateUser(@ModelAttribute User formUser, HttpSession session,
			RedirectAttributes redirectAttributes) {
		User currentAdmin = (User) session.getAttribute("loginUser");
		if (currentAdmin == null) {
			return "redirect:/login";
		}

		User existingUser = userRepository.findById(formUser.getUserId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + formUser.getUserId()));

		existingUser.setName(formUser.getName());

		// Regular Admins CAN toggle Account Status
		existingUser.setIsActive(formUser.getIsActive());

		// ONLY SuperAdmin can modify roles; regular admins won't trigger an error
		// message here.
		if (currentAdmin.getRole() == UserRole.SUPERADMIN) {
			existingUser.setRole(formUser.getRole());
		}

		userRepository.save(existingUser);

		if (currentAdmin.getUserId() == existingUser.getUserId()) {
			session.setAttribute("loginUser", existingUser);
		}

		redirectAttributes.addFlashAttribute("message", "User updated successfully!");
		return "redirect:/admin/users";
	}

	@GetMapping("/info/{id}")
	public String showUserInfo(@PathVariable int id, Model model) {
		model.addAttribute("user", userRepository.findById(id).orElseThrow());
		return "admin/adminUserView";
	}

	@GetMapping("/delete/{id}")
	public String deleteUser(@PathVariable int id, HttpSession session, RedirectAttributes redirectAttributes) {
		User currentAdmin = (User) session.getAttribute("loginUser");
		if (currentAdmin == null) {
			return "redirect:/login";
		}
		if (currentAdmin.getRole() != UserRole.SUPERADMIN) {
			redirectAttributes.addFlashAttribute("errorMessage", "Access Denied: Regular Admins cannot delete users.");
			return "redirect:/admin/users";
		}
		User user = userRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));

		user.setIsActive(false);
		userRepository.save(user);

		if (currentAdmin.getUserId() == id) {
			session.invalidate();
			return "redirect:/login?accountDeactivated=true";
		}

		redirectAttributes.addFlashAttribute("message", "User deactivated successfully!");
		return "redirect:/admin/users";
	}

	@GetMapping("/profile")
	public String viewAdminProfile(HttpSession session, Model model) {
		User currentAdmin = (User) session.getAttribute("loginUser");
		if (currentAdmin == null) {
			return "redirect:/login";
		}

		model.addAttribute("user", currentAdmin);
		return "admin/adminProfile";
	}

	// ==========================================
	// ADDED: Admin Profile Edit Handlers
	// ==========================================
	@GetMapping("/profile/edit")
	public String showAdminProfileEditForm(HttpSession session, Model model) {
		User currentAdmin = (User) session.getAttribute("loginUser");
		if (currentAdmin == null) {
			return "redirect:/login";
		}
		User freshUser = userRepository.findById(currentAdmin.getUserId()).orElse(currentAdmin);
		model.addAttribute("user", freshUser);
		return "admin/adminProfileEdit";
	}

	@PostMapping("/profile/update")
	public String updateAdminProfile(@ModelAttribute User formUser,
			@RequestParam(value = "currentPassword", required = false) String currentPassword,
			@RequestParam(value = "newPassword", required = false) String newPassword,
			@RequestParam(value = "confirmPassword", required = false) String confirmPassword, HttpSession session,
			Model model, RedirectAttributes redirectAttributes) {

		User currentAdmin = (User) session.getAttribute("loginUser");
		if (currentAdmin == null) {
			return "redirect:/login";
		}

		User existingUser = userRepository.findById(currentAdmin.getUserId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid admin ID"));

		if (formUser.getEmail() != null && !formUser.getEmail().trim().equalsIgnoreCase(existingUser.getEmail())) {
			java.util.Optional<User> emailCheck = userRepository.findByEmail(formUser.getEmail().trim());
			if (emailCheck.isPresent() && emailCheck.get().getUserId() != existingUser.getUserId()) {
				model.addAttribute("user", existingUser);
				model.addAttribute("errorMessage", "This email is already in use by another account.");
				return "admin/adminProfileEdit";
			}
		}

		existingUser.setName(formUser.getName());
		existingUser.setEmail(formUser.getEmail());

		// Optional Password Update Verification
		if (newPassword != null && !newPassword.trim().isEmpty()) {
			if (currentPassword == null || currentPassword.trim().isEmpty()) {
				return "redirect:/admin/profile/edit?error=currentPasswordRequired";
			}
			if (!passwordEncoder.matches(currentPassword, existingUser.getPassword_hash())) {
				return "redirect:/admin/profile/edit?error=wrongCurrentPassword";
			}
			if (!newPassword.equals(confirmPassword)) {
				return "redirect:/admin/profile/edit?error=passwordMismatch";
			}
			existingUser.setPassword_hash(passwordEncoder.encode(newPassword));
		}
		try {
			userRepository.save(existingUser);
			session.setAttribute("loginUser", existingUser); // Refresh session info
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
			model.addAttribute("user", existingUser);
			model.addAttribute("errorMessage",
					"This email address is already registered. Please use a different email.");
			return "admin/adminProfileEdit";
		}
		userRepository.save(existingUser);
		session.setAttribute("loginUser", existingUser); // Refresh session info

		redirectAttributes.addFlashAttribute("message", "Profile updated successfully!");
		return "redirect:/admin/profile";
	}

	@GetMapping("/register")
	public String showCreateUserForm(Model model, HttpSession session) {
		User currentAdmin = (User) session.getAttribute("loginUser");
		if (currentAdmin == null) {
			return "redirect:/login";
		}
		model.addAttribute("user", new User());
		model.addAttribute("loginUser", currentAdmin);
		return "user/create";
	}

	@PostMapping("/save")
	public String saveUser(@RequestParam("firstName") String firstName, @RequestParam("lastName") String lastName,
			@RequestParam("email") String email, @RequestParam("password") String password,
			@RequestParam("confirmPassword") String confirmPassword,
			@RequestParam(value = "role", required = false) UserRole role, Model model, HttpSession session,
			RedirectAttributes redirectAttributes) {

		User currentAdmin = (User) session.getAttribute("loginUser");
		if (currentAdmin == null) {
			return "redirect:/login";
		}
		boolean hasError = false;
		model.addAttribute("firstName", firstName);
		model.addAttribute("lastName", lastName);
		model.addAttribute("email", email);
		model.addAttribute("selectedRole", role);
		model.addAttribute("loginUser", currentAdmin);

		if (firstName == null || firstName.trim().isEmpty()) {
			model.addAttribute("firstNameError", "First name cannot be left blank.");
			hasError = true;
		}
		String fullName = (firstName != null ? firstName.trim() : "") + " " + (lastName != null ? lastName.trim() : "");
		if (email == null || email.trim().isEmpty()) {
			model.addAttribute("emailError", "Email is required.");
			hasError = true;
		} else if (userRepository.findByEmail(email).isPresent()) {
			model.addAttribute("emailError", "Gmail is already signed up! Please use another email.");
			hasError = true;
		}

		if (!hasError && userRepository.existsByName(fullName)) {
			model.addAttribute("firstNameError", "Name is already used! Please choose another name.");
			hasError = true;
		}

		if (password == null || password.trim().isEmpty()) {
			model.addAttribute("passwordError", "Password is required.");
			hasError = true;
		} else if (!Pattern.matches(PASSWORD_REGEX, password)) {
			model.addAttribute("passwordError",
					"Password must be at least 6 characters long and contain at least one special character.");
			hasError = true;
		}

		if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
			model.addAttribute("confirmPasswordError", "Confirm password is required.");
			hasError = true;
		} else if (!password.equals(confirmPassword)) {
			model.addAttribute("confirmPasswordError", "Passwords do not match.");
			hasError = true;
		}

		if (hasError) {
			return "user/create";
		}

		User user = new User();
		user.setName(fullName);
		user.setEmail(email);
		user.setPassword_hash(passwordEncoder.encode(password));

		if (currentAdmin.getRole() == UserRole.SUPERADMIN && role != null) {
			user.setRole(role);
		} else {
			user.setRole(UserRole.USER);
		}

		user.setIsActive(true);

		userRepository.save(user);
		redirectAttributes.addFlashAttribute("message", "User created successfully!");
		return "redirect:/admin/users";
	}
}