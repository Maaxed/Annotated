package fr.max2.annotated.processor.network.coder;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.api.processor.network.DataProperties;
import fr.max2.annotated.processor.network.coder.handler.NamedDataHandler;
import fr.max2.annotated.processor.network.coder.handler.TypedDataHandler;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ClassRef;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PropertyMap;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;
import fr.max2.annotated.processor.utils.exceptions.CoderExcepetion;

public class SerializableCoder extends DataCoder
{
	public static final TypedDataHandler NBT_SERIALISABLE = new NamedDataHandler(ClassRef.NBT_SERIALIZABLE_INTERFACE, SerializableCoder::new);
	
	private final TypeMirror nbtType, implType;
	private final DataCoder nbtCoder;
	
	public SerializableCoder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties) throws CoderExcepetion
	{
		super(tools, uniqueName, paramType, properties);
		
		String typeName = ClassRef.NBT_SERIALIZABLE_INTERFACE;
		DeclaredType serialisableType = tools.types.refineTo(paramType, tools.elements.getTypeElement(typeName).asType());
		if (serialisableType == null) throw new IncompatibleTypeException("The type '" + paramType + "' is not a sub type of " + typeName);
		
		this.nbtType = serialisableType.getTypeArguments().get(0);
		this.nbtCoder = tools.handlers.getDataType(uniqueName + "Data", this.nbtType, properties.getSubPropertiesOrEmpty("nbt"));
		
		String implName = properties.getValueOrEmpty("impl");
		
		TypeMirror implType = paramType;
		if (!implName.isEmpty())
		{
			TypeElement elem = tools.elements.getTypeElement(implName);
			if (elem == null)
				throw new IncompatibleTypeException("Unknown type '" + implName + "' as implementation for " + ClassRef.NBT_SERIALIZABLE_INTERFACE);

			implType = elem.asType();
			DeclaredType refinedImpl = tools.types.refineTo(implType, NBT_SERIALISABLE.getType());
			if (refinedImpl == null)
				throw new IncompatibleTypeException("The type '" + implName + "' is not a sub type of " + ClassRef.NBT_SERIALIZABLE_INTERFACE);
			
			TypeMirror implNbtType = refinedImpl.getTypeArguments().get(0);
			DeclaredType revisedImplType = tools.types.replaceTypeArgument((DeclaredType)implType, implNbtType, this.nbtType);
			if (!tools.types.isAssignable(revisedImplType, this.internalType))
				throw new IncompatibleTypeException("The type '" + implName + "' is not a sub type of '" + this.internalType + "'");
		}

		this.implType = implType;
		DataCoderUtils.requireDefaultConstructor(tools.types, this.implType, "Use the " + DataProperties.class.getCanonicalName() + " annotation with the 'impl' property to specify a valid implementation");
	}
	
	@Override
	public OutputExpressions addInstructions(IPacketBuilder builder, String saveAccessExpr, String internalAccessExpr, String externalAccessExpr)
	{
		tools.types.provideTypeImports(this.nbtType, builder);
		
		String dataVarName = this.uniqueName + "Data";
		
		builder.encoder().add(this.tools.naming.computeFullName(this.nbtType) + " " + dataVarName + " = " + saveAccessExpr + ".serializeNBT()" + ";");
		
		builder.decoder().add(this.tools.naming.computeFullName(this.paramType) + " " + this.uniqueName + " = new " + this.tools.naming.computeSimplifiedName(this.implType) + "();");
		
		OutputExpressions nbtOutput = builder.runCoderWithoutConversion(this.nbtCoder, dataVarName);
		builder.decoder().add(this.uniqueName + ".deserializeNBT(" + nbtOutput.decoded + ");");
		
		return new OutputExpressions(this.uniqueName, internalAccessExpr, externalAccessExpr); 
	}
}
