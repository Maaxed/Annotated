package fr.max2.annotated.processor.network.datahandler;

import java.util.function.Predicate;

import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import fr.max2.annotated.processor.network.DataHandlerParameters;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.TypeHelper;

public enum SpecialDataHandler implements IDataHandler
{
	WILDCRD
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			WildcardType wildcardType = TypeHelper.asWildcardType(params.type);
			if (wildcardType == null) throw incompatibleType("wildcard", params.type);
			
			TypeMirror extendsBound = wildcardType.getExtendsBound();
			
			if (extendsBound == null) throw new IllegalArgumentException("The wildcard type '" + params.type + "' has no extends bound");
			
			params.finder.getDataType(params.simpleName, params.saveAccessExpr, params.loadAccessExpr, params.setExpr, extendsBound, params.annotations).addInstructions(builder);
		}

		@Override
		public Predicate<TypeMirror> getTypeValidator(Elements elemUtils, Types typeUtils)
		{
			return type -> type.getKind() == TypeKind.WILDCARD;
		}
	},
	VARIABLE_TYPE
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			TypeVariable wildcardType = TypeHelper.asVariableType(params.type);
			if (wildcardType == null) throw incompatibleType("variable", params.type);
			
			TypeMirror extendsBound = wildcardType.getUpperBound();
			
			params.finder.getDataType(params.simpleName, params.saveAccessExpr, params.loadAccessExpr, params.setExpr, extendsBound, params.annotations).addInstructions(builder);
		}

		@Override
		public Predicate<TypeMirror> getTypeValidator(Elements elemUtils, Types typeUtils)
		{
			return type -> type.getKind() == TypeKind.TYPEVAR;
		}
	},
	INTERSECTION
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			IntersectionType intersectionType = TypeHelper.asIntersectionType(params.type);
			if (intersectionType == null) throw incompatibleType("intersection", params.type);
			
			boolean success = false;
			
			for (TypeMirror type : intersectionType.getBounds())
			{
				DataHandlerParameters newParams = params.finder.getDataTypeOrNull(params.simpleName, params.saveAccessExpr, params.loadAccessExpr, params.setExpr, type, params.annotations);
				if (newParams != null)
				{
					newParams.addInstructions(builder);
					success = true;
				}
			}
			
			if (!success)
			{
				throw new IllegalArgumentException("None of the bounds of the interaction type '" + params.type + "' is serializable");
			}
		}

		@Override
		public Predicate<TypeMirror> getTypeValidator(Elements elemUtils, Types typeUtils)
		{
			return type -> type.getKind() == TypeKind.INTERSECTION;
		}
	},
	DEFAULT
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			DataHandlerParameters handler = new DataHandlerParameters(params.simpleName, params.saveAccessExpr, params.loadAccessExpr, params.setExpr, params.type, params.annotations, params.finder.getDefaultDataType(params.type), params.finder, params.parameters);
			handler.addInstructions(builder);
		}
	},
	CUSTOM
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			//TODO [v1.2] custom handler
			
		}
	};

	@Override
	public Predicate<TypeMirror> getTypeValidator(Elements elemUtils, Types typeUtils)
	{
		return type -> false;
	}
	
	private static RuntimeException incompatibleType(String expected, TypeMirror actual)
	{
		throw new IllegalArgumentException("The type '" + actual + "' is not a " + expected + " type");
	}
	
}
