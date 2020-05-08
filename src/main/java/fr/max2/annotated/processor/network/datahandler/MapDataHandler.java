package fr.max2.annotated.processor.network.datahandler;

import java.util.Map;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.DataHandlerParameters;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.NamingUtils;
import fr.max2.annotated.processor.utils.TypeHelper;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;

public enum MapDataHandler implements INamedDataHandler
{
	INSTANCE;
	
	@Override
	public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
	{
		TypeMirror mapType = params.finder.elemUtils.getTypeElement(this.getTypeName()).asType();
		DeclaredType refinedType = TypeHelper.refineTo(params.type, mapType, params.finder.typeUtils);
		if (refinedType == null) throw new IncompatibleTypeException("The type '" + params.type + "' is not a sub type of " + this.getTypeName());
		
		TypeMirror keyFullType = refinedType.getTypeArguments().get(0);
		TypeMirror valueFullType = refinedType.getTypeArguments().get(1);
		TypeMirror keyType = TypeHelper.shallowErasure(keyFullType, params.finder.elemUtils);
		TypeMirror valueType = TypeHelper.shallowErasure(valueFullType, params.finder.elemUtils);
		DeclaredType revisedType = TypeHelper.replaceTypeArgument(TypeHelper.replaceTypeArgument((DeclaredType)params.type, keyFullType, keyType, params.finder.typeUtils), valueFullType, valueType, params.finder.typeUtils);
		TypeMirror implType = params.type;
		
		String implName = params.properties.getValueOrEmpty("impl");
		if (implName.isEmpty())
			implName = defaultImplementation(TypeHelper.asTypeElement(params.finder.typeUtils.asElement(params.type)));
		
		if (!implName.isEmpty())
		{
			implType = params.finder.elemUtils.getTypeElement(implName).asType();
			if (implType == null)
				throw new IncompatibleTypeException("Unknown type '" + implName + "' as implementation for " + this.getTypeName());
			
			DeclaredType refinedImpl = TypeHelper.refineTo(implType, mapType, params.finder.typeUtils);
			if (refinedImpl == null)
				throw new IncompatibleTypeException("The type '" + implName + "' is not a sub type of " + this.getTypeName());
			
			TypeMirror implKeyFullType = refinedImpl.getTypeArguments().get(0);
			TypeMirror implValueFullType = refinedImpl.getTypeArguments().get(1);
			DeclaredType revisedImplType = TypeHelper.replaceTypeArgument(TypeHelper.replaceTypeArgument((DeclaredType)implType, implKeyFullType, keyType, params.finder.typeUtils), implValueFullType, valueType, params.finder.typeUtils);
			if (!params.finder.typeUtils.isAssignable(revisedImplType, revisedType))
				throw new IncompatibleTypeException("The type '" + implName + "' is not a sub type of '" + params.type + "'");
			
			builder.addImport(implName);
		}
		
		DataHandlerUtils.requireDefaultConstructor(params.finder.typeUtils, implType);
		
		String keyVarName = params.uniqueName + "Key";
		String keyVarTmpName = params.uniqueName + "KeyTmp";
		String valueVarName = params.uniqueName + "Element";
		String entryVarName = params.uniqueName + "Entry";
		String lenghtVarName = params.uniqueName + "Length";
		String indexVarName = params.uniqueName + "Index";
		
		builder.addImport(this.getTypeName());
		TypeHelper.provideTypeImports(keyType, builder::addImport);
		TypeHelper.provideTypeImports(valueType, builder::addImport);
		
		builder.encoder()
			.add(DataHandlerUtils.writeBuffer("Int", params.saveAccessExpr + ".size()"))
			.add("for (Map.Entry<" + NamingUtils.computeFullName(keyFullType) + ", " + NamingUtils.computeFullName(valueFullType) + "> " + entryVarName + " : " + params.saveAccessExpr + ".entrySet())")
			.add("{");
		
		
		builder.decoder().add(
			"int " + lenghtVarName + " = " + DataHandlerUtils.readBuffer("Int") + ";",
			NamingUtils.computeFullName(revisedType) + " " + params.uniqueName + " = new " + NamingUtils.computeSimplifiedName(implType) + "();",
			"for (int " + indexVarName + " = 0; " + indexVarName + " < " + lenghtVarName + "; " + indexVarName + "++)",
			"{");
		

		DataHandlerParameters keyHandler = params.finder.getDataType(keyVarName, entryVarName + ".getKey()", (loadInst, key) -> loadInst.add(NamingUtils.computeFullName(keyType) + " " + keyVarTmpName + " = " + key + ";"), keyType, params.properties.getSubPropertiesOrEmpty("keys"));
		keyHandler.addInstructions(1, builder);
		
		DataHandlerParameters valueHandler = params.finder.getDataType(valueVarName, entryVarName + ".getValue()", (loadInst, value) -> loadInst.add(params.uniqueName + ".put(" + keyVarTmpName + ", " + value + ");"), valueType, params.properties.getSubPropertiesOrEmpty("values"));
		valueHandler.addInstructions(1, builder);
		
		builder.encoder().add("}");
		builder.decoder().add("}");
		
		params.setExpr.accept(builder.decoder(), params.uniqueName);
	}

	private static String defaultImplementation(TypeElement type)
	{
		switch (type.getQualifiedName().toString())
		{
		case "java.util.Map":
			return "java.util.HashMap";
		default:
			return "";
		}
	}

	@Override
	public String getTypeName()
	{
		return Map.class.getCanonicalName();
	}
	
}
