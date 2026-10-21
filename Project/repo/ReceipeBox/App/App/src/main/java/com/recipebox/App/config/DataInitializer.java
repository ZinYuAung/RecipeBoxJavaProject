package com.recipebox.App.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.recipebox.App.entity.User;
import com.recipebox.App.enums.UserRole;
import com.recipebox.App.repository.UserRepository;

@Configuration
public class DataInitializer {

	@Bean
	@Transactional
	CommandLineRunner init(UserRepository repo, PasswordEncoder encoder) {
		return args -> {

			if (repo.findByEmail("superadmin@gmail.com").isEmpty()) {
			    User admin = new User();
			    admin.setName("Super Admin");
			    admin.setEmail("superadmin@gmail.com");
			    admin.setPassword_hash(encoder.encode("supadmin123"));
			    admin.setRole(UserRole.SUPERADMIN);
			    admin.setIsActive(true);
			    repo.save(admin);
			}
			if (repo.findByEmail("admin@gmail.com").isEmpty()) {
				User admin = new User();
				admin.setName("Admin");
				admin.setEmail("admin@gmail.com");
				admin.setPassword_hash(encoder.encode("admin123"));
				admin.setRole(UserRole.ADMIN); //
				admin.setIsActive(true);

				repo.save(admin);
			}
			if (repo.findByEmail("user@gmail.com").isEmpty()) {
				User user = new User();
				user.setName("User");
				user.setEmail("user@gmail.com");
				user.setPassword_hash(encoder.encode("user123"));
				user.setRole(UserRole.USER);
				user.setIsActive(true);

				repo.save(user);
			}
		
		};
	}
}