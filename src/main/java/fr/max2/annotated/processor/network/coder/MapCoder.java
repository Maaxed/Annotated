package fr.max2.annotated.processor.network.coder;

import java.util.Map;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.NamedDataHandler;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PropertyMap;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;

public class MapCoder extends DataCoder
{
	private static final String MAP_TYPE = Map.class.getCanonicalName();
	public static final NamedDataHandler HANDLER = new NamedDataHandler(MAP_TYPE, MapCoder::new);

	private final DataCoder keyHandler, valueHandler;
	private final TypeMirror
		codedType, implType, extType,
		keyFullType, keyType,
		valueFullType, valueType;
	
	public MapCoder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties)
	{
		super(tools, uniqueName, paramType, properties);
		
		TypeElement mapElem = tools.elements.getTypeElement(MAP_TYPE);
		TypeMirror mapType = mapElem.asType();
		DeclaredType refinedType = tools.types.refineTo(paramType, mapType);
		if (refinedType == null) throw new IncompatibleTypeException("The type '" + paramType + "' is not a sub type of " + MAP_TYPE);
		
		this.keyFullType = refinedType.getTypeArguments().get(0);
		this.keyHandler = tools.handlers.getDataType(uniqueName + "Key", this.keyFullType, properties.getSubPropertiesOrEmpty("keys"));
		this.keyType = tools.types.shallowErasure(this.keyHandler.getInternalType());

		this.valueFullType = refinedType.getTypeArguments().get(1);
		this.valueHandler = tools.handlers.getDataType(uniqueName + "Element", this.valueFullType, properties.getSubPropertiesOrEmpty("values"));
		this.valueType = tools.types.shallowErasure(this.valueHandler.getInternalType());

		this.extType = tools.types.replaceTypeArgument(tools.types.replaceTypeArgument((DeclaredType)paramType, this.keyFullType, tools.types.shallowErasure(this.keyFullType)), this.valueFullType, tools.types.shallowErasure(this.valueFullType));
		this.internalType = tools.types.replaceTypeArgument(tools.types.replaceTypeArgument((DeclaredType)paramType, this.keyFullType, this.keyHandler.getInternalType()), this.valueFullType, this.valueHandler.getInternalType());
		this.codedType = tools.types.replaceTypeArgument(tools.types.replaceTypeArgument((DeclaredType)paramType, this.keyFullType, this.keyType), this.valueFullType, this.valueType);
		TypeMirror implType = paramType;
		
		String implName = properties.getValueOrEmpty("impl");
		if (implName.isEmpty())
			implName = defaultImplementation(tools.elements.asTypeElement(tools.types.asElement(paramType)));
		
		if (!implName.isEmpty())
		{
			TypeElement elem = tools.elements.getTypeElement(implName);
			if (elem == null)
				throw new IncompatibleTypeException("Unknown type '" + implName + "' as implementation for " + MAP_TYPE);
			
			implType = elem.asType();
			DeclaredType refinedImpl = tools.types.refineTo(implType, mapType);
			if (refinedImpl == null)
				throw new IncompatibleTypeException("The type '" + implName + "' is not a sub type of " + MAP_TYPE);
			
			TypeMirror implKeyFullType = refinedImpl.getTypeArguments().get(0);
			TypeMirror implValueFullType = refinedImpl.getTypeArguments().get(1);
			DeclaredType revisedImplType = tools.types.replaceTypeArgument(tools.types.replaceTypeArgument((DeclaredType)implType, implKeyFullType, this.keyType), implValueFullType, this.valueType);
			if (!tools.types.isAssignable(revisedImplType, this.codedType))
				throw new IncompatibleTypeException("The type '" + implName + "' is not a sub type of '" + paramType + "'");
		}
		
		this.implType = implType;
		DataCoderUtils.requireDefaultConstructor(tools.types, this.implType);
	}
	
	@Override
	public OutputExpressions addInstructions(IPacketBuilder builder, String saveAccessExpr, String internalAccessExpr, String externalAccessExpr)
	{
		String keyVarTmpName = this.uniqueName + "KeyTmp";
		String entryVarName = this.uniqueName + "Entry";
		String lenghtVarName = this.uniqueName + "Length";
		String indexVarName = this.uniqueName + "Index";
		String convertedName = this.uniqueName + "Converted";

		builder.addImport(this.tools.elements.asTypeElement(this.tools.types.asElement(this.implType)));
		builder.addImport(this.tools.elements.getTypeElement(MAP_TYPE));
		this.tools.types.provideTypeImports(this.keyType, builder);
		this.tools.types.provideTypeImports(this.valueType, builder);
		
		builder.encoder().add(
			DataCoderUtils.writeBuffer("Int", saveAccessExpr + ".size()"),
			"for (Map.Entry<" + this.tools.naming.computeFullName(this.keyFullType) + ", " +this. tools.naming.computeFullName(this.valueFullType) + "> " + entryVarName + " : " + saveAccessExpr + ".entrySet())",
			"{");
		builder.decoder().add(
			"int " + lenghtVarName + " = " + DataCoderUtils.readBuffer("Int") + ";",
			this.tools.naming.computeFullName(this.codedType) + " " + this.uniqueName + " = new " + this.tools.naming.computeSimplifiedName(implType) + "();",
			"for (int " + indexVarName + " = 0; " + indexVarName + " < " + lenghtVarName + "; " + indexVarName + "++)",
			"{");
		
		
		builder.internalizer().add(
			this.tools.naming.computeFullName(this.codedType) + " " + convertedName + " = new " + this.tools.naming.computeSimplifiedName(this.implType) + "();",
			"for (Map.Entry<" + this.tools.naming.computeFullName(this.keyFullType) + ", " + this.tools.naming.computeFullName(this.valueFullType) + "> " + entryVarName + " : " + internalAccessExpr + ".entrySet())",
			"{");
		builder.externalizer().add(
			this.tools.naming.computeFullName(this.extType) + " " + convertedName + " = new " + this.tools.naming.computeSimplifiedName(this.implType) + "();",
			"for (Map.Entry<" + this.tools.naming.computeFullName(this.keyType) + ", " + this.tools.naming.computeFullName(this.valueType) + "> " + entryVarName + " : " + externalAccessExpr + ".entrySet())",
			"{");

		builder.indentAll(1);
		OutputExpressions keyOutput = builder.runCoder(this.keyHandler, entryVarName + ".getKey()");
		builder.decoder().add(this.tools.naming.computeFullName(this.keyType) + " " + keyVarTmpName + " = " + keyOutput.decoded + ";");
		builder.internalizer().add(this.tools.naming.computeFullName(this.keyType) + " " + keyVarTmpName + " = " + keyOutput.internalized + ";");
		builder.externalizer().add(this.tools.naming.computeFullName(this.keyFullType) + " " + keyVarTmpName + " = " + keyOutput.externalized + ";");
		
		OutputExpressions valueOutput = builder.runCoder(this.valueHandler, entryVarName + ".getValue()");
		builder.decoder().add(this.uniqueName + ".put(" + keyVarTmpName + ", " + valueOutput.decoded + ");");
		builder.internalizer().add(convertedName + ".put(" + keyVarTmpName + ", " + valueOutput.internalized + ");");
		builder.externalizer().add(convertedName + ".put(" + keyVarTmpName + ", " + valueOutput.externalized + ");");
		builder.indentAll(-1);
		
		builder.encoder().add("}");
		builder.decoder().add("}");
		builder.internalizer().add("}");
		builder.externalizer().add("}");
		
		return new OutputExpressions(this.uniqueName, convertedName, convertedName);
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
	
}
