package fr.max2.annotated.processor.network.datahandler;

import java.util.Map;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.DataHandlerParameters;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.EmptyAnnotationConstruct;
import fr.max2.annotated.processor.utils.NamingUtils;
import fr.max2.annotated.processor.utils.TypeHelper;

public enum MapDataHandler implements INamedDataHandler
{
	INSTANCE;
	
	@Override
	public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
	{
		DeclaredType mapType = TypeHelper.refineTo(params.type, params.finder.elemUtils.getTypeElement(this.getTypeName()).asType(), params.finder.typeUtils);
		if (mapType == null) throw new IllegalArgumentException("The type '" + params.type + "' is not a sub type of " + this.getTypeName());
		
		TypeMirror keyType = mapType.getTypeArguments().get(0);
		TypeMirror valueType = mapType.getTypeArguments().get(1);
		String keyTypeName = NamingUtils.computeFullName(keyType);
		String valueTypeName = NamingUtils.computeFullName(valueType);
		
		String keyVarName = params.simpleName + "Key";
		String valueVarName = params.simpleName + "Element";
		String entryVarName = params.simpleName + "Entry";
		
		String lenghtVarName = params.simpleName + "Length";
		String indexVarName = params.simpleName + "Index";
		
		builder.addImport(this.getTypeName());
		
		builder.save()
			.add(DataHandlerUtils.writeBuffer("Int", params.saveAccessExpr + ".size()"))
			.add("for (Map.Entry<" + keyTypeName + ", " + valueTypeName + "> " + entryVarName + " : " + params.saveAccessExpr + ".entrySet())")
			.add("{");
		
		
		builder.load().add("int " + lenghtVarName + " = " + DataHandlerUtils.readBuffer("Int") + ";");
		
		params.setLoadedValue(builder.load(), "new " + NamingUtils.computeSimplifiedName(params.type) + "()"); //TODO [v1.1] use parameters to use the right class
		
		builder.load()
			.add("for (int " + indexVarName + " = 0; " + indexVarName + " < " + lenghtVarName + "; " + indexVarName + "++)")
			.add("{");
		

		builder.save().indent(1);
		builder.load().indent(1);
		DataHandlerParameters keyHandler = params.finder.getDataType(keyVarName, entryVarName + ".getKey()", keyVarName, (loadInst, key) -> loadInst.add(keyTypeName + " " + keyVarName + " = " + key + ";"), keyType, EmptyAnnotationConstruct.INSTANCE);
		keyHandler.addInstructions(builder);
		
		DataHandlerParameters valueHandler = params.finder.getDataType(valueVarName, entryVarName + ".getValue()", null, (loadInst, value) -> loadInst.add(params.getLoadAccessExpr() + ".put(" + keyVarName + ", " + value + ");"), valueType, EmptyAnnotationConstruct.INSTANCE);
		valueHandler.addInstructions(builder);
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
	public String getTypeName()
	{
		return Map.class.getCanonicalName();
	}
	
}
