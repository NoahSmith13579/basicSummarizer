package com.example.summarizer;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@EnableAsync
public class SummarizerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SummarizerApplication.class, args);
		System.out.println("Running...");
	}
}
