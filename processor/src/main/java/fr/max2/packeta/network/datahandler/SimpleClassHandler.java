package fr.max2.packeta.network.datahandler;

import java.util.UUID;
import java.util.function.Consumer;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

import fr.max2.packeta.network.DataHandlerParameters;

public enum SimpleClassHandler implements INamedDataHandler
{
	STRING(String.class)
	{
		@Override
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			DataHandlerUtils.addBufferUtilsInstructions("UTF8String", params.getExpr, params.firstSetInit(), saveInstructions, loadInstructions, imports);
		}
	},
	ENUM(Enum.class)
	{
		@Override
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			saveInstructions.accept(DataHandlerUtils.writeBuffer("Int", params.getExpr + ".ordinal()"));
			
			loadInstructions.accept(params.firstSetInit() + " = " + ((TypeElement)((DeclaredType)params.type).asElement()).getSimpleName() + ".values()[buf.readInt()];");
		}
	},
	UUID(UUID.class)
	{
		@Override
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			saveInstructions.accept(DataHandlerUtils.writeBuffer("Long", params.getExpr + ".getMostSignificantBits()"));
			saveInstructions.accept(DataHandlerUtils.writeBuffer("Long", params.getExpr + ".getLeastSignificantBits()"));
			
			loadInstructions.accept(params.firstSetInit() + " = new UUID(buf.readLong(), buf.readLong());");
		}
	},
	NBT_COMPOUND("net.minecraft.nbt.NBTTagCompound")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			DataHandlerUtils.addBufferUtilsInstructions("Tag", params.getExpr, params.firstSetInit(), saveInstructions, loadInstructions, imports);
		}
	},
	STACK("net.minecraft.item.ItemStack")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			DataHandlerUtils.addBufferUtilsInstructions("ItemStack", params.getExpr, params.firstSetInit(), saveInstructions, loadInstructions, imports);
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
