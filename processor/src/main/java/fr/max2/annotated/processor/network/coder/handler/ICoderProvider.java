package fr.max2.annotated.processor.network.coder.handler;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.CoderException;

public interface ICoderProvider<C>
{
	C createCoder(ProcessingTools tools, TypeMirror paramType) throws CoderException;
}
