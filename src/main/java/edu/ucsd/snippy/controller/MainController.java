package edu.ucsd.snippy.controller;

import edu.ucsd.snippy.Snippy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import scala.Option;
import edu.ucsd.snippy.utils.WebSynthResult;

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
	WebSynthResult synthesize(@RequestBody String problem)
	{
		final WebSynthResult rs = new WebSynthResult();

		try {
			this.logger.debug(problem);
			Option<String> solution = Snippy.synthesize(problem, SYNTH_DURATION, false)._1();

			if (solution.isDefined()) {
				rs.program = solution.get();
				rs.success = true;
			}
		} catch (Exception e) {
			// Catch all to make sure the front-end is updated.
			LoggerFactory.getLogger(this.getClass()).error("Synthesis failed.", e);
		}

		return rs;
	}

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final int SYNTH_DURATION = 7;
}
