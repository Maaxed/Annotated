package fr.max2.annotated.processor.coder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import fr.max2.annotated.processor.coder.handler.ICoderHandler;
import fr.max2.annotated.processor.util.ProcessingTools;
import fr.max2.annotated.processor.util.exceptions.CoderException;
import fr.max2.annotated.processor.util.exceptions.IncompatibleTypeException;

public abstract class CoderFinder<C>
{
	protected final ProcessingTools tools;
	protected final TypeMirror interfaceType;
	protected final Collection<ICoderHandler<C>> spacialHandlers = new ArrayList<>();
	protected final Collection<ICoderHandler<C>> handlers = new ArrayList<>();

	public CoderFinder(ProcessingTools tools, CharSequence interfaceName)
	{
		this.tools = tools;
		this.interfaceType = tools.elements.getTypeElement(interfaceName).asType();
	}

	protected void scanClass(CharSequence classFullName)
	{
		TypeElement elem = this.tools.elements.getTypeElement(classFullName);
		if (elem == null)
			throw new RuntimeException("Cannot find serializer class '" + classFullName + "'");

		this.scanClass(elem);
	}

	private void scanClass(TypeElement elem)
	{
		for (VariableElement field : ElementFilter.fieldsIn(elem.getEnclosedElements()))
		{
			if (!field.getModifiers().contains(Modifier.PUBLIC))
				continue; // Skip if private

			if (!field.getModifiers().contains(Modifier.STATIC))
				continue; // Skip if not static

			this.registerConstant(field);
		}
	}

	protected void registerConstant(CharSequence classFullName, CharSequence constantName)
	{
		this.registerConstant(this.tools.elements.getTypeElement(classFullName), constantName);
	}

	private void registerConstant(TypeElement elem, CharSequence constantName)
	{
		VariableElement field = ElementFilter.fieldsIn(elem.getEnclosedElements())
			.stream()
			.filter(f -> f.getSimpleName().contentEquals(constantName))
			.filter(f -> f.getModifiers().contains(Modifier.PUBLIC))
			.filter(f -> f.getModifiers().contains(Modifier.STATIC))
			.reduce((a, b) -> { throw new RuntimeException("Multiple fields with the same name: '" + constantName + "'"); })
			.orElseThrow(() -> new RuntimeException("No field find with the given name: '" + constantName + "'"));

		this.registerConstant(field);
	}

	private void registerConstant(VariableElement field)
	{
		TypeMirror type = field.asType();
		DeclaredType refinedType = this.tools.types.refineTo(type, this.interfaceType);

		if (refinedType == null)
			throw new RuntimeException("The type of the serializer field '" + field + "' doesn't implement '" + this.interfaceType + "'");

		this.handlers.add(this.constantToHandler(field, refinedType));
	}

	protected abstract ICoderHandler<C> constantToHandler(VariableElement field, DeclaredType type);

	public C getCoder(Element field) throws CoderException
	{
		/*Element elem = this.tools.types.asElement(field.asType());
		DataProperties typeData = elem == null ? null : elem.getAnnotation(DataProperties.class);
		PropertyMap typeProperties = typeData == null ? PropertyMap.EMPTY_PROPERTIES : PropertyMap.fromArray(typeData.value());

		DataProperties customData = field.getAnnotation(DataProperties.class);
		PropertyMap customProperties = customData == null ? PropertyMap.EMPTY_PROPERTIES : PropertyMap.fromArray(customData.value());

		PropertyMap properties = typeProperties.overrideWith(customProperties);*/
		return this.getCoder(field.asType());
	}

	public C getCoder(TypeMirror type) throws CoderException
	{
		C params = this.getCoderOrNull(type);

		if (params == null)
			this.onFailedToFindCoder(type);

		return params;
	}

	protected void onFailedToFindCoder(TypeMirror type) throws CoderException
	{
		throw new IncompatibleTypeException("No data coder found to process the type '" + type.toString() + "'");
	}

	public C getCoderOrNull(TypeMirror type) throws CoderException
	{
		ICoderHandler<C> handler = this.getHandler(type).orElse(null);
		if (handler == null)
			return null;

		return handler.createCoder(type);
	}

	public Optional<ICoderHandler<C>> getHandler(TypeMirror type)
	{
		return this.getSpecialHandler(type).or(() -> this.getDefaultHandler(type));
	}

	protected Optional<ICoderHandler<C>> getDefaultHandler(TypeMirror type)
	{
		MaxResult<C> res = this.handlers.stream().collect(MaxResult.collector(type));

		if (!res.maxCompat.isCompatible())
			return Optional.empty();

		if (res.serializers.size() == 1)
			return Optional.of(res.serializers.get(0));

		throw new IllegalArgumentException("The data handler of the '" + type.toString() + "' type couldn't be chosen: handler priorities are equal: " + res.serializers.stream().map(h -> h.getClass().getTypeName() + ":" + h.toString()).collect(Collectors.joining(", ")));
	}

	protected Optional<ICoderHandler<C>> getSpecialHandler(TypeMirror type)
	{
		return this.spacialHandlers.stream().filter(entry -> entry.getCompatibilityFor(type).isCompatible()).findAny();
	}

	public static class MaxResult<C>
	{
		private final TypeMirror type;
		private CoderCompatibility maxCompat = CoderCompatibility.INCOMPATIBLE;
		private final List<ICoderHandler<C>> serializers = new ArrayList<>();

		public MaxResult(TypeMirror type)
		{
			this.type = type;
		}

		public void accumulate(ICoderHandler<C> val)
		{
			CoderCompatibility compat = val.getCompatibilityFor(this.type);
			int cmp = compat.compareTo(this.maxCompat);
			if (cmp >= 0)
			{
				if (cmp > 0)
				{
					this.maxCompat = compat;
					this.serializers.clear();
				}
				this.serializers.add(val);
			}
		}

		public MaxResult<C> combine(MaxResult<C> other)
		{
			return this;
		}

		public static <C> Collector<ICoderHandler<C>, MaxResult<C>, MaxResult<C>> collector(TypeMirror type)
		{
			return Collector.of(() -> new MaxResult<>(type), MaxResult::accumulate, MaxResult::combine);
		}
	}
}
