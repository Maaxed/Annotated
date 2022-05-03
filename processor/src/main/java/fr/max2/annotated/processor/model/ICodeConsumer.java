package fr.max2.annotated.processor.model;


public interface ICodeConsumer
{
	void write(String code);
	default void writeLine(String code)
	{
		this.write(code);
		this.writeLine();
	}
	default void writeLine()
	{
		this.write(System.lineSeparator());
	}
}
