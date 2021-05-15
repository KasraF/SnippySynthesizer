package edu.ucsd.snippy.responses;

public class SynthResult
{
	public SynthResult(int id, boolean success, String program)
	{
		this.id = id;
		this.success = success;
		this.program = program;
	}

	public int id;
	public boolean success;
	public String program;
}
