package fr.max2.annotated.processor.network.coder;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.IDataHandler;
import fr.max2.annotated.processor.network.coder.handler.SpecialDataHandler;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PropertyMap;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;

public class ArrayCoder extends DataCoder
{
	public static final IDataHandler HANDLER = new SpecialDataHandler(TypeKind.ARRAY, ArrayCoder::new);

	private DataCoder contentCoder;
	
	public ArrayCoder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties)
	{
		super(tools, uniqueName, paramType, properties);
		
		ArrayType arrayType = tools.types.asArrayType(paramType);
		if (arrayType == null) throw new IncompatibleTypeException("The type '" + paramType + "' is not an array");
		TypeMirror contentType = arrayType.getComponentType();
		
		this.contentCoder = tools.handlers.getDataType(uniqueName + "Element", contentType, properties.getSubPropertiesOrEmpty("content"));
		this.internalType = tools.types.getArrayType(this.contentCoder.getInternalType());
	}
	
	@Override
	public OutputExpressions addInstructions(IPacketBuilder builder, String saveAccessExpr)
	{
		String contentTypeName = tools.naming.computeFullName(this.contentCoder.getInternalType());
		String elementVarName = uniqueName + "Element";
		builder.encoder().add(
			DataCoderUtils.writeBuffer("Int", saveAccessExpr + ".length"),
			"for (" + contentTypeName + " " + elementVarName + " : " + saveAccessExpr + ")",
			"{");
		
		
		String arrayTypeName = contentTypeName + "[]";
		
		int firstBrackets;
		for (firstBrackets = arrayTypeName.length() - 2; firstBrackets >= 2 && arrayTypeName.substring(firstBrackets - 2, firstBrackets).equals("[]"); firstBrackets-=2);

		String indexVarName = uniqueName + "Index";
		builder.decoder().add(
			arrayTypeName + " " + uniqueName + " = new " + arrayTypeName.substring(0, firstBrackets + 1) + DataCoderUtils.readBuffer("Int") + arrayTypeName.substring(firstBrackets + 1) + ";",
			"for (int " + indexVarName + " = 0; " + indexVarName + " < " + uniqueName + ".length; " + indexVarName + "++)",
			"{");
		
		OutputExpressions contentOutput = contentCoder.addInstructions(1, builder, elementVarName);
		builder.decoder().add(uniqueName + "[" + indexVarName + "] = " + contentOutput.decoded + ";");
		
		builder.encoder().add("}");
		builder.decoder().add("}");
		
		return new OutputExpressions(this.uniqueName); 
	}
}
