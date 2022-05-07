package fr.max2.annotated.processor.network.serializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import fr.max2.annotated.api.network.NetworkAdaptable;
import fr.max2.annotated.api.network.NetworkSerializable;
import fr.max2.annotated.processor.model.processor.UnitProcessor;
import fr.max2.annotated.processor.util.exceptions.ProcessorException;

public class SerializationProcessor extends UnitProcessor<SerializationProcessingUnit>
{
	public SerializationProcessor()
	{
		super(NetworkSerializable.class, NetworkAdaptable.class);
	}

	@Override
	protected Collection<SerializationProcessingUnit> buildProcessingUnits(RoundEnvironment roundEnv)
	{
		List<SerializationProcessingUnit> units = new ArrayList<>(this.deferredUnits);
		this.deferredUnits.clear();

		for (TypeElement type : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(NetworkSerializable.class)))
		{
			try
			{
				SerializationProcessingUnit unit = SerializationProcessingUnit.create(this.tools, type);
				if (unit != null)
					units.add(unit);
			}
			catch (ProcessorException e)
			{
				e.log(this.tools);
				continue;
			}
		}

		return units;
	}

	@Override
	protected void onFailedToDeferProcess(SerializationProcessingUnit unit)
	{
		ProcessorException.builder()
			.context(unit.serializableClass, unit.annotation)
			.build("Could not process serializable type !")
			.log(this.tools);
	}
}
