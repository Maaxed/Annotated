package fr.max2.annotated.processor.network.model;


public interface IPacketBuilder
{
	IPacketBuilder addImport(String className);
	
	// TODO [v2.2] IPacketBuilder addField(String field)
	
	IFunctionBuilder save();
	IFunctionBuilder load();
}
