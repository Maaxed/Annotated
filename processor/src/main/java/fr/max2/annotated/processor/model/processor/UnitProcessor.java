package fr.max2.annotated.processor.model.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.tools.Diagnostic.Kind;

import fr.max2.annotated.processor.util.ClassName;
import fr.max2.annotated.processor.util.ProcessingStatus;

public abstract class UnitProcessor<U extends IProcessingUnit> extends BaseProcessor
{
	protected final Set<ClassName> processedClasses = new HashSet<>();
	protected final Collection<U> deferredUnits = new ArrayList<>();

	public UnitProcessor(Class<?>... supportedAnnotations)
	{
		super(supportedAnnotations);
	}

	@Override
	public void process(RoundEnvironment roundEnv)
	{
		if (roundEnv.processingOver())
		{
			for (U unit : this.deferredUnits)
			{
				/*ProcessorException.builder()
					.context(unit.adaptableClass, unit.annotation)
					.build("Could not process adaptable type !")
					.log(this.tools);*/
				this.onFailedToDeferProcess(unit);
			}
			return;
		}

		Collection<U> units;
		try
		{
			units = this.buildProcessingUnits(roundEnv);
		}
		catch (Exception e)
		{
			this.tools.log(Kind.ERROR, "Unexpected exception while building of the processing units : " + e.getClass().getCanonicalName() + ": " + e.getMessage());
			return;
		}

		for (U unit : units)
		{
			if (this.processedClasses.contains(unit.getTargetClassName()))
				continue; // Skip the class if it has already been processed in a previous round

			ProcessingStatus status = unit.process();

			switch (status)
			{
			case SUCESSS:
				this.processedClasses.add(unit.getTargetClassName());
				break;
			case DEFERRED:
				this.deferredUnits.add(unit);
				break;
			default:
			case FAIL:
				break;
			}
		}
	}

	protected abstract Collection<U> buildProcessingUnits(RoundEnvironment roundEnv);

	protected abstract void onFailedToDeferProcess(U unit);
}
