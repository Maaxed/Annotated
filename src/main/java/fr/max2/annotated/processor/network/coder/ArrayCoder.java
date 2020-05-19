package fr.max2.annotated.processor.network.coder;

import java.util.function.BiConsumer;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.IDataHandler;
import fr.max2.annotated.processor.network.coder.handler.SimpleDataHandler;
import fr.max2.annotated.processor.network.model.IFunctionBuilder;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PropertyMap;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;

public class ArrayCoder extends DataCoder
{
	public static final IDataHandler HANDLER = new SimpleDataHandler(t -> t.getKind() == TypeKind.ARRAY, ArrayCoder::new);

	private DataCoder contentCoder;
	
	@Override
	public void init(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties)
	{
		super.init(tools, uniqueName, paramType, properties);
		
		ArrayType arrayType = tools.types.asArrayType(paramType);
		if (arrayType == null) throw new IncompatibleTypeException("The type '" + paramType + "' is not an array");
		TypeMirror contentType = arrayType.getComponentType();
		
		this.contentCoder = tools.handlers.getDataType(uniqueName + "Element", contentType, properties.getSubPropertiesOrEmpty("content"));
		this.codedType = tools.types.getArrayType(this.contentCoder.getCodedType());
	}
	
	@Override
	public void addInstructions(IPacketBuilder builder, String saveAccessExpr, BiConsumer<IFunctionBuilder, String> setExpr)
	{
		String contentTypeName = tools.naming.computeFullName(this.contentCoder.getCodedType());
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
		
		contentCoder.addInstructions(1, builder, elementVarName, (loadInst, value) -> loadInst.add(uniqueName + "[" + indexVarName + "] = " + value + ";"));
		
		builder.encoder().add("}");
		
		builder.decoder().add("}");
		
		setExpr.accept(builder.decoder(), uniqueName); 
	}
}
