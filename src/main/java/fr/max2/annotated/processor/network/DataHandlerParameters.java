package fr.max2.annotated.processor.network;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.api.processor.network.DataProperties;
import fr.max2.annotated.api.processor.network.DataType;
import fr.max2.annotated.processor.network.datahandler.ArrayDataHandler;
import fr.max2.annotated.processor.network.datahandler.CollectionDataHandler;
import fr.max2.annotated.processor.network.datahandler.IDataHandler;
import fr.max2.annotated.processor.network.datahandler.MapDataHandler;
import fr.max2.annotated.processor.network.datahandler.NBTDataHandler;
import fr.max2.annotated.processor.network.datahandler.PrimitiveDataHandler;
import fr.max2.annotated.processor.network.datahandler.RegistryEntryDataHandler;
import fr.max2.annotated.processor.network.datahandler.SerializableDataHandler;
import fr.max2.annotated.processor.network.datahandler.SimpleClassHandler;
import fr.max2.annotated.processor.network.datahandler.SpecialDataHandler;
import fr.max2.annotated.processor.network.model.IFunctionBuilder;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PriorityManager;
import fr.max2.annotated.processor.utils.PropertyMap;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;
import fr.max2.annotated.processor.utils.exceptions.InvalidPropertyException;

public class DataHandlerParameters
{
	public final ProcessingTools tools;
	public final String uniqueName;
	public final String saveAccessExpr;
	public final BiConsumer<IFunctionBuilder, String> setExpr;
	public final TypeMirror type;
	public final IDataHandler typeHandler;
	public final PropertyMap properties;
	
	public DataHandlerParameters(ProcessingTools tools, String uniqueName, String saveGetExpr, BiConsumer<IFunctionBuilder, String> setExpr, TypeMirror type, IDataHandler typeHandler, PropertyMap properties)
	{
		this.tools = tools;
		this.uniqueName = uniqueName;
		this.saveAccessExpr = saveGetExpr;
		this.setExpr = setExpr;
		this.type = type;
		this.typeHandler = typeHandler;
		this.properties = properties;
	}
	
	public void addInstructions(int indent, IPacketBuilder builder)
	{
		builder.encoder().indent(indent);
		builder.decoder().indent(indent);
		addInstructions(builder);
		builder.encoder().indent(-indent);
		builder.decoder().indent(-indent);
	}

	public void addInstructions(IPacketBuilder builder)
	{
		this.typeHandler.addInstructions(this, builder);
	}
	
	
	public static class Finder
	{
		private final List<Map.Entry<Predicate<TypeMirror>, IDataHandler>> typeMap;
		private final ProcessingTools tools;
		
		public Finder(ProcessingTools tools)
		{
			this.tools = tools;
			
			this.typeMap = TYPE_TO_HANDLER.values().stream().map(data -> new SimpleEntry<>(data.getTypeValidator(this.tools.elements, this.tools.types), data)).collect(Collectors.toList());
		}
		
		public DataHandlerParameters getDataType(Element field)
		{
			DataProperties customData = field.getAnnotation(DataProperties.class);
			if (customData == null)
			{
				Element elem = this.tools.types.asElement(field.asType());
				if (elem != null)
					customData = elem.getAnnotation(DataProperties.class);
			}
			PropertyMap properties = customData == null ? PropertyMap.EMPTY_PROPERTIES : new PropertyMap(customData.value());
			String setExpr = "msg." + field.getSimpleName() + " = ";
			return this.getDataType(field.getSimpleName().toString(), "msg." + field.getSimpleName(), (loadInst, value) -> loadInst.add(setExpr + value + ";"), field.asType(), properties);
		}
		
		public DataHandlerParameters getDataType(String uniqueName, String saveGetExpr, BiConsumer<IFunctionBuilder, String> setExpr, TypeMirror type, PropertyMap properties)
		{
			DataHandlerParameters params = this.getDataTypeOrNull(uniqueName, saveGetExpr, setExpr, type, properties);
			
			if (params == null)
				throw new IncompatibleTypeException("No data handler can process the type '" + type.toString() + "'");
			
			return params;
		}
		
		public DataHandlerParameters getDataTypeOrNull(String uniqueName, String saveGetExpr, BiConsumer<IFunctionBuilder, String> setExpr, TypeMirror type, PropertyMap properties)
		{
			return properties.getValue("type")
				.map(str ->
				{
					return new DataHandlerParameters(this.tools, uniqueName, saveGetExpr, setExpr, type, dataTypeToHandler(str), properties);
				})
				.orElseGet(() ->
				{
					IDataHandler defaultHandler = this.getDefaultDataType(type);
					
					if (defaultHandler == SpecialDataHandler.CUSTOM)
						return null;
					
					return new DataHandlerParameters(this.tools, uniqueName, saveGetExpr, setExpr, type, defaultHandler, properties);
				});
		}
		
		public IDataHandler getDefaultDataType(TypeMirror type)
		{
			List<IDataHandler> validHandlers = this.typeMap.stream().filter(entry -> entry.getKey().test(type)).map(Entry::getValue).collect(Collectors.toList());
			
			List<IDataHandler> prioritizedHandlers = HANDLER_PRIORITIES.getHighests(validHandlers);
			
			switch (prioritizedHandlers.size())
			{
			case 0:
				return SpecialDataHandler.CUSTOM;
			case 1:
				return prioritizedHandlers.get(0);
			default:
				throw new IllegalArgumentException("The data handler of the '" + type.toString() + "' type couldn't be chosen: handler priorities are equal: " + prioritizedHandlers.stream().map(h -> h.getClass().getTypeName() + ":" + h.toString()).collect(Collectors.joining(", ")));
			}
			
		}
	}
	
	
	private static final Map<DataType, IDataHandler> TYPE_TO_HANDLER = new EnumMap<>(DataType.class);
	private static final PriorityManager<IDataHandler> HANDLER_PRIORITIES = new PriorityManager<>();
	
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
		TYPE_TO_HANDLER.put(DataType.TIME, SimpleClassHandler.DATE);
		
		TYPE_TO_HANDLER.put(DataType.BLOCK_POS, SimpleClassHandler.BLOCK_POS);
		TYPE_TO_HANDLER.put(DataType.RESOURCE_LOCATION, SimpleClassHandler.RESOURCE_LOCATION);
		TYPE_TO_HANDLER.put(DataType.ITEM_STACK, SimpleClassHandler.ITEM_STACK);
		TYPE_TO_HANDLER.put(DataType.FLUID_STACK, SimpleClassHandler.FLUID_STACK);
		TYPE_TO_HANDLER.put(DataType.TEXT_COMPONENT, SimpleClassHandler.TEXT_COMPONENT);
		TYPE_TO_HANDLER.put(DataType.BLOCK_RAY_TRACE, SimpleClassHandler.BLOCK_RAY_TRACE);
		TYPE_TO_HANDLER.put(DataType.REGISTRY_ENTRY, RegistryEntryDataHandler.INSTANCE);
		TYPE_TO_HANDLER.put(DataType.NBT_SERIALIZABLE, SerializableDataHandler.NBT_SERIALISABLE);
		
		TYPE_TO_HANDLER.put(DataType.NBT_PRIMITIVE, NBTDataHandler.PRIMITIVE);
		TYPE_TO_HANDLER.put(DataType.NBT_CONCRETE, NBTDataHandler.CONCRETE);
		TYPE_TO_HANDLER.put(DataType.NBT_ABSTRACT, NBTDataHandler.ABSTRACT);
		
		
		TYPE_TO_HANDLER.put(DataType.WILDCARD, SpecialDataHandler.WILDCRD);
		TYPE_TO_HANDLER.put(DataType.TYPE_VARIABLE, SpecialDataHandler.VARIABLE_TYPE);
		TYPE_TO_HANDLER.put(DataType.INTERSECTION, SpecialDataHandler.INTERSECTION);
		
		TYPE_TO_HANDLER.put(DataType.DEFAULT, SpecialDataHandler.DEFAULT);
		TYPE_TO_HANDLER.put(DataType.CUSTOM, SpecialDataHandler.CUSTOM);
		
		
		HANDLER_PRIORITIES.prioritize(SimpleClassHandler.ITEM_STACK).over(SerializableDataHandler.NBT_SERIALISABLE);

		HANDLER_PRIORITIES.prioritize(NBTDataHandler.PRIMITIVE).over(NBTDataHandler.CONCRETE);
		HANDLER_PRIORITIES.prioritize(NBTDataHandler.CONCRETE).over(NBTDataHandler.ABSTRACT);
		HANDLER_PRIORITIES.prioritize(NBTDataHandler.CONCRETE).over(CollectionDataHandler.INSTANCE);
		HANDLER_PRIORITIES.prioritize(NBTDataHandler.ABSTRACT).over(CollectionDataHandler.INSTANCE);
	}
	
	private static IDataHandler dataTypeToHandler(String typeName)
	{
		DataType type;
		try
		{
			type = DataType.valueOf(typeName.toUpperCase());
		}
		catch (IllegalArgumentException e)
		{
			throw new InvalidPropertyException("The type '" + typeName + "' is invalid", e);
		}
		return TYPE_TO_HANDLER.get(type);
	}
	
}
