package fr.max2.annotated.test.init;

import org.lwjgl.glfw.GLFW;

import fr.max2.annotated.test.ModTestAnnotated;
import fr.max2.annotated.test.client.gui.GuiTest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = ModTestAnnotated.MOD_ID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class ModKeyBindings
{
	public static final KeyBinding OPEN_GUI = new KeyBinding("key.open_test_gui", KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_Y, "key.category.test_mod");
	
	public static void init()
	{
		ClientRegistry.registerKeyBinding(OPEN_GUI);
	}
	
	@SubscribeEvent
	public static void onKeyPressed(KeyInputEvent event)
	{
		if (OPEN_GUI.isPressed())
		{
			Minecraft.getInstance().displayGuiScreen(new GuiTest());
		}
	}
}
