package fr.max2.annotated.processor.util;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.Optional;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;

import org.junit.Assert;
import org.junit.Test;

import fr.max2.annotated.processor.model.SimpleCodeBuilder;
import fr.max2.annotated.processor.util.exceptions.ProcessorException;
import fr.max2.annotated.processor.util.exceptions.TemplateException;
import fr.max2.annotated.processor.util.model.FakeProcessingEnvironment;
import fr.max2.annotated.processor.util.template.ITemplateControl;
import fr.max2.annotated.processor.util.template.TemplateHelper.ReplacementMap;


public class TemplateHelperTest
{
	@Test
	public void testWriteFileFromTemplate()
	{
		FakeProcessingEnvironment processingEnv = new FakeProcessingEnvironment();
		processingEnv.filer = new FakeFiler("Output");
		ProcessingTools tools = new ProcessingTools(processingEnv);

		ReplacementMap replacements = new ReplacementMap();

		ProcessingTools tools1 = tools;
		assertThrows(ProcessorException.class, () ->
			tools1.templates.writeFileWithLog("Output", "templates/TemplateTest.jvtp", replacements, null, Optional.empty()));

		processingEnv.filer = new FakeFiler("Output2");
		tools = new ProcessingTools(processingEnv);
		replacements.putString("blue", "violets");

		ProcessingTools tools2 = tools;
		assertThrows(ProcessorException.class, () ->
			tools2.templates.writeFileWithLog("Output2", "templates/TemplateTest.jvtp", replacements, null, Optional.empty()));

		FakeFiler filer = new FakeFiler("Output3");
		processingEnv.filer = filer;
		tools = new ProcessingTools(processingEnv);
		replacements.putString("red", "roses");

		try
		{
			tools.templates.writeFileWithLog("Output3", "templates/TemplateTest.jvtp", replacements, null, Optional.empty());
			assertArrayEquals(new Object[] {"This is a test template.",
											"The value of \"red\" is roses.",
											"The value of \"blue\" is violets."},
							  filer.getOutput().split("\\R"));
		}
		catch (ProcessorException e)
		{
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testMapKeys() throws IOException
	{
		ProcessingTools tools = new ProcessingTools(new FakeProcessingEnvironment());
		ReplacementMap replacements = new ReplacementMap();

		ArrayDeque<ITemplateControl> controls = new ArrayDeque<>();

		SimpleCodeBuilder code = new SimpleCodeBuilder();
		tools.templates.mapKeys(code, controls, "test", 0, replacements);
		assertEquals("test", code.build());
		code = new SimpleCodeBuilder();
		tools.templates.mapKeys(code, controls, "$test", 0, replacements);
		assertEquals("$test", code.build());
		code = new SimpleCodeBuilder();
		tools.templates.mapKeys(code, controls, "$}test{", 0, replacements);
		assertEquals("$}test{", code.build());
		SimpleCodeBuilder code1 = new SimpleCodeBuilder();
		assertThrows(TemplateException.class, () -> tools.templates.mapKeys(code1, controls, "${test}", 0, replacements));
		//assertThrows(TemplateException.class, () -> helper.mapKeys(controls, "${${test}}", 0, replacements));
		SimpleCodeBuilder code2 = new SimpleCodeBuilder();
		assertThrows(TemplateException.class, () -> tools.templates.mapKeys(code2, controls, "${test1}a${test2}", 0, replacements));

		replacements.putString("test", "value");

		code = new SimpleCodeBuilder();
		tools.templates.mapKeys(code, controls, "test", 0, replacements);
		assertEquals("test", code.build());
		code = new SimpleCodeBuilder();
		tools.templates.mapKeys(code, controls, "$test", 0, replacements);
		assertEquals("$test", code.build());
		code = new SimpleCodeBuilder();
		tools.templates.mapKeys(code, controls, "$}test{", 0, replacements);
		assertEquals("$}test{", code.build());
		code = new SimpleCodeBuilder();
		tools.templates.mapKeys(code, controls, "${test}", 0, replacements);
		assertEquals("value", code.build());
		code = new SimpleCodeBuilder();
		tools.templates.mapKeys(code, controls, "${  test	}", 0, replacements);
		assertEquals("value", code.build());
		//assertEquals("${test}", mapKeys("${${test}}", 0, replacements));
		code = new SimpleCodeBuilder();
		tools.templates.mapKeys(code, controls, "${test}a${test}", 0, replacements);
		assertEquals("valueavalue", code.build());

		replacements.putString("test1", "VALUE");

		code = new SimpleCodeBuilder();
		tools.templates.mapKeys(code, controls, "${test}", 0, replacements);
		assertEquals("value", code.build());
		code = new SimpleCodeBuilder();
		tools.templates.mapKeys(code, controls, "${test1}", 0, replacements);
		assertEquals("VALUE", code.build());
		code = new SimpleCodeBuilder();
		tools.templates.mapKeys(code, controls, "${test1}a${test}", 0, replacements);
		assertEquals("VALUEavalue", code.build());
		code = new SimpleCodeBuilder();
		tools.templates.mapKeys(code, controls, "${test}a${test1}", 0, replacements);
		assertEquals("valueaVALUE", code.build());
	}

	private static class FakeFiler implements Filer
	{
		private final String validName;
		private FakeFileObject file;

		public FakeFiler(String validName)
		{
			this.validName = validName;
		}

		private String getOutput()
		{
			assertNotNull(this.file);
			return this.file.getOutput();
		}

		@Override
		public JavaFileObject createSourceFile(CharSequence name, Element... originatingElements) throws IOException
		{
			assertEquals(this.validName, name.toString());
			assertArrayEquals(new Object[0], originatingElements);
			assertNull(this.file);
			this.file = new FakeFileObject(name.toString());
			return this.file;
		}

		@Override
		public JavaFileObject createClassFile(CharSequence name, Element... originatingElements) throws IOException
		{
			Assert.fail("Illegal method call");
			return null;
		}

		@Override
		public FileObject createResource(Location location, CharSequence pkg, CharSequence relativeName, Element... originatingElements) throws IOException
		{
			Assert.fail("Illegal method call");
			return null;
		}

		@Override
		public FileObject getResource(Location location, CharSequence pkg, CharSequence relativeName) throws IOException
		{
			Assert.fail("Illegal method call");
			return null;
		}

	}

	private static class FakeFileObject implements JavaFileObject
	{
		private final String name;
		private StringWriter writer;
		private ByteArrayOutputStream outputStream;

		public FakeFileObject(String name)
		{
			this.name = name;
		}

		private String getOutput()
		{
			assertTrue(this.writer != null ^ this.outputStream != null);

			if (this.outputStream != null)
			{
				byte[] data = this.outputStream.toByteArray();
				try
				{
					return new String(data, "UTF-8");
				}
				catch (UnsupportedEncodingException e)
				{
					e.printStackTrace();
					fail();
					return null;
				}
			}
			else
			{
				return this.writer.toString();
			}
		}

		@Override
		public URI toUri()
		{
			Assert.fail("Illegal method call");
			return null;
		}

		@Override
		public String getName()
		{
			return this.name;
		}

		@Override
		public InputStream openInputStream() throws IOException
		{
			Assert.fail("Illegal method call");
			return null;
		}

		@Override
		public OutputStream openOutputStream() throws IOException
		{
			assertNull(this.writer);
			assertNull(this.outputStream);
			this.outputStream = new ByteArrayOutputStream();
			return this.outputStream;
		}

		@Override
		public Reader openReader(boolean ignoreEncodingErrors) throws IOException
		{
			Assert.fail("Illegal method call");
			return null;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException
		{
			Assert.fail("Illegal method call");
			return null;
		}

		@Override
		public Writer openWriter() throws IOException
		{
			assertNull(this.writer);
			assertNull(this.outputStream);
			this.writer = new StringWriter();
			return this.writer;
		}

		@Override
		public long getLastModified()
		{
			Assert.fail("Illegal method call");
			return 0;
		}

		@Override
		public boolean delete()
		{
			return true;
		}

		@Override
		public Kind getKind()
		{
			Assert.fail("Illegal method call");
			return null;
		}

		@Override
		public boolean isNameCompatible(String simpleName, Kind kind)
		{
			Assert.fail("Illegal method call");
			return false;
		}

		@Override
		public NestingKind getNestingKind()
		{
			Assert.fail("Illegal method call");
			return null;
		}

		@Override
		public Modifier getAccessLevel()
		{
			Assert.fail("Illegal method call");
			return null;
		}

	}

}
