package fr.max2.packeta.network.datahandler;

import java.util.Collection;
import java.util.function.Consumer;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.packeta.api.network.ConstSize;
import fr.max2.packeta.network.DataHandlerParameters;
import fr.max2.packeta.utils.EmptyAnnotationConstruct;
import fr.max2.packeta.utils.NamingUtils;
import fr.max2.packeta.utils.TypeHelper;
import fr.max2.packeta.utils.ValueInitStatus;

public enum CollectionDataHandler implements INamedDataHandler
{
	INSTANCE;

	@Override
	public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
	{
		DeclaredType collectionType = TypeHelper.refineTo(params.type, params.finder.elemUtils.getTypeElement(this.getTypeName()).asType(), params.finder.typeUtils);
		TypeMirror contentType = collectionType.getTypeArguments().get(0);
		String typeName = NamingUtils.simpleTypeName(contentType);
		
		boolean constSize = params.annotations.getAnnotation(ConstSize.class) != null || params.type.getAnnotation(ConstSize.class) != null;
		
		String elementVarName = params.simpleName + "Element";
		if (!constSize) saveInstructions.accept(DataHandlerUtils.writeBuffer("Int", params.getExpr + ".size()"));
		saveInstructions.accept("for (" + typeName + " " + elementVarName + " : " + params.getExpr + ")");
		saveInstructions.accept("{");
		
		String lenghtVarName = params.simpleName + "Length";
		String indexVarName = params.simpleName + "Index";
		if (constSize)
		{
			loadInstructions.accept("int " + lenghtVarName + " = " + params.setExpr + ".size();");
		}
		else
		{
			loadInstructions.accept(DataHandlerUtils.readBuffer("Int", "int " + lenghtVarName));
		}
		
		if (params.initStatus.isInitialised())
		{
			loadInstructions.accept(params.setExpr + ".clear();" );
		}
		else
		{
			loadInstructions.accept(params.firstSetInit() + " = new " + NamingUtils.simpleTypeName(params.type, true) + "();" ); //TODO use parameters to use the right class
		}
		
		loadInstructions.accept("for (int " + indexVarName + " = 0; " + indexVarName + " < " + lenghtVarName + "; " + indexVarName + "++)");
		loadInstructions.accept("{");
		
		DataHandlerParameters contentHandler = params.finder.getDataType(elementVarName, elementVarName, elementVarName, contentType, EmptyAnnotationConstruct.INSTANCE, ValueInitStatus.UNDEFINED);
		contentHandler.addInstructions(inst -> saveInstructions.accept("\t" + inst), inst -> loadInstructions.accept("\t" + inst), imports);
		
		saveInstructions.accept("}");
		
		loadInstructions.accept("\t" + params.setExpr + ".add(" + elementVarName + ");");
		loadInstructions.accept("}");
	}

	@Override
	public String getTypeName()
	{
		return Collection.class.getCanonicalName();
	}
	
}