package fr.max2.annotated.processor.network.coder.handler;

import fr.max2.annotated.processor.utils.ProcessingTools;

public interface IHandlerProvider
{
	IDataHandler createHandler(ProcessingTools tools);
}
