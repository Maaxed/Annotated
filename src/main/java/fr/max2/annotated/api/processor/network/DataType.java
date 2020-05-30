package fr.max2.annotated.api.processor.network;

// TODO [v2.0] use static strings instead
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
	NBT_PRIMITIVE,
	NBT_CONCRETE,
	NBT_ABSTRACT,
	ENTITY_ID,
	PLAYER_ID,
	//TODO [v2.1] Entity by copy type
	//TODO [v2.1] IDynamicSerializable
	//TODO [v2.1] Container
	//TODO [v2.1] JsonDeserializer + JsonSerializer
	//TODO [v2.1] Optional
	//TODO [v2.2] other data/packet class

	//TODO [v2.1] test special types
	// Special types
	WILDCARD,
	TYPE_VARIABLE,
	INTERSECTION,
	CUSTOM,
	DEFAULT;
	
}
