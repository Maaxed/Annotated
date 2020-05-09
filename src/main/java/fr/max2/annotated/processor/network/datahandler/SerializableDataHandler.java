package fr.max2.annotated.processor.network.datahandler;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.DataHandlerParameters;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ClassRef;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;

public enum SerializableDataHandler implements INamedDataHandler
{
	NBT_SERIALISABLE(ClassRef.NBT_SERIALIZABLE_INTERFACE)
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			DeclaredType serialisableType = params.tools.typeHelper.refineTo(params.type, params.tools.elements.getTypeElement(this.getTypeName()).asType());
			if (serialisableType == null) throw new IncompatibleTypeException("The type '" + params.type + "' is not a sub type of " + this.getTypeName());
			DataHandlerUtils.requireDefaultConstructor(params.tools.types, params.type);
			
			TypeMirror nbtType = serialisableType.getTypeArguments().get(0);
			
			params.tools.typeHelper.provideTypeImports(nbtType, builder::addImport);
			
			String dataVarName = params.uniqueName + "Data";
			
			builder.encoder().add(params.tools.naming.computeFullName(nbtType) + " " + dataVarName + " = " + params.saveAccessExpr + ".serializeNBT()" + ";");
			
			builder.decoder().add(params.tools.naming.computeFullName(params.type) + " " + params.uniqueName + " = new " + params.tools.naming.computeSimplifiedName(params.type) + "();");
			
			DataHandlerParameters handler = params.tools.handlers.getDataType(dataVarName, dataVarName, (loadInst, value) -> loadInst.add(params.uniqueName + ".deserializeNBT(" + value + ");"), nbtType, params.properties.getSubPropertiesOrEmpty("nbt"));
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
