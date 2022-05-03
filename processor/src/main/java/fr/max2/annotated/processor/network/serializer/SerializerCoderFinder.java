package fr.max2.annotated.processor.network.serializer;

import java.util.Optional;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.api.network.NetworkAdaptable;
import fr.max2.annotated.processor.coder.CoderFinder;
import fr.max2.annotated.processor.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.util.ClassRef;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.CoderException;
import fr.max2.annotated.processor.util.exceptions.IncompatibleTypeException;

public class SerializerCoderFinder extends CoderFinder<SerializationCoder>
{
	// TODO find available serializers using Filer.getResource / classloader / JavaFileManager.getFileForInput / ServiceLoader (see ToolProvider)
	public SerializerCoderFinder(ProcessingTools tools)
	{
		super(tools, "fr.max2.annotated.lib.network.serializer.NetworkSerializer");
		
		// Register primitives
		for (String primitiveName : new String[] {"Byte", "Short", "Int", "Long", "Float", "Double", "Boolean","Char"})
		{
			this.handlers.add(PrimitiveCoder.handler(tools, primitiveName));
		}
		
		scanClass("fr.max2.annotated.lib.network.serializer.SimpleClassSerializer");
		scanClass("fr.max2.annotated.lib.network.serializer.VectorClassSerializer");

		scanClass("fr.max2.annotated.lib.network.serializer.TagSerializer");
		this.handlers.add(NBTCoder.concreteHandler(tools));

		this.handlers.add(GenericCoder.handler(tools, ClassRef.REGISTRY_ENTRY, "fr.max2.annotated.lib.network.serializer.RegistryEntrySerializer", type -> tools.naming.erasedType.get(type) + ".class")); // TODO [v3.1] Find a good way to get the registry from the type
		this.handlers.add(NBTSerializableCoder.handler(tools));

		scanClass("fr.max2.annotated.lib.network.serializer.PrimitiveArraySerializer");
		this.handlers.add(GenericCoder.handler(tools, "java.lang.Enum", "fr.max2.annotated.lib.network.serializer.EnumSerializer", type -> tools.naming.erasedType.get(type) + ".class"));
		this.handlers.add(ArrayCoder.handler(tools));
		this.handlers.add(CollectionCoder.handler(tools));
		this.handlers.add(MapCoder.handler(tools));
		TypeMirror optionalType = tools.elements.getTypeElement(Optional.class.getCanonicalName()).asType();
		this.handlers.add(GenericCoder.handler(tools, Optional.class.getCanonicalName(), "fr.max2.annotated.lib.network.serializer.OptionalSerializer", (fieldType, builder) -> builder.addCoders(1, optionalType)));
		
		this.handlers.add(GeneratedCoder.handler(tools));
		
		this.spacialHandlers.add(SpecialCoder.wildcard(tools));
		this.spacialHandlers.add(SpecialCoder.variableType(tools));
		this.spacialHandlers.add(SpecialCoder.intersection(tools));
		
		//TODO [v2.1] DamageSource, VoxelShape
		//TODO [v2.1] Rotations, Size2i, Vec2f, GlobalPos
		//TODO [v2.1] Entity by copy type
		//TODO [v3.0] Entity by id + DimensionType
		//TODO [v3.0] DimensionType
		//TODO [v3.0] using Codex
		//TODO [v2.1] IDynamicSerializable
		//TODO [v2.1] TileEntity
		//TODO [v2.1] Container
		//TODO [v2.1] JsonDeserializer + JsonSerializer
		//TODO [v2.2] custom data class
	}
	
	@Override
	protected ICoderHandler<SerializationCoder> constantToHandler(VariableElement field, DeclaredType type)
	{
		this.tools.types.requireTypeArguments(type, 1);
		return SimpleCoder.handler(this.tools, this.tools.types.erasure(type.getTypeArguments().get(0)), field);
	}
	
	@Override
	protected void onFailedToFindCoder(TypeMirror type) throws CoderException
	{
		if (this.tools.adapterCoders.getCoderOrNull(type) != null)
			throw new IncompatibleTypeException("No data coder found to process the type '" + type.toString() + "', but an adapter is available. Consider adding the " + NetworkAdaptable.class.getName() + " annotation to fix the issue");
		
		super.onFailedToFindCoder(type);
	}
}
