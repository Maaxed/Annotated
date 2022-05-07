package fr.max2.annotated.processor.model.processor;

import fr.max2.annotated.processor.util.ClassName;
import fr.max2.annotated.processor.util.ProcessingStatus;

public interface IProcessingUnit
{
	ProcessingStatus process();
	ClassName getTargetClassName();
}
