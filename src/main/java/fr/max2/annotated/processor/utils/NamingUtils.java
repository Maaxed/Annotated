package fr.max2.annotated.processor.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.tools.Diagnostic.Kind;

public class NamingUtils
{
	private final ProcessingTools tools;
	private final HashMap<String, String> methods = new HashMap<>();
	private final HashMap<String, String> fields = new HashMap<>();
	
	NamingUtils(ProcessingTools tools)
	{
		this.tools = tools;
		loadMappings("methods.csv", this.methods::put);
		tools.log(Kind.NOTE, "Loaded " + this.methods.size() + " method mappings from methods.csv");
		loadMappings("fields.csv", this.fields::put);
		this.tools.log(Kind.NOTE, "Loaded " + this.fields.size() + " field mappings from fields.csv");
	}
	
	// MCP mappings from SRG names
	
	public String getMapping(String domain, String srgName, String defaultName)
	{
		switch (domain.toLowerCase())
		{
		case "class":
			return srgName;
		case "field":
			return this.getFieldMapping(srgName, defaultName);
		case "method":
			return this.getMethodMapping(srgName, defaultName);
		}
		return srgName;
	}
	
	public String getMethodMapping(String srgName, String defaultName)
	{
		return this.methods.getOrDefault(srgName, defaultName == null ? srgName : defaultName);
	}
	
	public String getFieldMapping(String srgName, String defaultName)
	{
		return this.fields.getOrDefault(srgName, defaultName == null ? srgName : defaultName);
	}
	
	private void loadMappings(final String mappingFileName, BiConsumer<String, String> mapStore)
	{
		URL mappingPath = NamingUtils.class.getClassLoader().getResource(mappingFileName);
		if (mappingPath == null)
			return;
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(mappingPath.openStream())))
		{
			reader.lines().skip(1).map(line -> line.split(",")).forEach(entry -> mapStore.accept(entry[0], entry[1]));
		}
		catch (IOException e)
		{
			this.tools.log(Kind.NOTE, "Error reading mappings: " + e.getClass().getTypeName() + ": " + e.getMessage());
		}
	}
	
	// Class naming from mirrors
	
	public ClassName buildClassName(Element type)
	{
		StringBuilder builder = new StringBuilder();
		do
		{
			if (builder.length() > 0)
				builder.append('.');
			builder.append(type.getSimpleName());
			type = type.getEnclosingElement();
		}
		while (type != null && type.getKind() != ElementKind.PACKAGE);

		String packageName = this.tools.elements.getPackageOf(type).getQualifiedName().toString();
		return new ClassName(packageName, builder.toString());
	}
	
	public String computeSimplifiedName(TypeMirror type)
	{
		return TypeToString.SIMPLIFIED.computeName(type);
	}
	
	public String computeFullName(TypeMirror type)
	{
		return TypeToString.FULL.computeName(type);
	}
	
	public enum TypeToString implements DefaultTypeVisitor<Void, StringBuilder>
	{
		FULL(false),
		SIMPLIFIED(true);
		
		private final boolean simplifyGenerics;
		
		private TypeToString(boolean simplifyGenerics)
		{
			this.simplifyGenerics = simplifyGenerics;
		}
		
		public String computeName(TypeMirror type)
		{
			StringBuilder builder = new StringBuilder();
			this.visit(type, builder);
			return builder.toString();
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
			builder.append(t.asElement().getSimpleName());
			
			return null;
		}
		
		@Override
		public Void visitWildcard(WildcardType t, StringBuilder builder)
		{
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
