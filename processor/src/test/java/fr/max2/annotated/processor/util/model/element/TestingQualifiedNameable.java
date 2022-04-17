package fr.max2.annotated.processor.util.model.element;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.util.model.SimpleName;

public class TestingQualifiedNameable extends TestingElement implements QualifiedNameable
{
	private final Name qualifiedName;
	
	public TestingQualifiedNameable(ElementKind kind, TypeMirror correspondingType, String fullName, String simpleName)
	{
		super(kind, correspondingType, simpleName);
		this.qualifiedName = new SimpleName(fullName);
	}

	@Override
	public Name getQualifiedName()
	{
		return this.qualifiedName;
	}
	
}
