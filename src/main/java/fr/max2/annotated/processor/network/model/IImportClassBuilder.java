package fr.max2.annotated.processor.network.model;

import javax.lang.model.element.TypeElement;

import fr.max2.annotated.processor.utils.ClassName;

public interface IImportClassBuilder<B extends IImportClassBuilder<B>>
{
	B addImport(String className);
	B addImport(ClassName className);
	B addImport(TypeElement classElem);
}
