package com.example;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@SpringBootApplication
@CrossOrigin
@RequestMapping("/testing")
public class Testing {

	@RequestMapping("/")
	public String testingDulu() {
		return "index";
	}

	@RequestMapping("/cobain")
	public String lain() {
		return "admin";
	}
}
