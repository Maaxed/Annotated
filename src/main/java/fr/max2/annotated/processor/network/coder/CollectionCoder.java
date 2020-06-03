package fr.max2.annotated.processor.network.coder;

import java.util.Collection;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.api.processor.network.DataProperties;
import fr.max2.annotated.processor.network.coder.handler.NamedDataHandler;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PropertyMap;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;
import fr.max2.annotated.processor.utils.exceptions.CoderExcepetion;

public class CollectionCoder extends DataCoder
{
	private static final String COLLECTION_TYPE = Collection.class.getCanonicalName();
	public static final NamedDataHandler HANDLER = new NamedDataHandler(COLLECTION_TYPE, CollectionCoder::new);
	
	private final DataCoder contentCoder;
	private final TypeMirror codedType, extType, implType, contentType, extContentType;
	
	public CollectionCoder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties) throws CoderExcepetion
	{
		super(tools, uniqueName, paramType, properties);
		
		DeclaredType refinedType = tools.types.refineTo(paramType, HANDLER.getType());
		if (refinedType == null)
			throw new IncompatibleTypeException("The type '" + paramType + "' is not a sub type of " + COLLECTION_TYPE);

		TypeMirror contentFullType = refinedType.getTypeArguments().get(0);
		this.extContentType = tools.types.shallowErasure(contentFullType);
		this.contentCoder = tools.coders.getCoder(uniqueName + "Element", contentFullType, properties.getSubPropertiesOrEmpty("content"));
		TypeMirror contentType = this.contentCoder.getInternalType();
		if (contentType.getKind().isPrimitive())
			contentType = tools.types.boxedClass(tools.types.asPrimitive(contentType)).asType();
		
		this.contentType = tools.types.shallowErasure(contentType);

		this.extType = tools.types.replaceTypeArgument((DeclaredType)paramType, contentFullType, this.extContentType);
		this.internalType = tools.types.replaceTypeArgument((DeclaredType)paramType, contentFullType, contentType);
		this.codedType = tools.types.replaceTypeArgument((DeclaredType)paramType, contentFullType, this.contentType);
		TypeMirror implType = paramType;
		
		String implName = properties.getValueOrEmpty("impl");
		if (implName.isEmpty())
			implName = defaultImplementation(tools.elements.asTypeElement(tools.types.asElement(paramType)));
		
		if (!implName.isEmpty())
		{
			TypeElement elem = tools.elements.getTypeElement(implName);
			if (elem == null)
				throw new IncompatibleTypeException("Unknown type '" + implName + "' as implementation for " + COLLECTION_TYPE);

			implType = elem.asType();
			DeclaredType refinedImpl = tools.types.refineTo(implType, HANDLER.getType());
			if (refinedImpl == null)
				throw new IncompatibleTypeException("The type '" + implName + "' is not a sub type of " + COLLECTION_TYPE);
			
			TypeMirror implContentType = refinedImpl.getTypeArguments().get(0);
			DeclaredType revisedImplType = tools.types.replaceTypeArgument((DeclaredType)implType, implContentType, this.contentType);
			if (!tools.types.isAssignable(revisedImplType, this.codedType))
				throw new IncompatibleTypeException("The type '" + implName + "' is not a sub type of '" + paramType + "'");
		}
		
		this.implType = implType;
		DataCoderUtils.requireDefaultConstructor(tools.types, this.implType, "Use the " + DataProperties.class.getCanonicalName() + " annotation with the 'impl' property to specify a valid implementation");
	}
	
	@Override
	public OutputExpressions addInstructions(IPacketBuilder builder, String saveAccessExpr, String internalAccessExpr, String externalAccessExpr)
	{
		builder.addImport(this.tools.elements.asTypeElement(this.tools.types.asElement(this.implType)));
		
		String contentTypeName = this.tools.naming.computeFullName(this.contentType);
		String extContentTypeName = this.tools.naming.computeFullName(this.extContentType);
		this.tools.types.provideTypeImports(this.contentType, builder);
		
		String elementVarName = this.uniqueName + "Element";
		String lenghtVarName = this.uniqueName + "Length";
		String indexVarName = this.uniqueName + "Index";
		String convertedName = uniqueName + "Converted";
		
		builder.encoder().add(
			DataCoderUtils.writeBuffer("Int", saveAccessExpr + ".size()"),
			"for (" + contentTypeName + " " + elementVarName + " : " + saveAccessExpr + ")",
			"{");
		builder.decoder().add(
			"int " + lenghtVarName + " = " + DataCoderUtils.readBuffer("Int") + ";",
			tools.naming.computeFullName(this.codedType) + " " + this.uniqueName + " = new " + this.tools.naming.computeSimplifiedName(this.implType) + "();",
			"for (int " + indexVarName + " = 0; " + indexVarName + " < " + lenghtVarName + "; " + indexVarName + "++)",
			"{");

		builder.internalizer().add(
			tools.naming.computeFullName(this.codedType) + " " + convertedName + " = new " + this.tools.naming.computeSimplifiedName(this.implType) + "();",
			"for (" + extContentTypeName + " " + elementVarName + " : " + internalAccessExpr + ")",
			"{");
		builder.externalizer().add(
			tools.naming.computeFullName(this.extType) + " " + convertedName + " = new " + this.tools.naming.computeSimplifiedName(this.implType) + "();",
			"for (" + contentTypeName + " " + elementVarName + " : " + externalAccessExpr + ")",
			"{");
		
		builder.indentAll(1);
		OutputExpressions contentOutput = builder.runCoder(this.contentCoder, elementVarName);
		builder.decoder().add(this.uniqueName + ".add(" + contentOutput.decoded + ");");
		builder.internalizer().add(convertedName + ".add(" + contentOutput.internalized + ");");
		builder.externalizer().add(convertedName + ".add(" + contentOutput.externalized + ");");
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
