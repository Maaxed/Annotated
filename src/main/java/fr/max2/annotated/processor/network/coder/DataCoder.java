package fr.max2.annotated.processor.network.coder;

import java.util.function.BiConsumer;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.DataCoderParameters;
import fr.max2.annotated.processor.network.model.IFunctionBuilder;
import fr.max2.annotated.processor.network.model.IPacketBuilder;

public abstract class DataCoder
{
	public DataCoderParameters params;
	protected TypeMirror codedType;
	
	public void init(DataCoderParameters params)
	{
		this.params = params;
		this.codedType = params.type;
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
