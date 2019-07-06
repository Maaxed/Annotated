package fr.max2.annotated.processor.network.datahandler;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DataHandlerUtils
{
	private DataHandlerUtils() { }
	
	public static String writeBuffer(String type, String value)
	{
		return "buf.write" + type + "(" + value + ");";
	}
	
	public static String readBuffer(String type)
	{
		return "buf.read" + type + "()";
	}
	
	public static void addBufferInstructions(String type, String saveValue, BiConsumer<Consumer<String>, String> loadValue, Consumer<String> saveInstructions, Consumer<String> loadInstructions)
	{
		saveInstructions.accept(writeBuffer(type, saveValue));
		loadValue.accept(loadInstructions, readBuffer(type));
	}
	
	public static String writeBufferUtils(String type, String value)
	{
		return "ByteBufUtils.write" + type + "(buf, " + value + ");";
	}

	public static String readBufferUtils(String type)
	{
		return "ByteBufUtils.read" + type + "(buf)";
	}
	
	public static void addBufferUtilsInstructions(String type, String saveValue, BiConsumer<Consumer<String>, String> loadValue, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
	{
		saveInstructions.accept(writeBufferUtils(type, saveValue));
		loadValue.accept(loadInstructions, readBufferUtils(type));
		imports.accept("net.minecraftforge.fml.common.network.ByteBufUtils");
	}
	
}
