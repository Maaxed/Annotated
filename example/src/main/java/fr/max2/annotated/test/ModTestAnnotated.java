package fr.max2.annotated.test;

import static fr.max2.annotated.test.ModTestAnnotated.*;

import fr.max2.annotated.test.init.ModKeyBindings;
import fr.max2.annotated.test.network.packet.SimpleData_Packets;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod(MOD_ID)
public class ModTestAnnotated
{
	public static final String MOD_ID = "annotatedexample";
	private static final String PROTOCOL_VERSION = "1";

	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
		new ResourceLocation(MOD_ID, "main"),
		() -> PROTOCOL_VERSION,
		PROTOCOL_VERSION::equals,
		PROTOCOL_VERSION::equals
	);

	public ModTestAnnotated()
	{
	    IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
	    modEventBus.addListener(ModTestAnnotated::preInit);
	    modEventBus.addListener(ModTestAnnotated::preCommonInit);
	}

	public static void preCommonInit(FMLCommonSetupEvent event)
	{
	    SimpleData_Packets.sendInt.registerTo(CHANNEL, 0);
	}

	public static void preInit(FMLClientSetupEvent event)
	{
		if (FMLEnvironment.dist == Dist.CLIENT)
		{
			ModKeyBindings.init();
		}
	}
}
