package fr.max2.packeta.api.network;

import java.security.InvalidParameterException;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public enum DataType
{
	// Integers
	BYTE(Byte.TYPE) {
		
		@Override
		protected String[] saveDataInstructions(Element field, String[] parameters)
		{
			return new String[] { writeBuffer("Byte", "this." + field.getSimpleName()) };
		}
		
		@Override
		protected String[] loadDataInstructions(Element field, String[] parameters)
		{
			return new String[] { readBuffer("Byte", "this." + field.getSimpleName()) };
		}
	},
	SHORT(Short.TYPE) {
		
		@Override
		protected String[] saveDataInstructions(Element field, String[] parameters)
		{
			return new String[] { writeBuffer("Short", "this." + field.getSimpleName()) };
		}
		
		@Override
		protected String[] loadDataInstructions(Element field, String[] parameters)
		{
			return new String[] { readBuffer("Short", "this." + field.getSimpleName()) };
		}
	},
	INT(Integer.TYPE) {
		
		@Override
		protected String[] saveDataInstructions(Element field, String[] parameters)
		{
			return new String[] { writeBuffer("Int", "this." + field.getSimpleName()) };
		}
		
		@Override
		protected String[] loadDataInstructions(Element field, String[] parameters)
		{
			return new String[] { readBuffer("Int", "this." + field.getSimpleName()) };
		}
	},
	LONG(Long.TYPE) {
		
		@Override
		protected String[] saveDataInstructions(Element field, String[] parameters)
		{
			return new String[] { writeBuffer("Long", "this." + field.getSimpleName()) };
		}
		
		@Override
		protected String[] loadDataInstructions(Element field, String[] parameters)
		{
			return new String[] { readBuffer("Long", "this." + field.getSimpleName()) };
		}
	},
	
	//Floats
	FLOAT(Float.TYPE) {
		
		@Override
		protected String[] saveDataInstructions(Element field, String[] parameters)
		{
			return new String[] { writeBuffer("Float", "this." + field.getSimpleName()) };
		}
		
		@Override
		protected String[] loadDataInstructions(Element field, String[] parameters)
		{
			return new String[] { readBuffer("Float", "this." + field.getSimpleName()) };
		}
	},
	DOUBLE(Double.TYPE) {
		
		@Override
		protected String[] saveDataInstructions(Element field, String[] parameters)
		{
			return new String[] { writeBuffer("Double", "this." + field.getSimpleName()) };
		}
		
		@Override
		protected String[] loadDataInstructions(Element field, String[] parameters)
		{
			return new String[] { readBuffer("Double", "this." + field.getSimpleName()) };
		}
	},
	
	// Other primitives
	BOOLEAN(Boolean.TYPE) {
		
		@Override
		protected String[] saveDataInstructions(Element field, String[] parameters)
		{
			return new String[] { writeBuffer("Boolean", "this." + field.getSimpleName()) };
		}
		
		@Override
		protected String[] loadDataInstructions(Element field, String[] parameters)
		{
			return new String[] { readBuffer("Boolean", "this." + field.getSimpleName()) };
		}
	},
	CHAR(Character.TYPE) {
		
		@Override
		protected String[] saveDataInstructions(Element field, String[] parameters)
		{
			return new String[] { writeBuffer("Char", "this." + field.getSimpleName()) };
		}
		
		@Override
		protected String[] loadDataInstructions(Element field, String[] parameters)
		{
			return new String[] { readBuffer("Char", "this." + field.getSimpleName()) };
		}
	},
	
	// Classes
	STRING(String.class) {

		@Override
		protected String[] saveDataInstructions(Element field, String[] parameters)
		{
			return new String[] { writeBufferUtils("UTF8String", "this." + field.getSimpleName()) };
		}

		@Override
		protected String[] loadDataInstructions(Element field, String[] parameters)
		{
			return new String[] { readBufferUtils("UTF8String", "this." + field.getSimpleName()) };
		}
		
		@Override
		protected void addImportsToSet(Element field, String[] parameters, Set<String> imports)
		{
			imports.add("net.minecraftforge.fml.common.network.ByteBufUtils");
		}
	},
	ENUM(Enum.class, false) {
		
		@Override
		protected String[] saveDataInstructions(Element field, String[] parameters)
		{
			return new String[] { "buf.writeInt(this." + field.getSimpleName() + ".ordinal());" };
		}
		
		@Override
		protected String[] loadDataInstructions(Element field, String[] parameters)
		{
			return new String[] { "this." + field.getSimpleName() + " = " + ((TypeElement)((DeclaredType)field.asType()).asElement()).getSimpleName() + ".values()[buf.readInt()];" };
		}
		
		@Override
		protected void addImportsToSet(Element field, String[] parameters, Set<String> imports)
		{
			imports.add(field.asType().toString());
		}
	},
	UUID(UUID.class, false)
	{
		
		@Override
		protected String[] saveDataInstructions(Element field, String[] parameters)
		{
			return new String[] {
				"buf.writeLong(this." + field.getSimpleName() + ".getMostSignificantBits())",
				"buf.writeLong(this." + field.getSimpleName() + ".getLeastSignificantBits())"};
		}
		
		@Override
		protected String[] loadDataInstructions(Element field, String[] parameters)
		{
			return new String[] { "this." + field.getSimpleName() + " = new UUID(buf.readLong(), buf.readLong());" };
		}
		
		@Override
		protected void addImportsToSet(Element field, String[] parameters, Set<String> imports)
		{
			imports.add("java.util.UUID");
		}
	},
	NBT_COMPOUND("net.minecraft.nbt.NBTTagCompound") {

		@Override
		protected String[] saveDataInstructions(Element field, String[] parameters)
		{
			return new String[] { writeBufferUtils("Tag", "this." + field.getSimpleName()) };
		}

		@Override
		protected String[] loadDataInstructions(Element field, String[] parameters)
		{
			return new String[] { readBufferUtils("Tag", "this." + field.getSimpleName()) };
		}
		
		@Override
		public void addImportsToSet(Element field, String[] parameters, Set<String> imports)
		{
			imports.add("net.minecraftforge.fml.common.network.ByteBufUtils");
		}
	},
	STACK("net.minecraft.item.ItemStack") {

		@Override
		protected String[] saveDataInstructions(Element field, String[] parameters)
		{
			return new String[] { writeBufferUtils("ItemStack", "this." + field.getSimpleName()) };
		}

		@Override
		protected String[] loadDataInstructions(Element field, String[] parameters)
		{
			return new String[] { readBufferUtils("ItemStack", "this." + field.getSimpleName()) };
		}
		
		@Override
		public void addImportsToSet(Element field, String[] parameters, Set<String> imports)
		{
			imports.add("net.minecraftforge.fml.common.network.ByteBufUtils");
		}
	},
	//TODO INBTSerializable, IByteSerialisable
	//TODO collections, arrays
	//TODO Entity + EntityPlayer
	CUSTOM() {

		@Override
		protected String[] saveDataInstructions(Element field, String[] parameters)
		{
			// TODO custom data handler
			return null;
		}

		@Override
		protected String[] loadDataInstructions(Element field, String[] parameters)
		{
			return null;
		}
		
	};
	
	private final String type;
	private final boolean useBackwardCast;
	
	private DataType(String typeName, boolean backwardCast)
	{
		this.type = typeName;
		this.useBackwardCast = backwardCast;
	}
	
	private DataType(Class<?> type, boolean backwardCast)
	{
		this(type.getTypeName(), backwardCast);
	}
	
	private DataType(String typeName)
	{
		this(typeName, true);
	}
	
	private DataType(Class<?> type)
	{
		this(type, true);
	}
	
	private DataType()
	{
		this.type = null;
		this.useBackwardCast = false;
	}
	
	protected abstract String[] saveDataInstructions(Element field, String[] parameters);
	protected abstract String[] loadDataInstructions(Element field, String[] parameters);
	
	protected void addImportsToSet(Element field, String[] parameters, Set<String> imports)
	{ }
	
	private static String writeBuffer(String type, String value)
	{
		return "buf.write" + type + "(" + value + ");";
	}
	
	private static String readBuffer(String type, String value)
	{
		return value + " = buf.read" + type + "();";
	}
	
	private static String writeBufferUtils(String type, String value)
	{
		return "ByteBufUtils.write" + type + "(buf, " + value + ");";
	}

	private static String readBufferUtils(String type, String value)
	{
		return value + " = ByteBufUtils.read" + type + "(buf);";
	}
	
	public static class DataHandler
	{
		private final Element field;
		private final DataType handler;
		private final String[] parameters;
		
		public DataHandler(Element field, DataType handler, String... parameters)
		{
			this.field = field;
			this.handler = handler;
			this.parameters = parameters;
		}
		
		public DataHandler(Element field, CustomData data)
		{
			this(field, data.type(), data.value());
		}
		
		public String[] saveDataInstructions()
		{
			return this.handler.saveDataInstructions(this.field, this.parameters);
		}
		
		public String[] loadDataInstructions()
		{
			return this.handler.loadDataInstructions(this.field, this.parameters);
		}
		
		public void addImportsToSet(Set<String> imports)
		{
			this.handler.addImportsToSet(this.field, this.parameters, imports);
		}
	}
	
	public static class Finder
	{
		private final List<Map.Entry<TypeMirror, DataType>> typeMap;
		private final Types typeUtils;
		
		public Finder(ProcessingEnvironment env)
		{
			Elements elemUtils = env.getElementUtils();
			this.typeUtils = env.getTypeUtils();
			
			this.typeMap = Stream.of(DataType.values()).filter(data -> data.type != null).map(data -> new SimpleEntry<>(getKind(data.type) != null ? this.typeUtils.getPrimitiveType(getKind(data.type)) : this.typeUtils.erasure(elemUtils.getTypeElement(data.type).asType()), data)).collect(Collectors.toList());
		}
		
		public DataHandler getDataType(Element field)
		{
			CustomData customData = field.getAnnotation(CustomData.class);
			
			if (customData != null) return new DataHandler(field, customData);

			TypeMirror type = field.asType();
			for (Entry<TypeMirror, DataType> entry : this.typeMap)
			{
				if (this.typeUtils.isAssignable(type, entry.getKey()) && (!entry.getValue().useBackwardCast || this.typeUtils.isAssignable(entry.getKey(), type))) return new DataHandler(field, entry.getValue());
			}
			
			throw new InvalidParameterException("Unknown type " + type + " on field " + field);
		}
	}
	
	private static Map<String, TypeKind> TYPE_KINDS;
	
	static
	{
		TYPE_KINDS = Stream.of(TypeKind.values()).filter(TypeKind::isPrimitive).collect(Collectors.toMap(k -> k.name().toLowerCase(), Function.identity()));
	}
	
	public static TypeKind getKind(String type)
	{
		return TYPE_KINDS.get(type);
	}
	
}
