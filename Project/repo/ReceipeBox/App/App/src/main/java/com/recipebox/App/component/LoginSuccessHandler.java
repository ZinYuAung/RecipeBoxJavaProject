package com.recipebox.App.component;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.recipebox.App.entity.User;
import com.recipebox.App.repository.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	@Autowired
	private UserRepository userRepository; 

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		HttpSession session = request.getSession();
		
		String username = authentication.getName();
		Optional<User> userOpt = userRepository.findByEmail(username); 
		
		if (userOpt.isPresent()) {
			User loggedInUser = userOpt.get(); 
			session.setAttribute("loginUser", loggedInUser); 
		}

		String targetUrl = "/home";

		for (GrantedAuthority authority : authentication.getAuthorities()) {
			if ("ROLE_ADMIN".equals(authority.getAuthority())|| "ROLE_SUPERADMIN".equals(authority.getAuthority())) {
				targetUrl = "/admin/dashboard";
				break;
			} else if ("ROLE_USER".equals(authority.getAuthority())) {
				targetUrl = "/user/dashboard";
				break;
			}
		}


		clearAuthenticationAttributes(request);
		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}
}