package fr.max2.packeta.network.datahandler;

import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import fr.max2.packeta.api.network.ConstSize;
import fr.max2.packeta.network.DataHandlerParameters;
import fr.max2.packeta.utils.EmptyAnnotationConstruct;
import fr.max2.packeta.utils.NamingUtils;
import fr.max2.packeta.utils.ValueInitStatus;

public enum ArrayDataHandler implements IDataHandler
{
	INSTANCE;

	@Override
	public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
	{
		ArrayType arrayType = (ArrayType)params.type;
		TypeMirror contentType = arrayType.getComponentType();
		String typeName = NamingUtils.simpleTypeName(contentType);
		String arrayTypeName = typeName + "[]";
		
		boolean constSize = params.annotations.getAnnotation(ConstSize.class) != null || params.type.getAnnotation(ConstSize.class) != null;
		
		int i;
		for (i = arrayTypeName.length() - 2; i >= 2 && arrayTypeName.substring(i - 2, i).equals("[]"); i-=2);
		
		String elementVarName = params.simpleName + "Element";
		if (!constSize) saveInstructions.accept(DataHandlerUtils.writeBuffer("Int", params.getExpr + ".length"));
		saveInstructions.accept("for (" + typeName + " " + elementVarName + " : " + params.getExpr + ")");
		saveInstructions.accept("{");
		
		String lenghtVarName;
		String indexVarName = params.simpleName + "Index";
		
		if (constSize)
		{
			lenghtVarName = params.simpleName + ".length";
		}
		else
		{
			lenghtVarName = params.simpleName + "Length";
			loadInstructions.accept(DataHandlerUtils.readBuffer("Int", "int " + lenghtVarName));
			loadInstructions.accept(params.firstSetInit() + " = new " + arrayTypeName.substring(0, i + 1) + lenghtVarName + arrayTypeName.substring(i + 1) + ";" );
		}
		
		loadInstructions.accept("for (int " + indexVarName + " = 0; " + indexVarName + " < " + lenghtVarName + "; " + indexVarName + "++)");
		loadInstructions.accept("{");
		
		DataHandlerParameters contentHandler = params.finder.getDataType(elementVarName, elementVarName, params.setExpr + "[" + indexVarName + "]", contentType, EmptyAnnotationConstruct.INSTANCE, ValueInitStatus.DECLARED);
		contentHandler.addInstructions(inst -> saveInstructions.accept("\t" + inst), inst -> loadInstructions.accept("\t" + inst), imports);
		
		saveInstructions.accept("}");
		
		loadInstructions.accept("}");
	}

	@Override
	public Predicate<TypeMirror> getTypeValidator(Elements elemUtils, Types typeUtils)
	{
		return t -> t.getKind() == TypeKind.ARRAY;
	}
	
}
