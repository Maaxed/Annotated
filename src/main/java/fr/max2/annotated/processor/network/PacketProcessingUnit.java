package fr.max2.annotated.processor.network;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;

import fr.max2.annotated.processor.network.model.SimplePacketBuilder;
import fr.max2.annotated.processor.utils.ClassName;
import fr.max2.annotated.processor.utils.ClassRef;
import fr.max2.annotated.processor.utils.EnumSide;
import fr.max2.annotated.processor.utils.NamingUtils;
import fr.max2.annotated.processor.utils.TypeHelper;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;
import fr.max2.annotated.processor.utils.template.TemplateHelper;

public class PacketProcessingUnit
{
	private PacketProcessor processor;
	private final NetworkProcessingUnit network;
	private final ExecutableElement method;
	private final EnumSide side;
	private Optional<? extends AnnotationMirror> annotation;
	public final ClassName messageClassName;

	public PacketProcessingUnit(PacketProcessor processor, NetworkProcessingUnit context, ExecutableElement packetMethod, EnumSide side)
	{
		this.processor = processor;
		this.network = context;
		this.method = packetMethod;
		this.side = side;
		this.annotation = TypeHelper.getAnnotationMirror(processor.typeUtils(), packetMethod, this.side.getAnnotationClass().getCanonicalName());
		
		String className = TypeHelper.getAnnotationValue(this.annotation, "className").map(anno -> anno.toString()).orElse("");
		
		int sep = className.lastIndexOf('.');
		
		String packageName = sep == -1 ? context.enclosingClassName.packageName() : className.substring(0, sep);
		
		if (sep != -1)
		{
			className = className.substring(sep);
		}
		else if (className.isEmpty())
		{
			className = context.enclosingClassName.shortName().replace('.', '_') + "_" + this.method.getSimpleName().toString();
		}
		
		this.messageClassName = new ClassName(packageName, className);
	}
	
	public boolean processPacket()
	{
		try
        {
			return this.writePacket();
        }
        catch (IOException e)
        {
        	this.processor.log(Kind.ERROR, "An IOException occured during the generation of the '" + this.messageClassName.qualifiedName() + "' class: " + e.getMessage(), this.method, this.annotation);
        }
		catch (Exception e)
		{
			this.processor.log(Kind.ERROR, "An unexpected exception occured during the generation of the '" + this.messageClassName.qualifiedName() + "' class: " + e.getClass().getCanonicalName() + ": " + e.getMessage(), this.method, this.annotation);
		}
		return false;
	}
	
	private boolean writePacket() throws IOException
	{
		if (!this.method.getModifiers().contains(Modifier.STATIC))
		{
			this.processor.log(Kind.ERROR, "Packet handler must be static", this.method, this.annotation);
			return false;
		}
		
		Elements elemUtils = this.processor.elementUtils();
		
		List<? extends VariableElement> parameters = this.method.getParameters();
		List<? extends VariableElement> messageParameters = parameters.stream().filter(p -> !this.specialValue(p.asType()).isPresent()).collect(Collectors.toList());
		List<DataHandlerParameters> dataHandlers = messageParameters.stream().map(p -> this.processor.getFinder().getDataType(p)).collect(Collectors.toList());
		
		SimplePacketBuilder builder = new SimplePacketBuilder(elemUtils, messageClassName.packageName());
		
		messageParameters.forEach(f -> TypeHelper.provideTypeImports(f.asType(), builder::addImport));
		
		//TODO [v2.2] use method templates, parameters map
		for (DataHandlerParameters handler : dataHandlers)
		{
			try
			{
				handler.addInstructions(builder);
			}
			catch (IncompatibleTypeException e)
			{
				this.processor.log(Kind.ERROR, "An IncompatibleTypeException occured on the '" + handler.uniqueName + "" + "' parameter: " + e.getMessage(), this.method, this.annotation);
				return false;
			}
			catch (Exception e)
			{
				this.processor.log(Kind.ERROR, "A '" + e.getClass().getCanonicalName() + "' exception occured during the processing of the '" + handler.uniqueName + "" + "' parameter: " + e.getMessage(), this.method, this.annotation);
				return false;
			}
		}

		String ls = System.lineSeparator();
		
		Map<String, String> replacements = new HashMap<>();
		replacements.put("package", messageClassName.packageName());
		replacements.put("className", this.messageClassName.shortName());
		replacements.put("generatorClass", network.networkClassName.shortName());
		replacements.put("allFields" , messageParameters.stream().map(p -> NamingUtils.computeFullName(p.asType()) + " " + p.getSimpleName()).collect(Collectors.joining(", ")));
		replacements.put("fieldsDeclaration", messageParameters.stream().map(p -> "\tprivate " + NamingUtils.computeFullName(p.asType()) + " " + p.getSimpleName() + ";").collect(Collectors.joining(ls)));
		replacements.put("fieldsInit", messageParameters.stream().map(p -> "\t\tthis." + p.getSimpleName() + " = " + p.getSimpleName() + ";").collect(Collectors.joining(ls)));
		replacements.put("encode", builder.saveInstructions(2).collect(Collectors.joining(ls)));
		replacements.put("decode", builder.loadInstructions(2).collect(Collectors.joining(ls)));
		replacements.put("function", network.enclosingClassName.shortName() + "." + this.method.getSimpleName().toString());
		replacements.put("parameters", parameters.stream().map(p -> this.specialValue(p.asType()).orElse("msg." + p.getSimpleName())).collect(Collectors.joining(", ")));
		replacements.put("messageParameters", messageParameters.stream().map(VariableElement::getSimpleName).collect(Collectors.joining(", ")));
		replacements.put("imports", builder.imports.stream().map(i -> "import " + i + ";" + ls).collect(Collectors.joining()));
		replacements.put("serverPacket", Boolean.toString(this.side.isServer()));
		replacements.put("clientPacket", Boolean.toString(this.side.isClient()));
		replacements.put("receiveSide", this.side.getSimpleName());
		replacements.put("sheduled", TypeHelper.getAnnotationValue(this.annotation, "runInMainThread").map(anno -> anno.toString()).orElse("true"));

		return TemplateHelper.writeFileFromTemplateWithLog(this.processor, this.messageClassName.qualifiedName(), "templates/TemplateMessage.jvtp", replacements, this.method, this.annotation);
	}
	
	private Optional<String> specialValue(TypeMirror type)
	{
		TypeElement elem = TypeHelper.asTypeElement(this.processor.typeUtils().asElement(type));
		if (elem != null)
		{
			if (elem.getQualifiedName().contentEquals(ClassRef.FORGE_NETWORK_CONTEXT))
				return Optional.of("ctx");
			
			if (elem.getQualifiedName().contentEquals(ClassRef.SERVER_PLAYER))
				return Optional.of("ctx.getSender()");
		}
		
		return Optional.empty();
	}
}
