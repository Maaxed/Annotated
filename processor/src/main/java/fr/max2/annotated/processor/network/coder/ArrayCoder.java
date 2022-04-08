package fr.max2.annotated.processor.network.coder;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.IDataHandler;
import fr.max2.annotated.processor.network.coder.handler.SpecialDataHandler;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PropertyMap;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;
import fr.max2.annotated.processor.utils.exceptions.CoderExcepetion;

public class ArrayCoder extends DataCoder
{
	public static final IDataHandler HANDLER = new SpecialDataHandler(TypeKind.ARRAY, ArrayCoder::new);

	private final DataCoder contentCoder;
	private final TypeMirror extContentType;
	
	public ArrayCoder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties) throws CoderExcepetion
	{
		super(tools, uniqueName, paramType, properties);
		
		ArrayType arrayType = tools.types.asArrayType(paramType);
		if (arrayType == null) throw new IncompatibleTypeException("The type '" + paramType + "' is not an array");
		this.extContentType = arrayType.getComponentType();
		
		this.contentCoder = tools.coders.getCoder(uniqueName + "Element", this.extContentType, properties.getSubPropertiesOrEmpty("content"));
		this.internalType = tools.types.getArrayType(this.contentCoder.getInternalType());
	}
	
	@Override
	public OutputExpressions addInstructions(IPacketBuilder builder, String saveAccessExpr, String internalAccessExpr, String externalAccessExpr)
	{
		String contentTypeName = this.tools.naming.computeFullName(this.contentCoder.getInternalType());
		String extContentTypeName = this.tools.naming.computeFullName(this.extContentType);
		String elementVarName = this.uniqueName + "Element";
		String indexVarName = this.uniqueName + "Index";
		String convertedName = this.uniqueName + "Converted";
		
		String arrayTypeName = contentTypeName + "[]";
		String extArrayTypeName = extContentTypeName + "[]";
		
		int firstBrackets;
		for (firstBrackets = arrayTypeName.length() - 2; firstBrackets >= 2 && arrayTypeName.substring(firstBrackets - 2, firstBrackets).equals("[]"); firstBrackets-=2);

		int extFirstBrackets;
		for (extFirstBrackets = extArrayTypeName.length() - 2; extFirstBrackets >= 2 && extArrayTypeName.substring(extFirstBrackets - 2, extFirstBrackets).equals("[]"); extFirstBrackets-=2);

		builder.encoder().add(
			this.writeBuffer("Int", saveAccessExpr + ".length", null),
			"for (" + contentTypeName + " " + elementVarName + " : " + saveAccessExpr + ")",
			"{");
		builder.decoder().add(
			arrayTypeName + " " + this.uniqueName + " = new " + arrayTypeName.substring(0, firstBrackets + 1) + this.readBuffer("Int", null) + arrayTypeName.substring(firstBrackets + 1) + ";",
			"for (int " + indexVarName + " = 0; " + indexVarName + " < " + this.uniqueName + ".length; " + indexVarName + "++)",
			"{");
		
		builder.internalizer().add(
			arrayTypeName + " " + convertedName + " = new " + arrayTypeName.substring(0, firstBrackets + 1) + internalAccessExpr + ".length" + arrayTypeName.substring(firstBrackets + 1) + ";",
			"for (int " + indexVarName + " = 0; " + indexVarName + " < " + convertedName + ".length; " + indexVarName + "++)",
			"{");
		builder.externalizer().add(
			extArrayTypeName + " " + convertedName + " = new " + extArrayTypeName.substring(0, extFirstBrackets + 1) + externalAccessExpr + ".length" + extArrayTypeName.substring(extFirstBrackets + 1) + ";",
			"for (int " + indexVarName + " = 0; " + indexVarName + " < " + convertedName + ".length; " + indexVarName + "++)",
			"{");
		
		builder.indentAll(1);
		OutputExpressions contentOutput = builder.runCoder(this.contentCoder, elementVarName);
		builder.decoder().add(this.uniqueName + "[" + indexVarName + "] = " + contentOutput.decoded + ";");
		builder.internalizer().add(convertedName + "[" + indexVarName + "] = " + contentOutput.internalized + ";");
		builder.externalizer().add(convertedName + "[" + indexVarName + "] = " + contentOutput.externalized + ";");
		builder.indentAll(-1);
		
		builder.encoder().add("}");
		builder.decoder().add("}");
		builder.internalizer().add("}");
		builder.externalizer().add("}");
		
		return new OutputExpressions(this.uniqueName, convertedName, convertedName); 
	}
}
