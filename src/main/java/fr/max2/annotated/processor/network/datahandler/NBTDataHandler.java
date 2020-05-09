package fr.max2.annotated.processor.network.datahandler;

import java.util.function.Predicate;

import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.DataHandlerParameters;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ClassRef;
import fr.max2.annotated.processor.utils.ExtendedElements;
import fr.max2.annotated.processor.utils.ExtendedTypes;

public enum NBTDataHandler implements INamedDataHandler
{
	END("End")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			//Just create a new TagEnd, doesn't need to be saved
			params.setExpr.accept(builder.decoder(), "EndNBT.INSTANCE");
		}
	},
	BYTE("Byte")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			addBufferInstructions("Byte", params, builder);
		}
	},
	SHORT("Short")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			addBufferInstructions("Short", params, builder);
		}
	},
	INT("Int")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			addBufferInstructions("Int", params, builder);
		}
	},
	LONG("Long")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			addBufferInstructions("Long", params, builder);
		}
	},
	FLOAT("Float")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			addBufferInstructions("Float", params, builder);
		}
	},
	DOUBLE("Double")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			addBufferInstructions("Double", params, builder);
		}
	},
	STRING("String")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			addBufferInstructions("String", params, builder);
		}
	},
	BYTE_ARRAY("ByteArray")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			addCustomInstructions("ByteArray", params, builder);
		}
	},
	INT_ARRAY("IntArray")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			addCustomInstructions("IntArray", params, builder);
		}
	},
	LONG_ARRAY("LongArray")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			addCustomInstructions("LongArray", params, builder);
		}
	},
	LIST("List")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			addCustomInstructions("List", params, builder);
		}
	},
	COMPOUND("Compound")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			addCustomInstructions("Compound", params, builder);
		}
	},
	PRIMITIVE("Number")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			addCustomInstructions("Number", params, builder);
		}
	},
	BASE("I")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			addCustomInstructions("Base", params, builder);
		}
	};
	
	private final String className;
	
	private NBTDataHandler(String nbtType)
	{
		this.className = "net.minecraft.nbt." + nbtType + "NBT";
	}
	
	private static void addBufferInstructions(String primitive, DataHandlerParameters params, IPacketBuilder builder)
	{
		DataHandlerUtils.addBufferInstructions(primitive, params.saveAccessExpr + ".get" + primitive + "()", params.setExpr, builder);
	}
	
	private static void addCustomInstructions(String type, DataHandlerParameters params, IPacketBuilder builder)
	{
		builder.encoder().add("NBTPacketHelper.writeNBT(buf, " + params.saveAccessExpr + ");");
		params.setExpr.accept(builder.decoder(), "NBTPacketHelper.read" + type + "(buf)");
		builder.addImport(ClassRef.NBT_HELPER);
	}
	
	@Override
	public Predicate<TypeMirror> getTypeValidator(ExtendedElements elemUtils, ExtendedTypes typeUtils)
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
		return super.toString() + "NBT";
	}
	
}
