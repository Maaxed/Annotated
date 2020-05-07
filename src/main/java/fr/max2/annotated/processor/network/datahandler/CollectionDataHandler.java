package fr.max2.annotated.processor.network.datahandler;

import java.util.Collection;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.DataHandlerParameters;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.EmptyAnnotationConstruct;
import fr.max2.annotated.processor.utils.NamingUtils;
import fr.max2.annotated.processor.utils.TypeHelper;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;

public enum CollectionDataHandler implements INamedDataHandler
{
	INSTANCE;
	
	@Override
	public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
	{
		DeclaredType collectionType = TypeHelper.refineTo(params.type, params.finder.elemUtils.getTypeElement(this.getTypeName()).asType(), params.finder.typeUtils);
		if (collectionType == null) throw new IncompatibleTypeException("The type '" + params.type + "' is not a sub type of " + this.getTypeName());
		DataHandlerUtils.requireDefaultConstructor(params.finder.typeUtils, params.type);
		
		TypeMirror contentFullType = collectionType.getTypeArguments().get(0);
		TypeMirror contentType = TypeHelper.shallowErasure(contentFullType, params.finder.elemUtils);
		DeclaredType type = TypeHelper.replaceTypeArgument((DeclaredType)params.type, contentFullType, contentType, params.finder.typeUtils);
		
		
		String typeName = NamingUtils.computeFullName(contentType);
		
		String elementVarName = params.uniqueName + "Element";
		builder.encoder()
			.add(DataHandlerUtils.writeBuffer("Int", params.saveAccessExpr + ".size()"))
			.add("for (" + typeName + " " + elementVarName + " : " + params.saveAccessExpr + ")")
			.add("{");
		
		String lenghtVarName = params.uniqueName + "Length";
		String indexVarName = params.uniqueName + "Index";
		
		builder.decoder().add(
			"int " + lenghtVarName + " = " + DataHandlerUtils.readBuffer("Int") + ";",
			NamingUtils.computeFullName(type) + " " + params.uniqueName + " = new " + NamingUtils.computeSimplifiedName(type) + "();", //TODO [v2.1] use parameters to use the right class
			"for (int " + indexVarName + " = 0; " + indexVarName + " < " + lenghtVarName + "; " + indexVarName + "++)",
			"{");
		
		DataHandlerParameters contentHandler = params.finder.getDataType(elementVarName, elementVarName, (loadInst, value) -> loadInst.add(params.uniqueName + ".add(" + value + ");"), contentType, EmptyAnnotationConstruct.INSTANCE);
		
		contentHandler.addInstructions(1, builder);
		
		builder.encoder().add("}");
		builder.decoder().add("}");
		
		params.setExpr.accept(builder.decoder(), params.uniqueName);
	}

	@Override
	public String getTypeName()
	{
		return Collection.class.getCanonicalName();
	}
	
}
