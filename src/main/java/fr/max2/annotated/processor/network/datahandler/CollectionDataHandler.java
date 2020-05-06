package fr.max2.annotated.processor.network.datahandler;

import java.util.Collection;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.DataHandlerParameters;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.EmptyAnnotationConstruct;
import fr.max2.annotated.processor.utils.NamingUtils;
import fr.max2.annotated.processor.utils.TypeHelper;

public enum CollectionDataHandler implements INamedDataHandler
{
	INSTANCE;
	
	@Override
	public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
	{
		DeclaredType collectionType = TypeHelper.refineTo(params.type, params.finder.elemUtils.getTypeElement(this.getTypeName()).asType(), params.finder.typeUtils);
		if (collectionType == null) throw new IllegalArgumentException("The type '" + params.type + "' is not a sub type of " + this.getTypeName());
		
		TypeMirror contentType = collectionType.getTypeArguments().get(0);
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
			NamingUtils.computeFullName(params.type) + " " + params.uniqueName + " = new " + NamingUtils.computeSimplifiedName(params.type) + "();", //TODO [v1.1] use parameters to use the right class
			"for (int " + indexVarName + " = 0; " + indexVarName + " < " + lenghtVarName + "; " + indexVarName + "++)",
			"{");
		
		DataHandlerParameters contentHandler = params.finder.getDataType(elementVarName, elementVarName, (loadInst, value) -> loadInst.add(params.uniqueName + ".add(" + value + ");"), contentType, EmptyAnnotationConstruct.INSTANCE);
		
		builder.encoder().indent(1);
		builder.decoder().indent(1);
		contentHandler.addInstructions(builder);
		builder.encoder().indent(-1);
		builder.decoder().indent(-1);
		
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
