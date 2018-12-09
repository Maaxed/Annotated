package fr.max2.packeta.utils;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;

public interface DefaultTypeVisitor<R, P> extends TypeVisitor<R, P>
{

	@Override
	default R visit(TypeMirror t, P p)
	{
		return t.accept(this, p);
	}

	@Override
	default R visit(TypeMirror t)
	{
		return t.accept(this, null);
	}

	@Override
	default R visitPrimitive(PrimitiveType t, P p)
	{
		return this.visitDefault(t, p);
	}

	@Override
	default R visitNull(NullType t, P p)
	{
		return this.visitDefault(t, p);
	}

	@Override
	default R visitArray(ArrayType t, P p)
	{
		return this.visitDefault(t, p);
	}

	@Override
	default R visitDeclared(DeclaredType t, P p)
	{
		return this.visitDefault(t, p);
	}

	@Override
	default R visitError(ErrorType t, P p)
	{
		return this.visitDeclared(t, p);
	}

	@Override
	default R visitTypeVariable(TypeVariable t, P p)
	{
		return this.visitDefault(t, p);
	}

	@Override
	default R visitWildcard(WildcardType t, P p)
	{
		return this.visitDefault(t, p);
	}

	@Override
	default R visitExecutable(ExecutableType t, P p)
	{
		return this.visitDefault(t, p);
	}

	@Override
	default R visitNoType(NoType t, P p)
	{
		return this.visitDefault(t, p);
	}

	@Override
	default R visitUnknown(TypeMirror t, P p)
	{
		return this.visitDefault(t, p);
	}

	@Override
	default R visitUnion(UnionType t, P p)
	{
		return this.visitDefault(t, p);
	}

	@Override
	default R visitIntersection(IntersectionType t, P p)
	{
		return this.visitDefault(t, p);
	}
	
	R visitDefault(TypeMirror t, P p);
	
}
