package fr.max2.annotated.processor.network.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import fr.max2.annotated.processor.network.coder.DataCoder;
import fr.max2.annotated.processor.network.coder.DataCoder.OutputExpressions;
import fr.max2.annotated.processor.utils.ProcessingTools;

public class SimplePacketBuilder extends SimpleImportClassBuilder<IPacketBuilder> implements IPacketBuilder
{
	public final Set<String> modules = new HashSet<>();
	public final SimpleFunctionBuilder encodeFunction = new SimpleFunctionBuilder();
	public final SimpleFunctionBuilder decodeFunction = new SimpleFunctionBuilder();
	public final SimpleFunctionBuilder internalizeFunction = new SimpleFunctionBuilder();
	public final SimpleFunctionBuilder externalizeFunction = new SimpleFunctionBuilder();
	
	public SimplePacketBuilder(ProcessingTools tools, String packageName)
	{
		super(tools, packageName);
	}
	
	private OutputExpressions runCoder(DataCoder coder, String saveAccessExpr, String internalAccessExpr, String externalAccessExpr, boolean conversion)
	{
		boolean prevInt = this.internalizeFunction.active;
		boolean prevExt = this.externalizeFunction.active;
		this.internalizeFunction.active = conversion;
		this.externalizeFunction.active = conversion;
		OutputExpressions output = IPacketBuilder.super.runCoder(coder, saveAccessExpr, internalAccessExpr, externalAccessExpr);
		this.internalizeFunction.active = prevInt;
		this.externalizeFunction.active = prevExt;
		
		if (!coder.requireConversion())
			return new OutputExpressions(output.decoded, internalAccessExpr, externalAccessExpr);
		
		return output;
	}
	
	@Override
	public OutputExpressions runCoder(DataCoder coder, String saveAccessExpr, String internalAccessExpr, String externalAccessExpr)
	{
		return runCoder(coder, saveAccessExpr, internalAccessExpr, externalAccessExpr, coder.requireConversion());
	}
	
	@Override
	public OutputExpressions runCoderWithoutConversion(DataCoder coder, String saveAccessExpr)
	{
		return runCoder(coder, saveAccessExpr, "UNUSED", "UNUSED", false);
	}

	@Override
	public IPacketBuilder require(String module)
	{
		this.modules.add(module);
		return this;
	}

	@Override
	public IFunctionBuilder encoder()
	{
		return this.encodeFunction;
	}

	@Override
	public IFunctionBuilder decoder()
	{
		return this.decodeFunction;
	}
	
	@Override
	public IFunctionBuilder internalizer()
	{
		return this.internalizeFunction;
	}
	
	@Override
	public IFunctionBuilder externalizer()
	{
		return this.externalizeFunction;
	}
	
	private static String indentString(String str, int indent)
	{
		if (indent == 0)
			return str;

	    final char[] array = new char[indent];
	    for (int i = 0; i < indent; i++)
	    {
	    	array[i] = '\t';
	    }
		
		return new String(array) + str;
	}
	
	public class SimpleFunctionBuilder implements IFunctionBuilder
	{
		private List<String> instructions = new ArrayList<>();
		private int indent = 0;
		private boolean active = true;

		@Override
		public IPacketBuilder end()
		{
			return SimplePacketBuilder.this;
		}

		@Override
		public IFunctionBuilder add(String... instructions)
		{
			if (this.active)
			{
				for (String instruction : instructions)
				{
					this.instructions.add(indentString(instruction, this.indent));
				}
			}
			return this;
		}

		@Override
		public IFunctionBuilder indent(int indent)
		{
			this.indent += indent;
			
			if (this.indent < 0)
			{
				throw new IllegalStateException("Negative indent");
			}
			
			return this;
		}
		
		public Stream<String> instructions(int indent)
		{
			return this.instructions.stream().map(str -> indentString(str, indent));
		}
	}
}
