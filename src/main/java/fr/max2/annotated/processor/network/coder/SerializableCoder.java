package fr.max2.annotated.processor.network.coder;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.IDataHandler;
import fr.max2.annotated.processor.network.coder.handler.NamedDataHandler;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ClassRef;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PropertyMap;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;

public class SerializableCoder extends DataCoder
{
	public static final IDataHandler NBT_SERIALISABLE = new NamedDataHandler(ClassRef.NBT_SERIALIZABLE_INTERFACE, SerializableCoder::new);
	
	private final TypeMirror nbtType;
	private final DataCoder nbtCoder;
	
	public SerializableCoder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties)
	{
		super(tools, uniqueName, paramType, properties);
		
		String typeName = ClassRef.NBT_SERIALIZABLE_INTERFACE;
		DeclaredType serialisableType = tools.types.refineTo(paramType, tools.elements.getTypeElement(typeName).asType());
		if (serialisableType == null) throw new IncompatibleTypeException("The type '" + paramType + "' is not a sub type of " + typeName);
		DataCoderUtils.requireDefaultConstructor(tools.types, paramType);
		
		this.nbtType = serialisableType.getTypeArguments().get(0);
		this.nbtCoder = tools.handlers.getDataType(uniqueName + "Data", this.nbtType, properties.getSubPropertiesOrEmpty("nbt"));
	}
	
	@Override
	public OutputExpressions addInstructions(IPacketBuilder builder, String saveAccessExpr, String internalAccessExpr, String externalAccessExpr)
	{
		tools.types.provideTypeImports(this.nbtType, builder);
		
		String dataVarName = this.uniqueName + "Data";
		
		builder.encoder().add(this.tools.naming.computeFullName(this.nbtType) + " " + dataVarName + " = " + saveAccessExpr + ".serializeNBT()" + ";");
		
		//TODO use impl param
		builder.decoder().add(this.tools.naming.computeFullName(this.paramType) + " " + this.uniqueName + " = new " + this.tools.naming.computeSimplifiedName(this.paramType) + "();");
		
		OutputExpressions nbtOutput = builder.runCoderWithoutConversion(this.nbtCoder, dataVarName);
		builder.decoder().add(this.uniqueName + ".deserializeNBT(" + nbtOutput.decoded + ");");
		
		return new OutputExpressions(this.uniqueName, internalAccessExpr, externalAccessExpr); 
	}
}
