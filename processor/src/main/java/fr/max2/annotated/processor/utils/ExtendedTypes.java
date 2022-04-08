package fr.max2.annotated.processor.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Types;

import fr.max2.annotated.processor.network.model.IImportClassBuilder;

public class ExtendedTypes implements Types
{
	private final ProcessingTools tools;
	private final Types base;
	
	ExtendedTypes(ProcessingTools tools, Types base)
	{
		this.tools = tools;
		this.base = base;
	}
	
	public PrimitiveType asPrimitive(TypeMirror type)
	{
		return type == null ? null : PrimitiveTypeCaster.INSTANCE.visit(type);
	}
	
	private enum PrimitiveTypeCaster implements DefaultTypeVisitor<PrimitiveType, Void>
	{
		INSTANCE;
		
		@Override
		public PrimitiveType visitPrimitive(PrimitiveType t, Void p)
		{
			return t;
		}

		@Override
		public PrimitiveType visitDefault(TypeMirror t, Void p)
		{
			return null;
		}
		
	}
	
	public ArrayType asArrayType(TypeMirror type)
	{
		return type == null ? null : ArrayTypeCaster.INSTANCE.visit(type);
	}
	
	private enum ArrayTypeCaster implements DefaultTypeVisitor<ArrayType, Void>
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
	
	public WildcardType asWildcardType(TypeMirror type)
	{
		return type == null ? null : WildcardTypeCaster.INSTANCE.visit(type);
	}
	
	private enum WildcardTypeCaster implements DefaultTypeVisitor<WildcardType, Void>
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
	
	public TypeVariable asVariableType(TypeMirror type)
	{
		return type == null ? null : VariableTypeCaster.INSTANCE.visit(type);
	}
	
	private enum VariableTypeCaster implements DefaultTypeVisitor<TypeVariable, Void>
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
	
	public IntersectionType asIntersectionType(TypeMirror type)
	{
		return type == null ? null : IntersectionTypeCaster.INSTANCE.visit(type);
	}
	
	private enum IntersectionTypeCaster implements DefaultTypeVisitor<IntersectionType, Void>
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
	
	public DeclaredType refineTo(TypeMirror type, TypeMirror base)
	{
		return type.accept(this.declaredTypeRefiner, this.tools.types.erasure(base));
	}
	
	private final TypeVisitor<DeclaredType, TypeMirror> declaredTypeRefiner = new DefaultTypeVisitor<>()
	{
		@Override
		public DeclaredType visitDeclared(DeclaredType t, TypeMirror p)
		{
			if (ExtendedTypes.this.tools.types.isSameType(ExtendedTypes.this.tools.types.erasure(t), p))
			{
				return t;
			}
			
			List<? extends TypeMirror> superTypes = ExtendedTypes.this.tools.types.directSupertypes(t);
			
			for (TypeMirror parent : superTypes)
			{
				DeclaredType result = this.visit(parent, p);
				
				if (result != null)
				{
					return result;
				}
			}
			return null;
		}
		
		@Override
		public DeclaredType visitTypeVariable(TypeVariable t, TypeMirror p)
		{
			return this.visit(t.getUpperBound(), p);
		}
		
		@Override
		public DeclaredType visitWildcard(WildcardType t, TypeMirror p)
		{
			TypeMirror extendsBound = t.getExtendsBound();
			return extendsBound == null ? null : this.visit(extendsBound, p);
		}
		
		@Override
		public DeclaredType visitIntersection(IntersectionType t, TypeMirror p)
		{
			for (TypeMirror subType : t.getBounds())
			{
				DeclaredType res = this.visit(subType, p);
				if (res != null)
					return res;
			}
			return null;
		}
		
		@Override
		public DeclaredType visitDefault(TypeMirror t, TypeMirror p)
		{
			return null;
		}
	};
	
	public void provideTypeImports(TypeMirror type, IImportClassBuilder<?> imports)
	{
		TypeImporter.INSTANCE.visit(type, imports);
	}
	
	private enum TypeImporter implements DefaultTypeVisitor<Void, IImportClassBuilder<?>>, DefaultElementVisitor<Void, IImportClassBuilder<?>>
	{
		INSTANCE;
		
		// TypeVisitor
		@Override
		public Void visit(TypeMirror t, IImportClassBuilder<?> imports)
		{
			return imports == null ? this.visit(t) : t.accept(this, imports);
		}
		
		@Override
		public Void visit(TypeMirror t)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public Void visitArray(ArrayType t, IImportClassBuilder<?> imports)
		{
			this.visit(t.getComponentType(), imports);
			return null;
		}
		
		@Override
		public Void visitDeclared(DeclaredType t, IImportClassBuilder<?> imports)
		{
			this.visit(t.asElement(), imports);
			
			for (TypeMirror subType : t.getTypeArguments())
			{
				this.visit(subType, imports);
			}
			return null;
		}
		
		@Override
		public Void visitWildcard(WildcardType t, IImportClassBuilder<?> imports)
		{
			TypeMirror extendsBound = t.getExtendsBound();
			TypeMirror superBound = t.getSuperBound();
			
			if (extendsBound != null) this.visit(extendsBound, imports);
			if (superBound != null) this.visit(superBound, imports);
			return null;
		}
		
		@Override
		public Void visitUnion(UnionType t, IImportClassBuilder<?> imports)
		{
			for (TypeMirror subType : t.getAlternatives())
			{
				this.visit(subType, imports);
			}
			return null;
		}
		
		@Override
		public Void visitIntersection(IntersectionType t, IImportClassBuilder<?> imports)
		{
			for (TypeMirror subType : t.getBounds())
			{
				this.visit(subType, imports);
			}
			return null;
		}
		
		@Override
		public Void visitDefault(TypeMirror t, IImportClassBuilder<?> imports)
		{
			return null;
		}
		
		// ElementVisitor
		@Override
		public Void visit(Element e, IImportClassBuilder<?> imports)
		{
			return imports == null ? this.visit(e) : e.accept(this, imports);
		}
		
		@Override
		public Void visit(Element e)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public Void visitType(TypeElement e, IImportClassBuilder<?> imports)
		{
			imports.addImport(e);
			return null;
		}
		
		@Override
		public Void visitDefault(Element e, IImportClassBuilder<?> imports)
		{
			return null;
		}
		
	}
	
	public List<Element> getAllAccessibleMembers(TypeElement type, Visibility visibility)
	{
		return getAllMembers(type, visibility.filterAtLeast);
	}
	
	public List<Element> getAllMembers(TypeElement type, Predicate<Element> predicate)
	{
		List<Element> elems = new ArrayList<>();
		Set<Name> usedNames = new HashSet<>(); // For optimization purposes
		
		visitAllMembers(type, elem -> {
			if (predicate.test(elem))
			{
				Name name = elem.getSimpleName();
				if (usedNames.contains(name))
				{
					for (Element olderElem : elems)
					{
						if (this.tools.elements.hides(olderElem, elem)) return;
					}
				}
				elems.add(elem);
				usedNames.add(name);
			}
		});
		
		return elems;
	}
	
	public void visitAllMembers(TypeElement type, Consumer<Element> memberConsumer)
	{
		ElementMemberVisitor.INSTANCE.visit(type, memberConsumer);
	}
	
	private enum ElementMemberVisitor implements DefaultElementVisitor<Void, Consumer<Element>>, DefaultTypeVisitor<Void, Consumer<Element>>
	{
		INSTANCE;

		// TypeVisitor
		@Override
		public Void visitType(TypeElement e, Consumer<Element> p)
		{
			this.visit(e.getSuperclass(), p);
			e.getEnclosedElements().forEach(p);
			
			return null;
		}

		@Override
		public Void visitDefault(Element e, Consumer<Element> memberConsumer)
		{
			return null;
		}

		// ElementVisitor
		@Override
		public Void visitDeclared(DeclaredType t, Consumer<Element> p)
		{
			this.visit(t.asElement(), p);
			return null;
		}
		
		@Override
		public Void visitDefault(TypeMirror t, Consumer<Element> p)
		{
			return null;
		}
	}

	public DeclaredType replaceTypeArgument(DeclaredType type, TypeMirror fromArg, TypeMirror toArg)
	{
		if (this.tools.types.isSameType(fromArg, toArg))
			return type; // No replacement needed
		
		List<? extends TypeMirror> prevArgs = type.getTypeArguments();
		
		TypeMirror[] newArgs = new TypeMirror[prevArgs.size()];
		
		for (int i = 0; i < prevArgs.size(); i++)
		{
			newArgs[i] = prevArgs.get(i).equals(fromArg) ? toArg : prevArgs.get(i);
		}
		
		return this.tools.types.getDeclaredType(this.tools.elements.asTypeElement(this.tools.types.asElement(type)), newArgs);
	}
	
	public TypeMirror shallowErasure(TypeMirror type)
	{
		return type == null ? null : this.shallowEraser.visit(type);
	}
	
	private final TypeVisitor<TypeMirror, Void> shallowEraser = new DefaultTypeVisitor<>()
	{
		@Override
		public TypeMirror visitTypeVariable(TypeVariable t, Void p)
		{
			return this.visit(t.getUpperBound(), p);
		}
		
		@Override
		public TypeMirror visitWildcard(WildcardType t, Void p)
		{
			TypeMirror extendsBound = t.getExtendsBound();
			return extendsBound == null ? ExtendedTypes.this.tools.elements.getTypeElement(Object.class.getCanonicalName()).asType() : this.visit(extendsBound);
		}

		@Override
		public TypeMirror visitDefault(TypeMirror t, Void p)
		{
			return t;
		}
	};

	
	// Delegate Types methods
	
	@Override
	public Element asElement(TypeMirror t)
	{
		return this.base.asElement(t);
	}

	@Override
	public boolean isSameType(TypeMirror t1, TypeMirror t2)
	{
		return this.base.isSameType(t1, t2);
	}

	@Override
	public boolean isSubtype(TypeMirror t1, TypeMirror t2)
	{
		return this.base.isSubtype(t1, t2);
	}

	@Override
	public boolean isAssignable(TypeMirror t1, TypeMirror t2)
	{
		return this.base.isAssignable(t1, t2);
	}

	@Override
	public boolean contains(TypeMirror t1, TypeMirror t2)
	{
		return this.base.contains(t1, t2);
	}

	@Override
	public boolean isSubsignature(ExecutableType m1, ExecutableType m2)
	{
		return this.base.isSubsignature(m1, m2);
	}

	@Override
	public List<? extends TypeMirror> directSupertypes(TypeMirror t)
	{
		return this.base.directSupertypes(t);
	}

	@Override
	public TypeMirror erasure(TypeMirror t)
	{
		return this.base.erasure(t);
	}

	@Override
	public TypeElement boxedClass(PrimitiveType p)
	{
		return this.base.boxedClass(p);
	}

	@Override
	public PrimitiveType unboxedType(TypeMirror t)
	{
		return this.base.unboxedType(t);
	}

	@Override
	public TypeMirror capture(TypeMirror t)
	{
		return this.base.capture(t);
	}

	@Override
	public PrimitiveType getPrimitiveType(TypeKind kind)
	{
		return this.base.getPrimitiveType(kind);
	}

	@Override
	public NullType getNullType()
	{
		return this.base.getNullType();
	}

	@Override
	public NoType getNoType(TypeKind kind)
	{
		return this.base.getNoType(kind);
	}

	@Override
	public ArrayType getArrayType(TypeMirror componentType)
	{
		return this.base.getArrayType(componentType);
	}

	@Override
	public WildcardType getWildcardType(TypeMirror extendsBound, TypeMirror superBound)
	{
		return this.base.getWildcardType(extendsBound, superBound);
	}

	@Override
	public DeclaredType getDeclaredType(TypeElement typeElem, TypeMirror... typeArgs)
	{
		return this.base.getDeclaredType(typeElem, typeArgs);
	}

	@Override
	public DeclaredType getDeclaredType(DeclaredType containing, TypeElement typeElem, TypeMirror... typeArgs)
	{
		return this.base.getDeclaredType(containing, typeElem, typeArgs);
	}

	@Override
	public TypeMirror asMemberOf(DeclaredType containing, Element element)
	{
		return this.base.asMemberOf(containing, element);
	}
}
