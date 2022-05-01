package fr.max2.annotated.processor.network.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;

import fr.max2.annotated.api.network.IgnoreField;
import fr.max2.annotated.api.network.IncludeField;
import fr.max2.annotated.api.network.NetworkAdaptable;
import fr.max2.annotated.api.network.NetworkSerializable;
import fr.max2.annotated.api.network.SelectionMode;
import fr.max2.annotated.processor.network.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.network.model.SimpleParameterListBuilder;
import fr.max2.annotated.processor.util.ClassName;
import fr.max2.annotated.processor.util.ProcessingStatus;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.CoderException;
import fr.max2.annotated.processor.util.exceptions.ProcessorException;
import fr.max2.annotated.processor.util.exceptions.RoundException;

public class AdapterProcessingUnit
{
	private final ProcessingTools tools;
	public final TypeElement adaptableClass;
	public final Optional<? extends AnnotationMirror> annotation;
	private final Optional<? extends AnnotationMirror> serializableAnnotation;
	private final SelectionMode fieldSelectionMode;
	public final ClassName adaptableClassName;
	public final ClassName adapterClassName;
	public final ClassName adaptedClassName;
	private ProcessingStatus status = ProcessingStatus.SUCESSS;

	public AdapterProcessingUnit(ProcessingTools tools, TypeElement serializableClass, Optional<? extends AnnotationMirror> annotation, NetworkAdaptable annotationData, Optional<? extends AnnotationMirror> serializableAnnotation, NetworkSerializable serializableData)
	{
		this.tools = tools;
		this.adaptableClass = serializableClass;
		this.annotation = annotation;
		this.serializableAnnotation = serializableAnnotation;
		this.fieldSelectionMode = serializableData.fieldSelectionMode();
		this.adaptableClassName = tools.naming.buildClassName(serializableClass);

		this.adapterClassName = getAdapterName(this.adaptableClassName, annotationData);
		this.adaptedClassName = getAdaptedName(this.adaptableClassName, annotationData);
	}

	public static ClassName getAdapterName(ClassName serializableName, NetworkAdaptable annotationData)
	{
		String className = annotationData.adapterClassName();

		int sep = className.lastIndexOf('.');

		String packageName = serializableName.packageName();

		if (sep != -1)
		{
			className = className.substring(sep + 1);
			packageName = className.substring(0, sep);
		}
		else if (className.isEmpty())
		{
			className = serializableName.shortName().replace('.', '_') + "_Adapter";
		}

		return new ClassName(packageName, className);
	}

	public static ClassName getAdaptedName(ClassName serializableName, NetworkAdaptable annotationData)
	{
		String className = annotationData.adaptedClassName();

		int sep = className.lastIndexOf('.');

		String packageName = serializableName.packageName();

		if (sep != -1)
		{
			className = className.substring(sep + 1);
			packageName = className.substring(0, sep);
		}
		else if (className.isEmpty())
		{
			className = serializableName.shortName().replace('.', '_') + "_Adapted";
		}

		return new ClassName(packageName, className);
	}

	public ProcessingStatus getStatus()
	{
		return this.status;
	}

	public void process()
	{
		try
		{
			this.writeAdapter();
			this.status = ProcessingStatus.SUCESSS;
			return;
		}
		catch (ProcessorException pe)
		{
			pe.log(this.tools);
		}
		catch (RoundException re)
		{
			this.status = ProcessingStatus.DEFERRED;
			return;
		}
		catch (Exception e)
		{
			ProcessorException.builder()
				.context(this.adaptableClass, this.annotation)
				.build("Unexpected exception generating the '" + this.adapterClassName.qualifiedName() + "' class: " + e.getClass().getCanonicalName() + ": " + e.getMessage(), e)
				.log(this.tools);
		}
		this.status = ProcessingStatus.FAIL;
	}

	private void writeAdapter() throws ProcessorException
	{
		List<AdapterCoder> dataCoders = new ArrayList<>();

		boolean hasAnyConversion = false;
		List<AdapterCoder.Field> serializerFields = new ArrayList<>();
		SimpleParameterListBuilder toNetworkCode = new SimpleParameterListBuilder();
		SimpleParameterListBuilder fromNetworkCode = new SimpleParameterListBuilder();
		SimpleParameterListBuilder adaptedCode = new SimpleParameterListBuilder();
		for (Data data : this.getDataToSerialize())
		{
			AdapterCoder coder;
			try
			{
				TypeMirror type = data.originElement.asType();

				ICoderHandler<AdapterCoder> handler = this.tools.adapterCoders.getHandler(type).orElseThrow();

				coder = handler.createCoder(type);

				if (!(coder instanceof IdentityCoder))
					hasAnyConversion = true;

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
				AdapterCoder.OutputExpressions output = coder.code(data.baseName, "value." + data.accessElement.getSimpleName() + (data.accessElement.getKind().isField() ? "" : "()"), "value." + data.baseName + "()", "ctx");
				output.field.ifPresent(serializerFields::add);

				toNetworkCode.add(output.toNetworkCode);
				fromNetworkCode.add(output.fromNetworkCode);
				adaptedCode.add(coder.typeTo + " " + data.baseName);
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

		if (!hasAnyConversion)
			this.tools.log(Kind.WARNING, "The type '" + this.adaptableClass + "' has no data to convert ! Please remove the " + NetworkAdaptable.class.getName() + " annotation", this.adaptableClass, this.annotation);

		String ls = System.lineSeparator();

		// Write adapted
		Map<String, String> adaptedRepl = new HashMap<>();
		adaptedRepl.put("package", this.adaptedClassName.packageName());
		adaptedRepl.put("adaptedName", this.adaptedClassName.shortName());
		adaptedRepl.put("annotations", this.serializableAnnotation.map(anno -> anno.toString()).orElse(""));
		adaptedRepl.put("fieldDeclaration", adaptedCode.buildMultiLines());

		this.tools.templates.writeFileWithLog(this.adaptedClassName.qualifiedName(), "templates/TemplateAdapted.jvtp", adaptedRepl, this.adaptableClass, this.annotation, this.adaptableClass);

		// Write adapter
		Map<String, String> adapterRepl = new HashMap<>();
		adapterRepl.put("package", this.adapterClassName.packageName());
		adapterRepl.put("adapterName", this.adapterClassName.shortName());
		adapterRepl.put("targetFromName", this.adaptableClassName.qualifiedName());
		adapterRepl.put("targetToName", this.adaptedClassName.qualifiedName());
		adapterRepl.put("fieldDeclaration", serializerFields.stream().map(f -> "\tprivate " + f.type + " " + f.uniqueName + ";").collect(Collectors.joining(ls)));
		adapterRepl.put("fieldInitialization", serializerFields.stream().map(f -> "this." + f.uniqueName + " = " + f.initializationCode + ";").collect(Collectors.joining(ls)));
		adapterRepl.put("adaptToNetwork", toNetworkCode.buildMultiLines());
		adapterRepl.put("adaptFromNetwork", fromNetworkCode.buildMultiLines());

		this.tools.templates.writeFileWithLog(this.adapterClassName.qualifiedName(), "templates/TemplateAdapter.jvtp", adapterRepl, this.adaptableClass, this.annotation, this.adaptableClass);
	}

	private List<Data> getDataToSerialize()
	{
		if (this.adaptableClass.getKind() == ElementKind.RECORD)
		{
			return this.adaptableClass.getRecordComponents()
				.stream()
				.filter(this::shouldSerializeElement)
				.map(c -> Data.fromRecordComp(this.tools, c))
				.toList();
		}
		else
		{
			return ElementFilter.fieldsIn(this.adaptableClass.getEnclosedElements())
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
			keep = this.adaptableClass.getKind() == ElementKind.RECORD || element.getModifiers().contains(Modifier.PUBLIC);
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
