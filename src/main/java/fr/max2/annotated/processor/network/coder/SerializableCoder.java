package fr.max2.annotated.processor.network.coder;

import java.util.function.BiConsumer;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.IDataHandler;
import fr.max2.annotated.processor.network.coder.handler.NamedDataHandler;
import fr.max2.annotated.processor.network.model.IFunctionBuilder;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ClassRef;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PropertyMap;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;

public class SerializableCoder extends DataCoder
{
	public static final IDataHandler NBT_SERIALISABLE = new NamedDataHandler(ClassRef.NBT_SERIALIZABLE_INTERFACE, SerializableCoder::new);
	
	private TypeMirror nbtType;
	private DataCoder nbtHandler;
	
	public SerializableCoder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties)
	{
		super(tools, uniqueName, paramType, properties);
		
		String typeName = ClassRef.NBT_SERIALIZABLE_INTERFACE;
		DeclaredType serialisableType = tools.types.refineTo(paramType, tools.elements.getTypeElement(typeName).asType());
		if (serialisableType == null) throw new IncompatibleTypeException("The type '" + paramType + "' is not a sub type of " + typeName);
		DataCoderUtils.requireDefaultConstructor(tools.types, paramType);
		
		this.nbtType = serialisableType.getTypeArguments().get(0);
		this.nbtHandler = tools.handlers.getDataType(uniqueName + "Data", nbtType, properties.getSubPropertiesOrEmpty("nbt"));
	}
	
	@Override
	public void addInstructions(IPacketBuilder builder, String saveAccessExpr, BiConsumer<IFunctionBuilder, String> setExpr)
	{
		tools.types.provideTypeImports(this.nbtType, builder);
		
		String dataVarName = uniqueName + "Data";
		
		builder.encoder().add(tools.naming.computeFullName(this.nbtType) + " " + dataVarName + " = " + saveAccessExpr + ".serializeNBT()" + ";");
		
		builder.decoder().add(tools.naming.computeFullName(paramType) + " " + uniqueName + " = new " + tools.naming.computeSimplifiedName(paramType) + "();");
		
		this.nbtHandler.addInstructions(builder, dataVarName, (loadInst, value) -> loadInst.add(uniqueName + ".deserializeNBT(" + value + ");"));
		
		setExpr.accept(builder.decoder(), uniqueName); 
	}
}
