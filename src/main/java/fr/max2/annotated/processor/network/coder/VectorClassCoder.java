package fr.max2.annotated.processor.network.coder;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.IHandlerProvider;
import fr.max2.annotated.processor.network.coder.handler.NamedDataHandler;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ClassRef;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PropertyMap;
import fr.max2.annotated.processor.utils.exceptions.CoderExcepetion;

public class VectorClassCoder extends DataCoder
{
	public static final IHandlerProvider
		AXIS_ALIGNED_BB = handler(ClassRef.AXIS_ALIGNED_BB, TypeKind.DOUBLE, null, null,
			"field_72340_a", "minX",
			"field_72338_b", "minY",
			"field_72339_c", "minZ",
			"field_72336_d", "maxX",
			"field_72337_e", "maxY",
			"field_72334_f", "maxZ"),
		MUTABLE_BB = handler(ClassRef.MUTABLE_BB, TypeKind.INT, "func_175899_a", "createProper",
			"field_78897_a", "minX",
			"field_78895_b", "minY",
			"field_78896_c", "minZ",
			"field_78893_d", "maxX",
			"field_78894_e", "maxY",
			"field_78892_f", "maxZ"),
		CHUNK_POS = handler(ClassRef.CHUNK_POS, TypeKind.INT, null, null,
			"field_77276_a", "x",
			"field_77275_b", "z"),
		SECTION_POS = handler(ClassRef.SECTION_POS, TypeKind.INT, "func_218154_a", "of",
			"func_177958_n", "getX",
			"func_177956_o", "getY",
			"func_177952_p", "getZ"),
		VECTOR_3D = handler(ClassRef.VECTOR_3D, TypeKind.DOUBLE, null, null,
			"field_72450_a", "x",
			"field_72448_b", "y",
			"field_72449_c", "z"),
		VECTOR_3I = handler(ClassRef.VECTOR_3I, TypeKind.INT, null, null,
			"func_177958_n", "getX",
			"func_177956_o", "getY",
			"func_177952_p", "getZ");
	
	private final String constructor;
	private final String[] dataFields;
	private final TypeMirror primitiveType;
	private final DataCoder primitiveCoder;

	public VectorClassCoder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties, TypeKind primitiveType, String constructorSRG, String constructor, String... dataFields) throws CoderExcepetion
	{
		super(tools, uniqueName, paramType, properties);
		if (dataFields.length % 2 != 0)
			throw new IllegalArgumentException("dataFields should contain pairs of srg and mcp names");
		
		this.constructor = constructorSRG == null ? "new " + tools.naming.computeSimplifiedName(paramType) : tools.naming.computeSimplifiedName(paramType) + "." + tools.naming.getMethodMapping(constructorSRG, constructor);
		this.dataFields = new String[dataFields.length / 2]; 
		for (int i = 0; i < dataFields.length / 2; i++)
		{
			if (dataFields[2 * i].startsWith("func"))
				this.dataFields[i] = tools.naming.getMethodMapping(dataFields[2 * i], dataFields[2 * i + 1]) + "()";
			else
				this.dataFields[i] = tools.naming.getFieldMapping(dataFields[2 * i], dataFields[2 * i + 1]);
		}
		
		this.primitiveType = tools.types.getPrimitiveType(primitiveType);
		this.primitiveCoder = tools.coders.getCoder(uniqueName + "Content", this.primitiveType, PropertyMap.EMPTY_PROPERTIES);
	}

	@Override
	public OutputExpressions addInstructions(IPacketBuilder builder, String saveAccessExpr, String internalAccessExpr, String externalAccessExpr)
	{
		String decodedParams = "";
		for (String field : this.dataFields)
		{
			OutputExpressions fieldOutput = builder.runCoderWithoutConversion(this.primitiveCoder, saveAccessExpr + "." + field);
			if (!decodedParams.isEmpty())
				decodedParams += ", ";
			decodedParams += fieldOutput.decoded;
		}
		return new OutputExpressions(this.constructor + "(" + decodedParams + ")", internalAccessExpr, externalAccessExpr);
	}
	
	private static IHandlerProvider handler(String typeName, TypeKind primitiveType, String constructorSRG, String constructor, String... dataFields)
	{
		if (dataFields.length % 2 != 0)
			throw new IllegalArgumentException("dataFields should contain pairs of srg and mcp names");
		return NamedDataHandler.provider(typeName, (tools, uniqueName, paramType, properties) -> new VectorClassCoder(tools, uniqueName, paramType, properties, primitiveType, constructorSRG, constructor, dataFields));
	}
	
}
