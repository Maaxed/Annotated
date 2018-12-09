package fr.max2.packeta.network.datahandler;

import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import fr.max2.packeta.network.DataHandlerParameters;

public enum SpecialDataHandler implements IDataHandler
{
	DEFAULT
	{
		@Override
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			DataHandlerParameters handler = new DataHandlerParameters(params.simpleName, params.getExpr, params.setExpr, params.type, params.annotations, params.finder.getDefaultDataType(params.type), params.initStatus, params.finder, params.parameters);
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
