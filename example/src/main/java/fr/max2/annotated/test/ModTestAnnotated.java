package fr.max2.annotated.test;

import static fr.max2.annotated.test.ModTestAnnotated.*;

import fr.max2.annotated.test.init.ModKeyBindings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MOD_ID)
public class ModTestAnnotated
{
	public static final String MOD_ID = "annotatedexample";
	
	public ModTestAnnotated()
	{
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(ModTestAnnotated::preInit);
	}
	
	public static void preInit(FMLClientSetupEvent event)
	{
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> ModKeyBindings.init());
	}
}
