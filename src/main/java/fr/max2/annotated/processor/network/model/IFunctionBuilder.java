package fr.max2.annotated.processor.network.model;


public interface IFunctionBuilder
{
	IPacketBuilder end();
	
	IFunctionBuilder add(String... instructions);
	
	IFunctionBuilder indent(int indent);
}
