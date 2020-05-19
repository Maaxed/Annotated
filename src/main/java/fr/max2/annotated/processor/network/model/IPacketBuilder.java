package fr.max2.annotated.processor.network.model;

public interface IPacketBuilder extends IImportClassBuilder<IPacketBuilder>
{
	IPacketBuilder require(String module);
	
	IFunctionBuilder encoder();
	IFunctionBuilder decoder();
}
