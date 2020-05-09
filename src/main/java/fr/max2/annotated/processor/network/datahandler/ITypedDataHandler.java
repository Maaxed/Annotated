package fr.max2.annotated.processor.network.datahandler;

import java.util.function.Predicate;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.utils.ExtendedElements;
import fr.max2.annotated.processor.utils.ExtendedTypes;

public interface ITypedDataHandler extends IDataHandler
{
	@Override
	default Predicate<TypeMirror> getTypeValidator(ExtendedElements elemUtils, ExtendedTypes typeUtils)
	{
		TypeMirror thisType = this.getType(elemUtils, typeUtils);
		return type -> typeUtils.isAssignable(type, thisType);
	}
	
	TypeMirror getType(ExtendedElements elemUtils, ExtendedTypes typeUtils);
}
