package com.recipebox.App.config;

import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// String uploadPath = System.getProperty("user.dir") + "/uploads/";
		String uploadPath = Paths.get("src/main/resources/static/uploads/").toAbsolutePath().toUri().toString();
		/*
		 * registry.addResourceHandler("/uploads/**") .addResourceLocations("file:" +
		 * uploadPath, "classpath:/static/uploads/");
		 */
		registry.addResourceHandler("/uploads/**").addResourceLocations(uploadPath);
	}
}