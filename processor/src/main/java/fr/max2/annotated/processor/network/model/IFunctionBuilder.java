package fr.max2.annotated.processor.network.model;


public interface IFunctionBuilder<P>
{
	P end();
	
	IFunctionBuilder<P> addLines(String... instructions);
	
	IFunctionBuilder<P> indent(int indent);
}
