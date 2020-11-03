package edu.ucsd.snippy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SnippyServer
{
	public static void main(String[] args) {
		SpringApplication.run(SnippyServer.class, args);
	}
}
