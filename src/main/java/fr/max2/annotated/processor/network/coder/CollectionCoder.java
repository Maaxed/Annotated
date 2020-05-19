package fr.max2.annotated.processor.network.coder;

import java.util.Collection;
import java.util.function.BiConsumer;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.DataCoderParameters;
import fr.max2.annotated.processor.network.coder.handler.NamedDataHandler;
import fr.max2.annotated.processor.network.model.IFunctionBuilder;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;

public class CollectionCoder extends DataCoder
{
	private static final String COLLECTION_TYPE = Collection.class.getCanonicalName();
	public static final NamedDataHandler HANDLER = new NamedDataHandler(COLLECTION_TYPE, CollectionCoder::new);
	
	private DataCoder contentHandler;
	private TypeMirror implType, contentType;
	
	@Override
	public void init(DataCoderParameters params)
	{
		super.init(params);
		
		DeclaredType refinedType = params.tools.types.refineTo(params.type, HANDLER.getType());
		if (refinedType == null)
			throw new IncompatibleTypeException("The type '" + params.type + "' is not a sub type of " + COLLECTION_TYPE);
		
		TypeMirror contentFullType = refinedType.getTypeArguments().get(0);
		this.contentHandler = params.tools.handlers.getDataType(params.uniqueName + "Element", contentFullType, params.properties.getSubPropertiesOrEmpty("content"));
		this.contentType = this.contentHandler.getCodedType();
		
		this.codedType = params.tools.types.replaceTypeArgument((DeclaredType)params.type, contentFullType, this.contentType);
		this.implType = params.type;
		
		String implName = params.properties.getValueOrEmpty("impl");
		if (implName.isEmpty())
			implName = defaultImplementation(params.tools.elements.asTypeElement(params.tools.types.asElement(params.type)));
		
		if (!implName.isEmpty())
		{
			TypeElement elem = params.tools.elements.getTypeElement(implName);
			if (elem == null)
				throw new IncompatibleTypeException("Unknown type '" + implName + "' as implementation for " + COLLECTION_TYPE);

			this.implType = elem.asType();
			DeclaredType refinedImpl = params.tools.types.refineTo(this.implType, HANDLER.getType());
			if (refinedImpl == null)
				throw new IncompatibleTypeException("The type '" + implName + "' is not a sub type of " + COLLECTION_TYPE);
			
			contentFullType = refinedImpl.getTypeArguments().get(0);
			DeclaredType revisedImplType = params.tools.types.replaceTypeArgument((DeclaredType)this.implType, contentFullType, this.contentType);
			if (!params.tools.types.isAssignable(revisedImplType, this.codedType))
				throw new IncompatibleTypeException("The type '" + implName + "' is not a sub type of '" + params.type + "'");
			
		}
		DataCoderUtils.requireDefaultConstructor(params.tools.types, this.implType);
	}
	
	@Override
	public void addInstructions(IPacketBuilder builder, String saveAccessExpr, BiConsumer<IFunctionBuilder, String> setExpr)
	{
		builder.addImport(params.tools.elements.asTypeElement(params.tools.types.asElement(this.implType)));
		
		String contentTypeName = params.tools.naming.computeFullName(this.contentType);
		params.tools.types.provideTypeImports(this.contentType, builder);
		
		String elementVarName = params.uniqueName + "Element";
		builder.encoder()
			.add(DataCoderUtils.writeBuffer("Int", saveAccessExpr + ".size()"))
			.add("for (" + contentTypeName + " " + elementVarName + " : " + saveAccessExpr + ")")
			.add("{");
		
		String lenghtVarName = params.uniqueName + "Length";
		String indexVarName = params.uniqueName + "Index";
		
		builder.decoder().add(
			"int " + lenghtVarName + " = " + DataCoderUtils.readBuffer("Int") + ";",
			params.tools.naming.computeFullName(this.codedType) + " " + params.uniqueName + " = new " + params.tools.naming.computeSimplifiedName(this.implType) + "();",
			"for (int " + indexVarName + " = 0; " + indexVarName + " < " + lenghtVarName + "; " + indexVarName + "++)",
			"{");
		
		this.contentHandler.addInstructions(1, builder, elementVarName, (loadInst, value) -> loadInst.add(params.uniqueName + ".add(" + value + ");"));
		
		builder.encoder().add("}");
		builder.decoder().add("}");
		
		setExpr.accept(builder.decoder(), params.uniqueName);
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
}
