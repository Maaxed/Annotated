package fr.max2.annotated.processor.coder.handler;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.util.exceptions.CoderException;

public interface ICoderProvider<C>
{
	C createCoder(TypeMirror paramType) throws CoderException;
}
