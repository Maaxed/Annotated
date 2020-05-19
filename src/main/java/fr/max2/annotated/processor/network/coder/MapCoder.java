package fr.max2.annotated.processor.network.coder;

import java.util.Map;
import java.util.function.BiConsumer;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.NamedDataHandler;
import fr.max2.annotated.processor.network.model.IFunctionBuilder;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PropertyMap;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;

public class MapCoder extends DataCoder
{
	private static final String MAP_TYPE = Map.class.getCanonicalName();
	public static final NamedDataHandler HANDLER = new NamedDataHandler(MAP_TYPE, MapCoder::new);

	private DataCoder keyHandler, valueHandler;
	private TypeMirror implType, keyType, keyFullType, valueType, valueFullType;
	
	public MapCoder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties)
	{
		super(tools, uniqueName, paramType, properties);
		
		TypeElement mapElem = tools.elements.getTypeElement(MAP_TYPE);
		TypeMirror mapType = mapElem.asType();
		DeclaredType refinedType = tools.types.refineTo(paramType, mapType);
		if (refinedType == null) throw new IncompatibleTypeException("The type '" + paramType + "' is not a sub type of " + MAP_TYPE);
		
		this.keyFullType = refinedType.getTypeArguments().get(0);
		this.keyHandler = tools.handlers.getDataType(uniqueName + "Key", this.keyFullType, properties.getSubPropertiesOrEmpty("keys"));
		this.keyType = this.keyHandler.getCodedType();

		this.valueFullType = refinedType.getTypeArguments().get(1);
		this.valueHandler = tools.handlers.getDataType(uniqueName + "Element", this.valueFullType, properties.getSubPropertiesOrEmpty("values"));
		this.valueType = this.valueHandler.getCodedType();
		
		this.codedType = tools.types.replaceTypeArgument(tools.types.replaceTypeArgument((DeclaredType)paramType, this.keyFullType, keyType), this.valueFullType, valueType);
		this.implType = paramType;
		
		String implName = properties.getValueOrEmpty("impl");
		if (implName.isEmpty())
			implName = defaultImplementation(tools.elements.asTypeElement(tools.types.asElement(paramType)));
		
		if (!implName.isEmpty())
		{
			TypeElement elem = tools.elements.getTypeElement(implName);
			if (elem == null)
				throw new IncompatibleTypeException("Unknown type '" + implName + "' as implementation for " + MAP_TYPE);
			
			this.implType = elem.asType();
			DeclaredType refinedImpl = tools.types.refineTo(this.implType, mapType);
			if (refinedImpl == null)
				throw new IncompatibleTypeException("The type '" + implName + "' is not a sub type of " + MAP_TYPE);
			
			TypeMirror implKeyFullType = refinedImpl.getTypeArguments().get(0);
			TypeMirror implValueFullType = refinedImpl.getTypeArguments().get(1);
			DeclaredType revisedImplType = tools.types.replaceTypeArgument(tools.types.replaceTypeArgument((DeclaredType)this.implType, implKeyFullType, keyType), implValueFullType, valueType);
			if (!tools.types.isAssignable(revisedImplType, this.codedType))
				throw new IncompatibleTypeException("The type '" + implName + "' is not a sub type of '" + paramType + "'");
			
		}
		
		DataCoderUtils.requireDefaultConstructor(tools.types, this.implType);
	}
	
	@Override
	public void addInstructions(IPacketBuilder builder, String saveAccessExpr, BiConsumer<IFunctionBuilder, String> setExpr)
	{
		String keyVarTmpName = uniqueName + "KeyTmp";
		String entryVarName = uniqueName + "Entry";
		String lenghtVarName = uniqueName + "Length";
		String indexVarName = uniqueName + "Index";

		builder.addImport(tools.elements.asTypeElement(tools.types.asElement(this.implType)));
		builder.addImport(tools.elements.getTypeElement(MAP_TYPE));
		tools.types.provideTypeImports(keyType, builder);
		tools.types.provideTypeImports(valueType, builder);
		
		builder.encoder()
			.add(DataCoderUtils.writeBuffer("Int", saveAccessExpr + ".size()"))
			.add("for (Map.Entry<" + tools.naming.computeFullName(keyFullType) + ", " + tools.naming.computeFullName(valueFullType) + "> " + entryVarName + " : " + saveAccessExpr + ".entrySet())")
			.add("{");
		
		
		builder.decoder().add(
			"int " + lenghtVarName + " = " + DataCoderUtils.readBuffer("Int") + ";",
			tools.naming.computeFullName(this.codedType) + " " + uniqueName + " = new " + tools.naming.computeSimplifiedName(implType) + "();",
			"for (int " + indexVarName + " = 0; " + indexVarName + " < " + lenghtVarName + "; " + indexVarName + "++)",
			"{");
		

		this.keyHandler.addInstructions(1, builder, entryVarName + ".getKey()", (loadInst, key) -> loadInst.add(tools.naming.computeFullName(keyType) + " " + keyVarTmpName + " = " + key + ";"));
		
		this.valueHandler.addInstructions(1, builder, entryVarName + ".getValue()", (loadInst, value) -> loadInst.add(uniqueName + ".put(" + keyVarTmpName + ", " + value + ");"));
		
		builder.encoder().add("}");
		builder.decoder().add("}");
		
		setExpr.accept(builder.decoder(), uniqueName);
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
