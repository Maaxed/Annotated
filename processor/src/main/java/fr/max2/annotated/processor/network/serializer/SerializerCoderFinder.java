package fr.max2.annotated.processor.network.serializer;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.CoderCompatibility;
import fr.max2.annotated.processor.network.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.util.ClassRef;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.CoderException;
import fr.max2.annotated.processor.util.exceptions.IncompatibleTypeException;

public class SerializerCoderFinder
{
	// TODO find available serializers using Filer.getResource / classloader / JavaFileManager.getFileForInput / ServiceLoader (see ToolProvider)
	private final Collection<ICoderHandler<SerializationCoder>> spacialHandlers = new ArrayList<>();
	private final Collection<ICoderHandler<SerializationCoder>> handlers = new ArrayList<>();
	
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

		this.handlers.add(SimpleCoder.handler(tools, ClassRef.BLOCK_POS, "fr.max2.annotated.lib.network.serializer.SimpleClassSerializer.BlockPosSerializer"));
		this.handlers.add(SimpleCoder.handler(tools, ClassRef.RESOURCE_LOCATION, "fr.max2.annotated.lib.network.serializer.SimpleClassSerializer.ResourceLocationSerializer"));
		this.handlers.add(SimpleCoder.handler(tools, ClassRef.ITEM_STACK, "fr.max2.annotated.lib.network.serializer.SimpleClassSerializer.ItemStackSerializer"));
		this.handlers.add(SimpleCoder.handler(tools, ClassRef.FLUID_STACK, "fr.max2.annotated.lib.network.serializer.SimpleClassSerializer.FluidStackSerializer"));
		this.handlers.add(SimpleCoder.handler(tools, ClassRef.TEXT_COMPONENT, "fr.max2.annotated.lib.network.serializer.SimpleClassSerializer.TextComponentSerializer")); // TODO [v3.1] allow serializing specific implementations
		this.handlers.add(SimpleCoder.handler(tools, ClassRef.BLOCK_RAY_TRACE, "fr.max2.annotated.lib.network.serializer.SimpleClassSerializer.BlockHitResultSerializer"));
		
		this.handlers.add(SimpleCoder.handler(tools, ClassRef.AXIS_ALIGNED_BB, "fr.max2.annotated.lib.network.serializer.VectorClassSerializer.AABBSerializer"));
		this.handlers.add(SimpleCoder.handler(tools, ClassRef.MUTABLE_BB, "fr.max2.annotated.lib.network.serializer.VectorClassSerializer.StructureBBSerializer"));
		this.handlers.add(SimpleCoder.handler(tools, ClassRef.CHUNK_POS, "fr.max2.annotated.lib.network.serializer.SimpleClassSerializer.ChunkPosSerializer"));
		this.handlers.add(SimpleCoder.handler(tools, ClassRef.SECTION_POS, "fr.max2.annotated.lib.network.serializer.SimpleClassSerializer.SectionPosSerializer"));
		this.handlers.add(SimpleCoder.handler(tools, ClassRef.VECTOR_3D, "fr.max2.annotated.lib.network.serializer.VectorClassSerializer.Vec3Serializer"));
		this.handlers.add(SimpleCoder.handler(tools, ClassRef.VECTOR_3I, "fr.max2.annotated.lib.network.serializer.VectorClassSerializer.Vec3ISerializer"));
		
		this.handlers.add(NBTCoder.abstractHandler(tools));
		this.handlers.add(NBTCoder.concreteHandler(tools));
		
		//this.handlers.add(EntityCoder.ENTITY_ID.createHandler(tools));
		//this.handlers.add(EntityCoder.PLAYER_ID.createHandler(tools));

		this.handlers.add(GenericCoder.handler(tools, ClassRef.REGISTRY_ENTRY, "fr.max2.annotated.lib.network.serializer.RegistryEntrySerializer", type -> tools.naming.erasedType.get(type) + ".class")); // TODO [v3.1] Find a good way to get the registry from the type
		this.handlers.add(NBTSerializableCoder.handler(tools));

		this.handlers.add(GenericCoder.handler(tools, "java.lang.Enum", "fr.max2.annotated.lib.network.serializer.EnumSerializer", type -> tools.naming.erasedType.get(type) + ".class"));
		this.handlers.add(ArrayCoder.handler(tools));
		this.handlers.add(CollectionCoder.handler(tools));
		this.handlers.add(MapCoder.handler(tools));
		
		
		this.spacialHandlers.add(SpecialCoder.wildcard(tools));
		this.spacialHandlers.add(SpecialCoder.variableType(tools));
		this.spacialHandlers.add(SpecialCoder.intersection(tools));
		
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
		MaxResult res = this.handlers.stream().collect(MaxResult.collector(type));
		
		if (!res.maxCompat.isCompatible())
			return Optional.empty();
		
		if (res.serializers.size() == 1)
			return Optional.of(res.serializers.get(0));
		
		throw new IllegalArgumentException("The data handler of the '" + type.toString() + "' type couldn't be chosen: handler priorities are equal: " + res.serializers.stream().map(h -> h.getClass().getTypeName() + ":" + h.toString()).collect(Collectors.joining(", ")));
	}
	
	private Optional<ICoderHandler<SerializationCoder>> getSpecialHandler(TypeMirror type)
	{
		return this.spacialHandlers.stream().filter(entry -> entry.getCompatibilityFor(type).isCompatible()).findAny();
	}
	
	public static class MaxResult
	{
		private final TypeMirror type;
		private CoderCompatibility maxCompat = CoderCompatibility.INCOMPATIBLE;
		private final List<ICoderHandler<SerializationCoder>> serializers = new ArrayList<>();
		
		public MaxResult(TypeMirror type)
		{
			this.type = type;
		}

		public void accumulate(ICoderHandler<SerializationCoder> val)
		{
			CoderCompatibility compat = val.getCompatibilityFor(this.type);
			int cmp = compat.compareTo(this.maxCompat);
			if (cmp >= 0)
			{
				if (cmp > 0)
				{
					this.maxCompat = compat;
					this.serializers.clear();
				}
				this.serializers.add(val);
			}
		}
		
		public MaxResult combine(MaxResult other)
		{
			return this;
		}
		
		public static Collector<ICoderHandler<SerializationCoder>, MaxResult, MaxResult> collector(TypeMirror type)
		{
			return Collector.of(() -> new MaxResult(type), MaxResult::accumulate, MaxResult::combine);
		}
	}
	
}
