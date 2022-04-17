package fr.max2.annotated.processor.network.coder.handler;

import javax.lang.model.type.TypeMirror;

public interface ICoderHandler<C> extends ICoderProvider<C>
{
	boolean canProcess(TypeMirror type);
}
