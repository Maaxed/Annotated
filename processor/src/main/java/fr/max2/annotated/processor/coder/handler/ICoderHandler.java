package fr.max2.annotated.processor.coder.handler;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.coder.CoderCompatibility;

public interface ICoderHandler<C> extends ICoderProvider<C>
{
	CoderCompatibility getCompatibilityFor(TypeMirror type);
}
