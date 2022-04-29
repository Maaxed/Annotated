package fr.max2.annotated.processor.util.template;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.tools.JavaFileObject;
import javax.annotation.processing.Filer;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import fr.max2.annotated.processor.network.PacketProcessor;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.IOConsumer;
import fr.max2.annotated.processor.util.exceptions.ProcessorException;
import fr.max2.annotated.processor.util.exceptions.TemplateException;

public class TemplateHelper
{
	private final ProcessingTools tools;
	
	public TemplateHelper(ProcessingTools tools) //TODO [v2.2] improve templates : method templates, for loop
	{
		this.tools = tools;
	}
	
	public void writeFileWithLog(String className, String templateFile, Map<String, String> replacements, Element originatingElement, Optional<? extends AnnotationMirror> annotation, Element... originatingClasses) throws ProcessorException
	{
		try
		{
			this.writeFile(this.tools.filer, className, templateFile, replacements, originatingClasses);
		}
		catch (Exception e)
		{
			throw ProcessorException.builder()
				.context(originatingElement, annotation)
				.build("Unable to write the class file '" + className + "' from template '" + templateFile + "': " + e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
		}
	}
	
	public void readWithLog(String templateFile, Map<String, String> replacements, IOConsumer<String> lines, Element originatingElement, Optional<? extends AnnotationMirror> annotation) throws ProcessorException
	{
		try
		{
			this.readTemplate(templateFile, replacements, lines);
		}
		catch (IOException e)
		{
			throw ProcessorException.builder().context(originatingElement, annotation).build("An IOException occured during the reading of the template '" + templateFile + "': " + e.getMessage(), e);
		}
		catch (Exception e)
		{
			throw ProcessorException.builder().context(originatingElement, annotation).build("An unexpected exception occured during the reading of the template '" + templateFile + "': " + e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
		}
	}
	
	public void writeFile(Filer filer, String className, String templateFile, Map<String, String> replacements, Element... originatingClasses) throws IOException
	{
		JavaFileObject file = filer.createSourceFile(className, originatingClasses);
		try (Writer writer = file.openWriter())
		{
			this.readTemplate(templateFile, replacements, writer::write);
			// TODO [v3.0] remove file if an error occurred
		}
		
	}
	
	public void readTemplate(String templateFile, Map<String, String> replacements, IOConsumer<String> lines) throws IOException
	{
		try (InputStream fileStream = PacketProcessor.class.getClassLoader().getResourceAsStream(templateFile);
			 Reader streamReader = new InputStreamReader(fileStream);
			 BufferedReader reader = new BufferedReader(streamReader))
		{
			ArrayDeque<ITemplateControl> controls = new ArrayDeque<>();
			String line;
			int i = 0;
			while ((line = reader.readLine()) != null)
			{
				String newLine = this.mapKeys(controls, line + System.lineSeparator(), i, replacements);
				
				if (!newLine.isEmpty())
					lines.accept(newLine);
				i++;
			}
			
			if (!controls.isEmpty())
			{
				throw new TemplateException("Unclosed control block");
			}
		}
		
	}
	
	/**
	 * Search the pattern '${key}' in the content string and replace it with the corresponding replacement
	 * @param content the content to search in
	 * @param replacements the map used to find the replacements
	 * @return the generated string
	 */
	public String mapKeys(ArrayDeque<ITemplateControl> controls, String content, int line, Map<String, String> replacements)
	{
		Pattern p = Pattern.compile("\\$\\{(.+?)\\}");
		Matcher m = p.matcher(content);
		
		StringBuffer sb = new StringBuffer();
		StringBuffer devNull = new StringBuffer();
		boolean hasSpecialCode = false;
		while (m.find())
		{
			hasSpecialCode = true;
			String key = m.group(1);
			String[] parts = Stream.of(key.split(" ")).map(w -> w.trim()).filter(w -> w.length() > 0).toArray(String[]::new);
			
			if (parts.length == 0)
			{
				throw new TemplateException("Unrecognised empty control in line '" + content + "'");
			}
			
			if (controls.isEmpty() || controls.peek().shouldPrint())
				m.appendReplacement(sb, "");
			else
				m.appendReplacement(devNull, "");
			
			switch (parts[0])
			{
			case "if":
				if (parts.length != 2)
					throw new TemplateException("Too many parameters for if control in '" + key + "' in line " + line + " '" + content + "'");

				if ((controls.isEmpty() || controls.peek().shouldPrint()) && replacements.containsKey(parts[1]) && !replacements.get(parts[1]).equals("false") && !replacements.get(parts[1]).isEmpty())
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
				
				String rep = replacements.get(parts[0]);
				
				if (rep == null)
					throw new TemplateException("Unable to find replacement for the key '" + key + "' in line '" + content + "'");
				
				sb.append(Matcher.quoteReplacement(rep));
				break;
			}
		}
		if (controls.isEmpty() || controls.peek().shouldPrint())
		{
			m.appendTail(sb);
		}
		
		String res = sb.toString();
		// Remove unnecessary empty lines
		return hasSpecialCode && res.trim().equals("") ? "" : res;
	}
	
}
