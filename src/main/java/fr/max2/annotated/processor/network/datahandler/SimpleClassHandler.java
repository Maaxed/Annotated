package fr.max2.annotated.processor.network.datahandler;

import java.util.Date;
import java.util.UUID;

import fr.max2.annotated.processor.network.DataHandlerParameters;
import fr.max2.annotated.processor.network.model.IPacketBuilder;

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
			builder.encoder().add(DataHandlerUtils.writeBuffer("Int", params.saveAccessExpr + ".ordinal()"));
			
			params.setExpr.accept(builder.decoder(), params.tools.naming.computeFullName(params.type) + ".values()[buf.readInt()]");
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
	DATE(Date.class)
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			DataHandlerUtils.addBufferInstructions("Time", params.saveAccessExpr, params.setExpr, builder);
		}
	},
	BLOCK_POS("net.minecraft.util.math.BlockPos")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			DataHandlerUtils.addBufferInstructions("BlockPos", params.saveAccessExpr, params.setExpr, builder);
		}
	},
	RESOURCE_LOCATION("net.minecraft.util.ResourceLocation")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			DataHandlerUtils.addBufferInstructions("ResourceLocation", params.saveAccessExpr, params.setExpr, builder);
		}
	},
	ITEM_STACK("net.minecraft.item.ItemStack")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			DataHandlerUtils.addBufferInstructions("ItemStack", params.saveAccessExpr, params.setExpr, builder);
		}
	},
	FLUID_STACK("net.minecraftforge.fluids.FluidStack")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			DataHandlerUtils.addBufferInstructions("FluidStack", params.saveAccessExpr, params.setExpr, builder);
		}
	},
	TEXT_COMPONENT("net.minecraft.util.text.ITextComponent")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			DataHandlerUtils.addBufferInstructions("TextComponent", params.saveAccessExpr, params.setExpr, builder);
		}
	},
	BLOCK_RAY_TRACE("net.minecraft.util.math.BlockRayTraceResult")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			DataHandlerUtils.addBufferInstructions("BlockRay", params.saveAccessExpr, params.setExpr, builder);
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
