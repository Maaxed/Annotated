package fr.max2.annotated.processor.network.coder;

import java.util.function.BiConsumer;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.DataCoderParameters;
import fr.max2.annotated.processor.network.coder.handler.IDataHandler;
import fr.max2.annotated.processor.network.coder.handler.NamedDataHandler;
import fr.max2.annotated.processor.network.model.IFunctionBuilder;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ClassRef;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;

public class SerializableCoder extends DataCoder
{
	public static final IDataHandler NBT_SERIALISABLE = new NamedDataHandler(ClassRef.NBT_SERIALIZABLE_INTERFACE.qualifiedName(), SerializableCoder::new);
	
	private TypeMirror nbtType;
	private DataCoder nbtHandler;
	
	@Override
	public void init(DataCoderParameters params)
	{
		super.init(params);
		
		String typeName = ClassRef.NBT_SERIALIZABLE_INTERFACE.qualifiedName();
		DeclaredType serialisableType = params.tools.types.refineTo(params.type, params.tools.elements.getTypeElement(typeName).asType());
		if (serialisableType == null) throw new IncompatibleTypeException("The type '" + params.type + "' is not a sub type of " + typeName);
		DataCoderUtils.requireDefaultConstructor(params.tools.types, params.type);
		
		this.nbtType = serialisableType.getTypeArguments().get(0);
		this.nbtHandler = params.tools.handlers.getDataType(params.uniqueName + "Data", nbtType, params.properties.getSubPropertiesOrEmpty("nbt"));
	}
	
	@Override
	public void addInstructions(IPacketBuilder builder, String saveAccessExpr, BiConsumer<IFunctionBuilder, String> setExpr)
	{
		params.tools.types.provideTypeImports(this.nbtType, builder);
		
		String dataVarName = params.uniqueName + "Data";
		
		builder.encoder().add(params.tools.naming.computeFullName(this.nbtType) + " " + dataVarName + " = " + saveAccessExpr + ".serializeNBT()" + ";");
		
		builder.decoder().add(params.tools.naming.computeFullName(params.type) + " " + params.uniqueName + " = new " + params.tools.naming.computeSimplifiedName(params.type) + "();");
		
		this.nbtHandler.addInstructions(builder, dataVarName, (loadInst, value) -> loadInst.add(params.uniqueName + ".deserializeNBT(" + value + ");"));
		
		setExpr.accept(builder.decoder(), params.uniqueName); 
	}
}
