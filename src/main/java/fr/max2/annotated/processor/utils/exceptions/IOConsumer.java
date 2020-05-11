package fr.max2.annotated.processor.utils.exceptions;

import java.io.IOException;

public interface IOConsumer<T>
{
	void accept(T t) throws IOException;
}
