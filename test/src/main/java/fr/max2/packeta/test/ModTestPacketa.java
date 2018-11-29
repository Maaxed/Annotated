package fr.max2.packeta.test;

import static fr.max2.packeta.test.ModTestPacketa.*;

import fr.max2.packeta.network.ModNetwork;
import fr.max2.packeta.test.network.TestDataMessage;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = MOD_ID, name = MOD_NAME, version = VERSION)
public class ModTestPacketa
{
	public static final String MOD_ID = "packetatest";
	public static final String MOD_NAME = "Test Packeta Mod";
	public static final String VERSION = "1.0";
	
	static
	{
		FluidRegistry.enableUniversalBucket();
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		ModNetwork.next();
		ModNetwork.registerServer(TestDataMessage.class);
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		
	}

}
