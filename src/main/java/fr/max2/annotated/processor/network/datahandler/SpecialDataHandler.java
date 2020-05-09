package fr.max2.annotated.processor.network.datahandler;

import java.util.function.Predicate;

import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

import fr.max2.annotated.processor.network.DataHandlerParameters;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ExtendedElements;
import fr.max2.annotated.processor.utils.ExtendedTypes;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;

public enum SpecialDataHandler implements IDataHandler
{
	WILDCRD
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			WildcardType wildcardType = params.tools.types.asWildcardType(params.type);
			if (wildcardType == null) throw incompatibleType("wildcard", params.type);
			
			TypeMirror extendsBound = wildcardType.getExtendsBound();
			
			if (extendsBound == null) throw new IncompatibleTypeException("The wildcard type '" + params.type + "' has no extends bound");
			
			params.tools.handlers.getDataType(params.uniqueName, params.saveAccessExpr, params.setExpr, extendsBound, params.properties).addInstructions(builder);
		}

		@Override
		public Predicate<TypeMirror> getTypeValidator(ExtendedElements elemUtils, ExtendedTypes typeUtils)
		{
			return type -> type.getKind() == TypeKind.WILDCARD;
		}
	},
	VARIABLE_TYPE
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			TypeVariable wildcardType = params.tools.types.asVariableType(params.type);
			if (wildcardType == null) throw incompatibleType("variable", params.type);
			
			TypeMirror extendsBound = wildcardType.getUpperBound();
			
			params.tools.handlers.getDataType(params.uniqueName, params.saveAccessExpr, params.setExpr, extendsBound, params.properties).addInstructions(builder);
		}

		@Override
		public Predicate<TypeMirror> getTypeValidator(ExtendedElements elemUtils, ExtendedTypes typeUtils)
		{
			return type -> type.getKind() == TypeKind.TYPEVAR;
		}
	},
	INTERSECTION
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			IntersectionType intersectionType = params.tools.types.asIntersectionType(params.type);
			if (intersectionType == null) throw incompatibleType("intersection", params.type);
			
			boolean success = false;
			
			for (TypeMirror type : intersectionType.getBounds())
			{
				DataHandlerParameters newParams = params.tools.handlers.getDataTypeOrNull(params.uniqueName, params.saveAccessExpr, params.setExpr, type, params.properties);
				if (newParams != null)
				{
					newParams.addInstructions(builder);
					success = true;
				}
			}
			
			if (!success)
			{
				throw new IncompatibleTypeException("None of the bounds of the interaction type '" + params.type + "' is serializable");
			}
		}

		@Override
		public Predicate<TypeMirror> getTypeValidator(ExtendedElements elemUtils, ExtendedTypes typeUtils)
		{
			return type -> type.getKind() == TypeKind.INTERSECTION;
		}
	},
	DEFAULT
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			DataHandlerParameters handler = new DataHandlerParameters(params.tools, params.uniqueName, params.saveAccessExpr, params.setExpr, params.type, params.tools.handlers.getDefaultDataType(params.type), params.properties);
			handler.addInstructions(builder);
		}
	},
	CUSTOM
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			//TODO [v2.1] custom handler
			throw new IncompatibleTypeException("No data handler can process the type '" + params.type.toString() + "'");
		}
	};

	@Override
	public Predicate<TypeMirror> getTypeValidator(ExtendedElements elemUtils, ExtendedTypes typeUtils)
	{
		return type -> false;
	}
	
	private static RuntimeException incompatibleType(String expected, TypeMirror actual)
	{
		throw new IncompatibleTypeException("The type '" + actual + "' is not a " + expected + " type");
	}
	
}
