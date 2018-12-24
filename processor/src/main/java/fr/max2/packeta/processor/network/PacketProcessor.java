package fr.max2.packeta.processor.network;

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
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import fr.max2.packeta.api.processor.network.GenerateNetwork;
import fr.max2.packeta.api.processor.network.GeneratePacket;
import fr.max2.packeta.api.processor.network.IgnoredData;
import fr.max2.packeta.processor.utils.ClassRef;
import fr.max2.packeta.processor.utils.EnumSides;
import fr.max2.packeta.processor.utils.ExceptionUtils;
import fr.max2.packeta.processor.utils.NamingUtils;
import fr.max2.packeta.processor.utils.TypeHelper;
import fr.max2.packeta.processor.utils.Visibility;

import javax.tools.JavaFileObject;

@SupportedAnnotationTypes({ClassRef.NETWORK_ANNOTATION, ClassRef.PACKET_ANNOTATION})
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
		Messager logs = this.processingEnv.getMessager();
		
		logs.printMessage(Kind.NOTE, "Starting " + this.getClass().getCanonicalName() + " annotation processos !");
		
		Set<? extends Element> networkAnnotations = roundEnv.getElementsAnnotatedWith(GenerateNetwork.class);
		
		if (networkAnnotations.size() > 1)
		{
			logs.printMessage(Kind.ERROR, "Multiple '" + GenerateNetwork.class.getCanonicalName() + "' annotations have been detected. The processor only suport one network currently.");
			
			throw new RuntimeException("The annotation '" + ClassRef.NETWORK_ANNOTATION + "' cannot be used twice.");
		}
		
		Map<EnumSides, Collection<String>> packetsToRegister = new EnumMap<>(EnumSides.class);
		
		for (EnumSides sides : EnumSides.values())
		{
			packetsToRegister.put(sides, new ArrayList<>());
		}
		
		for (TypeElement elem : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(GeneratePacket.class)))
		{
			if (elem.getKind() == ElementKind.CLASS)
			{
				logs.printMessage(Kind.NOTE, "Processing packet class generation", elem);
				
				EnumSides sides = sidesFromClass(elem);
				try
				{
					this.writePacket(elem, sides);
					
					packetsToRegister.get(sides).add(elem.getQualifiedName() + "Message");
				}
				catch (IOException e)
				{
					logs.printMessage(Kind.ERROR, "An error has occured during the generation of the packet class", elem);
					
					throw new UncheckedIOException(e);
				}
			}
		}
		
		//TODO [v1.2] multi networks
		if (networkAnnotations.size() == 1)
		{
			Element networkElement = networkAnnotations.iterator().next();
			logs.printMessage(Kind.NOTE, "Processing network class generation", networkElement);
			
			GenerateNetwork networkAnnotation = networkElement.getAnnotation(GenerateNetwork.class);
			
			String networkClass = networkAnnotation.className();
			String networkName = networkAnnotation.value();
			
			if (networkName.isEmpty())
			{
				Types typeUtils = this.processingEnv.getTypeUtils();
				
				TypeMirror modAnnotationType = this.processingEnv.getElementUtils().getTypeElement(ClassRef.FORGE_MOD_ANNOTATION).asType();
				
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
				
				/*if (parent.getKind() == ElementKind.PACKAGE)
				{
					networkClass = ((QualifiedNameable)parent).getQualifiedName() + "." + parent.getSimpleName() + "Network";
				}
				else */
				if (parent.getKind().isClass() || parent.getKind().isInterface())
				{
					networkClass = TypeHelper.asTypeElement(parent).getQualifiedName() + "Network";
				}
			}
			
			try
			{
				this.writeNetwork(networkClass, networkName, packetsToRegister);
			}
			catch (IOException e)
			{
				logs.printMessage(Kind.ERROR, "Error writing the network class (class name : " + networkClass + ", network name : " + networkName + ")", networkElement);
				throw new UncheckedIOException(e);
			}
		}
		
		logs.printMessage(Kind.NOTE, "End of the " + this.getClass().getCanonicalName() + " annotation processos !");
		
		return true;
	}
	
	public EnumSides sidesFromClass(TypeElement elem)
	{
		TypeMirror type = elem.asType();
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
			this.processingEnv.getMessager().printMessage(Kind.ERROR, "The packet data class diesn't implement any of the requested interfaces", elem);
			throw new IllegalArgumentException();
		}
	}
	
	private void writePacket(TypeElement packetClass, EnumSides sides) throws IOException
	{
		Elements elemUtils = this.processingEnv.getElementUtils();
		List<? extends Element> members = TypeHelper.getAllAccessibleMembers(packetClass, elemUtils, Visibility.PROTECTED);
		String className = packetClass.getQualifiedName().toString();
		String ls = System.lineSeparator();
		
		int packageSeparator = className.lastIndexOf('.');
		PackageElement packetPackage = TypeHelper.getPackage(packetClass);
		
		//TODO [v1.1] use getters and setters
		List<VariableElement> fields = ElementFilter.fieldsIn(members).stream().filter(field -> field.getAnnotation(IgnoredData.class) == null && !field.getModifiers().contains(Modifier.STATIC)).collect(Collectors.toList());
		List<DataHandlerParameters> dataHandlers = fields.stream().map(f -> this.finder.getDataType(f)).collect(Collectors.toList());
		
		Set<String> imports = new TreeSet<>();
		Consumer<String> importFilter = imp -> {
			TypeElement type = elemUtils.getTypeElement(imp);
			if (!packetPackage.equals(TypeHelper.getPackage(type)))
			{
				imports.add(imp);
			}
		};
		List<String> saveInstructions = new ArrayList<>();
		List<String> loadInstructions = new ArrayList<>();
		
		sides.addImports(importFilter);
		
		fields.forEach(f -> TypeHelper.addTypeImports(f.asType(), importFilter));
		
		Messager logs = this.processingEnv.getMessager();
		//TODO [v1.2] use method templates, parameters map
		dataHandlers.forEach(handler -> {
			if (handler.annotations instanceof Element)
			{
				logs.printMessage(Kind.NOTE, "Processing field '" + handler.simpleName + "' with DataHandler '" + handler.typeHandler + "'", (Element)handler.annotations);
			}
			else
			{
				logs.printMessage(Kind.NOTE, "Processing field '" + handler.simpleName + "' with DataHandler '" + handler.typeHandler + "'");
			}
			handler.addInstructions(saveInstructions::add, loadInstructions::add, importFilter);
		});
		
		Map<String, String> replacements = new HashMap<>();
		replacements.put("package", className.substring(0, packageSeparator));
		replacements.put("baseClass", className.substring(packageSeparator + 1));
		replacements.put("interfaces", sides.getInterfaces());
		replacements.put("allFields" , fields.stream().map(f -> NamingUtils.simpleTypeName(f.asType()) + " " + f.getSimpleName()).collect(Collectors.joining(", ")));
		replacements.put("fieldsInit", fields.stream().map(f -> "this." + f.getSimpleName() + " = " + f.getSimpleName() + ";").collect(Collectors.joining(ls + "\t\t")));
		replacements.put("toBytes"	, saveInstructions.stream().collect(Collectors.joining(ls + "\t\t")));
		replacements.put("fromBytes", loadInstructions.stream().collect(Collectors.joining(ls + "\t\t")));
		replacements.put("imports", imports.stream().map(i -> "import " + i + ";" + ls).collect(Collectors.joining()));

		this.writeFileFromTemplate(className + "Message", "templates/TemplatePacket.jvtp", replacements);
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
		
		this.writeFileFromTemplate(networkClass, "templates/TemplateNetwork.jvtp", replacements);
	}
	
	private static String registerPacketInstruction(EnumSides sides, String packetClass)
	{
		return "NETWORK.register" + sides.getSimpleName() + "(" + packetClass + ".class);";
	}
	
	private void writeFileFromTemplate(String className, String templateFile, Map<String, String> replacements) throws IOException
	{
		try
		{
			this.processingEnv.getMessager().printMessage(Kind.NOTE, "Generation file '" + className + "' from tmplate '" + templateFile + "'");
			
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
			this.processingEnv.getMessager().printMessage(Kind.ERROR, "An error occured during the generation of the file '" + className + "' from tmplate '" + templateFile + "'");
			throw e;
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
			
			try
			{
				m.appendReplacement(sb, rep == null ? key : rep);
			}
			catch (RuntimeException e)
			{
				throw new RuntimeException("Unable tu replace the value of '" + key + "' in line '" + content + "' with '" + rep + "'", e);
			}
		}
		m.appendTail(sb);
		
		return sb.toString();
	}
	
}
