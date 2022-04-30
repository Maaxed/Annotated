package fr.max2.annotated.processor.util;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
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
	private final ProcessingTools tools;
	public final Naming typeUse;
	public final Naming typeDeclaration;
	public final Naming erasedType;
	
	NamingUtils(ProcessingTools tools)
	{
		this.tools = tools;
		this.typeUse = builder().allowPrimitives().allowSpecialTypes().withGenericMode(Generics.WITH_PARAMETERS).withRawMode(RawNameMode.FULLY_QUALIFIED).build();
		this.typeDeclaration = builder().withGenericMode(Generics.NONE).withRawMode(RawNameMode.SHORT).build();
		this.erasedType = builder().allowPrimitives().withGenericMode(Generics.NONE).withRawMode(RawNameMode.FULLY_QUALIFIED).build();
	}
	
	// Class naming from mirrors
	
	public ClassName buildClassName(TypeElement type)
	{
		StringBuilder builder = new StringBuilder();
		Element elem = type;
		do
		{
			if (builder.length() > 0)
				builder.insert(0, '.');
			builder.insert(0,elem.getSimpleName());
			elem = elem.getEnclosingElement();
		}
		while (elem != null && elem.getKind() != ElementKind.PACKAGE && elem.getKind() != ElementKind.MODULE);

		String packageName = this.tools.elements.getPackageOf(type).getQualifiedName().toString();
		return new ClassName(packageName, builder.toString());
	}
	
	public static enum RawNameMode
	{
		SHORT
		{
			@Override
			protected void rawName(Naming naming, StringBuilder builder, DeclaredType type)
			{
				builder.append(type.asElement().getSimpleName());
			}
		},
		FULLY_QUALIFIED
		{
			@Override
			protected void rawName(Naming naming, StringBuilder builder, DeclaredType type)
			{
				builder.append(naming.tools.elements.asTypeElement(type.asElement()).getQualifiedName());
			}
		};

		protected abstract void rawName(Naming naming, StringBuilder builder, DeclaredType type);
	}
	
	public static enum Generics
	{
		NONE,
		WITH_DIAMOND,
		WITH_PARAMETERS;

		protected void generics(Naming naming, StringBuilder builder, List<? extends TypeMirror> arguments)
		{
			if (this == NONE)
				return;
			
			builder.append('<');

			if (this == WITH_PARAMETERS)
			{
				boolean first = true;
				for (TypeMirror arg : arguments)
				{
					if (!first)
					{
						builder.append(", ");
					}
					naming.visit(arg, builder);
					first = false;
				}
			}
			
			builder.append('>');
		}
	}
	
	public NamingBuilder builder()
	{
		return new NamingBuilder();
	}

	public class NamingBuilder
	{
		private RawNameMode rawMode = RawNameMode.FULLY_QUALIFIED;
		private Generics genericMode = Generics.NONE;
		private boolean allowArrays = true;
		private boolean allowPrimitives = false;
		private boolean allowSpecialTypes = false;
		
		private NamingBuilder()
		{ }
		
		public NamingBuilder withRawMode(RawNameMode rawMode)
		{
			this.rawMode = rawMode;
			return this;
		}
		
		public NamingBuilder withGenericMode(Generics genericMode)
		{
			this.genericMode = genericMode;
			return this;
		}

		public NamingBuilder disallowArrays()
		{
			this.allowArrays = false;
			return this;
		}

		public NamingBuilder allowPrimitives()
		{
			this.allowPrimitives = true;
			return this;
		}

		public NamingBuilder allowSpecialTypes()
		{
			this.allowSpecialTypes = true;
			return this;
		}
		
		public Naming build()
		{
			return new Naming(NamingUtils.this.tools, this.rawMode, this.genericMode, this.allowArrays, this.allowPrimitives, this.allowSpecialTypes);
		}
	}
	
	public static class Naming implements DefaultTypeVisitor<Void, StringBuilder>
	{
		private final ProcessingTools tools;
		private final RawNameMode rawMode;
		private final Generics genericMode;
		private final boolean allowArrays;
		private final boolean allowPrimitives;
		private final boolean allowSpecialTypes;

		public Naming(ProcessingTools tools, RawNameMode rawMode, Generics genericMode, boolean allowArrays, boolean allowPrimitives, boolean allowSpecialTypes)
		{
			this.tools = tools;
			this.rawMode = rawMode;
			this.genericMode = genericMode;
			this.allowArrays = allowArrays;
			this.allowPrimitives = allowPrimitives;
			this.allowSpecialTypes = allowSpecialTypes;
		}

		public String get(TypeMirror type)
		{
			StringBuilder builder = new StringBuilder();
			this.visit(type, builder);
			return builder.toString();
		}
		
		@Override
		public Void visitPrimitive(PrimitiveType t, StringBuilder builder)
		{
			if (!this.allowPrimitives)
				this.visitDefault(t, builder);
			
			builder.append(t.getKind().name().toLowerCase());
			return null;
		}

		@Override
		public Void visitArray(ArrayType t, StringBuilder builder)
		{
			if (!this.allowArrays)
				this.visitDefault(t, builder);
			
			this.visit(t.getComponentType(), builder);
			builder.append("[]");
			return null;
		}
		
		@Override
		public Void visitDeclared(DeclaredType t, StringBuilder builder)
		{
			this.rawMode.rawName(this, builder, t);
			
			List<? extends TypeMirror> arguments = t.getTypeArguments();
			
			if (!arguments.isEmpty())
			{
				this.genericMode.generics(this, builder, arguments);
			}
			
			return null;
		}
		
		@Override
		public Void visitTypeVariable(TypeVariable t, StringBuilder builder)
		{
			if (!this.allowSpecialTypes)
				this.visitDefault(t, builder);
			
			builder.append(t.asElement().getSimpleName());
			
			return null;
		}
		
		@Override
		public Void visitWildcard(WildcardType t, StringBuilder builder)
		{
			if (!this.allowSpecialTypes)
				this.visitDefault(t, builder);
			
			TypeMirror extendsBound = t.getExtendsBound();
			TypeMirror superBound = t.getSuperBound();
			
			builder.append('?');
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
			if (!this.allowSpecialTypes)
				this.visitDefault(t, builder);
			
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
			if (!this.allowSpecialTypes)
				this.visitDefault(t, builder);
			
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
			throw new UnsupportedOperationException("Cannot get the name of a type of kind '" + t.getKind() + "': " + t.toString());
		}
	}
}
