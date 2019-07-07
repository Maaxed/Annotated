package fr.max2.annotated.test;

import static fr.max2.annotated.test.ModTestAnnotated.*;

import fr.max2.annotated.api.processor.network.GenerateNetwork;
import fr.max2.annotated.test.ModTestAnnotatedNetwork;
import fr.max2.annotated.test.init.ModKeyBindings;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@GenerateNetwork
@Mod(modid = MOD_ID, name = MOD_NAME, version = VERSION, acceptedMinecraftVersions = "[1.9, 1.12.2]")
public class ModTestAnnotated
{
	public static final String MOD_ID = "annotatedtest";
	public static final String MOD_NAME = "Test Annotated Mod";
	public static final String VERSION = "@VERSION@";

	public static final SimpleNetworkWrapper MOD_CHANNEL = ModTestAnnotatedNetwork.initNetwork();
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		ModKeyBindings.init();
		MinecraftForge.EVENT_BUS.register(new ModKeyBindings());
	}
}
