package fr.max2.annotated.processor.network.coder;

import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.annotation.Nullable;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

import fr.max2.annotated.processor.network.coder.handler.IDataHandler;
import fr.max2.annotated.processor.network.coder.handler.SimpleDataHandler;
import fr.max2.annotated.processor.network.model.IFunctionBuilder;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PropertyMap;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;

public class SpecialDataHandler extends DataCoder
{
	public static final IDataHandler WILDCRD = handler(TypeKind.WILDCARD, (params) ->
	{
		WildcardType wildcardType = params.tools.types.asWildcardType(params.paramType);
		if (wildcardType == null) throw incompatibleType("wildcard", params.paramType);
		
		TypeMirror extendsBound = wildcardType.getExtendsBound();
		
		if (extendsBound == null) throw new IncompatibleTypeException("The wildcard type '" + params.paramType + "' has no extends bound");
		
		return params.tools.handlers.getDataType(params.uniqueName, extendsBound, params.properties);
	}),
	VARIABLE_TYPE = handler(TypeKind.TYPEVAR, (params) ->
	{
		TypeVariable varType = params.tools.types.asVariableType(params.paramType);
		if (varType == null) throw incompatibleType("variable", params.paramType);
		
		return params.tools.handlers.getDataType(params.uniqueName, varType.getUpperBound(), params.properties);
	}),
	INTERSECTION = handler(TypeKind.INTERSECTION, (params) ->
	{
		IntersectionType intersectionType = params.tools.types.asIntersectionType(params.paramType);
		if (intersectionType == null) throw incompatibleType("intersection", params.paramType);
		
		for (TypeMirror type : intersectionType.getBounds())
		{
			DataCoder newParams = params.tools.handlers.getDataTypeOrNull(params.uniqueName, type, params.properties);
			if (newParams != null)
				return newParams;
		}
		
		throw new IncompatibleTypeException("None of the bounds of the interaction type '" + params.paramType + "' is serializable");
	}),
	DEFAULT = handler(null, (params) ->
	{
		DataCoder coder = params.tools.handlers.getDefaultDataType(params.paramType).createCoder();
		coder.init(params.tools, params.uniqueName, params.paramType, params.properties);
		return coder;
	}),
	CUSTOM = handler(null, (params) ->
	{
		throw new IncompatibleTypeException("No data handler can process the type '" + params.paramType.toString() + "'");
	});
	
	private final Function<DataCoder, DataCoder> coderProvider;
	protected DataCoder actualCoder;
	
	private SpecialDataHandler(Function<DataCoder, DataCoder> coderProvider)
	{
		this.coderProvider = coderProvider;
	}
	
	@Override
	public void init(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties)
	{
		super.init(tools, uniqueName, paramType, properties);
		this.actualCoder = coderProvider.apply(this);
		this.codedType = this.actualCoder.getCodedType();
	}
	
	@Override
	public void addInstructions(IPacketBuilder builder, String saveAccessExpr, BiConsumer<IFunctionBuilder, String> setExpr)
	{
		this.actualCoder.addInstructions(builder, saveAccessExpr, setExpr);
	}
	
	private static RuntimeException incompatibleType(String expected, TypeMirror actual)
	{
		throw new IncompatibleTypeException("The type '" + actual + "' is not a " + expected + " type");
	}
	
	private static IDataHandler handler(@Nullable TypeKind kind, Function<DataCoder, DataCoder> coderProvider)
	{
		return new SimpleDataHandler(type -> kind != null && type.getKind() == kind, () -> new SpecialDataHandler(coderProvider));
	}
}
