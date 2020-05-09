package fr.max2.annotated.processor.network.datahandler;

import java.util.Collection;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.DataHandlerParameters;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;

public enum CollectionDataHandler implements INamedDataHandler
{
	INSTANCE;
	
	@Override
	public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
	{
		TypeMirror collectionType = params.tools.elements.getTypeElement(this.getTypeName()).asType();
		DeclaredType refinedType = params.tools.types.refineTo(params.type, collectionType);
		if (refinedType == null)
			throw new IncompatibleTypeException("The type '" + params.type + "' is not a sub type of " + this.getTypeName());
		
		TypeMirror contentFullType = refinedType.getTypeArguments().get(0);
		TypeMirror contentType = params.tools.types.shallowErasure(contentFullType);
		DeclaredType revisedType = params.tools.types.replaceTypeArgument((DeclaredType)params.type, contentFullType, contentType);
		TypeMirror implType = params.type;
		
		String implName = params.properties.getValueOrEmpty("impl");
		if (implName.isEmpty())
			implName = defaultImplementation(params.tools.elements.asTypeElement(params.tools.types.asElement(params.type)));
		
		if (!implName.isEmpty())
		{
			implType = params.tools.elements.getTypeElement(implName).asType();
			if (implType == null)
				throw new IncompatibleTypeException("Unknown type '" + implName + "' as implementation for " + this.getTypeName());
			
			DeclaredType refinedImpl = params.tools.types.refineTo(implType, collectionType);
			if (refinedImpl == null)
				throw new IncompatibleTypeException("The type '" + implName + "' is not a sub type of " + this.getTypeName());
			
			contentFullType = refinedImpl.getTypeArguments().get(0);
			DeclaredType revisedImplType = params.tools.types.replaceTypeArgument((DeclaredType)implType, contentFullType, contentType);
			if (!params.tools.types.isAssignable(revisedImplType, revisedType))
				throw new IncompatibleTypeException("The type '" + implName + "' is not a sub type of '" + params.type + "'");
			
			builder.addImport(implName);
		}

		DataHandlerUtils.requireDefaultConstructor(params.tools.types, implType);
		
		String contentTypeName = params.tools.naming.computeFullName(contentType);
		params.tools.types.provideTypeImports(contentType, builder::addImport);
		
		String elementVarName = params.uniqueName + "Element";
		builder.encoder()
			.add(DataHandlerUtils.writeBuffer("Int", params.saveAccessExpr + ".size()"))
			.add("for (" + contentTypeName + " " + elementVarName + " : " + params.saveAccessExpr + ")")
			.add("{");
		
		String lenghtVarName = params.uniqueName + "Length";
		String indexVarName = params.uniqueName + "Index";
		
		builder.decoder().add(
			"int " + lenghtVarName + " = " + DataHandlerUtils.readBuffer("Int") + ";",
			params.tools.naming.computeFullName(revisedType) + " " + params.uniqueName + " = new " + params.tools.naming.computeSimplifiedName(implType) + "();",
			"for (int " + indexVarName + " = 0; " + indexVarName + " < " + lenghtVarName + "; " + indexVarName + "++)",
			"{");
		
		DataHandlerParameters contentHandler = params.tools.handlers.getDataType(elementVarName, elementVarName, (loadInst, value) -> loadInst.add(params.uniqueName + ".add(" + value + ");"), contentType, params.properties.getSubPropertiesOrEmpty("content"));
		
		contentHandler.addInstructions(1, builder);
		
		builder.encoder().add("}");
		builder.decoder().add("}");
		
		params.setExpr.accept(builder.decoder(), params.uniqueName);
	}

	private static String defaultImplementation(TypeElement type)
	{
		switch (type.getQualifiedName().toString())
		{
		case "java.util.Collection":
		case "java.util.List":
			return "java.util.ArrayList";
		case "java.util.Set":
			return "java.util.HashSet";
		default:
			return "";
		}
	}

	@Override
	public String getTypeName()
	{
		return Collection.class.getCanonicalName();
	}
	
}
