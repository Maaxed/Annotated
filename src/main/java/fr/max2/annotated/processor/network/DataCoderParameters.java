package fr.max2.annotated.processor.network;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.api.processor.network.DataProperties;
import fr.max2.annotated.api.processor.network.DataType;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.network.coder.ArrayCoder;
import fr.max2.annotated.processor.network.coder.CollectionCoder;
import fr.max2.annotated.processor.network.coder.DataCoder;
import fr.max2.annotated.processor.network.coder.MapCoder;
import fr.max2.annotated.processor.network.coder.NBTCoder;
import fr.max2.annotated.processor.network.coder.PrimitiveCoder;
import fr.max2.annotated.processor.network.coder.RegistryEntryCoder;
import fr.max2.annotated.processor.network.coder.SerializableCoder;
import fr.max2.annotated.processor.network.coder.SimpleClassCoder;
import fr.max2.annotated.processor.network.coder.handler.IDataHandler;
import fr.max2.annotated.processor.network.coder.handler.SpecialDataHandler;
import fr.max2.annotated.processor.utils.PriorityManager;
import fr.max2.annotated.processor.utils.PropertyMap;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;
import fr.max2.annotated.processor.utils.exceptions.InvalidPropertyException;

public class DataCoderParameters
{
	public final ProcessingTools tools;
	public final String uniqueName;
	public final TypeMirror type;
	public final PropertyMap properties;
	
	public DataCoderParameters(ProcessingTools tools, String uniqueName, TypeMirror type, PropertyMap properties)
	{
		this.tools = tools;
		this.uniqueName = uniqueName;
		this.type = type;
		this.properties = properties;
	}
	
	
	public static class Finder
	{
		private final Collection<IDataHandler> typeMap;
		private final ProcessingTools tools;
		
		public Finder(ProcessingTools tools)
		{
			this.tools = tools;
			
			this.typeMap = TYPE_TO_HANDLER.values();
			this.typeMap.forEach(data -> data.init(this.tools));
		}
		
		public DataCoder getDataType(Element field)
		{
			DataProperties customData = field.getAnnotation(DataProperties.class);
			if (customData == null)
			{
				Element elem = this.tools.types.asElement(field.asType());
				if (elem != null)
					customData = elem.getAnnotation(DataProperties.class);
			}
			PropertyMap properties = customData == null ? PropertyMap.EMPTY_PROPERTIES : new PropertyMap(customData.value());
			return this.getDataType(field.getSimpleName().toString(), field.asType(), properties);
		}
		
		public DataCoder getDataType(String uniqueName, TypeMirror type, PropertyMap properties)
		{
			DataCoder params = this.getDataTypeOrNull(uniqueName, type, properties);
			
			if (params == null)
				throw new IncompatibleTypeException("No data handler can process the type '" + type.toString() + "'");
			
			return params;
		}
		
		public DataCoder getDataTypeOrNull(String uniqueName, TypeMirror type, PropertyMap properties)
		{
			IDataHandler handler = properties.getValue("type")
				.map(DataCoderParameters::dataTypeToHandler)
				.orElseGet(() ->
				{
					IDataHandler defaultHandler = this.getDefaultDataType(type);
					return defaultHandler == SpecialDataHandler.CUSTOM ? null : defaultHandler;
				});
			if (handler == null)
				return null;
			
			DataCoder coder = handler.createCoder();
			coder.init(new DataCoderParameters(this.tools, uniqueName, type, properties));
			return coder;
			
			
		}
		
		public IDataHandler getDefaultDataType(TypeMirror type)
		{
			List<IDataHandler> validHandlers = this.typeMap.stream().filter(entry -> entry.canProcess(type)).collect(Collectors.toList());
			
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
		TYPE_TO_HANDLER.put(DataType.BYTE, PrimitiveCoder.BYTE);
		TYPE_TO_HANDLER.put(DataType.SHORT, PrimitiveCoder.SHORT);
		TYPE_TO_HANDLER.put(DataType.INT, PrimitiveCoder.INT);
		TYPE_TO_HANDLER.put(DataType.LONG, PrimitiveCoder.LONG);
		TYPE_TO_HANDLER.put(DataType.FLOAT, PrimitiveCoder.FLOAT);
		TYPE_TO_HANDLER.put(DataType.DOUBLE, PrimitiveCoder.DOUBLE);
		TYPE_TO_HANDLER.put(DataType.BOOLEAN, PrimitiveCoder.BOOLEAN);
		TYPE_TO_HANDLER.put(DataType.CHAR, PrimitiveCoder.CHAR);
		
		TYPE_TO_HANDLER.put(DataType.ARRAY, ArrayCoder.HANDLER);
		
		TYPE_TO_HANDLER.put(DataType.STRING, SimpleClassCoder.STRING);
		TYPE_TO_HANDLER.put(DataType.ENUM, SimpleClassCoder.ENUM);
		TYPE_TO_HANDLER.put(DataType.COLLECTION, CollectionCoder.HANDLER);
		TYPE_TO_HANDLER.put(DataType.MAP, MapCoder.HANDLER);
		TYPE_TO_HANDLER.put(DataType.UUID, SimpleClassCoder.UUID);
		TYPE_TO_HANDLER.put(DataType.TIME, SimpleClassCoder.DATE);
		
		TYPE_TO_HANDLER.put(DataType.BLOCK_POS, SimpleClassCoder.BLOCK_POS);
		TYPE_TO_HANDLER.put(DataType.RESOURCE_LOCATION, SimpleClassCoder.RESOURCE_LOCATION);
		TYPE_TO_HANDLER.put(DataType.ITEM_STACK, SimpleClassCoder.ITEM_STACK);
		TYPE_TO_HANDLER.put(DataType.FLUID_STACK, SimpleClassCoder.FLUID_STACK);
		TYPE_TO_HANDLER.put(DataType.TEXT_COMPONENT, SimpleClassCoder.TEXT_COMPONENT);
		TYPE_TO_HANDLER.put(DataType.BLOCK_RAY_TRACE, SimpleClassCoder.BLOCK_RAY_TRACE);
		TYPE_TO_HANDLER.put(DataType.REGISTRY_ENTRY, RegistryEntryCoder.HANDLER);
		TYPE_TO_HANDLER.put(DataType.NBT_SERIALIZABLE, SerializableCoder.NBT_SERIALISABLE);
		
		TYPE_TO_HANDLER.put(DataType.NBT_PRIMITIVE, NBTCoder.PRIMITIVE);
		TYPE_TO_HANDLER.put(DataType.NBT_CONCRETE, NBTCoder.CONCRETE);
		TYPE_TO_HANDLER.put(DataType.NBT_ABSTRACT, NBTCoder.ABSTRACT);
		
		
		TYPE_TO_HANDLER.put(DataType.WILDCARD, SpecialDataHandler.WILDCRD);
		TYPE_TO_HANDLER.put(DataType.TYPE_VARIABLE, SpecialDataHandler.VARIABLE_TYPE);
		TYPE_TO_HANDLER.put(DataType.INTERSECTION, SpecialDataHandler.INTERSECTION);
		
		TYPE_TO_HANDLER.put(DataType.DEFAULT, SpecialDataHandler.DEFAULT);
		TYPE_TO_HANDLER.put(DataType.CUSTOM, SpecialDataHandler.CUSTOM);
		
		
		HANDLER_PRIORITIES.prioritize(SimpleClassCoder.ITEM_STACK).over(SerializableCoder.NBT_SERIALISABLE);

		HANDLER_PRIORITIES.prioritize(NBTCoder.PRIMITIVE).over(NBTCoder.CONCRETE);
		HANDLER_PRIORITIES.prioritize(NBTCoder.CONCRETE).over(NBTCoder.ABSTRACT);
		HANDLER_PRIORITIES.prioritize(NBTCoder.CONCRETE).over(CollectionCoder.HANDLER);
		HANDLER_PRIORITIES.prioritize(NBTCoder.ABSTRACT).over(CollectionCoder.HANDLER);
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
