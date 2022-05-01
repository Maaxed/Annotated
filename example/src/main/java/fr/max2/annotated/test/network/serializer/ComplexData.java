package fr.max2.annotated.test.network.serializer;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

import fr.max2.annotated.api.network.NetworkSerializable;
import fr.max2.annotated.test.util.EnumTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;

@NetworkSerializable
public record ComplexData
(
	// Primitives
	byte myByte,
	Byte myByteObj,
	short myShort,
	Short myShortObj,
	int myInt,
	Integer myIntObj,
	long myLong,
	Long myLongObj,
	float myFloat,
	Float myFoatObj,
	double myDouble,
	Double myDoubleObj,
	boolean myBoolean,
	Boolean myBooleanObj,
	char myChar,
	Character myCharObj,

	// Simple types
	String myString,
	UUID myUUID,
	Date myDate,

	BlockPos myBlockPos,
	ResourceLocation myresLoc,
	ItemStack myItemStack,
	FluidStack myFluidStack,
	Component myTextComponent,
	BlockHitResult myBlockHitResult,

	AABB myAABB,
	BoundingBox myBoundingBox,
	ChunkPos myChunkPos,
	SectionPos mySectionPos,
	Vec3 myVec3,
	Vec3i myVec3i,
	Vec2 myVec2,
	Vector3d myVector3d,
	Vector3f myVector3f,
	Vector4f myVector4f,

	// Complex types
	Tag myTag,
	CompoundTag myCompoundTag,
	ListTag myListTag,

	Item myItem,
	Block myBlock,
	ItemStackHandler myNBTSerializable,

	EnumTest myEnum,

	// Arrays
	int[] myIntArray,
	Integer[] myIntObjArray,

	// Collections
	Collection<Integer> myIntCollection,
	List<Integer> myIntList,
	Set<Integer> myIntSet,
	TreeSet<Integer> myIntTreeSet,
	Collection<EnumTest> myEnumCollection,
	Collection<Collection<Integer>> myNestedIntCollection,

	// Maps
	Map<Integer, Float> myIntToFloatMap,
	LinkedHashMap<Integer, Float> myIntToFloatLinkedHashMap,

	// Optional
	Optional<Integer> myOptionalInt
)
{ }
