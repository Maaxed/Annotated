package fr.max2.annotated.processor.network.datahandler;

import java.util.function.Predicate;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import fr.max2.annotated.processor.network.DataHandlerParameters;
import fr.max2.annotated.processor.network.model.IPacketBuilder;

public interface IDataHandler
{
	void addInstructions(DataHandlerParameters params, IPacketBuilder builder);
	
	Predicate<TypeMirror> getTypeValidator(Elements elemUtils, Types typeUtils);
}
