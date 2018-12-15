package fr.max2.packeta.network.datahandler;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class DataHandlerUtils
{
	
	public static String writeBuffer(String type, String value)
	{
		return "buf.write" + type + "(" + value + ");";
	}
	
	public static String readBuffer(String type)
	{
		return "buf.read" + type + "();";
	}
	
	public static void addBufferInstructions(String type, String saveValue, UnaryOperator<String> loadValue, Consumer<String> saveInstructions, Consumer<String> loadInstructions)
	{
		saveInstructions.accept(writeBuffer(type, saveValue));
		loadInstructions.accept(loadValue.apply(readBuffer(type)));
	}
	
	public static String writeBufferUtils(String type, String value)
	{
		return "ByteBufUtils.write" + type + "(buf, " + value + ");";
	}

	public static String readBufferUtils(String type)
	{
		return "ByteBufUtils.read" + type + "(buf);";
	}
	
	public static void addBufferUtilsInstructions(String type, String saveValue, UnaryOperator<String> loadValue, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
	{
		saveInstructions.accept(writeBufferUtils(type, saveValue));
		loadInstructions.accept(loadValue.apply(readBufferUtils(type)));
		imports.accept("net.minecraftforge.fml.common.network.ByteBufUtils");
	}
	
}
