package fr.max2.annotated.processor.network.model;

public interface IPacketBuilder extends IImportClassBuilder<IPacketBuilder>
{
	// TODO [v2.1] IPacketBuilder addField(String field)
	
	IFunctionBuilder encoder();
	IFunctionBuilder decoder();
}
