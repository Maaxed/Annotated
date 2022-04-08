package fr.max2.annotated.processor.network.coder;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.IDataCoderProvider;
import fr.max2.annotated.processor.network.coder.handler.IHandlerProvider;
import fr.max2.annotated.processor.network.coder.handler.NamedDataHandler;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PropertyMap;

public class SimpleDataCoder extends DataCoder
{
	private final String typeName;
	private final String writeSRGName;
	private final String readSRGName;

	public SimpleDataCoder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties, String typeName, String writeSRGName, String readSRGName)
	{
		super(tools, uniqueName, paramType, properties);
		this.typeName = typeName;
		this.writeSRGName = writeSRGName;
		this.readSRGName = readSRGName;
	}

	@Override
	public OutputExpressions addInstructions(IPacketBuilder builder, String saveAccessExpr, String internalAccessExpr, String externalAccessExpr)
	{
		return new OutputExpressions(this.addBufferInstructions(this.typeName, saveAccessExpr, this.writeSRGName, this.readSRGName, builder), internalAccessExpr, externalAccessExpr);
	}
	
	public static IDataCoderProvider build(String type, String writeSRGName, String readSRGName)
	{
		return (tools, uniqueName, paramType, properties) -> new SimpleDataCoder(tools, uniqueName, paramType, properties, type, writeSRGName, readSRGName);
	}
	
	public static IHandlerProvider handler(String className, String typeName, String writeSRGName, String readSRGName)
	{
		return NamedDataHandler.provider(className, build(typeName, writeSRGName, readSRGName));
	}
	
	public static IHandlerProvider handler(Class<?> clazz, String typeName, String writeSRGName, String readSRGName)
	{
		return handler(clazz.getTypeName(), typeName, writeSRGName, readSRGName);
	}
}