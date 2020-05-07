package fr.max2.annotated.processor.utils.template;

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
import javax.tools.Diagnostic.Kind;

import fr.max2.annotated.processor.network.PacketProcessor;
import fr.max2.annotated.processor.utils.exceptions.TemplateException;

public class TemplateHelper
{
	private TemplateHelper() { }

	
	public static void writeFileFromTemplateWithLog(PacketProcessor processor, String className, String templateFile, Map<String, String> replacements, Element originatingElement, Optional<? extends AnnotationMirror> annotation) throws IOException
	{
		try
		{
			processor.log(Kind.NOTE, "Generation file '" + className + "' from tmplate '" + templateFile + "'", originatingElement, annotation);
			
			writeFileFromTemplate(processor.filer(), className, templateFile, replacements, originatingElement);
		}
		catch (IOException e)
		{
			processor.log(Kind.ERROR, "An error occured during the generation of the file '" + className + "' from template '" + templateFile + "'", originatingElement, annotation);
			throw e;
		}
		
	}
	
	public static void writeFileFromTemplate(Filer filer, String className, String templateFile, Map<String, String> replacements, Element... originatingElements) throws IOException
	{
		JavaFileObject file = filer.createSourceFile(className, originatingElements);
		try (Writer writer = file.openWriter();
			 InputStream fileStream = PacketProcessor.class.getClassLoader().getResourceAsStream(templateFile);
			 Reader streamReader = new InputStreamReader(fileStream);
			 BufferedReader reader = new BufferedReader(streamReader))
		{
			ArrayDeque<ITemplateControl> controls = new ArrayDeque<>();
			String line;
			int i = 0;
			while ((line = reader.readLine()) != null)
			{
				String newLine = mapKeys(controls, line + System.lineSeparator(), i, replacements);
				
				writer.write(newLine);
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
	public static String mapKeys(ArrayDeque<ITemplateControl> controls, String content, int line, Map<String, String> replacements)
	{
		Pattern p = Pattern.compile("\\$\\{(.+?)\\}");
		Matcher m = p.matcher(content);
		
		StringBuffer sb = new StringBuffer();
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
			}
		}
		if (controls.isEmpty() || controls.peek().shouldPrint())
		{
			m.appendTail(sb);
		}
		
		return sb.toString();
	}
	
}
