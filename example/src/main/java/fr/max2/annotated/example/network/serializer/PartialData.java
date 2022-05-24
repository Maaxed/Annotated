package fr.max2.annotated.example.network.serializer;

import fr.max2.annotated.api.network.IgnoreField;
import fr.max2.annotated.api.network.NetworkSerializable;
import fr.max2.annotated.api.network.IncludeField;
import fr.max2.annotated.api.network.SelectionMode;

public class PartialData
{
	@NetworkSerializable(serializerClassName = "NoneSerializer", fieldSelectionMode = SelectionMode.NONE)
	public static class TestNone
	{
		public int testPublic;
		protected int testProtected;
		int testPackage;
		private int testPrivate;

		public TestNone(int testPublic, int testProtected, int testPackage, int testPrivate)
		{
			this.testPublic = testPublic;
			this.testProtected = testProtected;
			this.testPackage = testPackage;
			this.testPrivate = testPrivate;
		}

		public TestNone(int testPublic, int testProtected, int testPackage)
		{
			this.testPublic = testPublic;
			this.testProtected = testProtected;
			this.testPackage = testPackage;
		}

		public TestNone(int testPublic, int testProtected)
		{
			this.testPublic = testPublic;
			this.testProtected = testProtected;
		}

		public TestNone(int testPublic)
		{
			this.testPublic = testPublic;
		}
		
		public TestNone()
		{ }
	}
	
	@NetworkSerializable(serializerClassName = "PublicSerializer", fieldSelectionMode = SelectionMode.PUBLIC)
	public static class TestPublic
	{
		public int testPublic;
		protected int testProtected;
		int testPackage;
		private int testPrivate;

		public TestPublic(int testPublic, int testProtected, int testPackage, int testPrivate)
		{
			this.testPublic = testPublic;
			this.testProtected = testProtected;
			this.testPackage = testPackage;
			this.testPrivate = testPrivate;
		}

		public TestPublic(int testPublic, int testProtected, int testPackage)
		{
			this.testPublic = testPublic;
			this.testProtected = testProtected;
			this.testPackage = testPackage;
		}

		public TestPublic(int testPublic, int testProtected)
		{
			this.testPublic = testPublic;
			this.testProtected = testProtected;
		}

		public TestPublic(int testPublic)
		{
			this.testPublic = testPublic;
		}
		
		public TestPublic()
		{ }
	}
	
	@NetworkSerializable(serializerClassName = "AllSerializer", fieldSelectionMode = SelectionMode.ALL)
	public static class TestAll
	{
		public int testPublic;
		protected int testProtected;
		int testPackage;
		@IncludeField(getter = "getPrivate")
		private int testPrivate;

		public TestAll(int testPublic, int testProtected, int testPackage, int testPrivate)
		{
			this.testPublic = testPublic;
			this.testProtected = testProtected;
			this.testPackage = testPackage;
			this.testPrivate = testPrivate;
		}

		public TestAll(int testPublic, int testProtected, int testPackage)
		{
			this.testPublic = testPublic;
			this.testProtected = testProtected;
			this.testPackage = testPackage;
		}

		public TestAll(int testPublic, int testProtected)
		{
			this.testPublic = testPublic;
			this.testProtected = testProtected;
		}

		public TestAll(int testPublic)
		{
			this.testPublic = testPublic;
		}
		
		public TestAll()
		{ }
		
		public int getPrivate()
		{
			return this.testPrivate;
		}
	}
	
	@NetworkSerializable(serializerClassName = "IgnoreSerializer", fieldSelectionMode = SelectionMode.ALL)
	public static class TestIgnore
	{
		public int testPublic;
		protected int testProtected;
		int testPackage;
		@IgnoreField
		private int testPrivate;

		public TestIgnore(int testPublic, int testProtected, int testPackage, int testPrivate)
		{
			this.testPublic = testPublic;
			this.testProtected = testProtected;
			this.testPackage = testPackage;
			this.testPrivate = testPrivate;
		}

		public TestIgnore(int testPublic, int testProtected, int testPackage)
		{
			this.testPublic = testPublic;
			this.testProtected = testProtected;
			this.testPackage = testPackage;
		}

		public TestIgnore(int testPublic, int testProtected)
		{
			this.testPublic = testPublic;
			this.testProtected = testProtected;
		}

		public TestIgnore(int testPublic)
		{
			this.testPublic = testPublic;
		}
		
		public TestIgnore()
		{ }
	}
	
	@NetworkSerializable(serializerClassName = "IncludeSerializer", fieldSelectionMode = SelectionMode.NONE)
	public static class TestInclude
	{
		@IncludeField
		public int testPublic;
		protected int testProtected;
		int testPackage;
		private int testPrivate;

		public TestInclude(int testPublic, int testProtected, int testPackage, int testPrivate)
		{
			this.testPublic = testPublic;
			this.testProtected = testProtected;
			this.testPackage = testPackage;
			this.testPrivate = testPrivate;
		}

		public TestInclude(int testPublic, int testProtected, int testPackage)
		{
			this.testPublic = testPublic;
			this.testProtected = testProtected;
			this.testPackage = testPackage;
		}

		public TestInclude(int testPublic, int testProtected)
		{
			this.testPublic = testPublic;
			this.testProtected = testProtected;
		}

		public TestInclude(int testPublic)
		{
			this.testPublic = testPublic;
		}
		
		public TestInclude()
		{ }
	}
}
