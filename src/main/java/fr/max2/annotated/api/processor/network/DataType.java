package fr.max2.annotated.api.processor.network;


public enum DataType
{
	// Integers
	BYTE,
	SHORT,
	INT,
	LONG,
	
	// Floats
	FLOAT,
	DOUBLE,
	
	// Other primitives
	BOOLEAN,
	CHAR,
	ARRAY,
	
	// Java classes
	STRING,
	ENUM,
	COLLECTION,
	MAP,
	UUID,
	TIME,
	
	// Minecraft classes
	BLOCK_POS,
	RESOURCE_LOCATION,
	ITEM_STACK,
	FLUID_STACK,
	TEXT_COMPONENT,
	BLOCK_RAY_TRACE,
	REGISTRY_ENTRY,
	NBT_SERIALIZABLE,
	NBT_END,
	NBT_BYTE,
	NBT_SHORT,
	NBT_INT,
	NBT_LONG,
	NBT_FLOAT,
	NBT_DOUBLE,
	NBT_STRING,
	NBT_BYTE_ARRAY,
	NBT_INT_ARRAY,
	NBT_LONG_ARRAY,
	NBT_LIST,
	NBT_COMPOUND,
	NBT_ANY_NUMBER,
	NBT_ANY,
	//TODO [v2.0] Entity + EntityPlayer
	//TODO [v2.2] other data/packet class
	//TODO [v2.1] IDynamicSerializable, JsonDeserializer + JsonSerializer

	//TODO [v2.1] test special types
	// Special types
	WILDCARD,
	TYPE_VARIABLE,
	INTERSECTION,
	CUSTOM,
	DEFAULT;
	
}