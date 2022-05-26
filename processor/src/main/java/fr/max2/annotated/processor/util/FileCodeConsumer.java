package fr.max2.annotated.processor.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;

import fr.max2.annotated.processor.model.ICodeConsumer;
import fr.max2.annotated.processor.model.ICodeSupplier;
import fr.max2.annotated.processor.util.exceptions.ProcessorException;

public class FileCodeConsumer implements ICodeConsumer, Closeable
	{
	private final Writer writer;

	public FileCodeConsumer(Writer writer)
	{
		this.writer = writer;
	}

	public static void writeFile(ProcessingTools tools, String className, ICodeSupplier code, Element originatingElement, Optional<? extends AnnotationMirror> annotation, Element... originatingClasses)
	{
		JavaFileObject file = null;
		try
		{
			file = tools.filer.createSourceFile(className, originatingClasses);
			try (FileCodeConsumer codeConsumer = new FileCodeConsumer(file.openWriter()))
			{
				code.pipe(codeConsumer);
			}
		}
		catch (Exception e)
		{
			if (file != null)
			{
				file.delete();
			}
			throw ProcessorException.builder()
				.context(originatingElement, annotation)
				.build("Unable to write the class file '" + className + ": " + e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
		}
	}

	@Override
	public void write(CharSequence code) throws IOException
	{
		writer.write(code.toString());
	}

	@Override
	public void close() throws IOException
	{
		this.writer.close();
	}
}
