package fr.max2.annotated.test.network.adapter;

import java.util.List;

import fr.max2.annotated.api.network.NetworkAdaptable;
import fr.max2.annotated.api.network.NetworkSerializable;
import net.minecraft.world.entity.Entity;

public class DependencyData
{
	@NetworkSerializable
	@NetworkAdaptable
	public static record Base(Entity test)
	{ }

	@NetworkSerializable
	@NetworkAdaptable
	public static record Child(Base base, int test)
	{ }

	@NetworkSerializable
	@NetworkAdaptable
	public static record GrandChild(Child base, OtherParent otherParent, int test)
	{ }

	@NetworkSerializable
	@NetworkAdaptable
	public static record OtherParent(List<Entity> test)
	{ }
}
