package fr.max2.packeta.network.datahandler;

import java.util.UUID;
import java.util.function.Consumer;

import fr.max2.packeta.network.DataHandlerParameters;
import fr.max2.packeta.utils.NamingUtils;

public enum SimpleClassHandler implements INamedDataHandler
{
	STRING(String.class)
	{
		@Override
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			DataHandlerUtils.addBufferUtilsInstructions("UTF8String", params.saveAccessExpr, params.setExpr, saveInstructions, loadInstructions, imports);
		}
	},
	ENUM(Enum.class)
	{
		@Override
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			saveInstructions.accept(DataHandlerUtils.writeBuffer("Int", params.saveAccessExpr + ".ordinal()"));
			
			loadInstructions.accept(params.setExpr.apply(NamingUtils.simpleTypeName(params.type) + ".values()[buf.readInt()]"));
		}
	},
	UUID(UUID.class)
	{
		@Override
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			saveInstructions.accept(DataHandlerUtils.writeBuffer("Long", params.saveAccessExpr + ".getMostSignificantBits()"));
			saveInstructions.accept(DataHandlerUtils.writeBuffer("Long", params.saveAccessExpr + ".getLeastSignificantBits()"));
			
			loadInstructions.accept(params.setExpr.apply("new UUID(buf.readLong(), buf.readLong())"));
		}
	},
	STACK("net.minecraft.item.ItemStack")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			DataHandlerUtils.addBufferUtilsInstructions("ItemStack", params.saveAccessExpr, params.setExpr, saveInstructions, loadInstructions, imports);
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
