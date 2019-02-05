package fr.max2.packeta.processor.network.datahandler;

import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import fr.max2.packeta.api.processor.network.ConstSize;
import fr.max2.packeta.processor.network.DataHandlerParameters;
import fr.max2.packeta.processor.utils.EmptyAnnotationConstruct;
import fr.max2.packeta.processor.utils.NamingUtils;
import fr.max2.packeta.processor.utils.TypeHelper;
import fr.max2.packeta.processor.utils.ValueInitStatus;

public enum ArrayDataHandler implements IDataHandler
{
	INSTANCE;

	@Override
	public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
	{
		ArrayType arrayType = TypeHelper.asArrayType(params.type);
		if (arrayType == null) throw new IllegalArgumentException("The type '" + params.type + "' is not an array");
		
		TypeMirror contentType = arrayType.getComponentType();
		String typeName = NamingUtils.simplifiedTypeName(contentType);
		
		boolean constSize = params.annotations.getAnnotation(ConstSize.class) != null || params.type.getAnnotation(ConstSize.class) != null;
		
		String elementVarName = params.simpleName + "Element";
		if (!constSize) saveInstructions.accept(DataHandlerUtils.writeBuffer("Int", params.saveAccessExpr + ".length"));
		saveInstructions.accept("for (" + typeName + " " + elementVarName + " : " + params.saveAccessExpr + ")");
		saveInstructions.accept("{");
		
		String lenghtVarName;
		String indexVarName = params.simpleName + "Index";
		
		if (constSize)
		{
			lenghtVarName = params.getLoadAccessExpr() + ".length";
		}
		else
		{
			String arrayTypeName = typeName + "[]";
			
			int i;
			for (i = arrayTypeName.length() - 2; i >= 2 && arrayTypeName.substring(i - 2, i).equals("[]"); i-=2);
			
			lenghtVarName = params.simpleName + "Length";
			loadInstructions.accept("int " + lenghtVarName + " = " + DataHandlerUtils.readBuffer("Int") + ";");
			params.setLoadedValue(loadInstructions, "new " + arrayTypeName.substring(0, i + 1) + lenghtVarName + arrayTypeName.substring(i + 1));
		}
		
		loadInstructions.accept("for (int " + indexVarName + " = 0; " + indexVarName + " < " + lenghtVarName + "; " + indexVarName + "++)");
		loadInstructions.accept("{");
		
		String getLoadExpr = params.getLoadAccessExpr() + "[" + indexVarName + "]";
		DataHandlerParameters contentHandler = params.finder.getDataType(elementVarName, elementVarName, getLoadExpr, (loadInst, value) -> loadInst.accept(getLoadExpr + " = " + value + ";"), contentType, EmptyAnnotationConstruct.INSTANCE, ValueInitStatus.DECLARED);
		contentHandler.addInstructions(inst -> saveInstructions.accept("\t" + inst), inst -> loadInstructions.accept("\t" + inst), imports);
		
		saveInstructions.accept("}");
		
		loadInstructions.accept("}");
		
		if (!constSize && params.loadAccessExpr == null)
		{
			params.setExpr.accept(loadInstructions, params.simpleName); 
		}
	}

	@Override
	public Predicate<TypeMirror> getTypeValidator(Elements elemUtils, Types typeUtils)
	{
		return t -> t.getKind() == TypeKind.ARRAY;
	}
	
}
