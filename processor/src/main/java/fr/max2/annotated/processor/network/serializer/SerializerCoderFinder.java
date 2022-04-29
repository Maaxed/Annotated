package fr.max2.annotated.processor.network.serializer;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.util.ClassRef;
import fr.max2.annotated.processor.util.PriorityManager;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.CoderException;
import fr.max2.annotated.processor.util.exceptions.IncompatibleTypeException;

public class SerializerCoderFinder
{
	// TODO find available serializers using Filer.getResource / classloader / JavaFileManager.getFileForInput / ServiceLoader (see ToolProvider)
	private final Collection<ICoderHandler<SerializationCoder>> spacialHandlers = new HashSet<>();
	private final PriorityManager<ICoderHandler<SerializationCoder>> handlerPriorities = new PriorityManager<>();
	private final Collection<ICoderHandler<SerializationCoder>> handlers = new HashSet<>();
	
	public SerializerCoderFinder(ProcessingTools tools)
	{
		this.handlers.add(PrimitiveCoder.handler(tools, "Byte"));
		this.handlers.add(PrimitiveCoder.handler(tools, "Short"));
		this.handlers.add(PrimitiveCoder.handler(tools, "Int"));
		this.handlers.add(PrimitiveCoder.handler(tools, "Long"));
		this.handlers.add(PrimitiveCoder.handler(tools, "Float"));
		this.handlers.add(PrimitiveCoder.handler(tools, "Double"));
		this.handlers.add(PrimitiveCoder.handler(tools, "Boolean"));
		this.handlers.add(PrimitiveCoder.handler(tools, "Char"));
		
		this.handlers.add(SimpleCoder.handler(tools, "java.lang.String", "fr.max2.annotated.lib.network.serializer.SimpleClassSerializer.StringSerializer"));
		this.handlers.add(SimpleCoder.handler(tools, "java.util.UUID", "fr.max2.annotated.lib.network.serializer.SimpleClassSerializer.UUIDSerializer"));
		this.handlers.add(SimpleCoder.handler(tools, "java.util.Date", "fr.max2.annotated.lib.network.serializer.SimpleClassSerializer.DateSerializer"));

		ICoderHandler<SerializationCoder> itemStack = SimpleCoder.handler(tools, ClassRef.ITEM_STACK, "fr.max2.annotated.lib.network.serializer.SimpleClassSerializer.ItemStackSerializer");
		this.handlers.add(SimpleCoder.handler(tools, ClassRef.BLOCK_POS, "fr.max2.annotated.lib.network.serializer.SimpleClassSerializer.BlockPosSerializer"));
		this.handlers.add(SimpleCoder.handler(tools, ClassRef.RESOURCE_LOCATION, "fr.max2.annotated.lib.network.serializer.SimpleClassSerializer.ResourceLocationSerializer"));
		this.handlers.add(itemStack);
		this.handlers.add(SimpleCoder.handler(tools, ClassRef.FLUID_STACK, "fr.max2.annotated.lib.network.serializer.SimpleClassSerializer.FluidStackSerializer"));
		this.handlers.add(SimpleCoder.handler(tools, ClassRef.TEXT_COMPONENT, "fr.max2.annotated.lib.network.serializer.SimpleClassSerializer.TextComponentSerializer")); // TODO [v3.1] allow serializing specific implementations
		this.handlers.add(SimpleCoder.handler(tools, ClassRef.BLOCK_RAY_TRACE, "fr.max2.annotated.lib.network.serializer.SimpleClassSerializer.BlockHitResultSerializer"));
		
		this.handlers.add(SimpleCoder.handler(tools, ClassRef.AXIS_ALIGNED_BB, "fr.max2.annotated.lib.network.serializer.VectorClassSerializer.AABBSerializer"));
		this.handlers.add(SimpleCoder.handler(tools, ClassRef.MUTABLE_BB, "fr.max2.annotated.lib.network.serializer.VectorClassSerializer.StructureBBSerializer"));
		this.handlers.add(SimpleCoder.handler(tools, ClassRef.CHUNK_POS, "fr.max2.annotated.lib.network.serializer.SimpleClassSerializer.ChunkPosSerializer"));
		this.handlers.add(SimpleCoder.handler(tools, ClassRef.SECTION_POS, "fr.max2.annotated.lib.network.serializer.SimpleClassSerializer.SectionPosSerializer"));
		this.handlers.add(SimpleCoder.handler(tools, ClassRef.VECTOR_3D, "fr.max2.annotated.lib.network.serializer.VectorClassSerializer.Vec3Serializer"));
		this.handlers.add(SimpleCoder.handler(tools, ClassRef.VECTOR_3I, "fr.max2.annotated.lib.network.serializer.VectorClassSerializer.Vec3ISerializer"));
		
		ICoderHandler<SerializationCoder> nbtAbstract = SimpleCoder.handler(tools, ClassRef.NBT_BASE, "fr.max2.annotated.lib.network.serializer.TagSerializer.Abstract");
		ICoderHandler<SerializationCoder> nbtConcrete = GenericCoder.handler(tools, ClassRef.NBT_BASE, "fr.max2.annotated.lib.network.serializer.TagSerializer.Concrete", type -> tools.naming.erasedType.get(type) + ".TYPE");
		this.handlers.add(nbtAbstract);
		this.handlers.add(nbtConcrete);
		
		//ICoderHandler<SerializationCoder> entityId = EntityCoder.ENTITY_ID.createHandler(tools);
		//ICoderHandler<SerializationCoder> playerId = EntityCoder.PLAYER_ID.createHandler(tools);
		//this.handlers.add(entityId);
		//this.handlers.add(playerId);

		this.handlers.add(GenericCoder.handler(tools, ClassRef.REGISTRY_ENTRY, "fr.max2.annotated.lib.network.serializer.RegistryEntrySerializer", type -> tools.naming.erasedType.get(type) + ".class")); // TODO [v3.1] Find a good way to get the registry from the type
		ICoderHandler<SerializationCoder> nbtSerializable = NBTSerializableCoder.handler(tools);
		this.handlers.add(nbtSerializable);

		this.handlers.add(GenericCoder.handler(tools, "java.lang.Enum", "fr.max2.annotated.lib.network.serializer.EnumSerializer", type -> tools.naming.erasedType.get(type) + ".class"));
		this.handlers.add(ArrayCoder.handler(tools));
		ICoderHandler<SerializationCoder> collection = CollectionCoder.handler(tools);
		this.handlers.add(collection);
		this.handlers.add(MapCoder.handler(tools));
		
		
		this.spacialHandlers.add(SpecialCoder.wildcard(tools));
		this.spacialHandlers.add(SpecialCoder.variableType(tools));
		this.spacialHandlers.add(SpecialCoder.intersection(tools));
		
		
		this.handlerPriorities.prioritize(itemStack).over(nbtSerializable);
		
		/*this.handlerPriorities.prioritize(playerId).over(entityId);
		this.handlerPriorities.prioritize(playerId).over(nbtSerializable);
		this.handlerPriorities.prioritize(entityId).over(nbtSerializable);*/
		
		this.handlerPriorities.prioritize(nbtAbstract).over(nbtConcrete);
		this.handlerPriorities.prioritize(nbtConcrete).over(collection);
		this.handlerPriorities.prioritize(nbtAbstract).over(collection);
		
		//TODO [v2.1] DamageSource, VoxelShape
		//TODO [v2.1] Rotations, Size2i, Vec2f, GlobalPos
		//TODO [v2.1] Entity by copy type
		//TODO [v3.0] Entity by id + DimensionType
		//TODO [v3.0] DimensionType
		//TODO [v2.1] IDynamicSerializable
		//TODO [v2.1] TileEntity
		//TODO [v2.1] Container
		//TODO [v2.1] JsonDeserializer + JsonSerializer
		//TODO [v2.2] custom data class
		
		this.handlers.addAll(this.spacialHandlers);
	}
	
	public SerializationCoder getCoder(Element field) throws CoderException
	{
		/*Element elem = this.tools.types.asElement(field.asType());
		DataProperties typeData = elem == null ? null : elem.getAnnotation(DataProperties.class);
		PropertyMap typeProperties = typeData == null ? PropertyMap.EMPTY_PROPERTIES : PropertyMap.fromArray(typeData.value());
		
		DataProperties customData = field.getAnnotation(DataProperties.class);
		PropertyMap customProperties = customData == null ? PropertyMap.EMPTY_PROPERTIES : PropertyMap.fromArray(customData.value());
		
		PropertyMap properties = typeProperties.overrideWith(customProperties);*/
		return this.getCoder(field.asType());
	}
	
	public SerializationCoder getCoder(TypeMirror type) throws CoderException
	{
		SerializationCoder params = this.getCoderOrNull(type);
		
		if (params == null)
			throw new IncompatibleTypeException("No data coder found to process the type '" + type.toString() + "'");
		
		return params;
	}
	
	public SerializationCoder getCoderOrNull(TypeMirror type) throws CoderException
	{
		ICoderHandler<SerializationCoder> handler = getHandler(type).orElse(null);
		if (handler == null)
			return null;
		
		return handler.createCoder(type);
	}
	
	public Optional<ICoderHandler<SerializationCoder>> getHandler(TypeMirror type)
	{
		return getSpecialHandler(type).or(() -> this.getDefaultHandler(type));
	}
	
	private Optional<ICoderHandler<SerializationCoder>> getDefaultHandler(TypeMirror type)
	{
		List<ICoderHandler<SerializationCoder>> validHandlers = this.handlers.stream().filter(entry -> entry.canProcess(type)).collect(Collectors.toList());
		List<ICoderHandler<SerializationCoder>> prioritizedHandlers = this.handlerPriorities.getHighests(validHandlers);
		
		switch (prioritizedHandlers.size())
		{
		case 0:
			return Optional.empty();
		case 1:
			return Optional.of(prioritizedHandlers.get(0));
		default:
			throw new IllegalArgumentException("The data handler of the '" + type.toString() + "' type couldn't be chosen: handler priorities are equal: " + prioritizedHandlers.stream().map(h -> h.getClass().getTypeName() + ":" + h.toString()).collect(Collectors.joining(", ")));
		}
	}
	
	private Optional<ICoderHandler<SerializationCoder>> getSpecialHandler(TypeMirror type)
	{
		return this.spacialHandlers.stream().filter(entry -> entry.canProcess(type)).findAny();
	}
	
}
