package fr.max2.annotated.api.processor.network;

public class DataType
{
	private DataType() { }
	
	public static final String 
	// Integers
		BYTE = "BYTE",
		SHORT = "SHORT",
		INT = "INT",
		LONG = "LONG",
		
		// Floats
		FLOAT = "FLOAT",
		DOUBLE = "DOUBLE",
		
		// Other primitives
		BOOLEAN = "BOOLEAN",
		CHAR = "CHAR",
		ARRAY = "ARRAY",
		
		// Java classes
		STRING = "STRING",
		ENUM = "ENUM",
		COLLECTION = "COLLECTION",
		MAP = "MAP",
		UUID = "UUID",
		TIME = "TIME",
		
		// Minecraft classes
		BLOCK_POS = "BLOCK_POS",
		RESOURCE_LOCATION = "RESOURCE_LOCATION",
		ITEM_STACK = "ITEM_STACK",
		FLUID_STACK = "FLUID_STACK",
		TEXT_COMPONENT = "TEXT_COMPONENT",
		BLOCK_RAY_TRACE = "BLOCK_RAY_TRACE",
		REGISTRY_ENTRY = "REGISTRY_ENTRY",
		NBT_SERIALIZABLE = "NBT_SERIALIZABLE",
		NBT_PRIMITIVE = "NBT_PRIMITIVE",
		NBT_CONCRETE = "NBT_CONCRETE",
		NBT_ABSTRACT = "NBT_ABSTRACT",
		ENTITY_ID = "ENTITY_ID",
		PLAYER_ID = "PLAYER_ID",
		//TODO [v2.1] Entity by copy type
		//TODO [v2.1] IDynamicSerializable
		//TODO [v2.1] Container
		//TODO [v2.1] JsonDeserializer + JsonSerializer
		//TODO [v2.1] Optional
		//TODO [v2.2] other data/packet class
	
		//TODO [v2.1] test special types
		// Special types
		WILDCARD = "WILDCARD",
		TYPE_VARIABLE = "TYPE_VARIABLE",
		INTERSECTION = "INTERSECTION",
		CUSTOM = "CUSTOM",
		DEFAULT = "DEFAULT";
}
