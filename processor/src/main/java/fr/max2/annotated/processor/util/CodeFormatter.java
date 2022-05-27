package fr.max2.annotated.processor.util;

import java.io.IOException;
import java.util.Set;

import fr.max2.annotated.processor.model.ICodeConsumer;

public class CodeFormatter implements ICodeConsumer
{
	private final ICodeConsumer parent;

	private int indentation = 0;
	private int emptyLineCount = 1;
	private CommentKind comment = CommentKind.NONE;
	private char lastChar = '\0';
	private char lastWrittenChar = '\0';

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

		if (this.comment == CommentKind.NONE && this.emptyLineCount > 1)
			return; // Remove double new lines

		if (this.comment == CommentKind.LINE)
			this.comment = CommentKind.NONE; // Stop single line comment

		this.parent.writeLine();
		this.lastChar = '\n';
	}

	private void writeImpl(CharSequence code) throws IOException
	{
		int lastPartIndex = 0;
		for (int i = 0; i < code.length(); i++)
		{
			// TODO [v3.0] Do not format string content
			char c = code.charAt(i);
			char prevChar = this.lastChar;
			this.lastChar = c;
			switch (c)
			{
				case '(':
				case '{':
				case '[':
					if (this.comment != CommentKind.NONE)
						break;

					// Flush & add indent
					this.push(code.subSequence(lastPartIndex, i + 1));
					lastPartIndex = i + 1;
					this.indentation++;
					break;
				case ')':
				case '}':
				case ']':
					if (this.comment != CommentKind.NONE)
						break;

					// Flush & remove indent
					this.push(code.subSequence(lastPartIndex, i));
					lastPartIndex = i;
					this.lastChar = c;

					this.indentation--;
					if (this.indentation < 0)
						this.indentation = 0;
					break;
				case '\r':
					// Skip carriage return
					this.push(code.subSequence(lastPartIndex, i));
					lastPartIndex = i + 1;
					break;
				case '\n':
					this.push(code.subSequence(lastPartIndex, i));
					lastPartIndex = i + 1;
					this.writeLine();
					break;
				case ' ':
					if (this.comment != CommentKind.NONE)
						break;

					if (prevChar != ' ')
						break;

					// Remove double spaces
					this.push(code.subSequence(lastPartIndex, i));
					lastPartIndex = i + 1;
					break;
				case '*':
					if (this.comment != CommentKind.NONE || prevChar != '/')
						break;

					// Flush & open block comment
					this.push(code.subSequence(lastPartIndex, i + 1));
					lastPartIndex = i + 1;
					this.comment = CommentKind.BLOCK;
					this.lastChar = '\0'; // Prevent closing the comment immediately if followed by a '/'
					break;
				case '/':
					if (this.comment == CommentKind.NONE && prevChar == '/')
					{
						// Flush & open line comment
						this.push(code.subSequence(lastPartIndex, i + 1));
						lastPartIndex = i + 1;
						this.comment = CommentKind.LINE;
					}
					else if (this.comment == CommentKind.BLOCK && prevChar == '*')
					{
						// Flush & close block comment
						this.push(code.subSequence(lastPartIndex, i + 1));
						lastPartIndex = i + 1;
						this.comment = CommentKind.NONE;
					}
					break;
			}
		}

		// Flush remaining code
		if (lastPartIndex < code.length())
			this.push(code.subSequence(lastPartIndex, code.length()));
	}

	/** The set of characters before which empty lines should be removed */
	private static Set<Character> REMOVE_EMPTY_LINE_BEFORE_SET = Set.of('.', ')', '}', ']', '(', '{', '[', '|', '&', '+', '-');
	/** The set of characters after which empty lines should be removed */
	private static Set<Character> REMOVE_EMPTY_LINE_AFTER_SET = Set.of('.', '(', '{', '[', '|', '&', '+', '-');
	/** The set of characters before which an extra intentation should be added */
	private static Set<Character> EXTRA_INDENT_SET = Set.of('.', '|', '&', '+', '-');

	private void push(CharSequence code) throws IOException
	{
		if (this.emptyLineCount == 0)
		{
			// Add code normally
			if (!code.isEmpty())
			{
				this.parent.write(code);
				this.lastWrittenChar = code.charAt(code.length() - 1);
				this.lastChar = this.lastWrittenChar;
			}

			return;
		}

		// Remove existing indentation
		String codeStr = code.toString().stripLeading();
		if (codeStr.isEmpty())
			return;

		if (this.comment == CommentKind.NONE && this.emptyLineCount >= 2)
		{
			if (!REMOVE_EMPTY_LINE_BEFORE_SET.contains(codeStr.charAt(0)) && !REMOVE_EMPTY_LINE_AFTER_SET.contains(this.lastWrittenChar))
			{
				this.parent.writeLine();
			}
		}

		int indent = this.indentation;
		if (EXTRA_INDENT_SET.contains(codeStr.charAt(0)) || EXTRA_INDENT_SET.contains(this.lastWrittenChar))
			indent++; // Add extra indentation

		// Add calculated indentation
		for (int i = 0; i < indent; i++)
		{
			this.parent.write("\t");
		}
		this.parent.write(codeStr);
		this.lastWrittenChar = codeStr.charAt(codeStr.length() - 1);
		this.lastChar = this.lastWrittenChar;
		this.emptyLineCount = 0;
	}

	private static enum CommentKind
	{
		NONE,
		LINE,
		BLOCK
	}
}
