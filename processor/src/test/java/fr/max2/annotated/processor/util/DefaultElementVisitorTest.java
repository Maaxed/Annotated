package fr.max2.annotated.processor.util;

import static org.junit.Assert.*;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;

import org.junit.Before;
import org.junit.Test;

import fr.max2.annotated.processor.util.model.element.TestingAllElement;

public class DefaultElementVisitorTest
{
	private TestingAllElement passedElement;

	@Before
	public void setUp()
	{
		this.passedElement = new TestingAllElement();
	}

	@Test
	public void testVisit()
	{
		TestingVisitor visitor = new TestingVisitor();
		this.passedElement = new TestingAllElement()
		{
			@Override
			public <R, P> R accept(ElementVisitor<R, P> v, P p)
			{
				assertSame(visitor, v);
				return v.visitUnknown(this, p);
			}
		};

		assertEquals("Output", visitor.visit(this.passedElement, "Input"));
		assertEquals("Error", visitor.visit(this.passedElement));
	}

	@Test
	public void testVisitPackage()
	{
		assertEquals("Output", new TestingVisitor().visitPackage(this.passedElement, "Input"));
	}

	@Test
	public void testVisitType()
	{
		assertEquals("Output", new TestingVisitor().visitType(this.passedElement, "Input"));
	}

	@Test
	public void testVisitVariable()
	{
		assertEquals("Output", new TestingVisitor().visitVariable(this.passedElement, "Input"));
	}

	@Test
	public void testVisitExecutable()
	{
		assertEquals("Output", new TestingVisitor().visitExecutable(this.passedElement, "Input"));
	}

	@Test
	public void testVisitTypeParameter()
	{
		assertEquals("Output", new TestingVisitor().visitTypeParameter(this.passedElement, "Input"));
	}

	@Test
	public void testVisitUnknown()
	{
		assertEquals("Output", new TestingVisitor().visitUnknown(this.passedElement, "Input"));
	}

	private class TestingVisitor implements DefaultElementVisitor<String, String>
	{

		@Override
		public String visitDefault(Element e, String p)
		{
			assertSame(DefaultElementVisitorTest.this.passedElement, e);
			return p == "Input" ? "Output" : "Error";
		}

	}

}
