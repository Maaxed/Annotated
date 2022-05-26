package fr.max2.annotated.processor.model;

import java.io.IOException;

public interface ICodeSupplier
{
	void pipe(ICodeConsumer output) throws IOException;
}
