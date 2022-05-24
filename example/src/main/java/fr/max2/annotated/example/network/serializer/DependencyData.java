package fr.max2.annotated.example.network.serializer;

import fr.max2.annotated.api.network.NetworkSerializable;

public class DependencyData
{
	@NetworkSerializable
	public static record Base(int test)
	{ }

	@NetworkSerializable
	public static record Child(Base base, int test)
	{ }

	@NetworkSerializable
	public static record GrandChild(Child base, OtherParent otherParent, int test)
	{ }

	@NetworkSerializable
	public static record OtherParent(int test)
	{ }
}
