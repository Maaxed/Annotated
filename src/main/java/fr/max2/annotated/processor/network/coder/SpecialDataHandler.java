package fr.max2.annotated.processor.network.coder;

import java.util.function.BiConsumer;

import javax.annotation.Nullable;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

import fr.max2.annotated.processor.network.coder.handler.IDataCoderProvider;
import fr.max2.annotated.processor.network.coder.handler.IDataHandler;
import fr.max2.annotated.processor.network.coder.handler.SimpleDataHandler;
import fr.max2.annotated.processor.network.model.IFunctionBuilder;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PropertyMap;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;

public class SpecialDataHandler extends DataCoder
{
	public static final IDataHandler WILDCRD = handler(TypeKind.WILDCARD, (tools, uniqueName, paramType, properties) ->
	{
		WildcardType wildcardType = tools.types.asWildcardType(paramType);
		if (wildcardType == null) throw incompatibleType("wildcard", paramType);
		
		TypeMirror extendsBound = wildcardType.getExtendsBound();
		
		if (extendsBound == null) throw new IncompatibleTypeException("The wildcard type '" + paramType + "' has no extends bound");
		
		return tools.handlers.getDataType(uniqueName, extendsBound, properties);
	}),
	VARIABLE_TYPE = handler(TypeKind.TYPEVAR, (tools, uniqueName, paramType, properties) ->
	{
		TypeVariable varType = tools.types.asVariableType(paramType);
		if (varType == null) throw incompatibleType("variable", paramType);
		
		return tools.handlers.getDataType(uniqueName, varType.getUpperBound(), properties);
	}),
	INTERSECTION = handler(TypeKind.INTERSECTION, (tools, uniqueName, paramType, properties) ->
	{
		IntersectionType intersectionType = tools.types.asIntersectionType(paramType);
		if (intersectionType == null) throw incompatibleType("intersection", paramType);
		
		for (TypeMirror type : intersectionType.getBounds())
		{
			DataCoder newParams = tools.handlers.getDataTypeOrNull(uniqueName, type, properties);
			if (newParams != null)
				return newParams;
		}
		
		throw new IncompatibleTypeException("None of the bounds of the interaction type '" + paramType + "' is serializable");
	}),
	DEFAULT = handler(null, (tools, uniqueName, paramType, properties) ->
	{
		DataCoder coder = tools.handlers.getDefaultDataType(paramType)
			.createCoder(tools, uniqueName, paramType, properties);
		return coder;
	}),
	CUSTOM = handler(null, (tools, uniqueName, paramType, properties) ->
	{
		throw new IncompatibleTypeException("No data handler can process the type '" + paramType.toString() + "'");
	});
	
	protected DataCoder actualCoder;
	
	private SpecialDataHandler(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties, DataCoder actualCoder)
	{
		super(tools, uniqueName, paramType, properties, actualCoder.getCodedType());
		this.actualCoder = actualCoder;
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
	
	private static IDataHandler handler(@Nullable TypeKind kind, IDataCoderProvider coderProvider)
	{
		return new SimpleDataHandler(type -> kind != null && type.getKind() == kind, (tools, uniqueName, paramType, properties) -> new SpecialDataHandler(tools, uniqueName, paramType, properties, coderProvider.createCoder(tools, uniqueName, paramType, properties)));
	}
}
