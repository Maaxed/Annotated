package fr.max2.annotated.processor.network.datahandler;

import java.util.function.Predicate;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.DataHandlerParameters;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ExtendedElements;
import fr.max2.annotated.processor.utils.ExtendedTypes;

public interface IDataHandler
{
	void addInstructions(DataHandlerParameters params, IPacketBuilder builder);
	
	Predicate<TypeMirror> getTypeValidator(ExtendedElements elemUtils, ExtendedTypes typeUtils);
}
