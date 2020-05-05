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
		
		String elementVarName = params.simpleName + "Element";
		builder.save()
			.add(DataHandlerUtils.writeBuffer("Int", params.saveAccessExpr + ".size()"))
			.add("for (" + typeName + " " + elementVarName + " : " + params.saveAccessExpr + ")")
			.add("{");
		
		String lenghtVarName = params.simpleName + "Length";
		String indexVarName = params.simpleName + "Index";
		
		builder.load().add("int " + lenghtVarName + " = " + DataHandlerUtils.readBuffer("Int") + ";");
		
		params.setLoadedValue(builder.load(), "new " + NamingUtils.computeSimplifiedName(params.type) + "()"); //TODO [v1.1] use parameters to use the right class
		
		builder.load()
			.add("for (int " + indexVarName + " = 0; " + indexVarName + " < " + lenghtVarName + "; " + indexVarName + "++)")
			.add("{");
		
		DataHandlerParameters contentHandler = params.finder.getDataType(elementVarName, elementVarName, params.getLoadAccessExpr() + ".get(" + indexVarName + ")", (loadInst, value) -> loadInst.add(params.getLoadAccessExpr() + ".add(" + value + ");"), contentType, EmptyAnnotationConstruct.INSTANCE);
		
		builder.save().indent(1);
		builder.load().indent(1);
		contentHandler.addInstructions(builder);
		builder.save().indent(-1);
		builder.load().indent(-1);
		
		builder.save().add("}");
		builder.load().add("}");
		
		if (params.loadAccessExpr == null)
		{
			params.setExpr.accept(builder.load(), params.simpleName); 
		}
	}

	@Override
	public String getTypeName()
	{
		return Collection.class.getCanonicalName();
	}
	
}
