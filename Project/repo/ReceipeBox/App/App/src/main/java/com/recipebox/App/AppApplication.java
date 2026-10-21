package com.recipebox.App;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppApplication {

	public static void main(String[] args) {
		System.setProperty("org.apache.tomcat.util.http.fileupload.FileUploadBase.FILE_COUNT_LIMIT", "1000");
		SpringApplication.run(AppApplication.class, args);
	}

}
