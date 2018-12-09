package fr.max2.packeta.utils;

import java.util.List;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;

public class NamingUtils
{
	
	public static String simpleName(String qualifiedName)
	{
		int ditIndex = qualifiedName.lastIndexOf('.') ;
		return ditIndex < 0 ? qualifiedName : qualifiedName.substring(ditIndex + 1);
	}
	
	public static String simpleTypeName(TypeMirror type)
	{
		return simpleTypeName(type, false);
	}
	
	public static String simpleTypeName(TypeMirror type, boolean simplifyGenerics)
	{
		StringBuilder builder = new StringBuilder();
		new TypeToString(simplifyGenerics, builder).visit(type);
		return builder.toString();
	}
	
	private static class TypeToString implements DefaultTypeVisitor<Void, Void>
	{
		private final boolean simplifyGenerics;
		private final StringBuilder builder;
		
		public TypeToString(boolean simplifyGenerics, StringBuilder builder)
		{
			this.simplifyGenerics = simplifyGenerics;
			this.builder = builder;
		}
		
		@Override
		public Void visitPrimitive(PrimitiveType t, Void p)
		{
			builder.append(t.getKind().name().toLowerCase());
			
			return null;
		}

		@Override
		public Void visitArray(ArrayType t, Void p)
		{
			t.getComponentType().accept(this, null);
			builder.append("[]");
			
			return null;
		}
		
		@Override
		public Void visitDeclared(DeclaredType t, Void p)
		{
			builder.append(t.asElement().getSimpleName());
			
			List<? extends TypeMirror> arguments = t.getTypeArguments();
			
			if (!arguments.isEmpty())
			{
				builder.append('<');
				if (!this.simplifyGenerics)
				{
					boolean first = true;
					for (TypeMirror arg : arguments)
					{
						if (!first)
						{
							builder.append(", ");
						}
						this.visit(arg);
						first = false;
					}
				}
				builder.append('>');
			}
			
			return null;
		}
		
		@Override
		public Void visitWildcard(WildcardType t, Void p)
		{
			TypeMirror extendsBound = t.getExtendsBound();
			TypeMirror superBound = t.getSuperBound();
			
			builder.append('*');
			if (extendsBound != null)
			{
				builder.append(" extends ");
				this.visit(extendsBound);
			}
			if (superBound != null)
			{
				builder.append(" super ");
				this.visit(superBound);
			}
			
			return null;
		}
		
		@Override
		public Void visitUnion(UnionType t, Void p)
		{
			boolean first = true;
			for (TypeMirror alt : t.getAlternatives())
			{
				if (!first)
				{
					builder.append(" | ");
				}
				this.visit(alt);
				first = false;
			}
			
			return null;
		}
		
		@Override
		public Void visitIntersection(IntersectionType t, Void p)
		{
			boolean first = true;
			for (TypeMirror bound : t.getBounds())
			{
				if (!first)
				{
					builder.append(" & ");
				}
				this.visit(bound);
				first = false;
			}
			
			return null;
		}
		
		@Override
		public Void visitDefault(TypeMirror t, Void p)
		{
			builder.append(t.toString());
			
			return null;
		}
	}
}
