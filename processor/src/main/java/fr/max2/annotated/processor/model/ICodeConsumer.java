package fr.max2.annotated.processor.model;

import java.io.IOException;

public interface ICodeConsumer
{
	void write(CharSequence code) throws IOException;
	default void writeLine(CharSequence code) throws IOException
	{
		this.write(code);
		this.writeLine();
	}
	default void writeLine() throws IOException
	{
		this.write(System.lineSeparator());
	}
}
