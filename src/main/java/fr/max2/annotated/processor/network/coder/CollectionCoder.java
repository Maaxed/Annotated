package fr.max2.annotated.processor.network.coder;

import java.util.Collection;
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

public class CollectionCoder extends DataCoder
{
	private static final String COLLECTION_TYPE = Collection.class.getCanonicalName();
	public static final NamedDataHandler HANDLER = new NamedDataHandler(COLLECTION_TYPE, CollectionCoder::new);
	
	private DataCoder contentHandler;
	private TypeMirror implType, contentType;
	
	@Override
	public void init(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties)
	{
		super.init(tools, uniqueName, paramType, properties);
		
		DeclaredType refinedType = tools.types.refineTo(paramType, HANDLER.getType());
		if (refinedType == null)
			throw new IncompatibleTypeException("The type '" + paramType + "' is not a sub type of " + COLLECTION_TYPE);
		
		TypeMirror contentFullType = refinedType.getTypeArguments().get(0);
		this.contentHandler = tools.handlers.getDataType(uniqueName + "Element", contentFullType, properties.getSubPropertiesOrEmpty("content"));
		this.contentType = this.contentHandler.getCodedType();
		
		this.codedType = tools.types.replaceTypeArgument((DeclaredType)paramType, contentFullType, this.contentType);
		this.implType = paramType;
		
		String implName = properties.getValueOrEmpty("impl");
		if (implName.isEmpty())
			implName = defaultImplementation(tools.elements.asTypeElement(tools.types.asElement(paramType)));
		
		if (!implName.isEmpty())
		{
			TypeElement elem = tools.elements.getTypeElement(implName);
			if (elem == null)
				throw new IncompatibleTypeException("Unknown type '" + implName + "' as implementation for " + COLLECTION_TYPE);

			this.implType = elem.asType();
			DeclaredType refinedImpl = tools.types.refineTo(this.implType, HANDLER.getType());
			if (refinedImpl == null)
				throw new IncompatibleTypeException("The type '" + implName + "' is not a sub type of " + COLLECTION_TYPE);
			
			contentFullType = refinedImpl.getTypeArguments().get(0);
			DeclaredType revisedImplType = tools.types.replaceTypeArgument((DeclaredType)this.implType, contentFullType, this.contentType);
			if (!tools.types.isAssignable(revisedImplType, this.codedType))
				throw new IncompatibleTypeException("The type '" + implName + "' is not a sub type of '" + paramType + "'");
			
		}
		DataCoderUtils.requireDefaultConstructor(tools.types, this.implType);
	}
	
	@Override
	public void addInstructions(IPacketBuilder builder, String saveAccessExpr, BiConsumer<IFunctionBuilder, String> setExpr)
	{
		builder.addImport(tools.elements.asTypeElement(tools.types.asElement(this.implType)));
		
		String contentTypeName = tools.naming.computeFullName(this.contentType);
		tools.types.provideTypeImports(this.contentType, builder);
		
		String elementVarName = uniqueName + "Element";
		builder.encoder()
			.add(DataCoderUtils.writeBuffer("Int", saveAccessExpr + ".size()"))
			.add("for (" + contentTypeName + " " + elementVarName + " : " + saveAccessExpr + ")")
			.add("{");
		
		String lenghtVarName = uniqueName + "Length";
		String indexVarName = uniqueName + "Index";
		
		builder.decoder().add(
			"int " + lenghtVarName + " = " + DataCoderUtils.readBuffer("Int") + ";",
			tools.naming.computeFullName(this.codedType) + " " + uniqueName + " = new " + tools.naming.computeSimplifiedName(this.implType) + "();",
			"for (int " + indexVarName + " = 0; " + indexVarName + " < " + lenghtVarName + "; " + indexVarName + "++)",
			"{");
		
		this.contentHandler.addInstructions(1, builder, elementVarName, (loadInst, value) -> loadInst.add(uniqueName + ".add(" + value + ");"));
		
		builder.encoder().add("}");
		builder.decoder().add("}");
		
		setExpr.accept(builder.decoder(), uniqueName);
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
