package fr.max2.packeta.utils;

import java.util.List;
import java.util.function.Consumer;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
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
	
	public static ArrayType asArrayType(TypeMirror type)
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
	
	public static WildcardType asWildcardType(TypeMirror type)
	{
		return WildcardTypeCaster.INSTANCE.visit(type);
	}
	
	private static enum WildcardTypeCaster implements DefaultTypeVisitor<WildcardType, Void>
	{
		INSTANCE;
		
		@Override
		public WildcardType visitWildcard(WildcardType t, Void p)
		{
			return t;
		}

		@Override
		public WildcardType visitDefault(TypeMirror t, Void p)
		{
			return null;
		}
		
	}
	
	public static TypeVariable asVariableType(TypeMirror type)
	{
		return VariableTypeCaster.INSTANCE.visit(type);
	}
	
	private static enum VariableTypeCaster implements DefaultTypeVisitor<TypeVariable, Void>
	{
		INSTANCE;
		
		@Override
		public TypeVariable visitTypeVariable(TypeVariable t, Void p)
		{
			return t;
		}

		@Override
		public TypeVariable visitDefault(TypeMirror t, Void p)
		{
			return null;
		}
		
	}
	
	public static IntersectionType asIntersectionType(TypeMirror type)
	{
		return IntersectionTypeCaster.INSTANCE.visit(type);
	}
	
	private static enum IntersectionTypeCaster implements DefaultTypeVisitor<IntersectionType, Void>
	{
		INSTANCE;
		
		@Override
		public IntersectionType visitIntersection(IntersectionType t, Void p)
		{
			return t;
		}

		@Override
		public IntersectionType visitDefault(TypeMirror t, Void p)
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
	
	public static void addTypeImports(TypeMirror type,  Consumer<String> imports)
	{
		TypeImporter.INSTANCE.visit(type, imports);
	}
	
	private static enum TypeImporter implements DefaultTypeVisitor<Void, Consumer<String>>, DefaultElementVisitor<Void, Consumer<String>>
	{
		INSTANCE;
		
		// TypeVisitor
		@Override
		public Void visit(TypeMirror t, Consumer<String> imports)
		{
			return imports == null ? this.visit(t) : t.accept(this, imports);
		}
		
		@Override
		public Void visit(TypeMirror t)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public Void visitArray(ArrayType t, Consumer<String> imports)
		{
			this.visit(t.getComponentType(), imports);
			return null;
		}
		
		@Override
		public Void visitDeclared(DeclaredType t, Consumer<String> imports)
		{
			this.visit(t.asElement(), imports);
			
			for (TypeMirror subType : t.getTypeArguments())
			{
				this.visit(subType, imports);
			}
			return null;
		}
		
		@Override
		public Void visitWildcard(WildcardType t, Consumer<String> imports)
		{
			TypeMirror extendsBound = t.getExtendsBound();
			TypeMirror superBound = t.getSuperBound();
			
			if (extendsBound != null) this.visit(extendsBound, imports);
			if (superBound != null) this.visit(superBound, imports);
			return null;
		}
		
		@Override
		public Void visitUnion(UnionType t, Consumer<String> imports)
		{
			for (TypeMirror subType : t.getAlternatives())
			{
				addTypeImports(subType, imports);
			}
			return null;
		}
		
		@Override
		public Void visitIntersection(IntersectionType t, Consumer<String> imports)
		{
			for (TypeMirror subType : t.getBounds())
			{
				addTypeImports(subType, imports);
			}
			return null;
		}
		
		@Override
		public Void visitDefault(TypeMirror t, Consumer<String> imports)
		{
			return null;
		}
		
		//ElementVisitor
		@Override
		public Void visit(Element e, Consumer<String> imports)
		{
			return imports == null ? this.visit(e) : e.accept(this, imports);
		}
		
		@Override
		public Void visit(Element e)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public Void visitType(TypeElement e, Consumer<String> imports)
		{
			String name = e.getQualifiedName().toString();
			if (!name.startsWith("java.lang"))
			{
				imports.accept(name);
			}
			return null;
		}
		
		@Override
		public Void visitDefault(Element e, Consumer<String> imports)
		{
			return null;
		}
		
	}
}
