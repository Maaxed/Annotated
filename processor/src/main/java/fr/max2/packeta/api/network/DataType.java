package fr.max2.packeta.api.network;


public enum DataType
{
	// Integers
	BYTE,
	SHORT,
	INT,
	LONG,
	
	//Floats
	FLOAT,
	DOUBLE,
	
	// Other primitives
	BOOLEAN,
	CHAR,
	ARRAY,
	
	// Classes
	STRING,
	ENUM,
	UUID,
	NBT_COMPOUND,
	STACK,
	COLLECTION,
	//TODO INBTSerializable, IByteSerialisable
	//TODO Entity + EntityPlayer
	//TODO other data/packet class
	
	// Special types
	CUSTOM,
	DEFAULT;
	
}
