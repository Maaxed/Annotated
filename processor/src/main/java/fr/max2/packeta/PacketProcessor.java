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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

import fr.max2.packeta.api.network.DataType;
import fr.max2.packeta.api.network.GenerateNetwork;
import fr.max2.packeta.api.network.GeneratePacket;
import fr.max2.packeta.api.network.DataType.DataHandler;
import fr.max2.packeta.utils.ClassRef;
import fr.max2.packeta.utils.EnumSides;
import fr.max2.packeta.utils.ExceptionUtils;

@SupportedAnnotationTypes({ClassRef.NETWORK_ANNOTATION, ClassRef.PACKET_ANNOTATION, ClassRef.FORGE_MOD_ANNOTATION})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class PacketProcessor extends AbstractProcessor
{
	private DataType.Finder finder;
	
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv)
	{
		super.init(processingEnv);
		this.finder = new DataType.Finder(this.processingEnv);
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
					JavaFileObject file = this.processingEnv.getFiler().createSourceFile(className + "Message");
					Writer writer = file.openWriter();
					
					writePacket(writer, className.toString(), sides, members);
					
					writer.close();
					
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
			
			//TODO default name/class
			
			String networkClass = networkAnnotation.className();
			String networkName = networkAnnotation.value();
			
			if (networkName.isEmpty())
			{
				Types typeUtils = this.processingEnv.getTypeUtils();
				
				TypeMirror modAnnotationType = elemUtils.getTypeElement(ClassRef.FORGE_MOD_ANNOTATION).asType();
				
				networkName = networkElement.getAnnotationMirrors().stream()										// all annotations
					.filter(a -> typeUtils.isSameType(a.getAnnotationType(), modAnnotationType))	// mod annotation
					.flatMap(a -> a.getElementValues().entrySet().stream())							// mod annotation values
					.filter(e -> e.getKey().getSimpleName().contentEquals("modid"))					// modid value
					.map(entry -> entry.getValue().getValue().toString())										// modid string
					.findAny().orElse(networkElement.getSimpleName().toString());
				
			}
			
			if (networkClass.isEmpty())
			{
				Element parent = networkElement;
				while (parent.getEnclosingElement() != null && parent.getEnclosingElement().getKind() != ElementKind.PACKAGE)
				{
					parent = parent.getEnclosingElement();
				}
				
				if (parent.getKind() == ElementKind.PACKAGE)
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
				JavaFileObject file = this.processingEnv.getFiler().createSourceFile(networkClass);
				Writer writer = file.openWriter();
				
				writeNetwork(writer, networkClass, networkName, packetsToRegister);
				
				writer.close();
			}
			catch (IOException ee)
			{
				throw new UncheckedIOException(ee);
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
	
	private void writePacket(Writer wr, String className, EnumSides sides, List<? extends Element> members) throws IOException
	{
		String ls = System.lineSeparator();
		
		int packageSeparator = className.lastIndexOf('.');
		
		List<VariableElement> fields = ElementFilter.fieldsIn(members);
		List<DataHandler> dataHandlers = fields.stream().map(f -> this.finder.getDataType(f)).collect(Collectors.toList());
		
		Set<String> imports = new TreeSet<>();
		
		sides.addImports(imports);
		
		fields.stream().map(f -> f.asType()).filter(t -> t.getKind() == TypeKind.DECLARED).map(f -> f.toString()).filter(f -> !f.startsWith("java.lang")).forEach(imports::add);
		
		dataHandlers.forEach(handler -> handler.addImportsToSet(imports));
		
		Map<String, String> replacements = new HashMap<>();
		replacements.put("package", className.substring(0, packageSeparator));
		replacements.put("baseClass", className.substring(packageSeparator + 1));
		replacements.put("interfaces", sides.getInterfaces());
		replacements.put("allFields" , fields.stream().map(f -> simpleName(f.asType()) + " " + f.getSimpleName()).collect(Collectors.joining(", ")));
		replacements.put("fieldsInit", fields.stream().map(f -> "this." + f.getSimpleName() + " = " + f.getSimpleName() + ";").collect(Collectors.joining(ls + "\t\t")));
		replacements.put("toBytes"	, dataHandlers.stream().flatMap(h -> Stream.of(h.saveDataInstructions())).collect(Collectors.joining(ls + "\t\t")));
		replacements.put("fromBytes", dataHandlers.stream().flatMap(h -> Stream.of(h.loadDataInstructions())).collect(Collectors.joining(ls + "\t\t")));
		replacements.put("imports", imports.stream().map(i -> "import " + i + ";" + ls).collect(Collectors.joining()));

		writeFileFromTemplaTe(wr, "templates/TemplatePacket.jvtp", replacements);
	}
	
	private static void writeNetwork(Writer wr, String networkClass, String networkName, Map<EnumSides, Collection<String>> packetsToRegister) throws IOException
	{
		String ls = System.lineSeparator();
		
		int packageSeparator = networkClass.lastIndexOf('.');
		
		Map<String, String> replacements = new HashMap<>();
		replacements.put("package", networkClass.substring(0, packageSeparator));
		replacements.put("className", networkClass.substring(packageSeparator + 1));
		replacements.put("networkName", networkName);
		replacements.put("registerPackets", packetsToRegister.entrySet().stream().flatMap(entry -> entry.getValue().stream().map(packetName -> registerPacketInstruction(entry.getKey(), simpleName(packetName)))).collect(Collectors.joining(ls + "\t\t")));
		replacements.put("imports"		  , packetsToRegister.entrySet().stream().flatMap(entry -> entry.getValue().stream()).map(i -> "import " + i + ";" + ls).collect(Collectors.joining()));
		
		writeFileFromTemplaTe(wr, "templates/TemplateNetwork.jvtp", replacements);
	}
	
	private static String registerPacketInstruction(EnumSides sides, String packetClass)
	{
		return "NETWORK.register" + sides.getSimpleName() + "(" + packetClass + ".class);";
	}
	
	private static void writeFileFromTemplaTe(Writer wr, String templateFile, Map<String, String> replacements) throws IOException
	{
		try (InputStream fileStream = PacketProcessor.class.getClassLoader().getResourceAsStream(templateFile))
		{
			new BufferedReader(new InputStreamReader(fileStream)).lines().map(line -> mapKeys(line, replacements) + System.lineSeparator()).forEach(ExceptionUtils.wrapIOExceptions(wr::write));
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
	
	private static CharSequence simpleName(TypeMirror type)
	{
		return type.getKind() == TypeKind.DECLARED ? ((TypeElement)((DeclaredType)type).asElement()).getSimpleName() : type.toString();
	}
	
	public static String simpleName(String qualifiedName)
	{
		int ditIndex = qualifiedName.lastIndexOf('.') ;
		return ditIndex < 0 ? qualifiedName : qualifiedName.substring(ditIndex + 1);
	}
	
}
