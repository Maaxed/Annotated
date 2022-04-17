package fr.max2.annotated.api.network;

/**
 * This class defines the constants corresponding to the valid values for the {@code "type"} property.
 * @see DataProperties
 */
public class DataType
{
	private DataType() { }

	// Integers
	
	/**
	 * This type is applicable for: any type that can be implicitly casted to {@code byte}.
	 * <p>
	 * This is the default serializer for:
	 * <ul>
	 * <li>{@code byte}
	 * <li>{@link java.lang.Byte}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved to a single byte
	 */
	public static final String BYTE = "BYTE";
	/**
	 * This type is applicable for: any type that can be implicitly casted to {@code Short}.
	 * <p>
	 * This is the default serializer for:
	 * <ul>
	 * <li>{@code short}
	 * <li>{@link java.lang.Short}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved to a short (2 bytes)
	 */
	public static final String SHORT = "SHORT";
	/**
	 * This type is applicable for: any type that can be implicitly casted to {@code int}.
	 * <p>
	 * This is the default serializer for:
	 * <ul>
	 * <li>{@code int}
	 * <li>{@link java.lang.Integer}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved to an integer (4 bytes)
	 */
	public static final String INT = "INT";
	/**
	 * This type is applicable for: any type that can be implicitly casted to {@code long}.
	 * <p>
	 * This is the default serializer for:
	 * <ul>
	 * <li>{@code long}
	 * <li>{@link java.lang.Long}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved to a long (8 bytes)
	 */
	public static final String LONG = "LONG";
	
	// Floats

	/**
	 * This type is applicable for: any type that can be implicitly casted to {@code float}.
	 * <p>
	 * This is the default serializer for:
	 * <ul>
	 * <li>{@code float}
	 * <li>{@link java.lang.Float}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved to a float (4 bytes)
	 */
	public static final String FLOAT = "FLOAT";
	/**
	 * This type is applicable for: any type that can be implicitly casted to {@code double}.
	 * <p>
	 * This is the default serializer for:
	 * <ul>
	 * <li>{@code double}
	 * <li>{@link java.lang.Double}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved to a double (8 bytes)
	 */
	public static final String DOUBLE = "DOUBLE";
	
	// Other primitives
	
	/**
	 * This type is applicable for: any type that can be implicitly casted to {@code boolean}.
	 * <p>
	 * This is the default serializer for:
	 * <ul>
	 * <li>{@code boolean}
	 * <li>{@link java.lang.Boolean}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved to a boolean (1 byte)
	 */
	public static final String BOOLEAN = "BOOLEAN";
	/**
	 * This type is applicable for: any type that can be implicitly casted to {@code char}.
	 * <p>
	 * This is the default serializer for:
	 * <ul>
	 * <li>{@code char}
	 * <li>{@link java.lang.Character}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved to a char (2 bytes)
	 */
	public static final String CHAR = "CHAR";
	/**
	 * This type is applicable for: any array of a serializable type.
	 * <p>
	 * This is the default serializer for:
	 * <ul>
	 * <li>Every array
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved as an array of elements
	 */
	public static final String ARRAY = "ARRAY";
	
	// Java classes
	/**
	 * This type is applicable for: {@link java.lang.String}.
	 * <p>
	 * This is the default serializer for:
	 * <ul>
	 * <li>{@link java.lang.String}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved to an UTF-8 string
	 * 
	 * <p>
	 * Valid properties:
	 * <ul>
	 * <li>{@code "maxLength"}: the maximum size of the encoded string. Default : 32767.
	 * </ul>
	 */
	public static final String STRING = "STRING";
	/**
	 * This type is applicable for: every enums.
	 * <p>
	 * This is the default serializer for:
	 * <ul>
	 * <li>Every enums
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved to an integer representing the cardinal of the enum
	 */
	public static final String ENUM = "ENUM";
	/**
	 * This type is applicable for: any class implementing or any interface extending {@link java.util.Collection}.
	 * <p>
	 * This is the default serializer for:
	 * <ul>
	 * <li>{@link java.util.Collection}
	 * <li>{@link java.util.List}
	 * <li>{@link java.util.Set}
	 * <li>any class or interface extending {@link java.util.Collection}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved as a collection of elements
	 * 
	 * <p>
	 * Valid properties:
	 * <ul>
	 * <li>{@code "impl"}: the class to use as an implementation for the type. Default: {@link java.util.ArrayList} for a List and {@link java.util.HashSet} for a Set.
	 * <li>{@code "content"}: a property group to configure the way the content of the collection should be serialized.
	 * 						  The property {@code "content.type"} defines the serializer type that will be used for the content of the collection.
	 * </ul>
	 */
	public static final String COLLECTION = "COLLECTION";
	/**
	 * This type is applicable for: any class implementing or any interface extending {@link java.util.Map}.
	 * <p>
	 * This is the default serializer for:
	 * <ul>
	 * <li>{@link java.util.Map}
	 * <li>any class or interface extending {@link java.util.Map}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved as a map of elements
	 * 
	 * <p>
	 * Valid properties:
	 * <ul>
	 * <li>{@code "impl"}: the class to use as an implementation for the type. Default: {@link java.util.HashMap}.
	 * <li>{@code "keys"}: a property group to configure the way the keys of the map should be serialized.
	 * 						  The property {@code "keys.type"} defines the serializer type that will be used for the keys of the map.
	 * <li>{@code "values"}: a property group to configure the way the values of the map should be serialized.
	 * </ul>
	 */
	public static final String MAP = "MAP";
	/**
	 * This type is applicable for: {@link java.util.UUID}.
	 * <p>
	 * This is the default serializer for:
	 * <ul>
	 * <li>{@link java.util.UUID}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved to a pair of longs (16 bytes)
	 */
	public static final String UUID = "UUID";
	/**
	 * This type is applicable for: {@link java.util.Date}.
	 * <p>
	 * This is the default serializer for:
	 * <ul>
	 * <li>{@link java.util.Date}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved to a long (8 bytes)
	 */
	public static final String TIME = "TIME";
	
	// Minecraft classes
	
	/**
	 * This type is applicable for: {@link net.minecraft.util.math.BlockPos}.
	 * <p>
	 * This is the default serializer for:
	 * <ul>
	 * <li>{@link net.minecraft.util.math.BlockPos}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved to a long (8 bytes)
	 */
	public static final String BLOCK_POS = "BLOCK_POS";
	/**
	 * This type is applicable for: {@link net.minecraft.util.ResourceLocation}.
	 * <p>
	 * This is the default serializer for:
	 * <ul>
	 * <li>{@link net.minecraft.util.ResourceLocation}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved to a single string
	 */
	public static final String RESOURCE_LOCATION = "RESOURCE_LOCATION";
	/**
	 * This type is applicable for: {@link net.minecraft.item.ItemStack}.
	 * <p>
	 * This is the default serializer for:
	 * <ul>
	 * <li>{@link net.minecraft.item.ItemStack}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved to an ItemStack
	 */
	public static final String ITEM_STACK = "ITEM_STACK";
	/**
	 * This type is applicable for: {@link net.minecraftforge.fluids.FluidStack}.
	 * <p>
	 * This is the default serializer for:
	 * <ul>
	 * <li>{@link net.minecraftforge.fluids.FluidStack}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved to a FluidStack
	 */
	public static final String FLUID_STACK = "FLUID_STACK";
	/**
	 * This type is applicable for: {@link net.minecraft.util.text.ITextComponent}.
	 * <p>
	 * This is the default serializer for:
	 * <ul>
	 * <li>{@link net.minecraft.util.text.ITextComponent}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved to a String
	 */
	public static final String TEXT_COMPONENT = "TEXT_COMPONENT";
	/**
	 * This type is applicable for: {@link net.minecraft.util.math.BlockRayTraceResult}.
	 * <p>
	 * This is the default serializer for:
	 * <ul>
	 * <li>{@link net.minecraft.util.math.BlockRayTraceResult}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved to a BlockRayTraceResult
	 */
	public static final String BLOCK_RAY_TRACE = "BLOCK_RAY_TRACE";
	/**
	 * This type is applicable for: any class implementing or any interface extending {@link net.minecraftforge.registries.IForgeRegistryEntry}.
	 * <p>
	 * This is the default type for:
	 * <ul>
	 * <li>{@link net.minecraft.item.Item}
	 * <li>{@link net.minecraft.block.Block}
	 * <li>any class or interface extending {@link net.minecraftforge.registries.IForgeRegistryEntry}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved as a single int (4 bytes)
	 */
	public static final String REGISTRY_ENTRY = "REGISTRY_ENTRY";
	/**
	 * This type is applicable for: any class implementing or any interface extending {@link net.minecraftforge.common.util.INBTSerializable}.
	 * <p>
	 * This is the default type for:
	 * <ul>
	 * <li>any class or interface extending {@link net.minecraftforge.common.util.INBTSerializable}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved as a INBT
	 * 
	 * <p>
	 * Valid properties:
	 * <ul>
	 * <li>{@code "impl"}: the class to use as an implementation for the type.
	 * <li>{@code "nbt"}: a property group to configure the way the serialized nbt should be serialized.
	 * 						  The property {@code "content.type"} defines the serializer type that will be used for serialized nbt.
	 * </ul>
	 */
	public static final String NBT_SERIALIZABLE = "NBT_SERIALIZABLE";
	/**
	 * This type is applicable for: any concrete class implementing {@link net.minecraft.nbt.INBT}.
	 * <p>
	 * This is the default type for:
	 * <ul>
	 * <li>{@link net.minecraft.nbt.CompoundNBT}
	 * <li>{@link net.minecraft.nbt.ListNBT}
	 * <li>{@link net.minecraft.nbt.StringNBT}
	 * <li>{@link net.minecraft.nbt.IntNBT}
	 * <li>any concrete class implementing {@link net.minecraft.nbt.INBT}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved as a nbt object
	 */
	public static final String NBT_CONCRETE = "NBT_CONCRETE";
	/**
	 * This type is applicable for: any class implementing or any interface extending {@link net.minecraft.nbt.INBT}.
	 * <p>
	 * This is the default type for:
	 * <ul>
	 * <li>{@link net.minecraft.nbt.INBT}
	 * <li>{@link net.minecraft.nbt.CollectionNBT}
	 * <li>{@link net.minecraft.nbt.NumberNBT}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved as a byte and a nbt object
	 */
	public static final String NBT_ABSTRACT = "NBT_ABSTRACT";
	/**
	 * This type is applicable for: any class extending {@link net.minecraft.entity.Entity}.
	 * <p>
	 * This is the default type for:
	 * <ul>
	 * <li>{@link net.minecraft.entity.Entity}
	 * <li>{@link net.minecraft.entity.item.BoatEntity}
	 * <li>any class extending {@link net.minecraft.entity.Entity} but not {@link net.minecraft.entity.player.PlayerEntity}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved as an integer (4 bytes)
	 */
	public static final String ENTITY_ID = "ENTITY_ID";
	/**
	 * This type is applicable for: any class extending {@link net.minecraft.entity.player.PlayerEntity}.
	 * <p>
	 * This is the default type for:
	 * <ul>
	 * <li>{@link net.minecraft.entity.player.PlayerEntity}
	 * <li>any class extending {@link net.minecraft.entity.player.PlayerEntity}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved as a UUID (16 bytes)
	 */
	public static final String PLAYER_ID = "PLAYER_ID";
	/**
	 * This type is applicable for: {@link net.minecraft.util.math.AxisAlignedBB}.
	 * <p>
	 * This is the default serializer for:
	 * <ul>
	 * <li>{@link net.minecraft.util.math.AxisAlignedBB}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved to 6 doubles (48 bytes)
	 */
	public static final String AXIS_ALIGNED_BB = "AXIS_ALIGNED_BB";
	/**
	 * This type is applicable for: {@link net.minecraft.util.math.MutableBoundingBox}.
	 * <p>
	 * This is the default serializer for:
	 * <ul>
	 * <li>{@link net.minecraft.util.math.MutableBoundingBox}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved to 6 integers (24 bytes)
	 */
	public static final String MUTABLE_BB = "MUTABLE_BB";
	/**
	 * This type is applicable for: {@link net.minecraft.util.math.ChunkPos}.
	 * <p>
	 * This is the default serializer for:
	 * <ul>
	 * <li>{@link net.minecraft.util.math.ChunkPos}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved to 2 integers (8 bytes)
	 */
	public static final String CHUNK_POS = "CHUNK_POS";
	/**
	 * This type is applicable for: {@link net.minecraft.util.math.SectionPos}.
	 * <p>
	 * This is the default serializer for:
	 * <ul>
	 * <li>{@link net.minecraft.util.math.SectionPos}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved to 3 integers (12 bytes)
	 */
	public static final String SECTION_POS = "SECTION_POS";
	/**
	 * This type is applicable for: {@link net.minecraft.util.math.Vec3d}.
	 * <p>
	 * This is the default serializer for:
	 * <ul>
	 * <li>{@link net.minecraft.util.math.Vec3d}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved to 3 doubles (24 bytes)
	 */
	public static final String VECTOR_3D = "VECTOR_3D";
	/**
	 * This type is applicable for: {@link net.minecraft.util.math.Vec3i}.
	 * <p>
	 * This is the default serializer for:
	 * <ul>
	 * <li>{@link net.minecraft.util.math.Vec3i}
	 * </ul>
	 * 
	 * <p>
	 * The data will be saved to 3 integers (12 bytes)
	 */
	public static final String VECTOR_3I = "VECTOR_3I";
	
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
	
	// Special types
	
	/**
	 * This type is applicable for: any type.
	 * <p>
	 * This is the default serializer for: nothing
	 * 
	 * <p>
	 * This data will be saved using the default serializer for the type
	 * 
	 * <p>
	 * This type is the one used when no time is specified using the {@code "type"} property.
	 */
	public static final String DEFAULT = "DEFAULT";
}
