package fr.max2.annotated.processor.util;

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

		assertEquals(this.intElement, this.helper.asTypeElement(this.intElement));
		assertNull(this.helper.asTypeElement(this.packageType));
	}

	@Test
	public void testAsPackage()
	{
		this.setUpModel();

		assertEquals(this.packageType, this.helper.asPackage(this.packageType));
		assertNull(this.helper.asPackage(this.intElement));
	}
}
