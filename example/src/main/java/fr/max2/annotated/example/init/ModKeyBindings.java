package fr.max2.annotated.example.init;

import org.lwjgl.glfw.GLFW;

import fr.max2.annotated.example.ModTestAnnotated;
import fr.max2.annotated.example.client.gui.GuiTest;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = ModTestAnnotated.MOD_ID, value = Dist.CLIENT)
public class ModKeyBindings
{
    public static final KeyMapping OPEN_GUI = new KeyMapping("key.open_test_gui", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Y, "key.category.test_mod");

    public static void init()
    {
        ClientRegistry.registerKeyBinding(OPEN_GUI);
    }

    @SubscribeEvent
    public static void onKeyPressed(KeyInputEvent event)
    {
        if (OPEN_GUI.consumeClick())
        {
            Minecraft.getInstance().setScreen(new GuiTest());
        }
    }
}