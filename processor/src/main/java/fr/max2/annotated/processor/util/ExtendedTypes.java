package fr.max2.annotated.processor.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
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
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;

import fr.max2.annotated.processor.util.exceptions.IncompatibleTypeException;

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
		if (type == null)
			return null;
		return this.primitiveTypeCaster.visit(type);
	}

	private final DefaultTypeVisitor<PrimitiveType, Void> primitiveTypeCaster = new DefaultTypeVisitor<>()
	{
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
	};

	public ArrayType asArrayType(TypeMirror type)
	{
		if (type == null)
			return null;

		return this.arrayTypeCaster.visit(type);
	}

	private final DefaultTypeVisitor<ArrayType, Void> arrayTypeCaster = new DefaultTypeVisitor<>()
	{
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
	};

	public WildcardType asWildcardType(TypeMirror type)
	{
		if (type == null)
			return null;

		return this.wildcardTypeCaster.visit(type);
	}

	private final DefaultTypeVisitor<WildcardType, Void> wildcardTypeCaster = new DefaultTypeVisitor<>()
	{
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
	};

	public TypeVariable asVariableType(TypeMirror type)
	{
		if (type == null)
			return null;

		return this.variableTypeCaster.visit(type);
	}

	private final DefaultTypeVisitor<TypeVariable, Void> variableTypeCaster = new DefaultTypeVisitor<>()
	{
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
	};

	public IntersectionType asIntersectionType(TypeMirror type)
	{
		if (type == null)
			return null;

		return this.intersectionTypeCaster.visit(type);
	}

	private final DefaultTypeVisitor<IntersectionType, Void> intersectionTypeCaster = new DefaultTypeVisitor<>()
	{
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
	};

	public DeclaredType asDeclared(TypeMirror type)
	{
		if (type == null)
			return null;

		return this.declaredTypeCaster.visit(type);
	}

	private final DefaultTypeVisitor<DeclaredType, Void> declaredTypeCaster = new DefaultTypeVisitor<>()
	{
		public DeclaredType visitDeclared(DeclaredType t, Void p)
		{
			return t;
		}

		@Override
		public DeclaredType visitDefault(TypeMirror t, Void p)
		{
			return null;
		}
	};

	public DeclaredType refineTo(TypeMirror type, TypeMirror base)
	{
		if (type == null)
			return null;

		return type.accept(this.declaredTypeRefiner, this.erasure(base));
	}

	private final TypeVisitor<DeclaredType, TypeMirror> declaredTypeRefiner = new DefaultTypeVisitor<>()
	{
		@Override
		public DeclaredType visitDeclared(DeclaredType t, TypeMirror p)
		{
			if (ExtendedTypes.this.isSameType(ExtendedTypes.this.erasure(t), p))
			{
				return t;
			}

			List<? extends TypeMirror> superTypes = ExtendedTypes.this.directSupertypes(t);

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

	public List<Element> getAllAccessibleMembers(TypeElement type, Visibility visibility)
	{
		return this.getAllMembers(type, visibility.filterAtLeast);
	}

	public List<Element> getAllMembers(TypeElement type, Predicate<Element> predicate)
	{
		List<Element> elems = new ArrayList<>();
		Set<Name> usedNames = new HashSet<>(); // For optimization purposes

		this.visitAllMembers(type, elem -> {
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
		if (type == null)
			return;

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

	/**
	 * <code>? extends T</code> => <code>T</code>
	 * <br>
	 * <code>T</code> => <code>T</code>
	 */
	public TypeMirror shallowErasure(TypeMirror type)
	{
		if (type == null)
			return null;

		return this.shallowEraser.visit(type);
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
			return extendsBound == null ? ExtendedTypes.this.tools.elements.objectElement.asType() : this.visit(extendsBound);
		}

		@Override
		public TypeMirror visitDefault(TypeMirror t, Void p)
		{
			return t;
		}
	};

	/**
	 * <code>(T&lt;From&gt;, From, To)</code> => <code>T&lt;To&gt;</code>
	 */
	public DeclaredType replaceTypeArgument(DeclaredType type, TypeMirror fromArg, TypeMirror toArg)
	{
		if (type == null)
			return null;

		if (this.isSameType(fromArg, toArg))
			return type; // No replacement needed

		if (fromArg.getKind() != TypeKind.TYPEVAR)
			return type; // Only replace type arguments

		List<? extends TypeMirror> prevArgs = type.getTypeArguments();

		TypeMirror[] newArgs = new TypeMirror[prevArgs.size()];

		for (int i = 0; i < prevArgs.size(); i++)
		{
			newArgs[i] = prevArgs.get(i).equals(fromArg) ? toArg : prevArgs.get(i);
		}

		return this.getDeclaredType(this.tools.elements.asTypeElement(this.asElement(type)), newArgs);
	}

	/**
	 * <code>(A&lt;T&gt;, B, B&lt;U&gt;)</code> where <code>A&lt;T&gt; extends B&lt;T&gt;</code> => <code>A&lt;U&gt;</code>
	 */
	public DeclaredType replaceTypeArguments(TypeMirror type, DeclaredType baseType, DeclaredType targetRefinedType)
	{
		int expectedTypeArgCount = this.tools.elements.asTypeElement(baseType.asElement()).getTypeParameters().size();
		this.tools.types.requireTypeArguments(targetRefinedType, expectedTypeArgCount);

		return this.replaceTypeArguments(type, baseType, i -> targetRefinedType.getTypeArguments().get(i));
	}

	/**
	 * <code>(A&lt;T, U, V&gt;, B, f(int)->TypeMirror)</code> where <code>A&lt;T, U, V&gt; extends B&lt;T, U, V&gt;</code> => <code>A&lt;f(0), f(1), f(2)&gt;</code>
	 */
	public DeclaredType replaceTypeArguments(TypeMirror type, DeclaredType baseType, IntFunction<? extends TypeMirror> argProvider)
	{
		int expectedTypeArgCount = this.tools.elements.asTypeElement(baseType.asElement()).getTypeParameters().size();

		DeclaredType refinedType = this.tools.types.refineTo(type, baseType);
		if (refinedType == null)
			throw new IncompatibleTypeException("The implementation type '" + type + "' is not a sub type of " + baseType);

		if (refinedType.getTypeArguments().isEmpty())
		{
			type = this.tools.types.asElement(type).asType();
			refinedType = this.tools.types.refineTo(type, baseType);
		}

		this.tools.types.requireTypeArguments(refinedType, expectedTypeArgCount);

		DeclaredType resultType = this.tools.types.asDeclared(type);
		for (int argIndex = 0; argIndex < expectedTypeArgCount; argIndex++)
		{
			TypeMirror arg = refinedType.getTypeArguments().get(argIndex);
			resultType = this.tools.types.replaceTypeArgument(resultType, arg, argProvider.apply(argIndex));
		}

		return resultType;
	}


	public void requireAssignable(TypeMirror type, TypeMirror expectedSubType) throws IncompatibleTypeException
	{
		if (!this.tools.types.isAssignable(type, expectedSubType))
			throw new IncompatibleTypeException("The type '" + type + "' is not a sub-type of '" + expectedSubType + "'");
	}

	public void requireConcreteType(TypeMirror type) throws IncompatibleTypeException
	{
		Element elem = this.tools.types.asElement(type);
		if (elem == null)
			return; // Unknown type, assume it is concrete

		if (elem.getKind() == ElementKind.INTERFACE || elem.getModifiers().contains(Modifier.ABSTRACT))
			throw new IncompatibleTypeException("The type '" + type + "' is abstract and cannot be instantiated");
	}

	public void requireDefaultConstructor(TypeMirror type) throws IncompatibleTypeException
	{
		this.requireConstructor(type, cons -> cons.getParameters().isEmpty());
	}

	public void requireConstructor(TypeMirror type, List<TypeMirror> paramTypes) throws IncompatibleTypeException
	{
		this.requireConstructor(type, this.isConstructorCompatible(paramTypes));
	}

	public void requireConstructor(TypeMirror type, Predicate<? super ExecutableElement> constructorFilter) throws IncompatibleTypeException
	{
		if (this.findConstructor(type, constructorFilter) == null)
			throw new IncompatibleTypeException("The type '" + type + "' doesn't have the required constructor");
	}

	public ExecutableElement findConstructor(TypeMirror type) throws IncompatibleTypeException
	{
		return this.findConstructor(type, cons -> cons.getParameters().isEmpty());
	}

	public ExecutableElement findConstructor(TypeMirror type, List<TypeMirror> paramTypes) throws IncompatibleTypeException
	{
		return this.findConstructor(type, this.isConstructorCompatible(paramTypes));
	}

	public ExecutableElement findConstructor(TypeMirror type, Predicate<? super ExecutableElement> constructorFilter) throws IncompatibleTypeException
	{
		Element elem = this.tools.types.asElement(type);
		if (elem == null)
			throw new IncompatibleTypeException("The type '" + type + "' is not a DeclaredType");

		return ElementFilter.constructorsIn(elem.getEnclosedElements()).stream()
			.filter(constructorFilter)
			.reduce((a, b) -> { throw new IncompatibleTypeException("The type '" + elem + "' have multiple matching constructors"); })
			.orElse(null);
	}

	private Predicate<? super ExecutableElement> isConstructorCompatible(List<TypeMirror> paramTypes)
	{
		return cons ->
		{
			List<? extends VariableElement> actualParams = cons.getParameters();
			if (actualParams.size() != paramTypes.size())
				return false;

			for (int i = 0; i < paramTypes.size(); i++)
			{
				if (!this.tools.types.isAssignable(actualParams.get(i).asType(), paramTypes.get(i)))
					return false;
			}

			return true;
		};
	}

	public void requireTypeArguments(DeclaredType type, int expectedCount) throws IncompatibleTypeException
	{
		if (type.getTypeArguments().size() != expectedCount)
			throw new IncompatibleTypeException("The type '" + type + "' has the wrong number of arguments: expected " + expectedCount + ", but has " + type.getTypeArguments().size());
	}


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
