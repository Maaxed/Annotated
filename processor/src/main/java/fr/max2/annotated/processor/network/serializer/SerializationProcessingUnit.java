package fr.max2.annotated.processor.network.serializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

import fr.max2.annotated.processor.network.model.SimpleParameterListBuilder;
import fr.max2.annotated.processor.util.ClassName;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.CoderException;
import fr.max2.annotated.processor.util.exceptions.ProcessorException;

public class SerializationProcessingUnit
{
	private final ProcessingTools tools;
	private final TypeElement serialisableClass;
	private final Optional<? extends AnnotationMirror> annotation;
	private final List<? extends VariableElement> fields;
	public final ClassName serializableClassName;
	public final ClassName serializerClassName;
	private boolean hasErrors = false;

	public SerializationProcessingUnit(ProcessingTools tools, TypeElement serialisableClass, Optional<? extends AnnotationMirror> annotation)
	{
		this.tools = tools;
		this.serialisableClass = serialisableClass;
		this.annotation = annotation;
		this.fields = ElementFilter.fieldsIn(serialisableClass.getEnclosedElements());
		this.serializableClassName = tools.naming.buildClassName(serialisableClass);
		
		String className = tools.elements.getAnnotationValue(this.annotation, "className").map(anno -> anno.getValue().toString()).orElse("");
		
		int sep = className.lastIndexOf('.');
		
		String packageName = sep == -1 ? this.serializableClassName.packageName() : className.substring(0, sep);
		
		if (sep != -1)
		{
			className = className.substring(sep + 1);
		}
		else if (className.isEmpty())
		{
			className = this.serializableClassName.shortName().replace('.', '_') + "_Serializer";
		}
		
		this.serializerClassName = new ClassName(packageName, className);
	}
	
	public boolean hasErrors()
	{
		return this.hasErrors;
	}
	
	public void process()
	{
		try
        {
			this.writeSerializer();
			return;
        }
		catch (ProcessorException pe)
		{
			pe.log(this.tools);
		}
		catch (Exception e)
		{
			ProcessorException.builder()
				.context(this.serialisableClass, this.annotation)
				.build("Unexpected exception generating the '" + this.serializerClassName.qualifiedName() + "' class: " + e.getClass().getCanonicalName() + ": " + e.getMessage(), e)
				.log(this.tools);
		}
		this.hasErrors = true;
	}
	
	private void writeSerializer() throws ProcessorException
	{
		List<SerializationCoder> dataCoders = new ArrayList<>();
		
		List<SerializationCoder.Field> serializerFields = new ArrayList<>();
		List<String> encodeCode = new ArrayList<>();
		SimpleParameterListBuilder decodeCode = new SimpleParameterListBuilder();
		for (VariableElement field : this.fields)
		{
			SerializationCoder coder;
			try
			{
				coder = this.tools.coders.getCoder(field);
				dataCoders.add(coder);
			}
			catch (CoderException e)
			{
				throw ProcessorException.builder().context(field).build(e.getMessage(), e);
			}
			catch (Exception e)
			{
				throw ProcessorException.builder().context(field).build("Unable to create a coder: " + e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
			}
			
			try
			{
				SerializationCoder.OutputExpressions output = coder.code(field.getSimpleName().toString());
				output.field.ifPresent(serializerFields::add);
				
				encodeCode.add(output.encodeCode + ";");
				decodeCode.add(output.decodeCode);
			}
			catch (Exception e)
			{
				throw ProcessorException.builder().context(field).build("Unable to produce code : " + e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
			}
		}

		String ls = System.lineSeparator();
		
		Map<String, String> replacements = new HashMap<>();
		replacements.put("package", this.serializerClassName.packageName());
		replacements.put("serializerName", this.serializerClassName.shortName());
		replacements.put("targetName", this.serializerClassName.qualifiedName());
		replacements.put("fieldDeclaration", serializerFields.stream().map(f -> "\tprivate " + f.type + " " + f.uniqueName + ";").collect(Collectors.joining(ls)));
		replacements.put("constructorParams", "");
		replacements.put("fieldInitialization", serializerFields.stream().map(f -> "this." + f.uniqueName + " = " + f.initializationCode + ";").collect(Collectors.joining(ls)));
		replacements.put("encode", encodeCode.stream().collect(Collectors.joining(ls)));
		replacements.put("decode", decodeCode.build());
		
		this.tools.templates.writeFileWithLog(this.serializerClassName.qualifiedName(), "templates/TemplateMessage.jvtp", replacements, this.serialisableClass, this.annotation, this.serialisableClass);
	}
}
