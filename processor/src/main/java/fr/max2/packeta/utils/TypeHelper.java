package fr.max2.packeta.utils;

import java.util.List;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public class TypeHelper
{
	
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
