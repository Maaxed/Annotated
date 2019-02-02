package fr.max2.packeta.test.init;

import org.lwjgl.input.Keyboard;

import fr.max2.packeta.test.client.gui.GuiTest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@EventBusSubscriber(Side.CLIENT)
public class ModKeyBindings
{
	private ModKeyBindings() { }
	
	
	public static final KeyBinding OPEN_GUI = new KeyBinding("key.open_test_gui", KeyConflictContext.IN_GAME, Keyboard.KEY_Y, "key.category.test_mod");
	
	public static void redisterKeyBindings()
	{
		ClientRegistry.registerKeyBinding(OPEN_GUI);
	}
	
	@SubscribeEvent
	public static void onKeyPressed(KeyInputEvent event)
	{
		if (OPEN_GUI.isPressed())
		{
			Minecraft.getMinecraft().displayGuiScreen(new GuiTest());
		}
	}
}
