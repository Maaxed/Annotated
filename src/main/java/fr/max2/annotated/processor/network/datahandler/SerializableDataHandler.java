package fr.max2.annotated.processor.network.datahandler;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.DataHandlerParameters;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ClassRef;
import fr.max2.annotated.processor.utils.EmptyAnnotationConstruct;
import fr.max2.annotated.processor.utils.NamingUtils;
import fr.max2.annotated.processor.utils.TypeHelper;

public enum SerializableDataHandler implements INamedDataHandler
{
	NBT_SERIALISABLE(ClassRef.NBT_SERIALIZABLE_INTERFACE)
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			DeclaredType serialisableType = TypeHelper.refineTo(params.type, params.finder.elemUtils.getTypeElement(this.getTypeName()).asType(), params.finder.typeUtils);
			if (serialisableType == null) throw new IllegalArgumentException("The type '" + params.type + "' is not a sub type of " + this.getTypeName());
			
			TypeMirror nbtType = serialisableType.getTypeArguments().get(0);
			
			TypeHelper.provideTypeImports(nbtType, builder::addImport);
			
			params.setLoadedValue(builder.load(), "new " + NamingUtils.computeSimplifiedName(params.type) + "()");
			
			params.finder.getDataType(params.simpleName + "Data", params.saveAccessExpr + ".serializeNBT()", params.saveAccessExpr + ".serializeNBT()", (loadInst, value) -> loadInst.add(params.getLoadAccessExpr() + ".deserializeNBT(" + value + ");"), nbtType, EmptyAnnotationConstruct.INSTANCE);
			
			params.setExpr.accept(builder.load(), params.simpleName); 
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
