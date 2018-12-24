package fr.max2.packeta.api.network;


public enum DataType
{
	//TODO [all] test everything
	
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
	
	// Minecraft classes
	//NBT_COMPOUND,
	STACK,
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
	NBT_LIST,
	NBT_COMPOUND,
	NBT_ANY_NUMBER,
	NBT_ANY,
	//TODO [v1.1] BlockPos
	//TODO [v1.1] registry value, IByteSerialisable
	//TODO [v1.1] Entity + EntityPlayer
	//TODO [v1.1] other data/packet class
	
	// Special types
	WILDCARD,
	TYPE_VARIABLE,
	INTERSECTION,
	CUSTOM,
	DEFAULT;
	
}
