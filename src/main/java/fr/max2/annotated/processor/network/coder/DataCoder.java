package fr.max2.annotated.processor.network.coder;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PropertyMap;

public abstract class DataCoder
{
	public final ProcessingTools tools;
	public final String uniqueName;
	public final TypeMirror paramType;
	public final PropertyMap properties;
	protected TypeMirror internalType;
	
	public DataCoder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties)
	{
		this(tools, uniqueName, paramType, properties, paramType);
	}
	
	public DataCoder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties, TypeMirror internalType)
	{
		this.tools = tools;
		this.uniqueName = uniqueName;
		this.paramType = paramType;
		this.properties = properties;
		this.internalType = internalType;
	}
	
	public TypeMirror getInternalType()
	{
		return this.internalType;
	}
	
	public boolean requireConversion()
	{
		return !this.tools.types.isSameType(this.internalType, this.paramType);
	}
	
	public abstract OutputExpressions addInstructions(IPacketBuilder builder, String saveAccessExpr);
	
	public OutputExpressions addInstructions(int indent, IPacketBuilder builder, String saveAccessExpr)
	{
		builder.encoder().indent(indent);
		builder.decoder().indent(indent);
		OutputExpressions output = this.addInstructions(builder, saveAccessExpr);
		builder.encoder().indent(-indent);
		builder.decoder().indent(-indent);
		return output;
	}
	
	public static final class OutputExpressions
	{
		public final String decoded;

		public OutputExpressions(String decodedOutput)
		{
			this.decoded = decodedOutput;
		}
	}
}
