package fr.max2.packeta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

import fr.max2.packeta.api.network.GenerateNetwork;
import fr.max2.packeta.api.network.GeneratePacket;
import fr.max2.packeta.network.DataHandlerParameters;
import fr.max2.packeta.utils.ClassRef;
import fr.max2.packeta.utils.DefaultElementVisitor;
import fr.max2.packeta.utils.DefaultTypeVisitor;
import fr.max2.packeta.utils.EnumSides;
import fr.max2.packeta.utils.ExceptionUtils;
import fr.max2.packeta.utils.NamingUtils;

@SupportedAnnotationTypes({ClassRef.NETWORK_ANNOTATION, ClassRef.PACKET_ANNOTATION, ClassRef.FORGE_MOD_ANNOTATION})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class PacketProcessor extends AbstractProcessor
{
	private DataHandlerParameters.Finder finder;
	
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv)
	{
		super.init(processingEnv);
		this.finder = new DataHandlerParameters.Finder(this.processingEnv);
	}
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		Set<? extends Element> networkAnnotations = roundEnv.getElementsAnnotatedWith(GenerateNetwork.class);
		
		if (networkAnnotations.size() > 1)
		{
			throw new RuntimeException("The annotation '" + ClassRef.NETWORK_ANNOTATION + "' cannot be used twice.");
		}
		
		Map<EnumSides, Collection<String>> packetsToRegister = new EnumMap<>(EnumSides.class);
		
		for (EnumSides sides : EnumSides.values())
		{
			packetsToRegister.put(sides, new ArrayList<>());
		}
		
		Elements elemUtils = this.processingEnv.getElementUtils();
		
		for (Element elem : roundEnv.getElementsAnnotatedWith(GeneratePacket.class))
		{
			if (elem.getKind() == ElementKind.CLASS)
			{
				TypeElement type = (TypeElement)elem;
				
				Name className = type.getQualifiedName();
				EnumSides sides = sidesFromClass(type.asType());
				List<? extends Element> members = elemUtils.getAllMembers(type);
				try
				{
					this.writePacket(className.toString(), sides, members);
					
					packetsToRegister.get(sides).add(type.getQualifiedName() + "Message");
				}
				catch (IOException e)
				{
					throw new UncheckedIOException(e);
				}
			}
		}
		
		if (networkAnnotations.size() == 1)
		{
			Element networkElement = networkAnnotations.iterator().next();
			GenerateNetwork networkAnnotation = networkElement.getAnnotation(GenerateNetwork.class);
			
			String networkClass = networkAnnotation.className();
			String networkName = networkAnnotation.value();
			
			if (networkName.isEmpty())
			{
				Types typeUtils = this.processingEnv.getTypeUtils();
				
				TypeMirror modAnnotationType = elemUtils.getTypeElement(ClassRef.FORGE_MOD_ANNOTATION).asType();
				
				networkName = networkElement.getAnnotationMirrors().stream()						// all annotations
					.filter(a -> typeUtils.isSameType(a.getAnnotationType(), modAnnotationType))	// mod annotation
					.flatMap(a -> a.getElementValues().entrySet().stream())							// mod annotation values
					.filter(e -> e.getKey().getSimpleName().contentEquals("modid"))					// modid value
					.map(entry -> entry.getValue().getValue().toString())							// modid string
					.findAny().orElse(networkElement.getSimpleName().toString());
				
			}
			
			if (networkClass.isEmpty())
			{
				Element parent = networkElement;
				while (parent.getEnclosingElement() != null && parent.getEnclosingElement().getKind() != ElementKind.PACKAGE)
				{
					parent = parent.getEnclosingElement();
				}
				
				if (parent.getKind() == ElementKind.PACKAGE) //TODO use ElementVisitor
				{
					networkClass = ((QualifiedNameable)parent).getQualifiedName() + "." + parent.getSimpleName() + "Network";
				}
				else if (parent.getKind().isClass() || parent.getKind().isInterface())
				{
					networkClass = ((QualifiedNameable)parent).getQualifiedName() + "Network";
				}
			}
			
			try
			{
				this.writeNetwork(networkClass, networkName, packetsToRegister);
			}
			catch (IOException e)
			{
				throw new UncheckedIOException(e);
			}
		}
		return true;
	}
	
	public EnumSides sidesFromClass(TypeMirror type)
	{
		Elements elemUtils = this.processingEnv.getElementUtils();
		Types typeUtils = this.processingEnv.getTypeUtils();
		
		TypeMirror clientType = elemUtils.getTypeElement(ClassRef.CLIENT_PACKET).asType();
		TypeMirror serverType = elemUtils.getTypeElement(ClassRef.SERVER_PACKET).asType();
		
		boolean isClient = typeUtils.isAssignable(type, clientType);
		boolean isServer = typeUtils.isAssignable(type, serverType);
		
		if (isClient)
		{
			return isServer ? EnumSides.BOTH : EnumSides.CLIENT;
		}
		else if (isServer)
		{
			return EnumSides.SERVER;
		}
		else
		{
			throw new IllegalArgumentException();
		}
	}
	
	private void writePacket(String className, EnumSides sides, List<? extends Element> members) throws IOException
	{
		String ls = System.lineSeparator();
		
		int packageSeparator = className.lastIndexOf('.');
		
		List<VariableElement> fields = ElementFilter.fieldsIn(members).stream().filter(field -> !field.getModifiers().contains(Modifier.STATIC)).collect(Collectors.toList());
		List<DataHandlerParameters> dataHandlers = fields.stream().map(f -> this.finder.getDataType(f)).collect(Collectors.toList());
		
		Set<String> imports = new TreeSet<>();//TODO filter with the current package
		List<String> saveInstructions = new ArrayList<>();
		List<String> loadInstructions = new ArrayList<>();
		
		sides.addImports(imports);
		
		fields.forEach(f -> addTypeImports(f.asType(), imports::add));
		
		dataHandlers.forEach(handler -> handler.addInstructions(saveInstructions::add, loadInstructions::add, imports::add));
		
		Map<String, String> replacements = new HashMap<>();
		replacements.put("package", className.substring(0, packageSeparator));
		replacements.put("baseClass", className.substring(packageSeparator + 1));
		replacements.put("interfaces", sides.getInterfaces());
		replacements.put("allFields" , fields.stream().map(f -> NamingUtils.simpleTypeName(f.asType()) + " " + f.getSimpleName()).collect(Collectors.joining(", ")));
		replacements.put("fieldsInit", fields.stream().map(f -> "this." + f.getSimpleName() + " = " + f.getSimpleName() + ";").collect(Collectors.joining(ls + "\t\t")));
		replacements.put("toBytes"	, saveInstructions.stream().collect(Collectors.joining(ls + "\t\t")));
		replacements.put("fromBytes", loadInstructions.stream().collect(Collectors.joining(ls + "\t\t")));
		replacements.put("imports", imports.stream().map(i -> "import " + i + ";" + ls).collect(Collectors.joining()));

		this.writeFileFromTemplaTe(className + "Message", "templates/TemplatePacket.jvtp", replacements);
	}
	
	private void writeNetwork(String networkClass, String networkName, Map<EnumSides, Collection<String>> packetsToRegister) throws IOException
	{
		String ls = System.lineSeparator();
		
		int packageSeparator = networkClass.lastIndexOf('.');
		
		Map<String, String> replacements = new HashMap<>();
		replacements.put("package", networkClass.substring(0, packageSeparator));
		replacements.put("className", networkClass.substring(packageSeparator + 1));
		replacements.put("networkName", networkName);
		replacements.put("registerPackets", packetsToRegister.entrySet().stream().flatMap(entry -> entry.getValue().stream().map(packetName -> registerPacketInstruction(entry.getKey(), NamingUtils.simpleName(packetName)))).collect(Collectors.joining(ls + "\t\t")));
		replacements.put("imports"		  , packetsToRegister.entrySet().stream().flatMap(entry -> entry.getValue().stream()).map(i -> "import " + i + ";" + ls).collect(Collectors.joining()));
		
		this.writeFileFromTemplaTe(networkClass, "templates/TemplateNetwork.jvtp", replacements);
	}
	
	private static String registerPacketInstruction(EnumSides sides, String packetClass)
	{
		return "NETWORK.register" + sides.getSimpleName() + "(" + packetClass + ".class);";
	}
	
	private void writeFileFromTemplaTe(String className, String templateFile, Map<String, String> replacements) throws IOException
	{
		try
		{
			JavaFileObject file = this.processingEnv.getFiler().createSourceFile(className);
			Writer writer = file.openWriter();
			
			try (InputStream fileStream = PacketProcessor.class.getClassLoader().getResourceAsStream(templateFile))
			{
				new BufferedReader(new InputStreamReader(fileStream)).lines().map(line -> mapKeys(line, replacements) + System.lineSeparator()).forEach(ExceptionUtils.wrapIOExceptions(writer::write));
			}
			
			writer.close();
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
		
	}
	
	private static String mapKeys(String content, Map<String, String> replacements)
	{
		Pattern p = Pattern.compile("\\$\\{(.+?)\\}"); //	${key}
		Matcher m = p.matcher(content);
		
		StringBuffer sb = new StringBuffer();
		while (m.find())
		{
			String key = m.group(1);
			String rep = replacements.get(key);
		    m.appendReplacement(sb, rep == null ? key : rep);
		}
		m.appendTail(sb);
		
		return sb.toString();
	}
	
	private static void addTypeImports(TypeMirror type,  Consumer<String> imports)
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
