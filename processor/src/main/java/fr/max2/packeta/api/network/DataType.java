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
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import fr.max2.packeta.utils.EmptyAnnotationConstruct;

public enum DataType
{
	// Integers
	BYTE(Byte.TYPE) {
		
		@Override
		protected String[] saveDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			return new String[] { writeBuffer("Byte", accesExpr) };
		}
		
		@Override
		protected String[] loadDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			return new String[] { readBuffer("Byte", accesExpr) };
		}
	},
	SHORT(Short.TYPE) {
		
		@Override
		protected String[] saveDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			return new String[] { writeBuffer("Short", accesExpr) };
		}
		
		@Override
		protected String[] loadDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			return new String[] { readBuffer("Short", accesExpr) };
		}
	},
	INT(Integer.TYPE) {
		
		@Override
		protected String[] saveDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			return new String[] { writeBuffer("Int", accesExpr) };
		}
		
		@Override
		protected String[] loadDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			return new String[] { readBuffer("Int", accesExpr) };
		}
	},
	LONG(Long.TYPE) {
		
		@Override
		protected String[] saveDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			return new String[] { writeBuffer("Long", accesExpr) };
		}
		
		@Override
		protected String[] loadDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			return new String[] { readBuffer("Long", accesExpr) };
		}
	},
	
	//Floats
	FLOAT(Float.TYPE) {
		
		@Override
		protected String[] saveDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			return new String[] { writeBuffer("Float", accesExpr) };
		}
		
		@Override
		protected String[] loadDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			return new String[] { readBuffer("Float", accesExpr) };
		}
	},
	DOUBLE(Double.TYPE) {
		
		@Override
		protected String[] saveDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			return new String[] { writeBuffer("Double", accesExpr) };
		}
		
		@Override
		protected String[] loadDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			return new String[] { readBuffer("Double", accesExpr) };
		}
	},
	
	// Other primitives
	BOOLEAN(Boolean.TYPE) {
		
		@Override
		protected String[] saveDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			return new String[] { writeBuffer("Boolean", accesExpr) };
		}
		
		@Override
		protected String[] loadDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			return new String[] { readBuffer("Boolean", accesExpr) };
		}
	},
	CHAR(Character.TYPE) {
		
		@Override
		protected String[] saveDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			return new String[] { writeBuffer("Char", accesExpr) };
		}
		
		@Override
		protected String[] loadDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			return new String[] { readBuffer("Char", accesExpr) };
		}
	},
	
	// Classes
	STRING(String.class) {

		@Override
		protected String[] saveDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			return new String[] { writeBufferUtils("UTF8String", accesExpr) };
		}

		@Override
		protected String[] loadDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			return new String[] { readBufferUtils("UTF8String", accesExpr) };
		}
		
		@Override
		protected void addImportsToSet(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters, Set<String> imports)
		{
			imports.add("net.minecraftforge.fml.common.network.ByteBufUtils");
		}
	},
	ENUM(Enum.class, false) {
		
		@Override
		protected String[] saveDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			return new String[] { "buf.writeInt(" + accesExpr + ".ordinal());" };
		}
		
		@Override
		protected String[] loadDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			return new String[] { accesExpr + " = " + ((TypeElement)((DeclaredType)type).asElement()).getSimpleName() + ".values()[buf.readInt()];" };
		}
		
		@Override
		protected void addImportsToSet(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters, Set<String> imports)
		{
			imports.add(type.toString());
		}
	},
	UUID(UUID.class, false)
	{
		
		@Override
		protected String[] saveDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			return new String[] {
				"buf.writeLong(" + accesExpr + ".getMostSignificantBits())",
				"buf.writeLong(" + accesExpr + ".getLeastSignificantBits())"};
		}
		
		@Override
		protected String[] loadDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			return new String[] { accesExpr + " = new UUID(buf.readLong(), buf.readLong());" };
		}
		
		@Override
		protected void addImportsToSet(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters, Set<String> imports)
		{
			imports.add("java.util.UUID");
		}
	},
	NBT_COMPOUND("net.minecraft.nbt.NBTTagCompound") {

		@Override
		protected String[] saveDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			return new String[] { writeBufferUtils("Tag", accesExpr) };
		}

		@Override
		protected String[] loadDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			return new String[] { readBufferUtils("Tag", accesExpr) };
		}
		
		@Override
		public void addImportsToSet(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters, Set<String> imports)
		{
			imports.add("net.minecraftforge.fml.common.network.ByteBufUtils");
		}
	},
	STACK("net.minecraft.item.ItemStack") {

		@Override
		protected String[] saveDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			return new String[] { writeBufferUtils("ItemStack", accesExpr) };
		}

		@Override
		protected String[] loadDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			return new String[] { readBufferUtils("ItemStack", accesExpr) };
		}
		
		@Override
		public void addImportsToSet(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters, Set<String> imports)
		{
			imports.add("net.minecraftforge.fml.common.network.ByteBufUtils");
		}
	},
	ARRAY("array") {

		@Override
		protected String[] saveDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			ArrayType arrayType = (ArrayType)type;
			TypeMirror contentType = arrayType.getComponentType();
			String typeName = contentType instanceof DeclaredType ? ((DeclaredType)contentType).asElement().getSimpleName().toString() : contentType.toString();
			
			String s1 = writeBuffer("Int", accesExpr + ".length");
			String s2 = "for (" + typeName + " element : " + accesExpr + ")";
			String s3 = "\t";
			String[] s4 = finder.getDataType("element", contentType, EmptyAnnotationConstruct.INSTANCE).saveDataInstructions();
			//String s3 = 
			
			return null;
		}

		@Override
		protected String[] loadDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		protected void addImportsToSet(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters, Set<String> imports)
		{
			ArrayType arrayType = (ArrayType)type;
			TypeMirror contentType = arrayType.getComponentType();
			imports.add(simpleName(contentType));
			finder.getDataType("element", contentType, EmptyAnnotationConstruct.INSTANCE).addImportsToSet(imports);
		}
	},
	//TODO INBTSerializable, IByteSerialisable
	//TODO collections
	//TODO Entity + EntityPlayer
	CUSTOM() {

		@Override
		protected String[] saveDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
		{
			// TODO custom data handler
			return null;
		}

		@Override
		protected String[] loadDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters)
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
	
	//TODO merge methods and use Consumers
	protected abstract String[] saveDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters);
	protected abstract String[] loadDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters);
	
	protected void addImportsToSet(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters, Set<String> imports)
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
		private final String accesExpr;
		private final TypeMirror type;
		private final AnnotatedConstruct annotations;
		private final DataType handler;
		private final Finder finder;
		private final String[] parameters;
		
		public DataHandler(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, DataType handler, Finder finder, String... parameters)
		{
			this.accesExpr = accesExpr;
			this.type = type;
			this.annotations = annotations;
			this.handler = handler;
			this.finder = finder;
			this.parameters = parameters;
		}
		
		public DataHandler(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, CustomData data, Finder finder)
		{
			this(accesExpr, type, annotations, data.type(), finder, data.value());
		}
		
		public String[] saveDataInstructions()
		{
			return this.handler.saveDataInstructions(this.accesExpr, this.type, this.annotations, this.finder, this.parameters);
		}
		
		public String[] loadDataInstructions()
		{
			return this.handler.loadDataInstructions(this.accesExpr, this.type, this.annotations, this.finder, this.parameters);
		}
		
		public void addImportsToSet(Set<String> imports)
		{
			this.handler.addImportsToSet(this.accesExpr, this.type, this.annotations, this.finder, this.parameters, imports);
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
			return this.getDataType("this." + field.getSimpleName(), field.asType(), field);
		}
		
		public DataHandler getDataType(String accesExpr, TypeMirror type, AnnotatedConstruct annotations)
		{
			CustomData customData = annotations.getAnnotation(CustomData.class);
			
			if (customData != null) return new DataHandler(accesExpr, type, annotations, customData, this);
			
			for (Entry<TypeMirror, DataType> entry : this.typeMap)
			{
				if (this.typeUtils.isAssignable(type, entry.getKey()) && (!entry.getValue().useBackwardCast || this.typeUtils.isAssignable(entry.getKey(), type))) return new DataHandler(accesExpr, type, annotations, entry.getValue(), this);
			}
			
			throw new InvalidParameterException("Unknown type " + type + " on " + accesExpr);
		}
	}
	
	private static Map<String, TypeKind> TYPE_KINDS;
	
	static
	{
		TYPE_KINDS = Stream.of(TypeKind.values()).filter(k -> k.isPrimitive() || k == TypeKind.ARRAY).collect(Collectors.toMap(k -> k.name().toLowerCase(), Function.identity()));
	}
	
	public static TypeKind getKind(String type)
	{
		return TYPE_KINDS.get(type);
	}
	
	public static String simpleName(TypeMirror type)
	{
		if (type instanceof DeclaredType)
		{
			return ((DeclaredType)type).asElement().getSimpleName().toString();
		}
		return type.toString();
	}
	
}
