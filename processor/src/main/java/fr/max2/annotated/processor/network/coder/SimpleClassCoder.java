package fr.max2.annotated.processor.network.coder;

import java.util.Date;
import java.util.UUID;

import fr.max2.annotated.processor.network.coder.handler.IHandlerProvider;
import fr.max2.annotated.processor.network.coder.handler.NamedDataHandler;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ClassRef;

public class SimpleClassCoder
{
	public static final IHandlerProvider
		STRING = NamedDataHandler.provider(String.class.getCanonicalName(), (tools, uniqueName, paramType, properties) -> 
		{
			int maxLength = properties.getValue("maxLength").map(Integer::valueOf).orElse(32767);
			return new DataCoder(tools, uniqueName, paramType, properties)
			{
				@Override
				public OutputExpressions addInstructions(IPacketBuilder builder, String saveAccessExpr, String internalAccessExpr, String externalAccessExpr)
				{
					builder.encoder().add("buf." + this.tools.naming.getMethodMapping("func_211400_a", "writeString") + "(" + saveAccessExpr + ", " + maxLength + ");");
					return new OutputExpressions("buf." + this.tools.naming.getMethodMapping("func_150789_c", "readString") + "(" + maxLength + ")", internalAccessExpr, externalAccessExpr);
				}
			};
		}),
		UUID = SimpleDataCoder.handler(UUID.class, "UniqueId", "func_179252_a", "func_179253_g"),
		DATE = SimpleDataCoder.handler(Date.class, "Time", "func_192574_a", "func_192573_m"),
		BLOCK_POS = SimpleDataCoder.handler(ClassRef.BLOCK_POS, "BlockPos", "func_179255_a", "func_179259_c"),
		RESOURCE_LOCATION = SimpleDataCoder.handler(ClassRef.RESOURCE_LOCATION, "ResourceLocation", "func_192572_a", "func_192575_l"),
		ITEM_STACK = SimpleDataCoder.handler(ClassRef.ITEM_STACK, "ItemStack", "func_150788_a", "func_150791_c"),
		FLUID_STACK = SimpleDataCoder.handler(ClassRef.FLUID_STACK, "FluidStack", null, null),
		TEXT_COMPONENT = SimpleDataCoder.handler(ClassRef.TEXT_COMPONENT, "TextComponent", "func_179256_a", "func_179258_d"),
		BLOCK_RAY_TRACE = SimpleDataCoder.handler(ClassRef.BLOCK_RAY_TRACE, "BlockRay", "func_218668_a", "func_218669_q"),
		ENUM = NamedDataHandler.provider(Enum.class.getTypeName(), (tools, uniqueName, paramType, properties) -> new DataCoder(tools, uniqueName, paramType, properties)
		{
			@Override
			public OutputExpressions addInstructions(IPacketBuilder builder, String saveAccessExpr, String internalAccessExpr, String externalAccessExpr)
			{
				builder.encoder().add(this.writeBuffer("Int", saveAccessExpr + ".ordinal()", null));
				
				return new OutputExpressions(tools.naming.computeFullName(paramType) + ".values()[" + this.readBuffer("Int", null) + "]", internalAccessExpr, externalAccessExpr);
			}
		});
}
