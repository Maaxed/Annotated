package fr.max2.annotated.processor.network.datahandler;

import java.util.function.BiConsumer;

import fr.max2.annotated.processor.network.model.IFunctionBuilder;
import fr.max2.annotated.processor.network.model.IPacketBuilder;

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
	
	public static void addBufferInstructions(String type, String saveValue, BiConsumer<IFunctionBuilder, String> loadValue, IPacketBuilder builder)
	{
		builder.encoder().add(writeBuffer(type, saveValue));
		loadValue.accept(builder.decoder(), readBuffer(type));
	}
	
}
