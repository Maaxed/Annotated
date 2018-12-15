package fr.max2.packeta.network.datahandler;

import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import fr.max2.packeta.network.DataHandlerParameters;
import fr.max2.packeta.utils.TypeHelper;
import fr.max2.packeta.utils.ValueInitStatus;

public enum SpecialDataHandler implements IDataHandler
{
	WILDCRD
	{
		@Override
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			WildcardType wildcardType = TypeHelper.asWildcardType(params.type);
			if (wildcardType == null) throw new IllegalArgumentException("The type '" + params.type + "' is not a wildcard type");
			
			TypeMirror extendsBound = wildcardType.getExtendsBound();
			
			if (extendsBound == null) throw new IllegalArgumentException("The wildcard type '" + params.type + "' has no extends bound");
			
			params.finder.getDataType(params.simpleName, params.saveAccessExpr, params.loadAccessExpr, params.setExpr, extendsBound, params.annotations, params.initStatus).addInstructions(saveInstructions, loadInstructions, imports);
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
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			TypeVariable wildcardType = TypeHelper.asVariableType(params.type);
			if (wildcardType == null) throw new IllegalArgumentException("The type '" + params.type + "' is not a wildcard type");
			
			TypeMirror extendsBound = wildcardType.getUpperBound();
			
			params.finder.getDataType(params.simpleName, params.saveAccessExpr, params.loadAccessExpr, params.setExpr, extendsBound, params.annotations, params.initStatus).addInstructions(saveInstructions, loadInstructions, imports);
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
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			IntersectionType intersectionType = TypeHelper.asIntersectionType(params.type);
			if (intersectionType == null) throw new IllegalArgumentException("The type '" + params.type + "' is not an intersection type");
			
			ValueInitStatus initStatus = params.initStatus;
			boolean success = false;
			
			for (TypeMirror type : intersectionType.getBounds())
			{
				DataHandlerParameters newParams = params.finder.getDataTypeOrNull(params.simpleName, params.saveAccessExpr, params.loadAccessExpr, params.setExpr, type, params.annotations, initStatus);
				if (newParams != null)
				{
					newParams.addInstructions(saveInstructions, loadInstructions, imports);
					initStatus = ValueInitStatus.INITIALISED;
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
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			DataHandlerParameters handler = new DataHandlerParameters(params.simpleName, params.saveAccessExpr, params.loadAccessExpr, params.setExpr, params.type, params.annotations, params.finder.getDefaultDataType(params.type), params.initStatus, params.finder, params.parameters);
			handler.addInstructions(saveInstructions, loadInstructions, imports);
		}
	},
	CUSTOM
	{
		@Override
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			// TODO custom handler
			
		}
	};

	@Override
	public Predicate<TypeMirror> getTypeValidator(Elements elemUtils, Types typeUtils)
	{
		return type -> false;
	}
	
}
