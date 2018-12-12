package fr.max2.packeta.utils;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public class TypeHelper
{
	
	public static TypeElement asTypeElement(Element elem)
	{
		return TypeElementCaster.INSTANCE.visit(elem);
	}
	
	private static enum TypeElementCaster implements DefaultElementVisitor<TypeElement, Void>
	{
		INSTANCE;
		
		@Override
		public TypeElement visitType(TypeElement e, Void p)
		{
			return e;
		}
		
		@Override
		public TypeElement visitDefault(Element e, Void p)
		{
			return null;
		}
		
	}
	
	public static ArrayType asArrayElement(TypeMirror type)
	{
		return ArrayTypeCaster.INSTANCE.visit(type);
	}
	
	private static enum ArrayTypeCaster implements DefaultTypeVisitor<ArrayType, Void>
	{
		INSTANCE;
		
		@Override
		public ArrayType visitArray(ArrayType t, Void p)
		{
			return t;
		}

		@Override
		public ArrayType visitDefault(TypeMirror t, Void p)
		{
			return null;
		}
		
	}
	
	public static DeclaredType refineTo(TypeMirror type, TypeMirror base, Types types)
	{
		return type.accept(new DeclaredTypeRefiner(types), types.erasure(base));
	}
	
	private static class DeclaredTypeRefiner implements DefaultTypeVisitor<DeclaredType, TypeMirror>
	{
		private final Types types;
		
		public DeclaredTypeRefiner(Types types)
		{
			this.types = types;
		}

		@Override
		public DeclaredType visitDeclared(DeclaredType t, TypeMirror p)
		{
			if (types.isSameType(types.erasure(t), p))
			{
				return t;
			}
			
			List<? extends TypeMirror> superTypes = types.directSupertypes(t);
			
			for (TypeMirror parent : superTypes)
			{
				DeclaredType result = visit(parent, p);
				
				if (result != null)
				{
					return result;
				}
			}
			return null;
		}
		
		@Override
		public DeclaredType visitDefault(TypeMirror t, TypeMirror p)
		{
			return null;
		}
	}
}
