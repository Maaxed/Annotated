package fr.max2.annotated.processor.network.coder.handler;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.utils.ProcessingTools;

public interface IDataHandler extends IDataCoderProvider
{
	default void init(ProcessingTools tools)
	{ }
	
	boolean canProcess(TypeMirror type);
}
