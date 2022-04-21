package fr.max2.annotated.test.network;

import fr.max2.annotated.api.network.NetworkSerializable;

@NetworkSerializable
public class SimpleData
{
	public final int myInt;

	public SimpleData(int myInt)
	{
		this.myInt = myInt;
	}
}
