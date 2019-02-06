package fr.max2.packeta.processor.utils.model.element;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.type.TypeMirror;

import fr.max2.packeta.processor.utils.NamingUtils;
import fr.max2.packeta.processor.utils.model.SimpleName;

public class TestingQualifiedNameable extends TestingElement implements QualifiedNameable
{
	private final Name qualifiedName;
	
	public TestingQualifiedNameable(ElementKind kind, TypeMirror correspondingType, String qualifiedName)
	{
		super(kind, correspondingType, NamingUtils.simpleName(qualifiedName));
		this.qualifiedName = new SimpleName(qualifiedName);
	}

	@Override
	public Name getQualifiedName()
	{
		return this.qualifiedName;
	}
	
}
