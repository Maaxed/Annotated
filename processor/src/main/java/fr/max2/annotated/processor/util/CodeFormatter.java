package fr.max2.annotated.processor.util;

import java.io.IOException;

import fr.max2.annotated.processor.model.ICodeConsumer;

public class CodeFormatter implements ICodeConsumer
{
	private final ICodeConsumer parent;
	private int indentation = 0;
	private int emptyLineCount = 1;

	public CodeFormatter(ICodeConsumer parent)
	{
		this.parent = parent;
	}

	@Override
	public void write(CharSequence code) throws IOException
	{
		writeImpl(code);
	}

	@Override
	public void writeLine(CharSequence code) throws IOException
	{
		this.writeImpl(code);
		this.writeLine();
	}

	@Override
	public void writeLine() throws IOException
	{
		this.emptyLineCount++;
		if (this.emptyLineCount > 1)
			return; // Remove double new lines

		this.parent.writeLine();
	}

	private void writeImpl(CharSequence code) throws IOException
	{
		int lastPartIndex = 0;
		boolean prevCR = false;
		boolean prevSpace = false;
		for (int i = 0; i < code.length(); i++)
		{
			char c = code.charAt(i);
			boolean currentCR = false;
			boolean currentSpace = false;
			switch (c)
			{
				// TODO comments
				case '(':
				case '{':
				case '[':
					this.push(code.subSequence(lastPartIndex, i + 1));
					lastPartIndex = i + 1;
					indentation++;
					break;
				case ')':
				case '}':
				case ']':
					this.push(code.subSequence(lastPartIndex, i));
					lastPartIndex = i;
					indentation--;
					break;
				case '\r':
					currentCR = true;
					break;
				case '\n':
					if (lastPartIndex < i)
						this.push(code.subSequence(lastPartIndex, i - (prevCR ? 1 : 0)));
					this.writeLine();
					lastPartIndex = i + 1;
					break;
				case ' ':
					// Remove double spaces
					if (prevSpace)
					{
						if (lastPartIndex < i)
							this.push(code.subSequence(lastPartIndex, i));
						lastPartIndex = i + 1;
					}
					currentSpace = true;
					break;
			}
			prevCR = currentCR;
			prevSpace = currentSpace;
		}

		if (lastPartIndex < code.length())
			this.push(code.subSequence(lastPartIndex, code.length()));
	}

	private void push(CharSequence code) throws IOException
	{
		if (this.emptyLineCount == 0)
		{
			this.parent.write(code);
			return;
		}

		// Remove existing indentation
		String codeStr = code.toString().stripLeading();
		if (codeStr.isEmpty())
			return;

		if (this.emptyLineCount >= 2)
		{
			switch (codeStr.charAt(0))
			{
				case '.':
				case ')':
				case '}':
				case ']':
				case '(':
				case '{':
				case '[':
					// Disallow empty new lines
					break;
				default:
					this.parent.writeLine();
					break;
			}
		}

		int indent = this.indentation;
		if (codeStr.startsWith("."))
			indent++; // Add extra indentation for chained calls
		
		// Add calculated indentation
		for (int i = 0; i < indent; i++)
		{
			this.parent.write("\t");
		}
		this.parent.write(codeStr);
		this.emptyLineCount = 0;
	}
}
