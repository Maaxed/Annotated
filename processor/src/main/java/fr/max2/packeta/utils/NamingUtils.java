package fr.max2.packeta.utils;

import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;

public class NamingUtils
{
	
	public static String simpleName(String qualifiedName)
	{
		int ditIndex = qualifiedName.lastIndexOf('.') ;
		return ditIndex < 0 ? qualifiedName : qualifiedName.substring(ditIndex + 1);
	}
	
	public static String simpleTypeName(TypeMirror type)
	{
		return simpleTypeName(type, false);
	}
	
	public static String simpleTypeName(TypeMirror type, boolean simplifyGenerics)
	{
		switch (type.getKind())
		{
		case DECLARED:
		case ERROR:
			DeclaredType declaredType = ((DeclaredType)type);
			
			String decName = declaredType.asElement().getSimpleName().toString();
			
			List<? extends TypeMirror> arguments = declaredType.getTypeArguments();
			
			if (!arguments.isEmpty())
			{
				decName += "<";
				if (!simplifyGenerics) decName += arguments.stream().map(arg -> simpleTypeName(arg, simplifyGenerics)).collect(Collectors.joining(", "));
				decName += ">";
			}
			return decName;
		case ARRAY:
			return simpleTypeName(((ArrayType)type).getComponentType(), simplifyGenerics) + "[]";
			
		case UNION:
			return ((UnionType)type).getAlternatives().stream().map(alt -> simpleTypeName(alt, simplifyGenerics)).collect(Collectors.joining(" | "));
			
		case INTERSECTION:
			return ((IntersectionType)type).getBounds().stream().map(bound -> simpleTypeName(bound, simplifyGenerics)).collect(Collectors.joining(" & "));
			
		case WILDCARD:
			WildcardType wildcardType = (WildcardType)type;
			
			TypeMirror extendsBound = wildcardType.getExtendsBound();
			TypeMirror superBound = wildcardType.getSuperBound();
			
			String wcName = "*";
			if (extendsBound != null) wcName += " extends " + simpleTypeName(extendsBound, simplifyGenerics);
			if (superBound != null) wcName += " super " + simpleTypeName(superBound, simplifyGenerics);
			return wcName;
			
		default:
			return type.toString();
		}
	}
}
