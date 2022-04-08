package fr.max2.annotated.processor.network.coder;

import javax.annotation.Nullable;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;

import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PropertyMap;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;

public abstract class DataCoder
{
	public final ProcessingTools tools;
	public final String uniqueName;
	public final TypeMirror paramType;
	public final PropertyMap properties;
	protected TypeMirror internalType;
	
	public DataCoder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties)
	{
		this(tools, uniqueName, paramType, properties, paramType);
	}
	
	public DataCoder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties, TypeMirror internalType)
	{
		this.tools = tools;
		this.uniqueName = uniqueName;
		this.paramType = paramType;
		this.properties = properties;
		this.internalType = internalType;
	}
	
	public TypeMirror getInternalType()
	{
		return this.internalType;
	}
	
	public boolean requireConversion()
	{
		return !this.tools.types.isAssignable(this.internalType, this.paramType) || !this.tools.types.isAssignable(this.paramType, this.internalType);
	}
	
	public abstract OutputExpressions addInstructions(IPacketBuilder builder, String saveAccessExpr, String internalAccessExpr, String externalAccessExpr);
	
	public static final class OutputExpressions
	{
		public final String decoded;
		public final String internalized;
		public final String externalized;

		public OutputExpressions(String decodedOutput, String internalizedOutput, String externalizedOutput)
		{
			this.decoded = decodedOutput;
			this.internalized = internalizedOutput;
			this.externalized = externalizedOutput;
		}
	}
	
	// Utils
	
	public String writeBuffer(String type, String value, String writeSRGName)
	{
		String writeMethod = "write" + type;
		if (writeSRGName != null)
			writeMethod = this.tools.naming.getMethodMapping(writeSRGName, writeMethod);
		
		return "buf." + writeMethod + "(" + value + ");";
	}
	
	public String readBuffer(String type, String readSRGName)
	{
		String readMethod = "read" + type;
		if (readSRGName != null)
			readMethod = this.tools.naming.getMethodMapping(readSRGName, readMethod);
		
		return "buf." + readMethod + "()";
	}
	
	public String addBufferInstructions(String type, String saveValue, String writeSRGName, String readSRGName, IPacketBuilder builder)
	{
		builder.encoder().add(this.writeBuffer(type, saveValue, writeSRGName));
		return this.readBuffer(type, readSRGName);
	}
	
	public static void requireDefaultConstructor(Types typeHelper, TypeMirror type, @Nullable String errorHelpInfo) throws IncompatibleTypeException
	{
		Element elem = typeHelper.asElement(type);
		if (elem == null)
			return; // Unknown type, assume it has a default constructor
		
		if (errorHelpInfo != null)
			errorHelpInfo = ". " + errorHelpInfo;
		
		if (elem.getModifiers().contains(Modifier.ABSTRACT))
			throw new IncompatibleTypeException("The type '" + type + "' is abstract and can't be instantiated" + errorHelpInfo);
		
		if (!ElementFilter.constructorsIn(elem.getEnclosedElements()).stream().anyMatch(cons -> cons.getParameters().isEmpty()))
			throw new IncompatibleTypeException("The type '" + type + "' doesn't have a default constructor" + errorHelpInfo);
	}
}
