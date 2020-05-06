package fr.max2.annotated.processor.network.datahandler;

import java.util.function.Predicate;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import fr.max2.annotated.processor.network.DataHandlerParameters;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.EmptyAnnotationConstruct;
import fr.max2.annotated.processor.utils.NamingUtils;
import fr.max2.annotated.processor.utils.TypeHelper;

public enum ArrayDataHandler implements IDataHandler
{
	INSTANCE;

	@Override
	public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
	{
		ArrayType arrayType = TypeHelper.asArrayType(params.type);
		if (arrayType == null) throw new IllegalArgumentException("The type '" + params.type + "' is not an array");
		
		TypeMirror contentType = arrayType.getComponentType();
		String typeName = NamingUtils.computeFullName(contentType);
		
		String elementVarName = params.uniqueName + "Element";
		builder.encoder().add(
			DataHandlerUtils.writeBuffer("Int", params.saveAccessExpr + ".length"),
			"for (" + typeName + " " + elementVarName + " : " + params.saveAccessExpr + ")",
			"{");
		
		
		String arrayTypeName = typeName + "[]";
		
		int firstBrackets;
		for (firstBrackets = arrayTypeName.length() - 2; firstBrackets >= 2 && arrayTypeName.substring(firstBrackets - 2, firstBrackets).equals("[]"); firstBrackets-=2);

		String indexVarName = params.uniqueName + "Index";
		builder.decoder().add(
			arrayTypeName + " " + params.uniqueName + " = new " + arrayTypeName.substring(0, firstBrackets + 1) + DataHandlerUtils.readBuffer("Int") + arrayTypeName.substring(firstBrackets + 1) + ";",
			"for (int " + indexVarName + " = 0; " + indexVarName + " < " + params.uniqueName + ".length; " + indexVarName + "++)",
			"{");
		
		DataHandlerParameters contentHandler = params.finder.getDataType(elementVarName, elementVarName, (loadInst, value) -> loadInst.add(params.uniqueName + "[" + indexVarName + "] = " + value + ";"), contentType, EmptyAnnotationConstruct.INSTANCE);
		
		builder.encoder().indent(1);
		builder.decoder().indent(1);
		contentHandler.addInstructions(builder);
		builder.encoder().indent(-1);
		builder.decoder().indent(-1);
		
		builder.encoder().add("}");
		
		builder.decoder().add("}");
		
		params.setExpr.accept(builder.decoder(), params.uniqueName); 
	}

	@Override
	public Predicate<TypeMirror> getTypeValidator(Elements elemUtils, Types typeUtils)
	{
		return t -> t.getKind() == TypeKind.ARRAY;
	}
	
}
