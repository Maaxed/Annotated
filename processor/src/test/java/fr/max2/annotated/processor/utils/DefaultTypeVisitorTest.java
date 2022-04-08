package fr.max2.annotated.processor.utils;

import static org.junit.Assert.*;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import org.junit.Before;
import org.junit.Test;

import fr.max2.annotated.processor.utils.model.type.TestingAllType;


public class DefaultTypeVisitorTest
{
	private TestingAllType passedType;
	
	@Before
	public void setUp()
	{
		this.passedType = new TestingAllType();
	}
	
	@Test
	public void testVisit()
	{
		TestingVisitor visitor = new TestingVisitor();
		this.passedType = new TestingAllType()
		{
			@Override
			public <R, P> R accept(TypeVisitor<R, P> v, P p)
			{
				assertSame(visitor, v);
				return v.visitUnknown(this, p);
			}
		};
		
		assertEquals("Output", visitor.visit(this.passedType, "Input"));
		assertEquals("Error", visitor.visit(this.passedType));
	}
	
	@Test
	public void testVisitPrimitive()
	{
		assertEquals("Output", new TestingVisitor().visitPrimitive(this.passedType, "Input"));
	}
	
	@Test
	public void testVisitNull()
	{
		assertEquals("Output", new TestingVisitor().visitNull(this.passedType, "Input"));
	}
	
	@Test
	public void testVisitArray()
	{
		assertEquals("Output", new TestingVisitor().visitArray(this.passedType, "Input"));
	}
	
	@Test
	public void testVisitDeclared()
	{
		assertEquals("Output", new TestingVisitor().visitDeclared(this.passedType, "Input"));
	}
	
	@Test
	public void testVisitError()
	{
		assertEquals("Output", new TestingVisitor().visitError(this.passedType, "Input"));
	}
	
	@Test
	public void testVisitTypeVariable()
	{
		assertEquals("Output", new TestingVisitor().visitTypeVariable(this.passedType, "Input"));
	}
	
	@Test
	public void testVisitWildcard()
	{
		assertEquals("Output", new TestingVisitor().visitWildcard(this.passedType, "Input"));
	}
	
	@Test
	public void testVisitExecutable()
	{
		assertEquals("Output", new TestingVisitor().visitExecutable(this.passedType, "Input"));
	}
	
	@Test
	public void testVisitNoType()
	{
		assertEquals("Output", new TestingVisitor().visitNoType(this.passedType, "Input"));
	}
	
	@Test
	public void testVisitUnion()
	{
		assertEquals("Output", new TestingVisitor().visitUnion(this.passedType, "Input"));
	}
	
	@Test
	public void testVisitIntersection()
	{
		assertEquals("Output", new TestingVisitor().visitIntersection(this.passedType, "Input"));
	}
	
	@Test
	public void testVisitUnknown()
	{
		assertEquals("Output", new TestingVisitor().visitUnknown(this.passedType, "Input"));
	}
	
	private class TestingVisitor implements DefaultTypeVisitor<String, String>
	{

		@Override
		public String visitDefault(TypeMirror e, String p)
		{
			assertSame(DefaultTypeVisitorTest.this.passedType, e);
			return p == "Input" ? "Output" : "Error";
		}
		
	}
	
}
