package fr.max2.packeta.processor.utils;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static fr.max2.packeta.processor.utils.EnumSides.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import fr.max2.packeta.lib.network.IClientMessage;
import fr.max2.packeta.lib.network.IServerMessage;


public class EnumSidesTest
{
	
	@Test
	public void testIsClient()
	{
		assertTrue(CLIENT.isClient());
		assertTrue(BOTH.isClient());
		
		assertFalse(SERVER.isClient());
	}
	
	@Test
	public void testIsServer()
	{
		assertFalse(CLIENT.isServer());
		
		assertTrue(BOTH.isServer());
		assertTrue(SERVER.isServer());
	}
	
	@Test
	public void testGetInterfaces()
	{
		String clientInterface = IClientMessage.class.getSimpleName();
		String serverInterface = IServerMessage.class.getSimpleName();
		assertEquals(clientInterface, CLIENT.getInterfaces());
		assertEquals(serverInterface, SERVER.getInterfaces());
		
		assertThat(BOTH.getInterfaces().replaceAll("\\s",""),
			anyOf(equalTo(clientInterface + ',' + serverInterface),
				  equalTo(serverInterface + ',' + clientInterface)));
	}
	
	@Test
	public void testGetSimpleName()
	{
		assertEquals("Client", CLIENT.getSimpleName());
		assertEquals("Server", SERVER.getSimpleName());
		assertEquals("BothSides", BOTH.getSimpleName());
	}
	
	@Test
	public void testAddImports()
	{
		String clientImport = IClientMessage.class.getCanonicalName();
		String serverImport = IServerMessage.class.getCanonicalName();
		
		List<String> imports = new ArrayList<>();
		
		CLIENT.addImports(imports::add);
		assertEquals(1, imports.size());
		assertThat(imports, hasItems(clientImport));
		
		imports.clear();
		SERVER.addImports(imports::add);
		assertEquals(1, imports.size());
		assertThat(imports, hasItems(serverImport));

		imports.clear();
		BOTH.addImports(imports::add);
		assertEquals(2, imports.size());
		assertThat(imports, hasItems(clientImport, serverImport));
		
	}
	
}
