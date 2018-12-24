package fr.max2.packeta.test;

import static fr.max2.packeta.test.ModTestPacketa.*;

import fr.max2.packeta.api.network.GenerateNetwork;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@GenerateNetwork
@Mod(modid = MOD_ID, name = MOD_NAME, version = VERSION)
public class ModTestPacketa
{
	public static final String MOD_ID = "packetatest";
	public static final String MOD_NAME = "Test Packeta Mod";
	public static final String VERSION = "@VERSION@";

	public static final SimpleNetworkWrapper MOD_CHANNEL = ModTestPacketaNetwork.initNetwork();

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		
	}

}
