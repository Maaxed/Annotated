package fr.max2.packeta.processor.network.datahandler;

import java.util.Map;
import java.util.function.Consumer;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.packeta.processor.network.DataHandlerParameters;
import fr.max2.packeta.processor.utils.EmptyAnnotationConstruct;
import fr.max2.packeta.processor.utils.NamingUtils;
import fr.max2.packeta.processor.utils.TypeHelper;
import fr.max2.packeta.processor.utils.ValueInitStatus;

public enum MapDataHandler implements INamedDataHandler
{
	INSTANCE;
	//TODO [v1.1] put size in parameters
	@Override
	public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
	{
		DeclaredType mapType = TypeHelper.refineTo(params.type, params.finder.elemUtils.getTypeElement(this.getTypeName()).asType(), params.finder.typeUtils);
		if (mapType == null) throw new IllegalArgumentException("The type '" + params.type + "' is not a sub type of " + this.getTypeName());
		
		TypeMirror keyType = mapType.getTypeArguments().get(0);
		TypeMirror valueType = mapType.getTypeArguments().get(1);
		String keyTypeName = NamingUtils.simpleTypeName(keyType);
		String valueTypeName = NamingUtils.simpleTypeName(valueType);
		
		String keyVarName = params.simpleName + "Key";
		String valueVarName = params.simpleName + "Element";
		String entryVarName = params.simpleName + "Entry";
		
		String lenghtVarName = params.simpleName + "Length";
		String indexVarName = params.simpleName + "Index";
		
		imports.accept(this.getTypeName());
		
		saveInstructions.accept(DataHandlerUtils.writeBuffer("Int", params.saveAccessExpr + ".size()"));
		saveInstructions.accept("for (Map.Entry<" + keyTypeName + ", " + valueTypeName + "> " + entryVarName + " : " + params.saveAccessExpr + ".entrySet())");
		saveInstructions.accept("{");
		
		
		loadInstructions.accept("int " + lenghtVarName + " = " + DataHandlerUtils.readBuffer("Int") + ";");
		
		if (params.initStatus.isInitialised())
		{
			loadInstructions.accept(params.getLoadAccessExpr() + ".clear();" );
		}
		else
		{
			params.setLoadedValue(loadInstructions, "new " + NamingUtils.simpleTypeName(params.type, true) + "()"); //TODO [v1.1] use parameters to use the right class
		}
		
		loadInstructions.accept("for (int " + indexVarName + " = 0; " + indexVarName + " < " + lenghtVarName + "; " + indexVarName + "++)");
		loadInstructions.accept("{");
		
		DataHandlerParameters keyHandler = params.finder.getDataType(keyVarName, entryVarName + ".getKey()", keyVarName, (loadInst, key) -> loadInst.accept(keyTypeName + " " + keyVarName + " = " + key + ";"), keyType, EmptyAnnotationConstruct.INSTANCE, ValueInitStatus.UNDEFINED);
		keyHandler.addInstructions(inst -> saveInstructions.accept("\t" + inst), inst -> loadInstructions.accept("\t" + inst), imports);
		
		DataHandlerParameters valueHandler = params.finder.getDataType(valueVarName, entryVarName + ".getValue()", null, (loadInst, value) -> loadInst.accept(params.getLoadAccessExpr() + ".put(" + keyVarName + ", " + value + ");"), valueType, EmptyAnnotationConstruct.INSTANCE, ValueInitStatus.UNDEFINED);
		valueHandler.addInstructions(inst -> saveInstructions.accept("\t" + inst), inst -> loadInstructions.accept("\t" + inst), imports);
		
		saveInstructions.accept("}");
		loadInstructions.accept("}");
		
		if (!params.initStatus.isInitialised() && params.loadAccessExpr == null)
		{
			params.setExpr.accept(loadInstructions, params.simpleName);
		}
	}

	@Override
	public String getTypeName()
	{
		return Map.class.getCanonicalName();
	}
	
}
