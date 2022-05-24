package fr.max2.annotated.example.minimal;

import com.mojang.blaze3d.platform.InputConstants;

import org.lwjgl.glfw.GLFW;

import fr.max2.annotated.api.network.ServerPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod(MinimalExampleMod.MOD_ID)
public class MinimalExampleMod
{
	public static final String MOD_ID = "annotatedminimalexample";
	private static final String PROTOCOL_VERSION = "1";

	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
		new ResourceLocation(MOD_ID, "main"),
		() -> PROTOCOL_VERSION,
		PROTOCOL_VERSION::equals,
		PROTOCOL_VERSION::equals
	);

	public MinimalExampleMod()
	{
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(MinimalExampleMod::preInit);
		MinimalExampleMod_Packets.sendInt.registerTo(CHANNEL, 0);
	}

	public static void preInit(FMLClientSetupEvent event)
	{
		if (FMLEnvironment.dist == Dist.CLIENT)
		{
			ClientStuff.initKeyBindings();
		}
	}

	@ServerPacket
	public static void sendInt(ServerPlayer sender, int myInt)
	{
		System.out.println("Function called on " + (sender.level.isClientSide ? "CLIENT" : "SERVER"));
		System.out.println("My int value is " + myInt);
	}

	@EventBusSubscriber(modid = MinimalExampleMod.MOD_ID, value = Dist.CLIENT)
	private static class ClientStuff
	{
		public static final KeyMapping OPEN_GUI = new KeyMapping("key.example_send_int", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, "key.category" + MOD_ID);

		public static void initKeyBindings()
		{
			ClientRegistry.registerKeyBinding(OPEN_GUI);
		}

		@SubscribeEvent
		public static void onKeyPressed(KeyInputEvent event)
		{
			if (OPEN_GUI.consumeClick())
			{
            	CHANNEL.sendToServer(MinimalExampleMod_Packets.sendInt(5));
			}
		}
	}
}
