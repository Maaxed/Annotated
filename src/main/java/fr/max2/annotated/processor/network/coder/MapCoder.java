package fr.max2.annotated.processor.network.coder;

import java.util.Map;
import java.util.function.BiConsumer;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.DataCoderParameters;
import fr.max2.annotated.processor.network.coder.handler.NamedDataHandler;
import fr.max2.annotated.processor.network.model.IFunctionBuilder;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;

public class MapCoder extends DataCoder
{
	private static final String MAP_TYPE = Map.class.getCanonicalName();
	public static final NamedDataHandler HANDLER = new NamedDataHandler(MAP_TYPE, MapCoder::new);

	private DataCoder keyHandler, valueHandler;
	private TypeMirror implType, keyType, keyFullType, valueType, valueFullType;
	
	@Override
	public void init(DataCoderParameters params)
	{
		super.init(params);
		
		TypeElement mapElem = params.tools.elements.getTypeElement(MAP_TYPE);
		TypeMirror mapType = mapElem.asType();
		DeclaredType refinedType = params.tools.types.refineTo(params.type, mapType);
		if (refinedType == null) throw new IncompatibleTypeException("The type '" + params.type + "' is not a sub type of " + MAP_TYPE);
		
		this.keyFullType = refinedType.getTypeArguments().get(0);
		this.keyHandler = params.tools.handlers.getDataType(params.uniqueName + "Key", this.keyFullType, params.properties.getSubPropertiesOrEmpty("keys"));
		this.keyType = this.keyHandler.getCodedType();

		this.valueFullType = refinedType.getTypeArguments().get(1);
		this.valueHandler = params.tools.handlers.getDataType(params.uniqueName + "Element", this.valueFullType, params.properties.getSubPropertiesOrEmpty("values"));
		this.valueType = this.valueHandler.getCodedType();
		
		this.codedType = params.tools.types.replaceTypeArgument(params.tools.types.replaceTypeArgument((DeclaredType)params.type, this.keyFullType, keyType), this.valueFullType, valueType);
		this.implType = params.type;
		
		String implName = params.properties.getValueOrEmpty("impl");
		if (implName.isEmpty())
			implName = defaultImplementation(params.tools.elements.asTypeElement(params.tools.types.asElement(params.type)));
		
		if (!implName.isEmpty())
		{
			TypeElement elem = params.tools.elements.getTypeElement(implName);
			if (elem == null)
				throw new IncompatibleTypeException("Unknown type '" + implName + "' as implementation for " + MAP_TYPE);
			
			this.implType = elem.asType();
			DeclaredType refinedImpl = params.tools.types.refineTo(this.implType, mapType);
			if (refinedImpl == null)
				throw new IncompatibleTypeException("The type '" + implName + "' is not a sub type of " + MAP_TYPE);
			
			TypeMirror implKeyFullType = refinedImpl.getTypeArguments().get(0);
			TypeMirror implValueFullType = refinedImpl.getTypeArguments().get(1);
			DeclaredType revisedImplType = params.tools.types.replaceTypeArgument(params.tools.types.replaceTypeArgument((DeclaredType)this.implType, implKeyFullType, keyType), implValueFullType, valueType);
			if (!params.tools.types.isAssignable(revisedImplType, this.codedType))
				throw new IncompatibleTypeException("The type '" + implName + "' is not a sub type of '" + params.type + "'");
			
		}
		
		DataCoderUtils.requireDefaultConstructor(params.tools.types, this.implType);
	}
	
	@Override
	public void addInstructions(IPacketBuilder builder, String saveAccessExpr, BiConsumer<IFunctionBuilder, String> setExpr)
	{
		String keyVarTmpName = params.uniqueName + "KeyTmp";
		String entryVarName = params.uniqueName + "Entry";
		String lenghtVarName = params.uniqueName + "Length";
		String indexVarName = params.uniqueName + "Index";

		builder.addImport(params.tools.elements.asTypeElement(params.tools.types.asElement(this.implType)));
		builder.addImport(params.tools.elements.getTypeElement(MAP_TYPE));
		params.tools.types.provideTypeImports(keyType, builder);
		params.tools.types.provideTypeImports(valueType, builder);
		
		builder.encoder()
			.add(DataCoderUtils.writeBuffer("Int", saveAccessExpr + ".size()"))
			.add("for (Map.Entry<" + params.tools.naming.computeFullName(keyFullType) + ", " + params.tools.naming.computeFullName(valueFullType) + "> " + entryVarName + " : " + saveAccessExpr + ".entrySet())")
			.add("{");
		
		
		builder.decoder().add(
			"int " + lenghtVarName + " = " + DataCoderUtils.readBuffer("Int") + ";",
			params.tools.naming.computeFullName(this.codedType) + " " + params.uniqueName + " = new " + params.tools.naming.computeSimplifiedName(implType) + "();",
			"for (int " + indexVarName + " = 0; " + indexVarName + " < " + lenghtVarName + "; " + indexVarName + "++)",
			"{");
		

		this.keyHandler.addInstructions(1, builder, entryVarName + ".getKey()", (loadInst, key) -> loadInst.add(params.tools.naming.computeFullName(keyType) + " " + keyVarTmpName + " = " + key + ";"));
		
		this.valueHandler.addInstructions(1, builder, entryVarName + ".getValue()", (loadInst, value) -> loadInst.add(params.uniqueName + ".put(" + keyVarTmpName + ", " + value + ");"));
		
		builder.encoder().add("}");
		builder.decoder().add("}");
		
		setExpr.accept(builder.decoder(), params.uniqueName);
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
