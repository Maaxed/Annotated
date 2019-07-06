package fr.max2.annotated.processor.network.datahandler;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public interface INamedDataHandler extends ITypedDataHandler
{
	@Override
	default TypeMirror getType(Elements elemUtils, Types typeUtils)
	{
		return typeUtils.erasure(elemUtils.getTypeElement(this.getTypeName()).asType());
	}
	
	String getTypeName();
}
