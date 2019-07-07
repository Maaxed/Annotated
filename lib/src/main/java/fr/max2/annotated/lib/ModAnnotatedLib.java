package fr.max2.annotated.lib;

import static fr.max2.annotated.lib.ModAnnotatedLib.*;

import net.minecraftforge.fml.common.Mod;

@Mod(modid = MOD_ID, name = MOD_NAME, version = VERSION, acceptedMinecraftVersions = "[1.9, 1.12.2]")
public class ModAnnotatedLib
{
	public static final String MOD_ID = "annotated";
	public static final String MOD_NAME = "Annotated Lib Mod";
	public static final String VERSION = "@VERSION@";
}
