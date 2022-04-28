package fr.max2.annotated.processor.network.serializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

import fr.max2.annotated.processor.network.model.SimpleCodeBuilder;
import fr.max2.annotated.processor.network.model.SimpleParameterListBuilder;
import fr.max2.annotated.processor.util.ClassName;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.CoderException;
import fr.max2.annotated.processor.util.exceptions.ProcessorException;

public class SerializationProcessingUnit
{
	private final ProcessingTools tools;
	private final TypeElement serializableClass;
	private final Optional<? extends AnnotationMirror> annotation;
	public final ClassName serializableClassName;
	public final ClassName serializerClassName;
	private boolean hasErrors = false;

	public SerializationProcessingUnit(ProcessingTools tools, TypeElement serializableClass, Optional<? extends AnnotationMirror> annotation)
	{
		this.tools = tools;
		this.serializableClass = serializableClass;
		this.annotation = annotation;
		this.serializableClassName = tools.naming.buildClassName(serializableClass);
		
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
				.context(this.serializableClass, this.annotation)
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
		for (Data data : this.getDataToSerialize())
		{
			SerializationCoder coder;
			try
			{
				coder = this.tools.coders.getCoder(data.originElement);
				dataCoders.add(coder);
			}
			catch (CoderException e)
			{
				throw ProcessorException.builder().context(data.originElement).build(e.getMessage(), e);
			}
			catch (Exception e)
			{
				throw ProcessorException.builder().context(data.originElement).build("Unable to create a coder: " + e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
			}
			
			try
			{
				SerializationCoder.OutputExpressions output = coder.code(data.baseName, "value." + data.accessElement.getSimpleName() + (data.accessElement.getKind().isField() ? "" : "()"));
				output.field.ifPresent(serializerFields::add);
				
				encodeCode.add(output.encodeCode + ";");
				decodeCode.add(output.decodeCode);
			}
			catch (Exception e)
			{
				throw ProcessorException.builder().context(data.originElement).build("Unable to produce code : " + e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
			}
		}
		
		SimpleCodeBuilder decodeCodeBuilder = new SimpleCodeBuilder();
		decodeCode.build(decodeCodeBuilder);

		String ls = System.lineSeparator();
		
		Map<String, String> replacements = new HashMap<>();
		replacements.put("package", this.serializerClassName.packageName());
		replacements.put("serializerName", this.serializerClassName.shortName());
		replacements.put("targetName", this.serializableClassName.qualifiedName());
		replacements.put("fieldDeclaration", serializerFields.stream().map(f -> "\tprivate " + f.type + " " + f.uniqueName + ";").collect(Collectors.joining(ls)));
		replacements.put("constructorParams", "");
		replacements.put("fieldInitialization", serializerFields.stream().map(f -> "this." + f.uniqueName + " = " + f.initializationCode + ";").collect(Collectors.joining(ls)));
		replacements.put("encode", encodeCode.stream().collect(Collectors.joining(ls)));
		replacements.put("decode", decodeCodeBuilder.build());
		
		this.tools.templates.writeFileWithLog(this.serializerClassName.qualifiedName(), "templates/TemplateSerializer.jvtp", replacements, this.serializableClass, this.annotation, this.serializableClass);
	}
	
	private List<Data> getDataToSerialize()
	{
		if (this.serializableClass.getKind() == ElementKind.RECORD)
		{
			return this.serializableClass.getRecordComponents().stream().map(Data::fromRecordComp).toList();
		}
		else
		{
			return ElementFilter.fieldsIn(this.serializableClass.getEnclosedElements()).stream().map(Data::fromField).toList();
		}
	}
	
	private static class Data
	{
		private final Element originElement;
		private final String baseName;
		private final Element accessElement;
		
		public Data(Element originElement, String baseName, Element accessElement)
		{
			this.originElement = originElement;
			this.baseName = baseName;
			this.accessElement = accessElement;
		}
		
		public static Data fromField(VariableElement field)
		{
			return new Data(field, field.getSimpleName().toString(), field);
		}
		
		public static Data fromRecordComp(RecordComponentElement comp)
		{
			return new Data(comp, comp.getSimpleName().toString(), comp.getAccessor());
		}
	}
}
