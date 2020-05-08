package fr.max2.annotated.processor.network.model;

import javax.lang.model.element.Name;

public interface IPacketBuilder
{
	IPacketBuilder addImport(String className);
	default IPacketBuilder addImport(Name className)
	{
		return addImport(className.toString());
	}
	
	// TODO [v2.1] IPacketBuilder addField(String field)
	
	IFunctionBuilder encoder();
	IFunctionBuilder decoder();
}
