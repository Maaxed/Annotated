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
		
		String elementVarName = params.simpleName + "Element";
		builder.save().add(
			DataHandlerUtils.writeBuffer("Int", params.saveAccessExpr + ".length"),
			"for (" + typeName + " " + elementVarName + " : " + params.saveAccessExpr + ")",
			"{");
		
		
		String arrayTypeName = typeName + "[]";
		
		int firstBrackets;
		for (firstBrackets = arrayTypeName.length() - 2; firstBrackets >= 2 && arrayTypeName.substring(firstBrackets - 2, firstBrackets).equals("[]"); firstBrackets-=2);

		String indexVarName = params.simpleName + "Index";
		String lenghtVarName = params.simpleName + "Length";
		builder.load().add("int " + lenghtVarName + " = " + DataHandlerUtils.readBuffer("Int") + ";");
		params.setLoadedValue(builder.load(), "new " + arrayTypeName.substring(0, firstBrackets + 1) + lenghtVarName + arrayTypeName.substring(firstBrackets + 1));
		
		builder.load().add(
			"for (int " + indexVarName + " = 0; " + indexVarName + " < " + lenghtVarName + "; " + indexVarName + "++)",
			"{");
		
		String getLoadExpr = params.getLoadAccessExpr() + "[" + indexVarName + "]";
		DataHandlerParameters contentHandler = params.finder.getDataType(elementVarName, elementVarName, getLoadExpr, (loadInst, value) -> loadInst.add(getLoadExpr + " = " + value + ";"), contentType, EmptyAnnotationConstruct.INSTANCE);
		
		builder.save().indent(1);
		builder.load().indent(1);
		contentHandler.addInstructions(builder);
		builder.save().indent(-1);
		builder.load().indent(-1);
		
		builder.save().add("}");
		
		builder.load().add("}");
		
		if (params.loadAccessExpr == null)
		{
			params.setExpr.accept(builder.load(), params.simpleName); 
		}
	}

	@Override
	public Predicate<TypeMirror> getTypeValidator(Elements elemUtils, Types typeUtils)
	{
		return t -> t.getKind() == TypeKind.ARRAY;
	}
	
}
