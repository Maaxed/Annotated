package fr.max2.annotated.processor.network.adapter;

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

public class AdapterProcessor extends UnitProcessor<AdapterProcessingUnit>
{
	public AdapterProcessor()
	{
		super(NetworkSerializable.class, NetworkAdaptable.class);
	}

	@Override
	protected Collection<AdapterProcessingUnit> buildProcessingUnits(RoundEnvironment roundEnv)
	{
		List<AdapterProcessingUnit> units = new ArrayList<>(this.deferredUnits);
		this.deferredUnits.clear();

		for (TypeElement type : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(NetworkAdaptable.class)))
		{
			try
			{
				AdapterProcessingUnit unit = AdapterProcessingUnit.create(this.tools, type);
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
	protected void onFailedToDeferProcess(AdapterProcessingUnit unit)
	{
		ProcessorException.builder()
			.context(unit.adaptableClass, unit.annotation)
			.build("Could not process adaptable type !")
			.log(this.tools);
	}
}
