package fr.max2.annotated.processor.network.datahandler;

import java.util.UUID;

import fr.max2.annotated.processor.network.DataHandlerParameters;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.NamingUtils;

public enum SimpleClassHandler implements INamedDataHandler
{
	STRING(String.class)
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			DataHandlerUtils.addBufferInstructions("String", params.saveAccessExpr, params.setExpr, builder);
		}
	},
	ENUM(Enum.class)
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			builder.save().add(DataHandlerUtils.writeBuffer("Int", params.saveAccessExpr + ".ordinal()"));
			
			params.setExpr.accept(builder.load(), NamingUtils.computeFullName(params.type) + ".values()[buf.readInt()]");
		}
	},
	UUID(UUID.class)
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			DataHandlerUtils.addBufferInstructions("UniqueId", params.saveAccessExpr, params.setExpr, builder);
		}
	},
	STACK("net.minecraft.item.ItemStack")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			DataHandlerUtils.addBufferInstructions("ItemStack", params.saveAccessExpr, params.setExpr, builder);
		}
	};
	
	private final String className;
	
	private SimpleClassHandler(String className)
	{
		this.className = className;
	}
	
	private SimpleClassHandler(Class<?> type)
	{
		this(type.getTypeName());
	}

	@Override
	public String getTypeName()
	{
		return this.className;
	}
}
