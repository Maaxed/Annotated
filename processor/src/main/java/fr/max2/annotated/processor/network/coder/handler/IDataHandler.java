package fr.max2.annotated.processor.network.coder.handler;

import javax.lang.model.type.TypeMirror;

public interface IDataHandler extends IDataCoderProvider
{
	boolean canProcess(TypeMirror type);
}
