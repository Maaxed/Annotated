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

	private final DataCoder keyCoder, valueCoder;
	private final TypeMirror
		erasureIntType, extType, implType,
		keyExtType, keyIntType,
		valueExtType, valueIntType;
	
	public MapCoder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties)
	{
		super(tools, uniqueName, paramType, properties);
		
		TypeElement mapElem = tools.elements.getTypeElement(MAP_TYPE);
		TypeMirror mapType = mapElem.asType();
		DeclaredType refinedType = tools.types.refineTo(paramType, mapType);
		if (refinedType == null) throw new IncompatibleTypeException("The type '" + paramType + "' is not a sub type of " + MAP_TYPE);
		
		this.keyExtType = refinedType.getTypeArguments().get(0);
		this.keyCoder = tools.handlers.getDataType(uniqueName + "Key", this.keyExtType, properties.getSubPropertiesOrEmpty("keys"));
		TypeMirror keyType = this.keyCoder.getInternalType();
		if (keyType.getKind().isPrimitive())
			keyType = tools.types.boxedClass(tools.types.asPrimitive(keyType)).asType();
		this.keyIntType = keyType;

		this.valueExtType = refinedType.getTypeArguments().get(1);
		this.valueCoder = tools.handlers.getDataType(uniqueName + "Element", this.valueExtType, properties.getSubPropertiesOrEmpty("values"));
		TypeMirror valueType = this.valueCoder.getInternalType();
		if (valueType.getKind().isPrimitive())
			valueType = tools.types.boxedClass(tools.types.asPrimitive(valueType)).asType();
		this.valueIntType = valueType;

		this.extType = tools.types.replaceTypeArgument(tools.types.replaceTypeArgument((DeclaredType)paramType, this.keyExtType, tools.types.shallowErasure(this.keyExtType)), this.valueExtType, tools.types.shallowErasure(this.valueExtType));
		this.internalType = tools.types.replaceTypeArgument(tools.types.replaceTypeArgument((DeclaredType)paramType, this.keyExtType, keyType), this.valueExtType, valueType);
		this.erasureIntType = tools.types.replaceTypeArgument(tools.types.replaceTypeArgument((DeclaredType)paramType, this.keyExtType, tools.types.shallowErasure(this.keyIntType)), this.valueExtType, tools.types.shallowErasure(this.valueIntType));
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
			DeclaredType revisedImplType = tools.types.replaceTypeArgument(tools.types.replaceTypeArgument((DeclaredType)implType, implKeyFullType, tools.types.shallowErasure(this.keyIntType)), implValueFullType, tools.types.shallowErasure(this.valueIntType));
			if (!tools.types.isAssignable(revisedImplType, this.erasureIntType))
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
		
		TypeMirror erasedIntKey = this.tools.types.shallowErasure(this.keyIntType);

		builder.addImport(this.tools.elements.asTypeElement(this.tools.types.asElement(this.implType)));
		builder.addImport(this.tools.elements.getTypeElement(MAP_TYPE));
		this.tools.types.provideTypeImports(this.keyIntType, builder);
		this.tools.types.provideTypeImports(this.valueIntType, builder);
		
		builder.encoder().add(
			DataCoderUtils.writeBuffer("Int", saveAccessExpr + ".size()"),
			"for (Map.Entry<" + this.tools.naming.computeFullName(this.keyIntType) + ", " +this. tools.naming.computeFullName(this.valueIntType) + "> " + entryVarName + " : " + saveAccessExpr + ".entrySet())",
			"{");
		builder.decoder().add(
			"int " + lenghtVarName + " = " + DataCoderUtils.readBuffer("Int") + ";",
			this.tools.naming.computeFullName(this.erasureIntType) + " " + this.uniqueName + " = new " + this.tools.naming.computeSimplifiedName(implType) + "();",
			"for (int " + indexVarName + " = 0; " + indexVarName + " < " + lenghtVarName + "; " + indexVarName + "++)",
			"{");
		
		builder.internalizer().add(
			this.tools.naming.computeFullName(this.erasureIntType) + " " + convertedName + " = new " + this.tools.naming.computeSimplifiedName(this.implType) + "();",
			"for (Map.Entry<" + this.tools.naming.computeFullName(this.keyExtType) + ", " + this.tools.naming.computeFullName(this.valueExtType) + "> " + entryVarName + " : " + internalAccessExpr + ".entrySet())",
			"{");
		builder.externalizer().add(
			this.tools.naming.computeFullName(this.extType) + " " + convertedName + " = new " + this.tools.naming.computeSimplifiedName(this.implType) + "();",
			"for (Map.Entry<" + this.tools.naming.computeFullName(this.keyIntType) + ", " + this.tools.naming.computeFullName(this.valueIntType) + "> " + entryVarName + " : " + externalAccessExpr + ".entrySet())",
			"{");

		builder.indentAll(1);
		OutputExpressions keyOutput = builder.runCoder(this.keyCoder, entryVarName + ".getKey()");
		builder.decoder().add(this.tools.naming.computeFullName(erasedIntKey) + " " + keyVarTmpName + " = " + keyOutput.decoded + ";");
		
		OutputExpressions valueOutput = builder.runCoder(this.valueCoder, entryVarName + ".getValue()");
		builder.decoder().add(this.uniqueName + ".put(" + keyVarTmpName + ", " + valueOutput.decoded + ");");
		builder.internalizer().add(convertedName + ".put(" + keyOutput.internalized + ", " + valueOutput.internalized + ");");
		builder.externalizer().add(convertedName + ".put(" + keyOutput.externalized + ", " + valueOutput.externalized + ");");
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
