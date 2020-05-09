package fr.max2.annotated.processor.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;

public class TypeHelper
{
	private final ProcessingTools tools;
	
	TypeHelper(ProcessingTools tools)
	{
		this.tools = tools;
	}
	
	
	public TypeElement asTypeElement(Element elem)
	{
		return elem == null ? null : TypeElementCaster.INSTANCE.visit(elem);
	}
	
	private enum TypeElementCaster implements DefaultElementVisitor<TypeElement, Void>
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
	
	public PackageElement asPackage(Element type)
	{
		return type == null ? null : PackageElementCaster.INSTANCE.visit(type);
	}
	
	private enum PackageElementCaster implements DefaultElementVisitor<PackageElement, Void>
	{
		INSTANCE;
		
		@Override
		public PackageElement visitPackage(PackageElement e, Void p)
		{
			return e;
		}

		@Override
		public PackageElement visitDefault(Element e, Void p)
		{
			return null;
		}
		
	}
	
	public DeclaredType refineTo(TypeMirror type, TypeMirror base)
	{
		return type.accept(declaredTypeRefiner, this.tools.types.erasure(base));
	}
	
	private final TypeVisitor<DeclaredType, TypeMirror> declaredTypeRefiner = new DefaultTypeVisitor<DeclaredType, TypeMirror>()
	{
		@Override
		public DeclaredType visitDeclared(DeclaredType t, TypeMirror p)
		{
			if (tools.types.isSameType(tools.types.erasure(t), p))
			{
				return t;
			}
			
			List<? extends TypeMirror> superTypes = tools.types.directSupertypes(t);
			
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
	
	public void provideTypeImports(TypeMirror type, Consumer<String> imports)
	{
		TypeImporter.INSTANCE.visit(type, imports);
	}
	
	private enum TypeImporter implements DefaultTypeVisitor<Void, Consumer<String>>, DefaultElementVisitor<Void, Consumer<String>>
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
				this.visit(subType, imports);
			}
			return null;
		}
		
		@Override
		public Void visitIntersection(IntersectionType t, Consumer<String> imports)
		{
			for (TypeMirror subType : t.getBounds())
			{
				this.visit(subType, imports);
			}
			return null;
		}
		
		@Override
		public Void visitDefault(TypeMirror t, Consumer<String> imports)
		{
			return null;
		}
		
		// ElementVisitor
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

	public Optional<? extends AnnotationMirror> getAnnotationMirror(Element elem, CharSequence annotationType)
	{
		return elem.getAnnotationMirrors().stream().filter(a -> asTypeElement(a.getAnnotationType().asElement()).getQualifiedName().contentEquals(annotationType)).findAny();
	}

	public Optional<? extends AnnotationValue> getAnnotationValue(Optional<? extends AnnotationMirror> annotation, CharSequence propertyName)
	{
		return annotation
			.flatMap(an ->
				an.getElementValues().entrySet().stream()
				.filter(entry -> entry.getKey().getSimpleName().contentEquals(propertyName))
				.findAny()
				).map(entry -> entry.getValue());
	}

	public Optional<? extends AnnotationValue> getAnnotationValue(Element elem, CharSequence annotationType, CharSequence propertyName)
	{
		return getAnnotationValue(getAnnotationMirror(elem, annotationType), propertyName);
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
		
		return this.tools.types.getDeclaredType(this.asTypeElement(this.tools.types.asElement(type)), newArgs);
	}
	
	public TypeMirror shallowErasure(TypeMirror type)
	{
		return type == null ? null : shallowEraser.visit(type);
	}
	
	private final TypeVisitor<TypeMirror, Void> shallowEraser = new DefaultTypeVisitor<TypeMirror, Void>()
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
			return extendsBound == null ? tools.elements.getTypeElement(Object.class.getCanonicalName()).asType() : this.visit(extendsBound);
		}

		@Override
		public TypeMirror visitDefault(TypeMirror t, Void p)
		{
			return t;
		}
	};
}
