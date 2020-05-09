package fr.max2.annotated.processor.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;


public class ExtendedElementsTest extends TestModelProvider
{
	private ExtendedElements helper = new ExtendedElements(null, null);
	
	@Test
	public void testAsTypeElement()
	{
		this.setUpModel();

		assertEquals(this.intElement, helper.asTypeElement(this.intElement));
		assertNull(helper.asTypeElement(this.packageType));
	}
	
	@Test
	public void testAsPackage()
	{
		this.setUpModel();

		assertEquals(this.packageType, helper.asPackage(this.packageType));
		assertNull(helper.asPackage(this.intElement));
	}
}
