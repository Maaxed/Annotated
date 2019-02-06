package fr.max2.packeta.processor.utils;

import static org.junit.Assert.*;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;

import org.junit.Before;
import org.junit.Test;

import fr.max2.packeta.processor.utils.model.element.TestingAllElement;


public class DefaultElementVisitorTest
{
	private TestingAllElement passedElement;
	
	@Before
	public void setUp()
	{
		passedElement = new TestingAllElement();
	}
	
	@Test
	public void testVisit()
	{
		TestingVisitor visitor = new TestingVisitor();
		passedElement = new TestingAllElement()
		{
			@Override
			public <R, P> R accept(ElementVisitor<R, P> v, P p)
			{
				assertSame(visitor, v);
				return v.visitUnknown(this, p);
			}
		};
		
		assertEquals("Output", visitor.visit(passedElement, "Input"));
		assertEquals("Error", visitor.visit(passedElement));
	}
	
	@Test
	public void testVisitPackage()
	{
		assertEquals("Output", new TestingVisitor().visitPackage(passedElement, "Input"));
	}
	
	@Test
	public void testVisitType()
	{
		assertEquals("Output", new TestingVisitor().visitType(passedElement, "Input"));
	}
	
	@Test
	public void testVisitVariable()
	{
		assertEquals("Output", new TestingVisitor().visitVariable(passedElement, "Input"));
	}
	
	@Test
	public void testVisitExecutable()
	{
		assertEquals("Output", new TestingVisitor().visitExecutable(passedElement, "Input"));
	}
	
	@Test
	public void testVisitTypeParameter()
	{
		assertEquals("Output", new TestingVisitor().visitTypeParameter(passedElement, "Input"));
	}
	
	@Test
	public void testVisitUnknown()
	{
		assertEquals("Output", new TestingVisitor().visitUnknown(passedElement, "Input"));
	}
	
	private class TestingVisitor implements DefaultElementVisitor<String, String>
	{

		@Override
		public String visitDefault(Element e, String p)
		{
			assertSame(passedElement, e);
			return p == "Input" ? "Output" : "Error";
		}
		
	}
	
}
