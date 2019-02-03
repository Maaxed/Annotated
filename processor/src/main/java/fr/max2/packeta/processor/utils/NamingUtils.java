package fr.max2.packeta.processor.utils;

import java.util.List;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;

public class NamingUtils
{
	private NamingUtils() { }
	
	
	public static String simpleName(String qualifiedName)
	{
		int dotIndex = qualifiedName.lastIndexOf('.') ;
		return dotIndex < 0 ? qualifiedName : qualifiedName.substring(dotIndex + 1);
	}
	
	public static String simpleTypeName(TypeMirror type)
	{
		return simpleTypeName(type, false);
	}
	
	public static String simplifiedTypeName(TypeMirror type)
	{
		return simpleTypeName(type, false);
	}
	
	public static String simpleTypeName(TypeMirror type, boolean simplifyGenerics)
	{
		StringBuilder builder = new StringBuilder();
		TypeToString visitor = simplifyGenerics ? TypeToString.SIMPLIFIED : TypeToString.FULL;
		visitor.visit(type, builder);
		return builder.toString();
	}
	
	private enum TypeToString implements DefaultTypeVisitor<Void, StringBuilder>
	{
		FULL(false),
		SIMPLIFIED(true);
		
		private final boolean simplifyGenerics;
		
		private TypeToString(boolean simplifyGenerics)
		{
			this.simplifyGenerics = simplifyGenerics;
		}
		
		@Override
		public Void visitPrimitive(PrimitiveType t, StringBuilder builder)
		{
			builder.append(t.getKind().name().toLowerCase());
			
			return null;
		}

		@Override
		public Void visitArray(ArrayType t, StringBuilder builder)
		{
			this.visit(t.getComponentType(), builder);
			builder.append("[]");
			
			return null;
		}
		
		@Override
		public Void visitDeclared(DeclaredType t, StringBuilder builder)
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
						this.visit(arg, builder);
						first = false;
					}
				}
				builder.append('>');
			}
			
			return null;
		}
		
		@Override
		public Void visitTypeVariable(TypeVariable t, StringBuilder builder)
		{
			TypeMirror extendsBound = t.getUpperBound();
			TypeMirror superBound = t.getLowerBound();
			
			builder.append(t.asElement().getSimpleName());
			if (extendsBound != null)
			{
				builder.append(" extends ");
				this.visit(extendsBound, builder);
			}
			if (superBound != null)
			{
				builder.append(" super ");
				this.visit(superBound, builder);
			}
			
			return null;
		}
		
		@Override
		public Void visitWildcard(WildcardType t, StringBuilder builder)
		{
			TypeMirror extendsBound = t.getExtendsBound();
			TypeMirror superBound = t.getSuperBound();
			
			builder.append('*');
			if (extendsBound != null)
			{
				builder.append(" extends ");
				this.visit(extendsBound, builder);
			}
			if (superBound != null)
			{
				builder.append(" super ");
				this.visit(superBound, builder);
			}
			
			return null;
		}		
		
		@Override
		public Void visitUnion(UnionType t, StringBuilder builder)
		{
			boolean first = true;
			for (TypeMirror alt : t.getAlternatives())
			{
				if (!first)
				{
					builder.append(" | ");
				}
				this.visit(alt, builder);
				first = false;
			}
			
			return null;
		}
		
		@Override
		public Void visitIntersection(IntersectionType t, StringBuilder builder)
		{
			boolean first = true;
			for (TypeMirror bound : t.getBounds())
			{
				if (!first)
				{
					builder.append(" & ");
				}
				this.visit(bound, builder);
				first = false;
			}
			
			return null;
		}
		
		@Override
		public Void visitDefault(TypeMirror t, StringBuilder builder)
		{
			builder.append(t.toString());
			
			return null;
		}
	}
}