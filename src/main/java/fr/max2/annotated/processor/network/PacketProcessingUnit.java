package fr.max2.annotated.processor.network;

import java.util.ArrayList;
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
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import fr.max2.annotated.processor.network.coder.DataCoder;
import fr.max2.annotated.processor.network.model.EnumSide;
import fr.max2.annotated.processor.network.model.SimplePacketBuilder;
import fr.max2.annotated.processor.utils.ClassName;
import fr.max2.annotated.processor.utils.ClassRef;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.exceptions.CoderExcepetion;

public class PacketProcessingUnit
{
	private final ProcessingTools tools;
	private final NetworkProcessingUnit network;
	private final ExecutableElement method;
	private final EnumSide side;
	private final Optional<? extends AnnotationMirror> annotation;
	public final ClassName messageClassName;
	private boolean hasErrors = false;

	public PacketProcessingUnit(ProcessingTools tools, NetworkProcessingUnit context, ExecutableElement packetMethod, EnumSide side)
	{
		this.tools = tools;
		this.network = context;
		this.method = packetMethod;
		this.side = side;
		this.annotation = tools.elements.getAnnotationMirror(packetMethod, this.side.getAnnotationClass().getCanonicalName());
		
		String className = tools.elements.getAnnotationValue(this.annotation, "className").map(anno -> anno.getValue().toString()).orElse("");
		
		int sep = className.lastIndexOf('.');
		
		String packageName = sep == -1 ? context.enclosingClassName.packageName() : className.substring(0, sep);
		
		if (sep != -1)
		{
			className = className.substring(sep + 1);
		}
		else if (className.isEmpty())
		{
			className = context.enclosingClassName.shortName().replace('.', '_') + "_" + this.method.getSimpleName().toString();
		}
		
		this.messageClassName = new ClassName(packageName, className);
	}
	
	public boolean hasErrors()
	{
		return this.hasErrors;
	}
	
	public void processPacket()
	{
		try
        {
			if (this.writePacket())
				return;
        }
		catch (Exception e)
		{
			this.tools.log(Kind.ERROR, "Unexpected exception generating the '" + this.messageClassName.qualifiedName() + "' class: " + e.getClass().getCanonicalName() + ": " + e.getMessage(), this.method, this.annotation);
		}
		this.hasErrors = true;
	}
	
	private boolean writePacket()
	{
		if (!this.method.getModifiers().contains(Modifier.STATIC))
		{
			this.tools.log(Kind.ERROR, "The packet method must be static", this.method, this.annotation);
			return false;
		}
		
		if (this.method.getReturnType().getKind() != TypeKind.VOID)
		{
			this.tools.log(Kind.ERROR, "The packet method must retrun void", this.method, this.annotation);
			return false;
		}
		
		List<? extends VariableElement> parameters = this.method.getParameters();
		List<? extends VariableElement> messageParameters = parameters.stream().filter(p -> !this.specialValue(p.asType()).isPresent()).collect(Collectors.toList());
		
		SimplePacketBuilder builder = new SimplePacketBuilder(this.tools, messageClassName.packageName());
		
		List<DataCoder> dataCoders = new ArrayList<>();
		
		Map<String, String> parameterValues = new HashMap<>();
		for (VariableElement param : messageParameters)
		{
			DataCoder coder;
			try
			{
				coder = this.tools.handlers.getDataType(param);
				dataCoders.add(coder);
			}
			catch (CoderExcepetion e)
			{
				this.tools.log(Kind.ERROR, e.getMessage(), param);
				return false;
			}
			catch (Exception e)
			{
				this.tools.log(Kind.ERROR, "Unable to create a coder: " + e.getClass().getCanonicalName() + ": " + e.getMessage(), param);
				return false;
			}
			
			this.tools.types.provideTypeImports(param.asType(), builder);
			
			try
			{
				this.tools.types.provideTypeImports(coder.getInternalType(), builder);
				DataCoder.OutputExpressions paramOutput = builder.runCoder(coder, "msg." + coder.uniqueName, coder.uniqueName, "msg." + coder.uniqueName);
				builder.decoder().add("msg." + coder.uniqueName + " = " + paramOutput.decoded + ";");
				builder.internalizer().add("this." + coder.uniqueName + " = " + paramOutput.internalized + ";");
				parameterValues.put(coder.uniqueName, paramOutput.externalized);
				
				coder.properties.checkUnusedProperties();
			}
			catch (Exception e)
			{
				this.tools.log(Kind.ERROR, "Unable to produce code : " + e.getClass().getCanonicalName() + ": " + e.getMessage(), param);
				return false;
			}
		}

		String ls = System.lineSeparator();
		String sheduled = this.tools.elements.getAnnotationValue(this.annotation, "runInMainThread").map(anno -> anno.getValue().toString()).orElse("true");
		
		Map<String, String> replacements = new HashMap<>();
		replacements.put("package", messageClassName.packageName());
		replacements.put("className", this.messageClassName.shortName());
		replacements.put("networkClass", network.networkClassName.shortName());
		replacements.put("allFields" , messageParameters.stream().map(p -> this.tools.naming.computeFullName(p.asType()) + " " + p.getSimpleName()).collect(Collectors.joining(", ")));
		replacements.put("fieldsDeclaration", dataCoders.stream().map(c -> "\tprivate " + this.tools.naming.computeFullName(c.getInternalType()) + " " + c.uniqueName + ";").collect(Collectors.joining(ls)));
		replacements.put("internalize", builder.internalizeFunction.instructions(2).collect(Collectors.joining(ls)));
		replacements.put("externalize", builder.externalizeFunction.instructions(sheduled.equals("true") ? 3 : 2).collect(Collectors.joining(ls)));
		replacements.put("encode", builder.encodeFunction.instructions(2).collect(Collectors.joining(ls)));
		replacements.put("decode", builder.decodeFunction.instructions(2).collect(Collectors.joining(ls)));
		replacements.put("function", network.enclosingClassName.shortName() + "." + this.method.getSimpleName().toString());
		replacements.put("parameters", parameters.stream().map(p -> this.specialValue(p.asType()).orElse(parameterValues.get(p.getSimpleName().toString()))).collect(Collectors.joining(", ")));
		replacements.put("messageParameters", messageParameters.stream().map(VariableElement::getSimpleName).collect(Collectors.joining(", ")));
		replacements.put("imports", builder.imports.stream().map(i -> "import " + i + ";" + ls).collect(Collectors.joining()));
		replacements.put("serverPacket", Boolean.toString(this.side.isServer()));
		replacements.put("clientPacket", Boolean.toString(this.side.isClient()));
		replacements.put("receiveSide", this.side.getSimpleName().toUpperCase());
		replacements.put("sheduled", sheduled);
		replacements.put("modulesContent", builder.modules.stream().map(this::readModule).flatMap(List::stream).map(l -> '\t' + l).collect(Collectors.joining()));
		
		return this.tools.templates.writeFileWithLog(this.messageClassName.qualifiedName(), "templates/TemplateMessage.jvtp", replacements, this.method, this.annotation);
	}
	
	private List<String> readModule(String module)
	{
		List<String> lines = new ArrayList<>();
		this.tools.templates.readWithLog(module, new HashMap<>(), lines::add, this.method, this.annotation);
		return lines;
	}
	
	private Optional<String> specialValue(TypeMirror type)
	{
		TypeElement elem = this.tools.elements.asTypeElement(this.tools.types.asElement(type));
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
