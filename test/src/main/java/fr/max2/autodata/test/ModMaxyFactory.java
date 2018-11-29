package fr.max2.autodata.test;

import static fr.max2.autodata.test.ModMaxyFactory.*;

import fr.max2.autodata.network.ModNetwork;
import fr.max2.autodata.test.network.TestDataMessage;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = MOD_ID, name = MOD_NAME, version = VERSION)
public class ModMaxyFactory
{
	public static final String MOD_ID = "autodatatest";
	public static final String MOD_NAME = "Test AutoData Mod";
	public static final String VERSION = "1.0";

	/*@SidedProxy(clientSide = "fr.max2.autodata.test.proxy.ClientProxy",
				serverSide = "fr.max2.autodata.test.proxy.CommonProxy")
	public static CommonProxy proxy;*/
	
	static
	{
		FluidRegistry.enableUniversalBucket();
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		//proxy.preInit();
		ModNetwork.next();
		ModNetwork.registerServer(TestDataMessage.class);
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		//proxy.init();
	}

}
