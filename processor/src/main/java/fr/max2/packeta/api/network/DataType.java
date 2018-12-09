package fr.max2.packeta.api.network;

import java.security.InvalidParameterException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Consumer;
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
import fr.max2.packeta.utils.NamingUtils;
import fr.max2.packeta.utils.TypeHelper;
import fr.max2.packeta.utils.ValueInitStatus;

public enum DataType
{
	// Integers
	BYTE(Byte.TYPE)
	{
		@Override
		protected void addInstructions(DataHandler handler, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			addBufferInstructions("Byte", handler.getExpr, handler.firstSetInit(), saveInstructions, loadInstructions);
		}
	},
	SHORT(Short.TYPE)
	{
		@Override
		protected void addInstructions(DataHandler handler, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			addBufferInstructions("Short", handler.getExpr, handler.firstSetInit(), saveInstructions, loadInstructions);
		}
	},
	INT(Integer.TYPE)
	{
		@Override
		protected void addInstructions(DataHandler handler, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			addBufferInstructions("Int", handler.getExpr, handler.firstSetInit(), saveInstructions, loadInstructions);
		}
	},
	LONG(Long.TYPE)
	{
		@Override
		protected void addInstructions(DataHandler handler, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			addBufferInstructions("Long", handler.getExpr, handler.firstSetInit(), saveInstructions, loadInstructions);
		}
	},
	
	//Floats
	FLOAT(Float.TYPE)
	{
		@Override
		protected void addInstructions(DataHandler handler, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			addBufferInstructions("Float", handler.getExpr, handler.firstSetInit(), saveInstructions, loadInstructions);
		}
	},
	DOUBLE(Double.TYPE)
	{
		@Override
		protected void addInstructions(DataHandler handler, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			addBufferInstructions("Double", handler.getExpr, handler.firstSetInit(), saveInstructions, loadInstructions);
		}
	},
	
	// Other primitives
	BOOLEAN(Boolean.TYPE)
	{
		@Override
		protected void addInstructions(DataHandler handler, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			addBufferInstructions("Boolean", handler.getExpr, handler.firstSetInit(), saveInstructions, loadInstructions);
		}
	},
	CHAR(Character.TYPE)
	{
		@Override
		protected void addInstructions(DataHandler handler, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			addBufferInstructions("Char", handler.getExpr, handler.firstSetInit(), saveInstructions, loadInstructions);
		}
	},
	ARRAY()
	{
		@Override
		protected void addInstructions(DataHandler handler, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			ArrayType arrayType = (ArrayType)handler.type;
			TypeMirror contentType = arrayType.getComponentType();
			String typeName = NamingUtils.simpleTypeName(contentType);
			String arrayTypeName = typeName + "[]";
			
			boolean constSize = handler.annotations.getAnnotation(ConstSize.class) != null || handler.type.getAnnotation(ConstSize.class) != null;
			
			int i;
			for (i = arrayTypeName.length() - 2; i >= 2 && arrayTypeName.substring(i - 2, i).equals("[]"); i-=2);
			
			String elementVarName = handler.simpleName + "Element";
			if (!constSize) saveInstructions.accept(writeBuffer("Int", handler.getExpr + ".length"));
			saveInstructions.accept("for (" + typeName + " " + elementVarName + " : " + handler.getExpr + ")");
			saveInstructions.accept("{");
			
			String lenghtVarName;
			String indexVarName = handler.simpleName + "Index";
			
			if (constSize)
			{
				lenghtVarName = handler.simpleName + ".length";
			}
			else
			{
				lenghtVarName = handler.simpleName + "Length";
				loadInstructions.accept(readBuffer("Int", "int " + lenghtVarName));
				loadInstructions.accept(handler.firstSetInit() + " = new " + arrayTypeName.substring(0, i + 1) + lenghtVarName + arrayTypeName.substring(i + 1) + ";" );
			}
			
			loadInstructions.accept("for (int " + indexVarName + " = 0; " + indexVarName + " < " + lenghtVarName + "; " + indexVarName + "++)");
			loadInstructions.accept("{");
			
			DataHandler contentHandler = handler.finder.getDataType(elementVarName, elementVarName, handler.setExpr + "[" + indexVarName + "]", contentType, EmptyAnnotationConstruct.INSTANCE, ValueInitStatus.DECLARED);
			contentHandler.addInstructions(inst -> saveInstructions.accept("\t" + inst), inst -> loadInstructions.accept("\t" + inst), imports);
			
			saveInstructions.accept("}");
			
			loadInstructions.accept("}");
		}
	},
	
	// Classes
	STRING(String.class)
	{
		@Override
		protected void addInstructions(DataHandler handler, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			addBufferUtilsInstructions("UTF8String", handler.getExpr, handler.firstSetInit(), saveInstructions, loadInstructions, imports);
		}
	},
	ENUM(Enum.class)
	{
		@Override
		protected void addInstructions(DataHandler handler, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			saveInstructions.accept(writeBuffer("Int", handler.getExpr + ".ordinal()"));
			
			loadInstructions.accept(handler.firstSetInit() + " = " + ((TypeElement)((DeclaredType)handler.type).asElement()).getSimpleName() + ".values()[buf.readInt()];");
		}
	},
	UUID(UUID.class)
	{
		@Override
		protected void addInstructions(DataHandler handler, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			saveInstructions.accept(writeBuffer("Long", handler.getExpr + ".getMostSignificantBits()"));
			saveInstructions.accept(writeBuffer("Long", handler.getExpr + ".getLeastSignificantBits()"));
			
			loadInstructions.accept(handler.firstSetInit() + " = new UUID(buf.readLong(), buf.readLong());");
		}
	},
	NBT_COMPOUND("net.minecraft.nbt.NBTTagCompound")
	{
		@Override
		protected void addInstructions(DataHandler handler, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			addBufferUtilsInstructions("Tag", handler.getExpr, handler.firstSetInit(), saveInstructions, loadInstructions, imports);
		}
	},
	STACK("net.minecraft.item.ItemStack")
	{
		@Override
		protected void addInstructions(DataHandler handler, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			addBufferUtilsInstructions("ItemStack", handler.getExpr, handler.firstSetInit(), saveInstructions, loadInstructions, imports);
		}
	},
	COLLECTION(Collection.class)
	{
		@Override
		protected void addInstructions(DataHandler handler, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			DeclaredType collectionType = TypeHelper.refineTo(handler.type, handler.finder.elemUtils.getTypeElement(Collection.class.getCanonicalName()).asType(), handler.finder.typeUtils);
			TypeMirror contentType = collectionType.getTypeArguments().get(0);
			String typeName = NamingUtils.simpleTypeName(contentType);
			
			boolean constSize = handler.annotations.getAnnotation(ConstSize.class) != null || handler.type.getAnnotation(ConstSize.class) != null;
			
			String elementVarName = handler.simpleName + "Element";
			if (!constSize) saveInstructions.accept(writeBuffer("Int", handler.getExpr + ".size()"));
			saveInstructions.accept("for (" + typeName + " " + elementVarName + " : " + handler.getExpr + ")");
			saveInstructions.accept("{");
			
			String lenghtVarName = handler.simpleName + "Length";
			String indexVarName = handler.simpleName + "Index";
			if (constSize)
			{
				loadInstructions.accept("int " + lenghtVarName + " = " + handler.setExpr + ".size();");
			}
			else
			{
				loadInstructions.accept(readBuffer("Int", "int " + lenghtVarName));
			}
			
			if (handler.initStatus.isInitialised())
			{
				loadInstructions.accept(handler.setExpr + ".clear();" );
			}
			else
			{
				loadInstructions.accept(handler.firstSetInit() + " = new " + NamingUtils.simpleTypeName(handler.type, true) + "();" ); //TODO use parameters to use the right class
			}
			
			loadInstructions.accept("for (int " + indexVarName + " = 0; " + indexVarName + " < " + lenghtVarName + "; " + indexVarName + "++)");
			loadInstructions.accept("{");
			
			DataHandler contentHandler = handler.finder.getDataType(elementVarName, elementVarName, elementVarName, contentType, EmptyAnnotationConstruct.INSTANCE, ValueInitStatus.UNDEFINED);
			contentHandler.addInstructions(inst -> saveInstructions.accept("\t" + inst), inst -> loadInstructions.accept("\t" + inst), imports);
			
			saveInstructions.accept("}");
			
			loadInstructions.accept("\t" + handler.setExpr + ".add(" + elementVarName + ");");
			loadInstructions.accept("}");
		}
	},
	//TODO INBTSerializable, IByteSerialisable
	//TODO Entity + EntityPlayer
	CUSTOM()
	{
		@Override
		protected void addInstructions(DataHandler handler, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			// TODO Auto-generated method stub
			
		}
	},
	DEFAULT()
	{
		@Override
		protected void addInstructions(DataHandler handler, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			handler.finder.getDefaultDataType(handler.type).addInstructions(handler, saveInstructions, loadInstructions, imports);
		}
	};
	
	private final String typeName;
	private final boolean useBackwardCast;
	
	private DataType(String typeName, boolean backwardCast)
	{
		this.typeName = typeName;
		this.useBackwardCast = backwardCast;
	}
	
	private DataType(Class<?> type, boolean backwardCast)
	{
		this(type.getTypeName(), backwardCast);
	}
	
	private DataType(String typeName)
	{
		this(typeName, false);
	}
	
	private DataType(Class<?> type)
	{
		this(type, type.isPrimitive());
	}
	
	private DataType()
	{
		this.typeName = null;
		this.useBackwardCast = false;
	}
	
	protected abstract void addInstructions(DataHandler handler, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports);
	
	
	/*protected abstract String[] saveDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters);
	protected abstract String[] loadDataInstructions(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters);
	
	protected void addImportsToSet(String accesExpr, TypeMirror type, AnnotatedConstruct annotations, Finder finder, String[] parameters, Set<String> imports)
	{ }*/
	
	private static String writeBuffer(String type, String value)
	{
		return "buf.write" + type + "(" + value + ");";
	}
	
	private static String readBuffer(String type, String value)
	{
		return value + " = buf.read" + type + "();";
	}
	
	private static void addBufferInstructions(String type, String saveValue, String loadValue, Consumer<String> saveInstructions, Consumer<String> loadInstructions)
	{
		saveInstructions.accept(writeBuffer(type, saveValue));
		loadInstructions.accept(readBuffer(type, loadValue));
	}
	
	private static String writeBufferUtils(String type, String value)
	{
		return "ByteBufUtils.write" + type + "(buf, " + value + ");";
	}

	private static String readBufferUtils(String type, String value)
	{
		return value + " = ByteBufUtils.read" + type + "(buf);";
	}
	
	private static void addBufferUtilsInstructions(String type, String saveValue, String loadValue, Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
	{
		saveInstructions.accept(writeBufferUtils(type, saveValue));
		loadInstructions.accept(readBufferUtils(type, loadValue));
		imports.accept("net.minecraftforge.fml.common.network.ByteBufUtils");
	}
	
	public static class DataHandler
	{
		public final String simpleName, getExpr, setExpr;
		public final TypeMirror type;
		public final AnnotatedConstruct annotations;
		public final DataType typeHandler;
		public final ValueInitStatus initStatus;
		public final Finder finder;
		public final String[] parameters;
		
		public DataHandler(String simpleName, String getExpr, String setExpr, TypeMirror type, AnnotatedConstruct annotations, DataType typeHandler, ValueInitStatus initStatus, Finder finder, String... parameters)
		{
			this.simpleName = simpleName;
			this.getExpr = getExpr;
			this.setExpr = setExpr;
			this.type = type;
			this.annotations = annotations;
			this.typeHandler = typeHandler;
			this.initStatus = initStatus;
			this.finder = finder;
			this.parameters = parameters;
		}
		
		public DataHandler(String simpleName, String getExpr, String setExpr, TypeMirror type, AnnotatedConstruct annotations, CustomData data, ValueInitStatus hasDefaultValue, Finder finder)
		{
			this(simpleName, getExpr, setExpr, type, annotations, data.type(), hasDefaultValue, finder, data.value());
		}
		
		public void addInstructions(Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
		{
			this.typeHandler.addInstructions(this, saveInstructions, loadInstructions, imports);
		}
		
		private String firstSetInit()
		{
			return this.initStatus.isDeclared() ? this.setExpr : NamingUtils.simpleTypeName(this.type) + " " + this.setExpr;
		}
	}
	
	public static class Finder
	{
		private final List<Map.Entry<TypeMirror, DataType>> typeMap;
		private final Elements elemUtils;
		private final Types typeUtils;
		
		public Finder(ProcessingEnvironment env)
		{
			this.elemUtils = env.getElementUtils();
			this.typeUtils = env.getTypeUtils();
			
			this.typeMap = Stream.of(DataType.values()).filter(data -> data.typeName != null).map(data -> new SimpleEntry<>(getKind(data.typeName) != null ? this.typeUtils.getPrimitiveType(getKind(data.typeName)) : this.typeUtils.erasure(elemUtils.getTypeElement(data.typeName).asType()), data)).collect(Collectors.toList());
		}
		
		public DataHandler getDataType(Element field)
		{
			return this.getDataType(field.getSimpleName().toString(), "this." + field.getSimpleName(), "this." + field.getSimpleName(), field.asType(), field, ValueInitStatus.INITIALISED);
		}
		
		public DataHandler getDataType(String simpleName, String getExpr, String setExpr, TypeMirror type, AnnotatedConstruct annotations, ValueInitStatus initStatus)
		{
			CustomData customData = annotations.getAnnotation(CustomData.class);
			if (customData == null) customData = type.getAnnotation(CustomData.class);
			if (customData == null && type instanceof DeclaredType) customData = ((DeclaredType)type).asElement().getAnnotation(CustomData.class);
			
			if (customData != null)
			{
				return new DataHandler(simpleName, getExpr, setExpr, type, annotations, customData, initStatus, this);
			}
			
			return new DataHandler(simpleName, getExpr, setExpr, type, annotations, this.getDefaultDataType(type), initStatus, this);
		}
		
		public DataType getDefaultDataType(TypeMirror type)
		{
			if (type.getKind() == TypeKind.ARRAY)
			{
				return ARRAY;
			}
			
			for (Entry<TypeMirror, DataType> entry : this.typeMap)
			{
				if (this.typeUtils.isAssignable(type, entry.getKey()) && (!entry.getValue().useBackwardCast || this.typeUtils.isAssignable(entry.getKey(), type)))
				{
					return entry.getValue();
				}
			}
			
			throw new InvalidParameterException("Unknown default DataType for type '" + type + "'");
		}
	}
	
	private static Map<String, TypeKind> TYPE_KINDS;
	
	static
	{
		TYPE_KINDS = Stream.of(TypeKind.values()).filter(k -> k.isPrimitive()).collect(Collectors.toMap(k -> k.name().toLowerCase(), Function.identity()));
	}
	
	public static TypeKind getKind(String type)
	{
		return TYPE_KINDS.get(type);
	}
	
}
