package fr.max2.annotated.processor.network.coder.handler;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.DataCoder;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PropertyMap;
import fr.max2.annotated.processor.utils.exceptions.CoderExcepetion;

public interface IDataCoderProvider
{
	DataCoder createCoder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties) throws CoderExcepetion;
}
