package fr.max2.annotated.processor.network.datahandler;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.DataHandlerParameters;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ClassRef;
import fr.max2.annotated.processor.utils.EmptyAnnotationConstruct;
import fr.max2.annotated.processor.utils.NamingUtils;
import fr.max2.annotated.processor.utils.TypeHelper;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;

public enum SerializableDataHandler implements INamedDataHandler
{
	NBT_SERIALISABLE(ClassRef.NBT_SERIALIZABLE_INTERFACE)
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			//TODO [v2.0] check if params.type has a default constructor or throw an error
			DeclaredType serialisableType = TypeHelper.refineTo(params.type, params.finder.elemUtils.getTypeElement(this.getTypeName()).asType(), params.finder.typeUtils);
			if (serialisableType == null) throw new IncompatibleTypeException("The type '" + params.type + "' is not a sub type of " + this.getTypeName());
			
			TypeMirror nbtType = serialisableType.getTypeArguments().get(0);
			
			TypeHelper.provideTypeImports(nbtType, builder::addImport);
			
			String dataVarName = params.uniqueName + "Data";
			
			builder.encoder().add(NamingUtils.computeFullName(nbtType) + " " + dataVarName + " = " + params.saveAccessExpr + ".serializeNBT()" + ";");
			
			builder.decoder().add(NamingUtils.computeFullName(params.type) + " " + params.uniqueName + " = new " + NamingUtils.computeSimplifiedName(params.type) + "();");
			
			DataHandlerParameters handler = params.finder.getDataType(dataVarName, dataVarName, (loadInst, value) -> loadInst.add(params.uniqueName + ".deserializeNBT(" + value + ");"), nbtType, EmptyAnnotationConstruct.INSTANCE);
			handler.addInstructions(builder);
			
			params.setExpr.accept(builder.decoder(), params.uniqueName); 
		}
	};
	
	private final String className;
	
	private SerializableDataHandler(String className)
	{
		this.className = className;
	}
	
	private SerializableDataHandler(Class<?> type)
	{
		this(type.getTypeName());
	}

	@Override
	public String getTypeName()
	{
		return this.className;
	}
}
