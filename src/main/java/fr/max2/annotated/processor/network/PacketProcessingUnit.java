package fr.max2.annotated.processor.network;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import fr.max2.annotated.processor.network.model.SimplePacketBuilder;
import fr.max2.annotated.processor.utils.ClassName;
import fr.max2.annotated.processor.utils.ClassRef;
import fr.max2.annotated.processor.utils.EnumSide;
import fr.max2.annotated.processor.utils.NamingUtils;
import fr.max2.annotated.processor.utils.TypeHelper;
import fr.max2.annotated.processor.utils.exceptions.AnnotationStructureException;
import fr.max2.annotated.processor.utils.template.TemplateHelper;

public class PacketProcessingUnit
{
	private PacketProcessor processor;
	private final NetworkProcessingUnit context;
	private final ExecutableElement method;
	private final EnumSide side;
	private Optional<? extends AnnotationMirror> annotation;
	public final ClassName messageClassName;

	public PacketProcessingUnit(PacketProcessor processor, NetworkProcessingUnit context, ExecutableElement packetMethod, EnumSide side)
	{
		this.processor = processor;
		this.context = context;
		this.method = packetMethod;
		this.side = side;
		this.annotation = TypeHelper.getAnnotationMirror(processor.typeUtils(), packetMethod, this.side.getAnnotationClass().getCanonicalName());

		this.messageClassName = new ClassName(context.enclosingClassName.packageName(), context.enclosingClassName.shortName().replace('.', '_') + "_" + this.method.getSimpleName().toString());
	}
	
	public void processPacket()
	{
		try
        {
			this.writePacket();
        }
        catch (IOException e)
        {
        	this.processor.log(Kind.ERROR, "An exception has occured during the generation of the packet class", this.method, this.annotation);
            throw new UncheckedIOException("An exception has occured during the generation of the packet class", e);
        }
		catch (Exception e)
		{
			this.processor.log(Kind.ERROR, "An unexpected exception has occured during the generation of the packet class", this.method, this.annotation);
            throw e;
		}
	}
	
	private void writePacket() throws IOException
	{
		if (!this.method.getModifiers().contains(Modifier.STATIC))
		{
			this.processor.log(Kind.ERROR, "Packet handlers must be static", this.method, this.annotation);
			throw new AnnotationStructureException("Packet handlers must be static");
		}
		
		Elements elemUtils = this.processor.elementUtils();
		
		List<? extends VariableElement> parameters = this.method.getParameters();
		List<? extends VariableElement> messageParameters = parameters.stream().filter(p -> !this.isSpecialType(p.asType())).collect(Collectors.toList());
		List<DataHandlerParameters> dataHandlers = messageParameters.stream().map(p -> this.processor.getFinder().getDataType(p)).collect(Collectors.toList());
		
		SimplePacketBuilder builder = new SimplePacketBuilder(elemUtils, messageClassName.packageName());
		
		messageParameters.forEach(f -> TypeHelper.provideTypeImports(f.asType(), builder::addImport));
		
		//TODO [v1.2] use method templates, parameters map
		dataHandlers.forEach(handler -> {
			if (handler.annotations instanceof Element)
			{
				this.processor.log(Kind.NOTE, "Processing field '" + handler.uniqueName + "' with DataHandler '" + handler.typeHandler + "'", (Element)handler.annotations);
			}
			else
			{
				this.processor.log(Kind.NOTE, "Processing field '" + handler.uniqueName + "' with DataHandler '" + handler.typeHandler + "'", this.method);
			}
			handler.addInstructions(builder);
		});

		String ls = System.lineSeparator();
		
		Map<String, String> replacements = new HashMap<>();
		replacements.put("package", messageClassName.packageName());
		replacements.put("className", this.messageClassName.shortName());
		replacements.put("generatorClass", context.networkClassName.shortName());
		replacements.put("allFields" , messageParameters.stream().map(p -> NamingUtils.computeFullName(p.asType()) + " " + p.getSimpleName()).collect(Collectors.joining(", ")));
		replacements.put("fieldsDeclaration", messageParameters.stream().map(p -> "\tprivate " + NamingUtils.computeFullName(p.asType()) + " " + p.getSimpleName() + ";").collect(Collectors.joining(ls)));
		replacements.put("fieldsInit", messageParameters.stream().map(p -> "\t\tthis." + p.getSimpleName() + " = " + p.getSimpleName() + ";").collect(Collectors.joining(ls)));
		replacements.put("encode", builder.saveInstructions(2).collect(Collectors.joining(ls)));
		replacements.put("decode", builder.loadInstructions(2).collect(Collectors.joining(ls)));
		replacements.put("function", context.enclosingClassName.shortName() + "." + this.method.getSimpleName().toString());
		replacements.put("parameters", parameters.stream().map(p -> this.isSpecialType(p.asType()) ? this.specialValue(p.asType()) : "msg." + p.getSimpleName()).collect(Collectors.joining(", ")));
		replacements.put("messageParameters", messageParameters.stream().map(VariableElement::getSimpleName).collect(Collectors.joining(", ")));
		replacements.put("imports", builder.imports.stream().map(i -> "import " + i + ";" + ls).collect(Collectors.joining()));
		replacements.put("serverPacket", Boolean.toString(this.side.isServer()));
		replacements.put("clientPacket", Boolean.toString(this.side.isClient()));
		replacements.put("receiveSide", this.side.getSimpleName());
		replacements.put("sheduled", Boolean.toString(this.side.isSheduled(this.method)));

		TemplateHelper.writeFileFromTemplateWithLog(this.processor, this.messageClassName.qualifiedName(), "templates/TemplateMessage.jvtp", replacements, this.method, this.annotation);
	}
	
	private boolean isSpecialType(TypeMirror type)
	{
		Types typeUtils = this.processor.typeUtils();
		TypeElement elem = TypeHelper.asTypeElement(typeUtils.asElement(type));
		if (elem == null)
			return false;
		
		return elem.getQualifiedName().contentEquals(ClassRef.SERVER_PLAYER) || elem.getQualifiedName().contentEquals(ClassRef.FORGE_NETWORK_CONTEXT);
	}
	
	private String specialValue(TypeMirror type)
	{
		Types typeUtils = this.processor.typeUtils();
		TypeElement elem = TypeHelper.asTypeElement(typeUtils.asElement(type));
		
		if (elem != null)
		{
			if (elem.getQualifiedName().contentEquals(ClassRef.FORGE_NETWORK_CONTEXT))
				return "ctx";
			
			if (elem.getQualifiedName().contentEquals(ClassRef.SERVER_PLAYER))
				return "ctx.getSender()";
		}
		
		throw new InvalidParameterException("The type '" + NamingUtils.computeFullName(type) + "' is not a special type"); 
	}
}
