package edu.ucsd.snippy.configurations;

import org.springframework.boot.web.server.MimeMappings;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebServerConfigurations implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory>
{
	@Override
	public void customize(ConfigurableServletWebServerFactory factory) {
		MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);
		mappings.add("wasm", "application/wasm");
		factory.setMimeMappings(mappings);
	}
}