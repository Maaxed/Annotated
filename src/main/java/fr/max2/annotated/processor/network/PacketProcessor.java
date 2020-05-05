package fr.max2.annotated.processor.network;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import fr.max2.annotated.api.processor.network.ClientPacket;
import fr.max2.annotated.api.processor.network.PacketGenerator;
import fr.max2.annotated.api.processor.network.ServerPacket;
import fr.max2.annotated.processor.network.model.SimplePacketBuilder;
import fr.max2.annotated.processor.utils.ClassRef;
import fr.max2.annotated.processor.utils.EnumSides;
import fr.max2.annotated.processor.utils.NamingUtils;
import fr.max2.annotated.processor.utils.TypeHelper;
import fr.max2.annotated.processor.utils.Visibility;
import fr.max2.annotated.processor.utils.exceptions.AnnotationStructureException;
import fr.max2.annotated.processor.utils.template.TemplateHelper;

@SupportedAnnotationTypes({ClassRef.GENERATOR_ANNOTATION})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class PacketProcessor extends AbstractProcessor
{
	private DataHandlerParameters.Finder finder;
	private TypeMirror serverPlayerType, networkContextType; 
	
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv)
	{
		super.init(processingEnv);
		this.finder = new DataHandlerParameters.Finder(this.processingEnv);
		
		Elements elemUtils = this.processingEnv.getElementUtils();
		Types typeUtils = this.processingEnv.getTypeUtils();
		
		this.serverPlayerType = typeUtils.erasure(elemUtils.getTypeElement(ClassRef.SERVER_PLAYER).asType());
		this.networkContextType = typeUtils.erasure(elemUtils.getTypeElement(ClassRef.FORGE_NETWORK_CONTEXT).asType());
	}
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		if (roundEnv.processingOver()) return true;
		
		Messager logs = this.processingEnv.getMessager();
		
		logs.printMessage(Kind.NOTE, "Starting " + this.getClass().getCanonicalName() + " annotation processos !");
		
		handleAllPackets(roundEnv);
		
		logs.printMessage(Kind.NOTE, "End of the " + this.getClass().getCanonicalName() + " annotation processos !");
		
		return true;
	}
	
	private void handleAllPackets(RoundEnvironment roundEnv)
	{
		Messager logs = this.processingEnv.getMessager();
		
		for (TypeElement elem : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(PacketGenerator.class)))
		{
			if (elem.getKind() == ElementKind.CLASS)
			{
				logs.printMessage(Kind.NOTE, "Processing packet class generation", elem);
				
				try
				{
					this.writePackets(elem);
				}
				catch (IOException e)
				{
					logs.printMessage(Kind.ERROR, "An error has occured during the generation of the packet class", elem);
					
					throw new UncheckedIOException(e);
				}
			}
		}
	}
	
	private void writePackets(TypeElement packetClass) throws IOException
	{
		Messager logs = this.processingEnv.getMessager();
		
		Elements elemUtils = this.processingEnv.getElementUtils();
		List<? extends Element> members = TypeHelper.getAllAccessibleMembers(packetClass, elemUtils, Visibility.PROTECTED);
		List<ExecutableElement> methods = ElementFilter.methodsIn(members);
		List<String> packets = new ArrayList<>();
		
		for (ExecutableElement method : methods)
		{
			ServerPacket sp = method.getAnnotation(ServerPacket.class);
			ClientPacket cp = method.getAnnotation(ClientPacket.class);
			
			if (sp != null && cp != null)
			{
				logs.printMessage(Kind.ERROR, "Packets can only be send to one side", method);
				
				throw new AnnotationStructureException("Packets can only be send to one side");
			}
			
			if (sp != null)
			{
				packets.add(writePacket(packetClass, method, EnumSides.SERVER, sp.runInServerThread()));
			}
			
			if (cp != null)
			{
				packets.add(writePacket(packetClass, method, EnumSides.CLIENT, cp.runInClientThread()));
			}
		}
		
		writeNetwork(packetClass, packets);
	}
	
	private boolean isSpecialType(TypeMirror type)
	{
		Types typeUtils = this.processingEnv.getTypeUtils();
		return typeUtils.isSameType(type, this.serverPlayerType) || typeUtils.isSameType(type, this.networkContextType);
	}
	
	private String specialValue(TypeMirror type)
	{
		Types typeUtils = this.processingEnv.getTypeUtils();
		
		if (typeUtils.isSameType(type, this.networkContextType))
			return "ctx";
		
		if (typeUtils.isSameType(type, this.serverPlayerType))
			return "ctx.getSender()";
		
		throw new InvalidParameterException("The type '" + NamingUtils.computeFullName(type) + "' is not a special type"); 
	}
	
	private String writePacket(TypeElement packetClass, ExecutableElement packetMethod, EnumSides sides, boolean sheduled) throws IOException
	{
		Messager logs = this.processingEnv.getMessager();
		
		if (!packetMethod.getModifiers().contains(Modifier.STATIC))
		{
			logs.printMessage(Kind.ERROR, "Packet handlers must be static", packetMethod);
			
			throw new AnnotationStructureException("Packet handlers must be static");
		}
		
		Elements elemUtils = this.processingEnv.getElementUtils();
		
		String classEnclosingName = packetClass.getQualifiedName().toString();
		String newClassName = classEnclosingName + "_" + packetMethod.getSimpleName().toString() + "Message";
		
		int packageSeparator = classEnclosingName.lastIndexOf('.');
		String packageName = classEnclosingName.substring(0, packageSeparator);
		PackageElement packetPackage = TypeHelper.getPackage(packetClass);
		
		List<? extends VariableElement> parameters = packetMethod.getParameters();
		List<? extends VariableElement> messageParameters = parameters.stream().filter(p -> !isSpecialType(p.asType())).collect(Collectors.toList());
		List<DataHandlerParameters> dataHandlers = messageParameters.stream().map(p -> this.finder.getDataType(p)).collect(Collectors.toList());
		
		SimplePacketBuilder builder = new SimplePacketBuilder(elemUtils, packetPackage);
		
		messageParameters.forEach(f -> TypeHelper.provideTypeImports(f.asType(), builder::addImport));
		
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
			handler.addInstructions(builder);
		});

		String ls = System.lineSeparator();
		
		Map<String, String> replacements = new HashMap<>();
		replacements.put("package", packageName);
		replacements.put("className", newClassName.substring(packageSeparator + 1));
		replacements.put("generatorClass", classEnclosingName.substring(packageSeparator + 1) + "Network");
		replacements.put("allFields" , messageParameters.stream().map(p -> NamingUtils.computeFullName(p.asType()) + " " + p.getSimpleName()).collect(Collectors.joining(", ")));
		replacements.put("fieldsDeclaration", messageParameters.stream().map(p -> "\tprivate " + NamingUtils.computeFullName(p.asType()) + " " + p.getSimpleName() + ";").collect(Collectors.joining(ls)));
		replacements.put("fieldsInit", messageParameters.stream().map(p -> "\t\tthis." + p.getSimpleName() + " = " + p.getSimpleName() + ";").collect(Collectors.joining(ls)));
		replacements.put("encode", builder.saveInstructions(2).collect(Collectors.joining(ls)));
		replacements.put("decode", builder.loadInstructions(2).collect(Collectors.joining(ls)));
		replacements.put("function", classEnclosingName.substring(packageSeparator + 1) + "." + packetMethod.getSimpleName().toString());
		replacements.put("parameters", parameters.stream().map(p -> isSpecialType(p.asType()) ? specialValue(p.asType()) : "msg." + p.getSimpleName()).collect(Collectors.joining(", ")));
		replacements.put("messageParameters", messageParameters.stream().map(VariableElement::getSimpleName).collect(Collectors.joining(", ")));
		replacements.put("imports", builder.imports.stream().map(i -> "import " + i + ";" + ls).collect(Collectors.joining()));
		replacements.put("serverPacket", Boolean.toString(sides.isServer()));
		replacements.put("clientPacket", Boolean.toString(sides.isClient()));
		replacements.put("receiveSide", sides.getSimpleName());
		replacements.put("sheduled", Boolean.toString(sheduled));

		TemplateHelper.writeFileFromTemplateWithLog(this.processingEnv, newClassName, "templates/TemplateMessage.jvtp", replacements, packetMethod);
		
		return newClassName.substring(packageSeparator + 1);
	}
	
	private void writeNetwork(TypeElement packetClass, List<String> packets) throws IOException
	{
		PacketGenerator generatorData = packetClass.getAnnotation(PacketGenerator.class);
		String classEnclosingName = packetClass.getQualifiedName().toString();
		String newClassName = classEnclosingName + "Network";
		
		int packageSeparator = classEnclosingName.lastIndexOf('.');
		String packageName = classEnclosingName.substring(0, packageSeparator);

		String ls = System.lineSeparator();
		
		List<String> registerPackets = new ArrayList<>();
		
		for (int i = 0; i < packets.size(); i++)
		{
			registerPackets.add("\t\t" + packets.get(i) + ".registerTo(CHANNEL, " + i + ");");
		}
		
		Map<String, String> replacements = new HashMap<>();
		replacements.put("package", packageName);
		replacements.put("className", newClassName.substring(packageSeparator + 1));
		replacements.put("channelName", (generatorData.channelName().isEmpty() ? classEnclosingName.substring(packageSeparator + 1) : generatorData.channelName()).toLowerCase()); //TODO use mod id
		replacements.put("protocolVersion", generatorData.protocolVersion());
		replacements.put("registerPackets", registerPackets.stream().collect(Collectors.joining(ls)));

		TemplateHelper.writeFileFromTemplateWithLog(this.processingEnv, newClassName, "templates/TemplateNetwork.jvtp", replacements, packetClass);
	}
	
}
