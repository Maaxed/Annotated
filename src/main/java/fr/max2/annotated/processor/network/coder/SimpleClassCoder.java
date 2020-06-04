package fr.max2.annotated.processor.network.coder;

import java.util.Date;
import java.util.UUID;

import fr.max2.annotated.processor.network.coder.handler.IHandlerProvider;
import fr.max2.annotated.processor.network.coder.handler.NamedDataHandler;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ClassRef;

public class SimpleClassCoder
{
	public static IHandlerProvider
		STRING = DataCoderUtils.simpleHandler(String.class, "String"),
		UUID = DataCoderUtils.simpleHandler(UUID.class, "UniqueId"),
		DATE = DataCoderUtils.simpleHandler(Date.class, "Time"),
		BLOCK_POS = DataCoderUtils.simpleHandler(ClassRef.BLOCK_POS, "BlockPos"),
		RESOURCE_LOCATION = DataCoderUtils.simpleHandler(ClassRef.RESOURCE_LOCATION, "ResourceLocation"),
		ITEM_STACK = DataCoderUtils.simpleHandler(ClassRef.ITEM_STACK, "ItemStack"),
		FLUID_STACK = DataCoderUtils.simpleHandler(ClassRef.FLUID_STACK, "FluidStack"),
		TEXT_COMPONENT = DataCoderUtils.simpleHandler(ClassRef.TEXT_COMPONENT, "TextComponent"),
		BLOCK_RAY_TRACE = DataCoderUtils.simpleHandler(ClassRef.BLOCK_RAY_TRACE, "BlockRay"),
		ENUM = NamedDataHandler.provider(Enum.class.getTypeName(), (tools, uniqueName, paramType, properties) -> new DataCoder(tools, uniqueName, paramType, properties)
		{
			@Override
			public OutputExpressions addInstructions(IPacketBuilder builder, String saveAccessExpr, String internalAccessExpr, String externalAccessExpr)
			{
				builder.encoder().add(DataCoderUtils.writeBuffer("Int", saveAccessExpr + ".ordinal()"));
				
				return new OutputExpressions(tools.naming.computeFullName(paramType) + ".values()[buf.readInt()]", internalAccessExpr, externalAccessExpr);
			}
		});
}
