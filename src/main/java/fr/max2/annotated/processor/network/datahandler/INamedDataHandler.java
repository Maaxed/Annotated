package fr.max2.annotated.processor.network.datahandler;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.utils.ExtendedElements;
import fr.max2.annotated.processor.utils.ExtendedTypes;

public interface INamedDataHandler extends ITypedDataHandler
{
	@Override
	default TypeMirror getType(ExtendedElements elemUtils, ExtendedTypes typeUtils)
	{
		return typeUtils.erasure(elemUtils.getTypeElement(this.getTypeName()).asType());
	}
	
	String getTypeName();
}
