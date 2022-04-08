package fr.max2.annotated.processor.utils;

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
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;

import org.junit.Assert;
import org.junit.Test;

import fr.max2.annotated.processor.utils.exceptions.TemplateException;
import fr.max2.annotated.processor.utils.template.ITemplateControl;
import fr.max2.annotated.processor.utils.template.TemplateHelper;


public class TemplateHelperTest
{
	private TemplateHelper helper = new TemplateHelper(null);
	
	@Test
	public void testWriteFileFromTemplate()
	{
		FakeFiler filer1 = new FakeFiler("Output");
		Map<String, String> replacements = new HashMap<>();
		
		assertThrows(TemplateException.class, () ->
			this.helper.writeFile(filer1, "Output", "templates/TemplateTest.jvtp", replacements));

		FakeFiler filer2 = new FakeFiler("Output2");
		replacements.put("blue", "violets");
		
		assertThrows(TemplateException.class, () ->
			this.helper.writeFile(filer2, "Output2", "templates/TemplateTest.jvtp", replacements));

		FakeFiler filer3 = new FakeFiler("Output3");
		replacements.put("red", "roses");
		
		try
		{
			this.helper.writeFile(filer3, "Output3", "templates/TemplateTest.jvtp", replacements);
			assertArrayEquals(new Object[] {"This is a test template.",
											"The value of \"red\" is roses.",
											"The value of \"blue\" is violets."},
							  filer3.getOutput().split("\\R"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testMapKeys()
	{
		Map<String, String> replacements = new HashMap<>();
		
		ArrayDeque<ITemplateControl> controls = new ArrayDeque<>();
		
		assertEquals("test", this.helper.mapKeys(controls, "test", 0, replacements));
		assertEquals("$test", this.helper.mapKeys(controls, "$test", 0, replacements));
		assertEquals("$}test{", this.helper.mapKeys(controls, "$}test{", 0, replacements));
		assertThrows(TemplateException.class, () -> this.helper.mapKeys(controls, "${test}", 0, replacements));
		//assertThrows(TemplateException.class, () -> helper.mapKeys(controls, "${${test}}", 0, replacements));
		assertThrows(TemplateException.class, () -> this.helper.mapKeys(controls, "${test1}a${test2}", 0, replacements));
		
		replacements.put("test", "value");

		assertEquals("test", this.helper.mapKeys(controls, "test", 0, replacements));
		assertEquals("$test", this.helper.mapKeys(controls, "$test", 0, replacements));
		assertEquals("$}test{", this.helper.mapKeys(controls, "$}test{", 0, replacements));
		assertEquals("value", this.helper.mapKeys(controls, "${test}", 0, replacements));
		assertEquals("value", this.helper.mapKeys(controls, "${  test	}", 0, replacements));
		//assertEquals("${test}", mapKeys("${${test}}", 0, replacements));
		assertEquals("valueavalue", this.helper.mapKeys(controls, "${test}a${test}", 0, replacements));
		
		replacements.put("test1", "VALUE");

		assertEquals("value", this.helper.mapKeys(controls, "${test}", 0, replacements));
		assertEquals("VALUE", this.helper.mapKeys(controls, "${test1}", 0, replacements));
		assertEquals("VALUEavalue", this.helper.mapKeys(controls, "${test1}a${test}", 0, replacements));
		assertEquals("valueaVALUE", this.helper.mapKeys(controls, "${test}a${test1}", 0, replacements));
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
			Assert.fail("Illegal method call");
			return false;
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
