package fr.max2.annotated.processor.network.serializer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

import fr.max2.annotated.api.network.IgnoreField;
import fr.max2.annotated.api.network.NetworkSerializable;
import fr.max2.annotated.api.network.IncludeField;
import fr.max2.annotated.api.network.NetworkAdaptable;
import fr.max2.annotated.api.network.SelectionMode;
import fr.max2.annotated.processor.model.SimpleCodeBuilder;
import fr.max2.annotated.processor.model.SimpleParameterListBuilder;
import fr.max2.annotated.processor.model.processor.IProcessingUnit;
import fr.max2.annotated.processor.util.ClassName;
import fr.max2.annotated.processor.util.ProcessingStatus;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.Visibility;
import fr.max2.annotated.processor.util.exceptions.CoderException;
import fr.max2.annotated.processor.util.exceptions.ProcessorException;
import fr.max2.annotated.processor.util.exceptions.RoundException;
import fr.max2.annotated.processor.util.template.TemplateHelper.ReplacementMap;

public class SerializationProcessingUnit implements IProcessingUnit
{
	private final ProcessingTools tools;
	public final TypeElement serializableClass;
	public final Optional<? extends AnnotationMirror> annotation;
	private final SelectionMode fieldSelectionMode;
	public final ClassName serializableClassName;
	public final ClassName serializerClassName;

	private SerializationProcessingUnit(ProcessingTools tools, TypeElement serializableClass, Optional<? extends AnnotationMirror> annotation, NetworkSerializable annotationData)
	{
		this.tools = tools;
		this.serializableClass = serializableClass;
		this.annotation = annotation;
		this.fieldSelectionMode = annotationData.fieldSelectionMode();
		this.serializableClassName = tools.naming.buildClassName(serializableClass);

		this.serializerClassName = getSerializerName(this.serializableClassName, annotationData);
	}

	public static SerializationProcessingUnit create(ProcessingTools tools, TypeElement type) throws ProcessorException
	{
		Optional<? extends AnnotationMirror> annotation = tools.elements.getAnnotationMirror(type, NetworkSerializable.class);
		Optional<? extends AnnotationMirror> adaptableAnno = tools.elements.getAnnotationMirror(type, NetworkAdaptable.class);
		if (adaptableAnno.isPresent())
			return null; // Skip this class

		switch (type.getNestingKind())
		{
		default:
		case ANONYMOUS:
		case LOCAL:
			throw ProcessorException.builder()
				.context(type, annotation)
				.build("Anonymous and local classes are not supported !");
		case MEMBER:
			if (!type.getModifiers().contains(Modifier.STATIC))
			{
				throw ProcessorException.builder()
					.context(type, annotation)
					.build("Non-static nested classes are not supported !");
			}
			break;
		case TOP_LEVEL:
			break;
		}

		if (Visibility.getTopLevelVisibility(type) != Visibility.PUBLIC)
		{
			throw ProcessorException.builder()
				.context(type, annotation)
				.build("Non-public classes are not supported !");
		}

		return new SerializationProcessingUnit(tools, type, annotation, type.getAnnotation(NetworkSerializable.class));
	}

	public static ClassName getSerializerName(ClassName serializableName, @Nullable NetworkSerializable annotationData)
	{
		String className = annotationData == null ? "" : annotationData.serializerClassName();

		int sep = className.lastIndexOf('.');

		String packageName = serializableName.packageName();

		if (sep != -1)
		{
			className = className.substring(sep + 1);
			packageName = className.substring(0, sep);
		}
		else if (className.isEmpty())
		{
			className = serializableName.shortName().replace('.', '_') + "_Serializer";
		}

		return new ClassName(packageName, className);
	}

	@Override
	public ClassName getTargetClassName()
	{
		return this.serializableClassName;
	}

	@Override
	public ProcessingStatus process()
	{
		try
        {
			this.writeSerializer();
			return ProcessingStatus.SUCESSS;
        }
		catch (ProcessorException pe)
		{
			pe.log(this.tools);
		}
		catch (RoundException re)
		{
			return ProcessingStatus.DEFERRED;
		}
		catch (Exception e)
		{
			ProcessorException.builder()
				.context(this.serializableClass, this.annotation)
				.build("Unexpected exception generating the '" + this.serializerClassName.qualifiedName() + "' class: " + e.getClass().getCanonicalName() + ": " + e.getMessage(), e)
				.log(this.tools);
		}
		return ProcessingStatus.FAIL;
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
				coder = this.tools.serializerCoders.getCoder(data.originElement);
				dataCoders.add(coder);
			}
			catch (CoderException e)
			{
				throw ProcessorException.builder().context(data.originElement).build(e.getMessage(), e);
			}
			catch (RoundException re)
			{
				throw re;
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
			catch (RoundException re)
			{
				throw re;
			}
			catch (Exception e)
			{
				throw ProcessorException.builder().context(data.originElement).build("Unable to produce code : " + e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
			}
		}

		SimpleCodeBuilder decodeCodeBuilder = new SimpleCodeBuilder();
		try
		{
			decodeCode.pipe(decodeCodeBuilder);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}

		ReplacementMap replacements = new ReplacementMap();
		replacements.putString("package", this.serializerClassName.packageName());
		replacements.putString("serializerName", this.serializerClassName.shortName());
		replacements.putString("targetName", this.serializableClassName.qualifiedName());
		replacements.putLines("fieldDeclaration", serializerFields.stream().map(f -> "\tprivate " + f.type + " " + f.uniqueName + ";"));
		replacements.putLines("fieldInitialization", serializerFields.stream().map(f -> "this." + f.uniqueName + " = " + f.initializationCode + ";"));
		replacements.putLines("encode", encodeCode.stream());
		replacements.putCode("decode", decodeCodeBuilder);

		this.tools.templates.writeFileWithLog(this.serializerClassName.qualifiedName(), "templates/TemplateSerializer.jvtp", replacements, this.serializableClass, this.annotation, this.serializableClass);
	}

	private List<Data> getDataToSerialize()
	{
		if (this.serializableClass.getKind() == ElementKind.RECORD)
		{
			return this.serializableClass.getRecordComponents()
				.stream()
				.filter(this::shouldSerializeElement)
				.map(c -> Data.fromRecordComp(this.tools, c))
				.toList();
		}
		else
		{
			return ElementFilter.fieldsIn(this.serializableClass.getEnclosedElements())
				.stream()
				.filter(this::shouldSerializeElement)
				.map(f -> Data.fromField(this.tools, f))
				.toList();
		}
	}

	public boolean shouldSerializeElement(Element element)
	{
		IncludeField include = element.getAnnotation(IncludeField.class);
		IgnoreField ignore = element.getAnnotation(IgnoreField.class);

		if (element.getModifiers().contains(Modifier.STATIC))
		{
			if (include != null)
			{
				ProcessorException.builder()
					.context(element)
					.build("A static field cannot have the " + IncludeField.class.getSimpleName() + " annotations")
					.log(this.tools);
			}
			if (ignore != null)
			{
				ProcessorException
					.builder()
					.context(element)
					.build("A static field cannot have the " + IgnoreField.class.getSimpleName() + " annotations")
					.log(this.tools);
			}
			return false;
		}

		if (include != null && ignore != null)
			throw ProcessorException.builder().context(element).build("A field cannot have both ProcessField and IgnoreField annotations");

		if (include != null)
			return true;

		if (ignore != null)
			return false;

		boolean keep;
		switch (this.fieldSelectionMode)
		{
		case ALL:
			keep = true;
			break;
		case NONE:
			keep = false;
			break;
		default:
		case PUBLIC:
			keep = this.serializableClass.getKind() == ElementKind.RECORD || element.getModifiers().contains(Modifier.PUBLIC);
			break;
		}

		return keep;
	}

	private static class Data
	{
		private final Element originElement;
		private final String baseName;
		private final Element accessElement;

		public Data(ProcessingTools tools, Element originElement, String baseName, Element accessElement)
		{
			IncludeField include = originElement.getAnnotation(IncludeField.class);
			this.originElement = originElement;
			this.baseName = baseName;
			if (include == null || include.getter().isEmpty())
			{
				this.accessElement = accessElement;
			}
			else
			{
				ExecutableElement getter = ElementFilter.methodsIn(originElement.getEnclosingElement().getEnclosedElements())
										.stream()
										.filter(elem -> elem.getSimpleName().contentEquals(include.getter()))
										.filter(elem -> elem.getParameters().isEmpty())
										.reduce((a, b) -> { throw ProcessorException.builder().context(originElement).build("Found multiple matching getter methods with name '" + include.getter() + "' in class '" + originElement.getEnclosingElement() + "'"); })
										.orElseThrow(() -> ProcessorException.builder().context(originElement).build("Cannot find the getter method '" + include.getter() + "' in class '" + originElement.getEnclosingElement() + "'"));

				this.accessElement = getter;

				if (!tools.types.isSameType(getter.getReturnType(), originElement.asType()))
					ProcessorException.builder().context(originElement).build("The getter method '" + include.getter() + "' has a wrong type: expected " + originElement.asType() + ", but got " + getter.getReturnType());
			}
			if (!this.accessElement.getModifiers().contains(Modifier.PUBLIC))
				ProcessorException.builder().context(originElement).build("The element '" + this.accessElement + "' is not public");
		}

		public static Data fromField(ProcessingTools tools, VariableElement field)
		{
			return new Data(tools, field, field.getSimpleName().toString(), field);
		}

		public static Data fromRecordComp(ProcessingTools tools, RecordComponentElement comp)
		{
			return new Data(tools, comp, comp.getSimpleName().toString(), comp.getAccessor());
		}
	}
}
