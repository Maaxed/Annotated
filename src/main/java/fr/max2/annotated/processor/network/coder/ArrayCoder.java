package fr.max2.annotated.processor.network.coder;

import java.util.function.BiConsumer;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.DataCoderParameters;
import fr.max2.annotated.processor.network.coder.handler.IDataHandler;
import fr.max2.annotated.processor.network.coder.handler.SimpleDataHandler;
import fr.max2.annotated.processor.network.model.IFunctionBuilder;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;

public class ArrayCoder extends DataCoder
{
	public static final IDataHandler HANDLER = new SimpleDataHandler(t -> t.getKind() == TypeKind.ARRAY, ArrayCoder::new);

	private DataCoder contentCoder;
	
	@Override
	public void init(DataCoderParameters params)
	{
		this.params = params;
		
		ArrayType arrayType = params.tools.types.asArrayType(params.type);
		if (arrayType == null) throw new IncompatibleTypeException("The type '" + params.type + "' is not an array");
		TypeMirror contentType = arrayType.getComponentType();
		
		this.contentCoder = params.tools.handlers.getDataType(params.uniqueName + "Element", contentType, params.properties.getSubPropertiesOrEmpty("content"));
		this.codedType = params.tools.types.getArrayType(this.contentCoder.getCodedType());
	}
	
	@Override
	public void addInstructions(IPacketBuilder builder, String saveAccessExpr, BiConsumer<IFunctionBuilder, String> setExpr)
	{
		String contentTypeName = params.tools.naming.computeFullName(this.contentCoder.getCodedType());
		String elementVarName = params.uniqueName + "Element";
		builder.encoder().add(
			DataCoderUtils.writeBuffer("Int", saveAccessExpr + ".length"),
			"for (" + contentTypeName + " " + elementVarName + " : " + saveAccessExpr + ")",
			"{");
		
		
		String arrayTypeName = contentTypeName + "[]";
		
		int firstBrackets;
		for (firstBrackets = arrayTypeName.length() - 2; firstBrackets >= 2 && arrayTypeName.substring(firstBrackets - 2, firstBrackets).equals("[]"); firstBrackets-=2);

		String indexVarName = params.uniqueName + "Index";
		builder.decoder().add(
			arrayTypeName + " " + params.uniqueName + " = new " + arrayTypeName.substring(0, firstBrackets + 1) + DataCoderUtils.readBuffer("Int") + arrayTypeName.substring(firstBrackets + 1) + ";",
			"for (int " + indexVarName + " = 0; " + indexVarName + " < " + params.uniqueName + ".length; " + indexVarName + "++)",
			"{");
		
		contentCoder.addInstructions(1, builder, elementVarName, (loadInst, value) -> loadInst.add(params.uniqueName + "[" + indexVarName + "] = " + value + ";"));
		
		builder.encoder().add("}");
		
		builder.decoder().add("}");
		
		setExpr.accept(builder.decoder(), params.uniqueName); 
	}
}
