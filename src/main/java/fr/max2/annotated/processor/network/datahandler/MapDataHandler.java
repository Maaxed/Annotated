package fr.max2.annotated.processor.network.datahandler;

import java.util.Map;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.DataHandlerParameters;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;

public enum MapDataHandler implements INamedDataHandler
{
	INSTANCE;
	
	@Override
	public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
	{
		TypeMirror mapType = params.tools.elements.getTypeElement(this.getTypeName()).asType();
		DeclaredType refinedType = params.tools.types.refineTo(params.type, mapType);
		if (refinedType == null) throw new IncompatibleTypeException("The type '" + params.type + "' is not a sub type of " + this.getTypeName());
		
		TypeMirror keyFullType = refinedType.getTypeArguments().get(0);
		TypeMirror valueFullType = refinedType.getTypeArguments().get(1);
		TypeMirror keyType = params.tools.types.shallowErasure(keyFullType);
		TypeMirror valueType = params.tools.types.shallowErasure(valueFullType);
		DeclaredType revisedType = params.tools.types.replaceTypeArgument(params.tools.types.replaceTypeArgument((DeclaredType)params.type, keyFullType, keyType), valueFullType, valueType);
		TypeMirror implType = params.type;
		
		String implName = params.properties.getValueOrEmpty("impl");
		if (implName.isEmpty())
			implName = defaultImplementation(params.tools.elements.asTypeElement(params.tools.types.asElement(params.type)));
		
		if (!implName.isEmpty())
		{
			implType = params.tools.elements.getTypeElement(implName).asType();
			if (implType == null)
				throw new IncompatibleTypeException("Unknown type '" + implName + "' as implementation for " + this.getTypeName());
			
			DeclaredType refinedImpl = params.tools.types.refineTo(implType, mapType);
			if (refinedImpl == null)
				throw new IncompatibleTypeException("The type '" + implName + "' is not a sub type of " + this.getTypeName());
			
			TypeMirror implKeyFullType = refinedImpl.getTypeArguments().get(0);
			TypeMirror implValueFullType = refinedImpl.getTypeArguments().get(1);
			DeclaredType revisedImplType = params.tools.types.replaceTypeArgument(params.tools.types.replaceTypeArgument((DeclaredType)implType, implKeyFullType, keyType), implValueFullType, valueType);
			if (!params.tools.types.isAssignable(revisedImplType, revisedType))
				throw new IncompatibleTypeException("The type '" + implName + "' is not a sub type of '" + params.type + "'");
			
			builder.addImport(implName);
		}
		
		DataHandlerUtils.requireDefaultConstructor(params.tools.types, implType);
		
		String keyVarName = params.uniqueName + "Key";
		String keyVarTmpName = params.uniqueName + "KeyTmp";
		String valueVarName = params.uniqueName + "Element";
		String entryVarName = params.uniqueName + "Entry";
		String lenghtVarName = params.uniqueName + "Length";
		String indexVarName = params.uniqueName + "Index";
		
		builder.addImport(this.getTypeName());
		params.tools.types.provideTypeImports(keyType, builder::addImport);
		params.tools.types.provideTypeImports(valueType, builder::addImport);
		
		builder.encoder()
			.add(DataHandlerUtils.writeBuffer("Int", params.saveAccessExpr + ".size()"))
			.add("for (Map.Entry<" + params.tools.naming.computeFullName(keyFullType) + ", " + params.tools.naming.computeFullName(valueFullType) + "> " + entryVarName + " : " + params.saveAccessExpr + ".entrySet())")
			.add("{");
		
		
		builder.decoder().add(
			"int " + lenghtVarName + " = " + DataHandlerUtils.readBuffer("Int") + ";",
			params.tools.naming.computeFullName(revisedType) + " " + params.uniqueName + " = new " + params.tools.naming.computeSimplifiedName(implType) + "();",
			"for (int " + indexVarName + " = 0; " + indexVarName + " < " + lenghtVarName + "; " + indexVarName + "++)",
			"{");
		

		DataHandlerParameters keyHandler = params.tools.handlers.getDataType(keyVarName, entryVarName + ".getKey()", (loadInst, key) -> loadInst.add(params.tools.naming.computeFullName(keyType) + " " + keyVarTmpName + " = " + key + ";"), keyType, params.properties.getSubPropertiesOrEmpty("keys"));
		keyHandler.addInstructions(1, builder);
		
		DataHandlerParameters valueHandler = params.tools.handlers.getDataType(valueVarName, entryVarName + ".getValue()", (loadInst, value) -> loadInst.add(params.uniqueName + ".put(" + keyVarTmpName + ", " + value + ");"), valueType, params.properties.getSubPropertiesOrEmpty("values"));
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
