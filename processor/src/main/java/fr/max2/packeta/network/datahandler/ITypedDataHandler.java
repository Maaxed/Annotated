package fr.max2.packeta.network.datahandler;

import java.util.function.Predicate;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public interface ITypedDataHandler extends IDataHandler
{
	@Override
	default Predicate<TypeMirror> getTypeValidator(Elements elemUtils, Types typeUtils)
	{
		TypeMirror thisType = this.getType(elemUtils, typeUtils);
		return type -> typeUtils.isAssignable(type, thisType);
	}
	
	TypeMirror getType(Elements elemUtils, Types typeUtils);
}
