package fr.max2.annotated.processor.network;

import java.util.HashMap;
import java.util.HashSet;
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
import fr.max2.annotated.processor.network.coder.EntityCoder;
import fr.max2.annotated.processor.network.coder.MapCoder;
import fr.max2.annotated.processor.network.coder.NBTCoder;
import fr.max2.annotated.processor.network.coder.PrimitiveCoder;
import fr.max2.annotated.processor.network.coder.RegistryEntryCoder;
import fr.max2.annotated.processor.network.coder.SerializableCoder;
import fr.max2.annotated.processor.network.coder.SimpleClassCoder;
import fr.max2.annotated.processor.network.coder.SpecialCoder;
import fr.max2.annotated.processor.network.coder.handler.IDataHandler;
import fr.max2.annotated.processor.utils.PriorityManager;
import fr.max2.annotated.processor.utils.PropertyMap;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;
import fr.max2.annotated.processor.utils.exceptions.CoderExcepetion;

public class DataCoderFinder
{
	private final Collection<IDataHandler> typeMap;
	private final ProcessingTools tools;
	
	public DataCoderFinder(ProcessingTools tools)
	{
		this.tools = tools;
		
		this.typeMap = new HashSet<>(HANDLERS.values());
		this.typeMap.addAll(SPACIAL_HANDLERS);
		this.typeMap.forEach(data -> data.init(this.tools));
	}
	
	public DataCoder getCoder(Element field) throws CoderExcepetion
	{
		Element elem = this.tools.types.asElement(field.asType());
		DataProperties typeData = elem == null ? null : elem.getAnnotation(DataProperties.class);
		PropertyMap typeProperties = typeData == null ? PropertyMap.EMPTY_PROPERTIES : PropertyMap.fromArray(typeData.value());
		
		DataProperties customData = field.getAnnotation(DataProperties.class);
		PropertyMap customProperties = customData == null ? PropertyMap.EMPTY_PROPERTIES : PropertyMap.fromArray(customData.value());
		
		PropertyMap properties = typeProperties.overrideWith(customProperties);
		return this.getCoder(field.getSimpleName().toString(), field.asType(), properties);
	}
	
	public DataCoder getCoder(String uniqueName, TypeMirror type, PropertyMap properties) throws CoderExcepetion
	{
		DataCoder params = this.getCoderOrNull(uniqueName, type, properties);
		
		if (params == null)
			throw new IncompatibleTypeException("No data coder found to process the type '" + type.toString() + "'. Use the " + DataProperties.class.getCanonicalName() + " annotation with the 'type' property to specify a DataType");
		
		return params;
	}
	
	public DataCoder getCoderOrNull(String uniqueName, TypeMirror type, PropertyMap properties) throws CoderExcepetion
	{
		IDataHandler handler = getSpecialHandler(type);
		if (handler == null)
			handler = properties.getValue("type")
				.map(DataCoderFinder::handlerFromName)
				.orElseGet(() -> this.getDefaultHandler(type));
		if (handler == null)
			return null;
		
		return handler.createCoder(this.tools, uniqueName, type, properties);
	}
	
	public IDataHandler getDefaultHandler(TypeMirror type)
	{
		List<IDataHandler> validHandlers = this.typeMap.stream().filter(entry -> entry.canProcess(type)).collect(Collectors.toList());
		List<IDataHandler> prioritizedHandlers = HANDLER_PRIORITIES.getHighests(validHandlers);
		
		switch (prioritizedHandlers.size())
		{
		case 0:
			return null;
		case 1:
			return prioritizedHandlers.get(0);
		default:
			throw new IllegalArgumentException("The data handler of the '" + type.toString() + "' type couldn't be chosen: handler priorities are equal: " + prioritizedHandlers.stream().map(h -> h.getClass().getTypeName() + ":" + h.toString()).collect(Collectors.joining(", ")));
		}
	}
	
	public IDataHandler getSpecialHandler(TypeMirror type)
	{
		return SPACIAL_HANDLERS.stream().filter(entry -> entry.canProcess(type)).findAny().orElse(null);
	}
	
	
	private static final Map<String, IDataHandler> HANDLERS = new HashMap<>();
	private static final Collection<IDataHandler> SPACIAL_HANDLERS = new HashSet<>();
	private static final PriorityManager<IDataHandler> HANDLER_PRIORITIES = new PriorityManager<>();
	
	static
	{
		HANDLERS.put(DataType.BYTE, PrimitiveCoder.BYTE);
		HANDLERS.put(DataType.SHORT, PrimitiveCoder.SHORT);
		HANDLERS.put(DataType.INT, PrimitiveCoder.INT);
		HANDLERS.put(DataType.LONG, PrimitiveCoder.LONG);
		HANDLERS.put(DataType.FLOAT, PrimitiveCoder.FLOAT);
		HANDLERS.put(DataType.DOUBLE, PrimitiveCoder.DOUBLE);
		HANDLERS.put(DataType.BOOLEAN, PrimitiveCoder.BOOLEAN);
		HANDLERS.put(DataType.CHAR, PrimitiveCoder.CHAR);
		
		HANDLERS.put(DataType.ARRAY, ArrayCoder.HANDLER);
		
		HANDLERS.put(DataType.STRING, SimpleClassCoder.STRING);
		HANDLERS.put(DataType.ENUM, SimpleClassCoder.ENUM);
		HANDLERS.put(DataType.COLLECTION, CollectionCoder.HANDLER);
		HANDLERS.put(DataType.MAP, MapCoder.HANDLER);
		HANDLERS.put(DataType.UUID, SimpleClassCoder.UUID);
		HANDLERS.put(DataType.TIME, SimpleClassCoder.DATE);
		
		HANDLERS.put(DataType.BLOCK_POS, SimpleClassCoder.BLOCK_POS);
		HANDLERS.put(DataType.RESOURCE_LOCATION, SimpleClassCoder.RESOURCE_LOCATION);
		HANDLERS.put(DataType.ITEM_STACK, SimpleClassCoder.ITEM_STACK);
		HANDLERS.put(DataType.FLUID_STACK, SimpleClassCoder.FLUID_STACK);
		HANDLERS.put(DataType.TEXT_COMPONENT, SimpleClassCoder.TEXT_COMPONENT);
		HANDLERS.put(DataType.BLOCK_RAY_TRACE, SimpleClassCoder.BLOCK_RAY_TRACE);
		HANDLERS.put(DataType.REGISTRY_ENTRY, RegistryEntryCoder.HANDLER);
		HANDLERS.put(DataType.NBT_SERIALIZABLE, SerializableCoder.NBT_SERIALISABLE);
		HANDLERS.put(DataType.ENTITY_ID, EntityCoder.ENTITY_ID);
		HANDLERS.put(DataType.PLAYER_ID, EntityCoder.PLAYER_ID);
		
		HANDLERS.put(DataType.NBT_PRIMITIVE, NBTCoder.PRIMITIVE);
		HANDLERS.put(DataType.NBT_CONCRETE, NBTCoder.CONCRETE);
		HANDLERS.put(DataType.NBT_ABSTRACT, NBTCoder.ABSTRACT);
		
		
		SPACIAL_HANDLERS.add(SpecialCoder.WILDCRD);
		SPACIAL_HANDLERS.add(SpecialCoder.VARIABLE_TYPE);
		SPACIAL_HANDLERS.add(SpecialCoder.INTERSECTION);
		
		HANDLERS.put(DataType.DEFAULT, SpecialCoder.DEFAULT);
		
		
		HANDLER_PRIORITIES.prioritize(SimpleClassCoder.ITEM_STACK).over(SerializableCoder.NBT_SERIALISABLE);
		
		HANDLER_PRIORITIES.prioritize(EntityCoder.PLAYER_ID).over(EntityCoder.ENTITY_ID);
		HANDLER_PRIORITIES.prioritize(EntityCoder.PLAYER_ID).over(SerializableCoder.NBT_SERIALISABLE);
		HANDLER_PRIORITIES.prioritize(EntityCoder.ENTITY_ID).over(SerializableCoder.NBT_SERIALISABLE);

		HANDLER_PRIORITIES.prioritize(NBTCoder.PRIMITIVE).over(NBTCoder.CONCRETE);
		HANDLER_PRIORITIES.prioritize(NBTCoder.CONCRETE).over(NBTCoder.ABSTRACT);
		HANDLER_PRIORITIES.prioritize(NBTCoder.CONCRETE).over(CollectionCoder.HANDLER);
		HANDLER_PRIORITIES.prioritize(NBTCoder.ABSTRACT).over(CollectionCoder.HANDLER);
	}
	
	private static IDataHandler handlerFromName(String handlerName)
	{
		IDataHandler handler = HANDLERS.get(handlerName);
		if (handler == null)
			throw new IllegalArgumentException("The type '" + handlerName + "' is invalid");
		
		return handler;
	}
	
}
