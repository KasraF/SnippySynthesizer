package edu.ucsd.snippy.controller;

import edu.ucsd.snippy.Snippy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainController
{
	@GetMapping({"/", ""})
	String title()
	{
		return "title";
	}

	@GetMapping({"/editor", "editor"})
	String editor()
	{
		return "editor";
	}

	@PostMapping("/synthesize")
	@ResponseBody
	String synthesize(@RequestBody String problem)
	{
		String rs;

		try {
			this.logger.debug(problem);
			rs = Snippy.synthesize(problem, SYNTH_DURATION, false)
					._1()
					.getOrElse(() -> "# Synthesis Failed");
			this.logger.debug(rs);
		} catch (Exception e) {
			// Catch all to make sure the front-end is updated.
			LoggerFactory.getLogger(this.getClass()).error("Synthesis failed.", e);
			rs = "# Synthesis Failed";
		}

		return rs;
	}

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final int SYNTH_DURATION = 7;
}
