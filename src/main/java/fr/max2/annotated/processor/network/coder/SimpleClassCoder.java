package fr.max2.annotated.processor.network.coder;

import java.util.Date;
import java.util.UUID;
import java.util.function.BiConsumer;

import fr.max2.annotated.processor.network.coder.handler.IDataHandler;
import fr.max2.annotated.processor.network.coder.handler.NamedDataHandler;
import fr.max2.annotated.processor.network.model.IFunctionBuilder;
import fr.max2.annotated.processor.network.model.IPacketBuilder;

public class SimpleClassCoder
{
	public static IDataHandler
		STRING = DataCoderUtils.simpleHandler(String.class, "String"),
		UUID = DataCoderUtils.simpleHandler(UUID.class, "UniqueId"),
		DATE = DataCoderUtils.simpleHandler(Date.class, "Time"),
		BLOCK_POS = DataCoderUtils.simpleHandler("net.minecraft.util.math.BlockPos", "BlockPos"),
		RESOURCE_LOCATION = DataCoderUtils.simpleHandler("net.minecraft.util.ResourceLocation", "ResourceLocation"),
		ITEM_STACK = DataCoderUtils.simpleHandler("net.minecraft.item.ItemStack", "ItemStack"),
		FLUID_STACK = DataCoderUtils.simpleHandler("net.minecraftforge.fluids.FluidStack", "FluidStack"),
		TEXT_COMPONENT = DataCoderUtils.simpleHandler("net.minecraft.util.text.ITextComponent", "TextComponent"),
		BLOCK_RAY_TRACE = DataCoderUtils.simpleHandler("net.minecraft.util.math.BlockRayTraceResult", "BlockRay"),
		ENUM = new NamedDataHandler(Enum.class.getTypeName(), () -> new DataCoder()
		{
			@Override
			public void addInstructions(IPacketBuilder builder, String saveAccessExpr, BiConsumer<IFunctionBuilder, String> setExpr)
			{
				builder.encoder().add(DataCoderUtils.writeBuffer("Int", saveAccessExpr + ".ordinal()"));
				
				setExpr.accept(builder.decoder(), tools.naming.computeFullName(paramType) + ".values()[buf.readInt()]");
			}
		});
}
