package fr.max2.packeta.network.datahandler;

import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import fr.max2.packeta.network.DataHandlerParameters;

public enum NBTDataHandler implements INamedDataHandler
{
	END("TagEnd")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			//Just create a new TagEnd, doesn't need to be saved
			params.setExpr.accept(loadInstructions, "new TagEnd()");
		}
	},
	BYTE("TagByte")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			addPrimitiveInstructions("Byte", params, saveInstructions, loadInstructions);
		}
	},
	SHORT("TagShort")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			addPrimitiveInstructions("Short", params, saveInstructions, loadInstructions);
		}
	},
	INT("TagInt")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			addPrimitiveInstructions("Int", params, saveInstructions, loadInstructions);
		}
	},
	LONG("TagLong")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			addPrimitiveInstructions("Long", params, saveInstructions, loadInstructions);
		}
	},
	FLOAT("TagFloat")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			addPrimitiveInstructions("Float", params, saveInstructions, loadInstructions);
		}
	},
	DOUBLE("TagDouble")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			addPrimitiveInstructions("Double", params, saveInstructions, loadInstructions);
		}
	},
	STRING("TagString")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			addBufferUtilsInstructions("String", params, saveInstructions, loadInstructions, imports);
		}
	},
	BYTE_ARRAY("TagByteArray")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			addCustomInstructions("ByteArray", params, saveInstructions, loadInstructions, imports);
		}
	},
	INT_ARRAY("TagIntArray")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			addCustomInstructions("IntArray", params, saveInstructions, loadInstructions, imports);
		}
	},
	LIST("TagList")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			addCustomInstructions("List", params, saveInstructions, loadInstructions, imports);
		}
	},
	COMPOUND("TagCompound")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			addCustomInstructions("Compound", params, saveInstructions, loadInstructions, imports);
		}
	},
	PRIMITIVE("Primitive")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			addCustomInstructions("Primitive", params, saveInstructions, loadInstructions, imports);
		}
	},
	BASE("Base")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			addCustomInstructions("Base", params, saveInstructions, loadInstructions, imports);
		}
	};
	
	private final String className;
	
	private NBTDataHandler(String nbtType)
	{
		this.className = "net.minecraft.nbt.NBT" + nbtType;
	}
	
	private static void addPrimitiveInstructions(String primitive, DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions)
	{
		DataHandlerUtils.addBufferInstructions(primitive, params.saveAccessExpr + ".get" + primitive + "()", params.setExpr, saveInstructions, loadInstructions);
	}
	
	private static void addBufferUtilsInstructions(String primitive, DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
	{
		DataHandlerUtils.addBufferUtilsInstructions(primitive, params.saveAccessExpr + ".get" + primitive + "()", params.setExpr, saveInstructions, loadInstructions, imports);
	}
	
	private static void addCustomInstructions(String type, DataHandlerParameters params, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
	{
		saveInstructions.accept("NBTPacketHelper.writeNBT" + type + "(buf, " + params.saveAccessExpr + ");");
		params.setExpr.accept(loadInstructions, "NBTPacketHelper.readNBT" + type + "(buf)");
		imports.accept("fr.max2.packeta.network.NBTPacketHelper");
	}
	
	@Override
	public Predicate<TypeMirror> getTypeValidator(Elements elemUtils, Types typeUtils)
	{
		TypeMirror thisType = this.getType(elemUtils, typeUtils);
		return type -> typeUtils.isAssignable(type, thisType) && typeUtils.isAssignable(thisType, type);
	}

	@Override
	public String getTypeName()
	{
		return this.className;
	}
	
	@Override
	public String toString()
	{
		return "NBT" + super.toString();
	}
	
}
