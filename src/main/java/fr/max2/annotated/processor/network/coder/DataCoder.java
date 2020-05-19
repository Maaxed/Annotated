package fr.max2.annotated.processor.network.coder;

import java.util.function.BiConsumer;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.model.IFunctionBuilder;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PropertyMap;

public abstract class DataCoder
{
	public final ProcessingTools tools;
	public final String uniqueName;
	public final TypeMirror paramType;
	public final PropertyMap properties;
	protected TypeMirror codedType;
	
	public DataCoder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties)
	{
		this(tools, uniqueName, paramType, properties, paramType);
	}
	
	public DataCoder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties, TypeMirror codedType)
	{
		this.tools = tools;
		this.uniqueName = uniqueName;
		this.paramType = paramType;
		this.properties = properties;
		this.codedType = codedType;
	}
	
	public TypeMirror getCodedType()
	{
		return this.codedType;
	}
	
	public abstract void addInstructions(IPacketBuilder builder, String saveAccessExpr, BiConsumer<IFunctionBuilder, String> setExpr);
	
	public void addInstructions(int indent, IPacketBuilder builder, String saveAccessExpr, BiConsumer<IFunctionBuilder, String> setExpr)
	{
		builder.encoder().indent(indent);
		builder.decoder().indent(indent);
		this.addInstructions(builder, saveAccessExpr, setExpr);
		builder.encoder().indent(-indent);
		builder.decoder().indent(-indent);
	}
}
