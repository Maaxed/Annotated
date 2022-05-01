package fr.max2.annotated.test.network.adapter;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import fr.max2.annotated.api.network.NetworkAdaptable;
import fr.max2.annotated.api.network.NetworkSerializable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

@NetworkSerializable
@NetworkAdaptable
public record ComplexData
(
	int myInt,
	// Arrays
	int[] myIntArray,
	Integer[] myIntObjArray,

	// Collections
	Collection<Integer> myIntCollection,
	Collection<Entity> myEntityCollection,
	List<Entity> myEntityList,
	Set<Entity> myEntitySet,
	TreeSet<Entity> myEntityTreeSet,
	Collection<Collection<Entity>> myNestedEntityCollection,

	// Maps
	Map<Integer, Entity> myIntToEntityMap,
	LinkedHashMap<Player, Entity> myPlayerToEntityLinkedHashMap,

	// Optional
	Optional<Entity> myOptionalEntity
)
{ }
