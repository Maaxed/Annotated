package fr.max2.annotated.processor.network.model;

import fr.max2.annotated.processor.network.coder.DataCoder;
import fr.max2.annotated.processor.network.coder.DataCoder.OutputExpressions;

public interface IPacketBuilder extends IImportClassBuilder<IPacketBuilder>
{
	default OutputExpressions runCoder(DataCoder coder, String saveAccessExpr, String internalAccessExpr, String externalAccessExpr)
	{
		return coder.addInstructions(this, saveAccessExpr, internalAccessExpr, externalAccessExpr);
	}
	
	default OutputExpressions runCoder(DataCoder coder, String genericAccessExpr)
	{
		return this.runCoder(coder, genericAccessExpr, genericAccessExpr, genericAccessExpr);
	}
	
	OutputExpressions runCoderWithoutConversion(DataCoder coder, String saveAccessExpr);
	
	IPacketBuilder require(String module);
	
	IFunctionBuilder encoder();
	IFunctionBuilder decoder();

	IFunctionBuilder internalizer();
	IFunctionBuilder externalizer();
	
	default IPacketBuilder indentAll(int indent)
	{
		encoder().indent(indent);
		decoder().indent(indent);
		internalizer().indent(indent);
		externalizer().indent(indent);
		return this;
	}
}
