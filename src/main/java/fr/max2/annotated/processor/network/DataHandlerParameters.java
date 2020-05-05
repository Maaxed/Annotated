package fr.max2.annotated.processor.network;

import java.security.InvalidParameterException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import fr.max2.annotated.api.processor.network.CustomData;
import fr.max2.annotated.api.processor.network.DataType;
import fr.max2.annotated.processor.network.datahandler.ArrayDataHandler;
import fr.max2.annotated.processor.network.datahandler.CollectionDataHandler;
import fr.max2.annotated.processor.network.datahandler.IDataHandler;
import fr.max2.annotated.processor.network.datahandler.MapDataHandler;
import fr.max2.annotated.processor.network.datahandler.NBTDataHandler;
import fr.max2.annotated.processor.network.datahandler.PrimitiveDataHandler;
import fr.max2.annotated.processor.network.datahandler.SerializableDataHandler;
import fr.max2.annotated.processor.network.datahandler.SimpleClassHandler;
import fr.max2.annotated.processor.network.datahandler.SpecialDataHandler;
import fr.max2.annotated.processor.network.model.IFunctionBuilder;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.NamingUtils;

public class DataHandlerParameters
{
	public final String simpleName;
	public final String saveAccessExpr;
	@Nullable
	public final String loadAccessExpr;
	public final BiConsumer<IFunctionBuilder, String> setExpr;
	public final TypeMirror type;
	public final AnnotatedConstruct annotations;
	public final IDataHandler typeHandler;
	public final Finder finder;
	public final String[] parameters;
	
	public DataHandlerParameters(String simpleName, String saveGetExpr, String loadGetExpr, BiConsumer<IFunctionBuilder, String> setExpr, TypeMirror type, AnnotatedConstruct annotations, IDataHandler typeHandler, Finder finder, String... parameters)
	{
		this.simpleName = simpleName;
		this.saveAccessExpr = saveGetExpr;
		this.loadAccessExpr = loadGetExpr;
		this.setExpr = setExpr;
		this.type = type;
		this.annotations = annotations;
		this.typeHandler = typeHandler;
		this.finder = finder;
		this.parameters = parameters;
	}
	
	public DataHandlerParameters(String simpleName, String saveGetExpr, String loadGetExpr, BiConsumer<IFunctionBuilder, String> setExpr, TypeMirror type, AnnotatedConstruct annotations, CustomData data, Finder finder)
	{
		this(simpleName, saveGetExpr, loadGetExpr, setExpr, type, annotations, dataTypeToHandler(data.type()), finder, data.value());
	}
	
	public void addInstructions(IPacketBuilder builder)
	{
		this.typeHandler.addInstructions(this, builder);
	}

	public void setLoadedValue(IFunctionBuilder loadFunction, String value)
	{
		if (this.loadAccessExpr == null)
		{
			loadFunction.add(NamingUtils.computeFullName(this.type) + " " + this.simpleName + " = " + value + ";");
		}
		else
		{
			this.setExpr.accept(loadFunction, value);
		}
	}
	
	public String getLoadAccessExpr()
	{
		return this.loadAccessExpr == null ? this.simpleName : this.loadAccessExpr;
	}
	
	
	
	public static class Finder
	{
		private final List<Map.Entry<Predicate<TypeMirror>, IDataHandler>> typeMap;
		public final Elements elemUtils;
		public final Types typeUtils;
		
		public Finder(ProcessingEnvironment env)
		{
			this.elemUtils = env.getElementUtils();
			this.typeUtils = env.getTypeUtils();
			
			this.typeMap = TYPE_TO_HANDLER.values().stream().map(data -> new SimpleEntry<>(data.getTypeValidator(this.elemUtils, this.typeUtils), data)).collect(Collectors.toList());
		}
		
		public DataHandlerParameters getDataType(Element field)
		{
			String setExpr = "msg." + field.getSimpleName() + " = ";
			return this.getDataType(field.getSimpleName().toString(), "msg." + field.getSimpleName(), "msg." + field.getSimpleName(), (loadInst, value) -> loadInst.add(setExpr + value + ";"), field.asType(), field);
		}
		
		public DataHandlerParameters getDataType(String simpleName, String saveGetExpr, @Nullable String loadGetExpr, BiConsumer<IFunctionBuilder, String> setExpr, TypeMirror type, AnnotatedConstruct annotations)
		{
			DataHandlerParameters params = this.getDataTypeOrNull(simpleName, saveGetExpr, loadGetExpr, setExpr, type, annotations);
			if (params == null) throw new InvalidParameterException("Unknown default DataHandler for type '" + type + "'");
			return params;
		}
		
		public DataHandlerParameters getDataTypeOrNull(String simpleName, String saveGetExpr, @Nullable String loadGetExpr, BiConsumer<IFunctionBuilder, String> setExpr, TypeMirror type, AnnotatedConstruct annotations)
		{
			CustomData customData = annotations.getAnnotation(CustomData.class);
			if (customData == null) customData = type.getAnnotation(CustomData.class);
			if (customData == null && type instanceof DeclaredType) customData = ((DeclaredType)type).asElement().getAnnotation(CustomData.class);
			
			if (customData != null)
			{
				return new DataHandlerParameters(simpleName, saveGetExpr, loadGetExpr, setExpr, type, annotations, customData, this);
			}
			
			IDataHandler defaultHandler = this.getDefaultDataType(type);
			
			if (defaultHandler == SpecialDataHandler.CUSTOM) return null;
			
			return new DataHandlerParameters(simpleName, saveGetExpr, loadGetExpr, setExpr, type, annotations, defaultHandler, this);
		}
		
		public IDataHandler getDefaultDataType(TypeMirror type)
		{
			for (Entry<Predicate<TypeMirror>, IDataHandler> entry : this.typeMap)
			{
				if (entry.getKey().test(type))
				{
					return entry.getValue();
				}
			}
			
			return SpecialDataHandler.CUSTOM;
		}
	}
	
	
	private static final Map<DataType, IDataHandler> TYPE_TO_HANDLER = new EnumMap<>(DataType.class);
	
	static
	{
		TYPE_TO_HANDLER.put(DataType.BYTE, PrimitiveDataHandler.BYTE);
		TYPE_TO_HANDLER.put(DataType.SHORT, PrimitiveDataHandler.SHORT);
		TYPE_TO_HANDLER.put(DataType.INT, PrimitiveDataHandler.INT);
		TYPE_TO_HANDLER.put(DataType.LONG, PrimitiveDataHandler.LONG);
		TYPE_TO_HANDLER.put(DataType.FLOAT, PrimitiveDataHandler.FLOAT);
		TYPE_TO_HANDLER.put(DataType.DOUBLE, PrimitiveDataHandler.DOUBLE);
		TYPE_TO_HANDLER.put(DataType.BOOLEAN, PrimitiveDataHandler.BOOLEAN);
		TYPE_TO_HANDLER.put(DataType.CHAR, PrimitiveDataHandler.CHAR);
		
		TYPE_TO_HANDLER.put(DataType.ARRAY, ArrayDataHandler.INSTANCE);
		
		TYPE_TO_HANDLER.put(DataType.STRING, SimpleClassHandler.STRING);
		TYPE_TO_HANDLER.put(DataType.ENUM, SimpleClassHandler.ENUM);
		TYPE_TO_HANDLER.put(DataType.COLLECTION, CollectionDataHandler.INSTANCE);
		TYPE_TO_HANDLER.put(DataType.MAP, MapDataHandler.INSTANCE);
		TYPE_TO_HANDLER.put(DataType.UUID, SimpleClassHandler.UUID);
		
		TYPE_TO_HANDLER.put(DataType.STACK, SimpleClassHandler.STACK);
		
		TYPE_TO_HANDLER.put(DataType.NBT_SERIALIZABLE, SerializableDataHandler.NBT_SERIALISABLE);
		
		TYPE_TO_HANDLER.put(DataType.NBT_END, NBTDataHandler.END);
		TYPE_TO_HANDLER.put(DataType.NBT_BYTE, NBTDataHandler.BYTE);
		TYPE_TO_HANDLER.put(DataType.NBT_SHORT, NBTDataHandler.SHORT);
		TYPE_TO_HANDLER.put(DataType.NBT_INT, NBTDataHandler.INT);
		TYPE_TO_HANDLER.put(DataType.NBT_LONG, NBTDataHandler.LONG);
		TYPE_TO_HANDLER.put(DataType.NBT_FLOAT, NBTDataHandler.FLOAT);
		TYPE_TO_HANDLER.put(DataType.NBT_DOUBLE, NBTDataHandler.DOUBLE);
		TYPE_TO_HANDLER.put(DataType.NBT_STRING, NBTDataHandler.STRING);
		TYPE_TO_HANDLER.put(DataType.NBT_BYTE_ARRAY, NBTDataHandler.BYTE_ARRAY);
		TYPE_TO_HANDLER.put(DataType.NBT_INT_ARRAY, NBTDataHandler.INT_ARRAY);
		TYPE_TO_HANDLER.put(DataType.NBT_LIST, NBTDataHandler.LIST);
		TYPE_TO_HANDLER.put(DataType.NBT_COMPOUND, NBTDataHandler.COMPOUND);
		TYPE_TO_HANDLER.put(DataType.NBT_ANY_NUMBER, NBTDataHandler.PRIMITIVE);
		TYPE_TO_HANDLER.put(DataType.NBT_ANY, NBTDataHandler.BASE);
		
		
		TYPE_TO_HANDLER.put(DataType.WILDCARD, SpecialDataHandler.WILDCRD);
		TYPE_TO_HANDLER.put(DataType.TYPE_VARIABLE, SpecialDataHandler.VARIABLE_TYPE);
		TYPE_TO_HANDLER.put(DataType.INTERSECTION, SpecialDataHandler.INTERSECTION);
		
		TYPE_TO_HANDLER.put(DataType.DEFAULT, SpecialDataHandler.DEFAULT);
		TYPE_TO_HANDLER.put(DataType.CUSTOM, SpecialDataHandler.CUSTOM);
	}
	
	private static IDataHandler dataTypeToHandler(DataType type)
	{
		return TYPE_TO_HANDLER.get(type);
	}
	
}
