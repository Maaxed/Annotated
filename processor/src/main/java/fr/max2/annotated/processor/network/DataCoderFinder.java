package fr.max2.annotated.processor.network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.api.network.DataProperties;
import fr.max2.annotated.api.network.DataType;
import fr.max2.annotated.processor.network.coder.ArrayCoder;
import fr.max2.annotated.processor.network.coder.CollectionCoder;
import fr.max2.annotated.processor.network.coder.DataCoder;
import fr.max2.annotated.processor.network.coder.EntityCoder;
import fr.max2.annotated.processor.network.coder.VectorClassCoder;
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
	private final Map<String, IDataHandler> handlers = new HashMap<>();
	private final Collection<IDataHandler> spacialHandlers = new HashSet<>();
	private final PriorityManager<IDataHandler> handlerPriorities = new PriorityManager<>();
	private final Collection<IDataHandler> typeMap;
	private final ProcessingTools tools;
	
	public DataCoderFinder(ProcessingTools tools)
	{
		this.tools = tools;
		
		this.handlers.put(DataType.BYTE, PrimitiveCoder.BYTE.createHandler(tools));
		this.handlers.put(DataType.SHORT, PrimitiveCoder.SHORT.createHandler(tools));
		this.handlers.put(DataType.INT, PrimitiveCoder.INT.createHandler(tools));
		this.handlers.put(DataType.LONG, PrimitiveCoder.LONG.createHandler(tools));
		this.handlers.put(DataType.FLOAT, PrimitiveCoder.FLOAT.createHandler(tools));
		this.handlers.put(DataType.DOUBLE, PrimitiveCoder.DOUBLE.createHandler(tools));
		this.handlers.put(DataType.BOOLEAN, PrimitiveCoder.BOOLEAN.createHandler(tools));
		this.handlers.put(DataType.CHAR, PrimitiveCoder.CHAR.createHandler(tools));
		
		this.handlers.put(DataType.ARRAY, ArrayCoder.HANDLER);
		
		IDataHandler collection = CollectionCoder.HANDLER.createHandler(tools);
		this.handlers.put(DataType.STRING, SimpleClassCoder.STRING.createHandler(tools));
		this.handlers.put(DataType.ENUM, SimpleClassCoder.ENUM.createHandler(tools));
		this.handlers.put(DataType.COLLECTION, collection);
		this.handlers.put(DataType.MAP, MapCoder.HANDLER.createHandler(tools));
		this.handlers.put(DataType.UUID, SimpleClassCoder.UUID.createHandler(tools));
		this.handlers.put(DataType.TIME, SimpleClassCoder.DATE.createHandler(tools));

		IDataHandler blockPos = SimpleClassCoder.BLOCK_POS.createHandler(tools);
		IDataHandler itemStack = SimpleClassCoder.ITEM_STACK.createHandler(tools);
		IDataHandler nbtSerializable = SerializableCoder.NBT_SERIALIZABLE.createHandler(tools);
		IDataHandler entityId = EntityCoder.ENTITY_ID.createHandler(tools);
		IDataHandler playerId = EntityCoder.PLAYER_ID.createHandler(tools);
		this.handlers.put(DataType.BLOCK_POS, blockPos);
		this.handlers.put(DataType.RESOURCE_LOCATION, SimpleClassCoder.RESOURCE_LOCATION.createHandler(tools));
		this.handlers.put(DataType.ITEM_STACK, itemStack);
		this.handlers.put(DataType.FLUID_STACK, SimpleClassCoder.FLUID_STACK.createHandler(tools));
		this.handlers.put(DataType.TEXT_COMPONENT, SimpleClassCoder.TEXT_COMPONENT.createHandler(tools));
		this.handlers.put(DataType.BLOCK_RAY_TRACE, SimpleClassCoder.BLOCK_RAY_TRACE.createHandler(tools));
		this.handlers.put(DataType.REGISTRY_ENTRY, RegistryEntryCoder.HANDLER.createHandler(tools));
		this.handlers.put(DataType.NBT_SERIALIZABLE, nbtSerializable);
		this.handlers.put(DataType.ENTITY_ID, entityId);
		this.handlers.put(DataType.PLAYER_ID, playerId);

		IDataHandler sectionPos = VectorClassCoder.SECTION_POS.createHandler(tools);
		IDataHandler vec3i = VectorClassCoder.VECTOR_3I.createHandler(tools);
		this.handlers.put(DataType.AXIS_ALIGNED_BB, VectorClassCoder.AXIS_ALIGNED_BB.createHandler(tools));
		this.handlers.put(DataType.MUTABLE_BB, VectorClassCoder.MUTABLE_BB.createHandler(tools));
		this.handlers.put(DataType.CHUNK_POS, VectorClassCoder.CHUNK_POS.createHandler(tools));
		this.handlers.put(DataType.SECTION_POS, sectionPos);
		this.handlers.put(DataType.VECTOR_3D, VectorClassCoder.VECTOR_3D.createHandler(tools));
		this.handlers.put(DataType.VECTOR_3I, vec3i);
		
		IDataHandler nbtPrimitive = NBTCoder.PRIMITIVE.createHandler(tools);
		IDataHandler nbtConcrete = NBTCoder.CONCRETE.createHandler(tools);
		IDataHandler nbtAbstract = NBTCoder.ABSTRACT.createHandler(tools);
		this.handlers.put(DataType.NBT_PRIMITIVE, nbtPrimitive);
		this.handlers.put(DataType.NBT_CONCRETE, nbtConcrete);
		this.handlers.put(DataType.NBT_ABSTRACT, nbtAbstract);
		
		this.handlers.put(DataType.DEFAULT, SpecialCoder.DEFAULT);
		
		
		this.spacialHandlers.add(SpecialCoder.WILDCRD);
		this.spacialHandlers.add(SpecialCoder.VARIABLE_TYPE);
		this.spacialHandlers.add(SpecialCoder.INTERSECTION);
		
		
		this.handlerPriorities.prioritize(blockPos).over(vec3i);
		this.handlerPriorities.prioritize(sectionPos).over(vec3i);
		this.handlerPriorities.prioritize(itemStack).over(nbtSerializable);
		
		this.handlerPriorities.prioritize(playerId).over(entityId);
		this.handlerPriorities.prioritize(playerId).over(nbtSerializable);
		this.handlerPriorities.prioritize(entityId).over(nbtSerializable);
		
		this.handlerPriorities.prioritize(nbtPrimitive).over(nbtConcrete);
		this.handlerPriorities.prioritize(nbtConcrete).over(nbtAbstract);
		this.handlerPriorities.prioritize(nbtConcrete).over(collection);
		this.handlerPriorities.prioritize(nbtAbstract).over(collection);
		
		this.typeMap = new HashSet<>(this.handlers.values());
		this.typeMap.addAll(this.spacialHandlers);
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
				.map(this::handlerFromName)
				.orElseGet(() -> this.getDefaultHandler(type));
		if (handler == null)
			return null;
		
		return handler.createCoder(this.tools, uniqueName, type, properties);
	}
	
	public IDataHandler getDefaultHandler(TypeMirror type)
	{
		List<IDataHandler> validHandlers = this.typeMap.stream().filter(entry -> entry.canProcess(type)).collect(Collectors.toList());
		List<IDataHandler> prioritizedHandlers = this.handlerPriorities.getHighests(validHandlers);
		
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
		return this.spacialHandlers.stream().filter(entry -> entry.canProcess(type)).findAny().orElse(null);
	}
	
	private IDataHandler handlerFromName(String handlerName)
	{
		IDataHandler handler = this.handlers.get(handlerName);
		if (handler == null)
			throw new IllegalArgumentException("The type '" + handlerName + "' is invalid");
		
		return handler;
	}
	
}
