package fr.max2.annotated.test.init;

import org.lwjgl.input.Keyboard;

import fr.max2.annotated.test.client.gui.GuiTest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModKeyBindings
{
	public static final KeyBinding OPEN_GUI = new KeyBinding("key.open_test_gui", KeyConflictContext.IN_GAME, Keyboard.KEY_Y, "key.category.test_mod");
	
	public static void init()
	{
		ClientRegistry.registerKeyBinding(OPEN_GUI);
	}
	
	@SubscribeEvent
	public void onKeyPressed(KeyInputEvent event)
	{
		if (OPEN_GUI.isPressed())
		{
			Minecraft.getMinecraft().displayGuiScreen(new GuiTest());
		}
	}
}
