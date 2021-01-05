package fr.max2.annotated.processor.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import javax.tools.FileObject;
import javax.tools.StandardLocation;

public class NamingUtils
{
	private final ProcessingTools tools;
	private final Map<String, String> methods;
	private final Map<String, String> fields;
	
	NamingUtils(ProcessingTools tools)
	{
		this.tools = tools;
		this.methods = loadMappings("methods.csv");
		this.fields = loadMappings("fields.csv");
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
		if (this.methods == null)
			return defaultName == null ? srgName : defaultName;
		
		return this.methods.getOrDefault(srgName, srgName);
	}
	
	public String getFieldMapping(String srgName, String defaultName)
	{
		if (this.fields == null)
			return defaultName == null ? srgName : defaultName;
		
		return this.fields.getOrDefault(srgName, srgName);
	}
	
	private Map<String, String> loadMappings(final String mappingFileName)
	{
		Reader fileReader = null;
		URI path = null;
		try
		{
			// Try to find the mapping file using the Filer tool
			FileObject file = this.tools.filer.getResource(StandardLocation.CLASS_PATH, "", mappingFileName);
			fileReader = file.openReader(true);
			path = file.toUri();
		}
		catch (IOException | IllegalArgumentException e)
		{
			this.tools.log(Kind.NOTE, "Errors opening mapping file from filer: " + e.getClass().getTypeName() + ": " + e.getMessage());
		}

		if (fileReader == null)
		{
			// Try to find the mapping file using the ClassLoader
			URL mappingPath = NamingUtils.class.getClassLoader().getResource(mappingFileName);
			if (mappingPath == null)
				return null;
			try
			{
				fileReader = new InputStreamReader(mappingPath.openStream());
				path = mappingPath.toURI();
			}
			catch (IOException e)
			{
				this.tools.log(Kind.NOTE, "Errors opening mapping file from class loader: "
					+ e.getClass().getTypeName() + ": " + e.getMessage());
				return null;
			}
			catch (URISyntaxException e)
			{
				this.tools.log(Kind.NOTE, "Error opening mapping file: " + e.getClass().getTypeName() + ": " + e.getMessage());
				return null;
			}
		}
		
		try (BufferedReader reader = new BufferedReader(fileReader))
		{
			Map<String, String> mappings = new HashMap<>();
			reader.lines().skip(1).map(line -> line.split(",")).forEach(entry -> mappings.put(entry[0], entry[1]));
			this.tools.log(Kind.NOTE, "Loaded " + mappings.size() + " mappings from " + mappingFileName + " (" + path.toString() + ")");
			return mappings;
		}
		catch (IOException e)
		{
			this.tools.log(Kind.NOTE, "Error reading mappings: " + e.getClass().getTypeName() + ": " + e.getMessage());
			return null;
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
