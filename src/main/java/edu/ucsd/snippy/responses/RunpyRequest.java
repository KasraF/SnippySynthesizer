package edu.ucsd.snippy.responses;

public class RunpyRequest
{
	public RunpyRequest(int id, String name, String content, String values)
	{
		this.id = id;
		this.name = name;
		this.content = content;
		this.values = values;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public String getValues()
	{
		return values;
	}

	public void setValues(String values)
	{
		this.values = values;
	}

	protected int id;
	protected String name;
	protected String content;
	protected String values;
}
