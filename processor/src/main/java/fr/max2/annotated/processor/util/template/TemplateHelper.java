package fr.max2.annotated.processor.util.template;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import fr.max2.annotated.processor.AnnotatedProcessor;
import fr.max2.annotated.processor.model.ICodeConsumer;
import fr.max2.annotated.processor.model.ICodeSupplier;
import fr.max2.annotated.processor.util.FileCodeConsumer;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.ProcessorException;
import fr.max2.annotated.processor.util.exceptions.TemplateException;

public class TemplateHelper
{
	private final ProcessingTools tools;

	public TemplateHelper(ProcessingTools tools) //TODO [v2.2] improve templates : method templates, for loop
	{
		this.tools = tools;
	}

	public void writeFileWithLog(String className, String templateFile, ReplacementMap replacements, Element originatingElement, Optional<? extends AnnotationMirror> annotation, Element... originatingClasses) throws ProcessorException
	{
		try
		{
			FileCodeConsumer.writeFile(tools, className, this.readTemplate(templateFile, replacements), originatingElement, annotation, originatingClasses);
		}
		catch (Exception e)
		{
			throw ProcessorException.builder()
				.context(originatingElement, annotation)
				.build("Unable to write the class file '" + className + "' from template '" + templateFile + "': " + e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
		}
	}

	public ICodeSupplier readWithLog(String templateFile, ReplacementMap replacements, Element originatingElement, Optional<? extends AnnotationMirror> annotation) throws ProcessorException
	{
		ICodeSupplier supplier = this.readTemplate(templateFile, replacements);
		return output ->
		{
			try
			{
				supplier.pipe(output);
			}
			catch (IOException e)
			{
				throw ProcessorException.builder()
					.context(originatingElement, annotation)
					.build("An IOException occured during the reading of the template '" + templateFile + "': " + e.getMessage(), e);
			}
			catch (Exception e)
			{
				throw ProcessorException.builder()
					.context(originatingElement, annotation)
					.build("An unexpected exception occured during the reading of the template '" + templateFile + "': " + e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
			}
		};
	}

	public ICodeSupplier readTemplate(String templateFile, ReplacementMap replacements)
	{
		return output ->
		{
			try (InputStream fileStream = AnnotatedProcessor.class.getClassLoader().getResourceAsStream(templateFile);
				 Reader streamReader = new InputStreamReader(fileStream);
				 BufferedReader reader = new BufferedReader(streamReader))
			{
				ArrayDeque<ITemplateControl> controls = new ArrayDeque<>();
				String line;
				int i = 0;
				while ((line = reader.readLine()) != null)
				{
					this.mapKeys(output, controls, line, i, replacements);
					output.writeLine();
					i++;
				}
		
				if (!controls.isEmpty())
				{
					throw new TemplateException("Unclosed control block");
				}
			}
		};
	}

	/**
	 * Search the pattern '${key}' in the content string and replace it with the corresponding replacement
	 * @param content the content to search in
	 * @param replacements the map used to find the replacements
	 * @return the generated string
	 */
	public void mapKeys(ICodeConsumer output, ArrayDeque<ITemplateControl> controls, String content, int line, ReplacementMap replacements) throws IOException
	{
		Pattern p = Pattern.compile("\\$\\{(.+?)\\}");
		Matcher m = p.matcher(content);

		StringBuffer devNull = new StringBuffer();
		while (m.find())
		{
			String key = m.group(1);
			String[] parts = Stream.of(key.split(" ")).map(w -> w.trim()).filter(w -> w.length() > 0).toArray(String[]::new);

			if (parts.length == 0)
			{
				throw new TemplateException("Unrecognised empty control in line '" + content + "'");
			}

			if (controls.isEmpty() || controls.peek().shouldPrint())
			{
				StringBuilder sb = new StringBuilder();
				m.appendReplacement(sb, "");
				output.write(sb);
			}
			else
			{
				m.appendReplacement(devNull, "");
			}

			switch (parts[0])
			{
			case "if":
				if (parts.length != 2)
					throw new TemplateException("Too many parameters for if control in '" + key + "' in line " + line + " '" + content + "'");

				if ((controls.isEmpty() || controls.peek().shouldPrint()) && replacements.map.containsKey(parts[1]) && replacements.map.get(parts[1]).booleanValue())
				{
					controls.push(IfControl.TRUE);
				}
				else
				{
					controls.push(IfControl.FALSE);
				}
				break;
			case "else":
				if (parts.length != 1)
					throw new TemplateException("Too many parameters for end control in '" + key + "' in line " + line + " '" + content + "'");

				if (controls.isEmpty())
					throw new TemplateException("Invalid closing control in line " + line + " '" + content + "'");

				ITemplateControl prev = controls.pop();

				if (prev.shouldPrint() || (!controls.isEmpty() && !controls.peek().shouldPrint()))
				{
					controls.push(IfControl.FALSE);
				}
				else
				{
					controls.push(IfControl.TRUE);
				}

				break;
			case "end":
				if (parts.length != 1)
					throw new TemplateException("Too many parameters for end control in '" + key + "' in line " + line + " '" + content + "'");

				if (controls.isEmpty())
					throw new TemplateException("Invalid closing control in line " + line + " '" + content + "'");

				controls.pop();
				break;
			default:
				if (parts.length != 1)
					throw new TemplateException("Unrecognised control '" + key + "' in line '" + content + "'");

				if (!controls.isEmpty() && !controls.peek().shouldPrint())
					break;

				ICodeSupplier rep = replacements.map.get(parts[0]);

				if (rep == null)
					throw new TemplateException("Unable to find replacement for the key '" + key + "' in line '" + content + "'");

				rep.pipe(output);
				break;
			}
		}
		if (controls.isEmpty() || controls.peek().shouldPrint())
		{
			StringBuilder sb = new StringBuilder();
			m.appendTail(sb);
			output.write(sb);
		}
	}

	public static interface ITemplateReplacement extends ICodeSupplier
	{
		boolean booleanValue();
	}

	public static class ReplacementMap
	{
		private final Map<String, ITemplateReplacement> map = new HashMap<>();

		public void putString(String identifier, CharSequence code)
		{
			map.put(identifier, new ITemplateReplacement()
			{
				@Override
				public void pipe(ICodeConsumer output) throws IOException
				{
					if (this.booleanValue())
					{
						output.write(code);
					}
				}

				@Override
				public boolean booleanValue()
				{
					return code != null && !code.isEmpty();
				}
			});
		}

		public void putLines(String identifier, Stream<? extends CharSequence> codeLines)
		{
			map.put(identifier, new ITemplateReplacement()
			{
				@Override
				public void pipe(ICodeConsumer output) throws IOException
				{
					if (this.booleanValue())
					{
						try
						{
							codeLines.forEach(line ->
							{
								try
								{
									output.writeLine(line);
								}
								catch (IOException e)
								{
									throw new UncheckedIOException(e); // Wrap IOException
								}
							});
						}
						catch (UncheckedIOException e)
						{
							throw e.getCause(); // Unwrap IOException
						}
					}
				}

				@Override
				public boolean booleanValue()
				{
					return codeLines != null;
				}
			});
		}

		public void putBoolean(String identifier, boolean bool)
		{
			map.put(identifier, new ITemplateReplacement()
			{
				@Override
				public void pipe(ICodeConsumer output) throws IOException
				{
					output.write(Boolean.toString(bool));
				}

				@Override
				public boolean booleanValue()
				{
					return bool;
				}
			});
		}
		
		public void putCode(String identifier, ICodeSupplier code)
		{
			map.put(identifier, new ITemplateReplacement()
			{
				@Override
				public void pipe(ICodeConsumer output) throws IOException
				{
					if (this.booleanValue())
					{
						code.pipe(output);
					}
				}

				@Override
				public boolean booleanValue()
				{
					return code != null;
				}
			});
		}
	}
}
