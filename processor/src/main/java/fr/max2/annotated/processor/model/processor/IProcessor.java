package fr.max2.annotated.processor.model.processor;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;

import fr.max2.annotated.processor.util.ProcessingTools;

public interface IProcessor
{
    Set<? extends Class<?>> getSupportedAnnotations();
    void init(ProcessingTools tools);
    void process(RoundEnvironment roundEnv);
}
