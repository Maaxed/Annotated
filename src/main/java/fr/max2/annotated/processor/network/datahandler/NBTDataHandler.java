package fr.max2.annotated.processor.network.datahandler;

import java.util.function.Predicate;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import fr.max2.annotated.processor.network.DataHandlerParameters;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ClassName;
import fr.max2.annotated.processor.utils.ExtendedElements;
import fr.max2.annotated.processor.utils.ExtendedTypes;

public enum NBTDataHandler implements INamedDataHandler
{
	PRIMITIVE("NumberNBT")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			Element elem = params.tools.types.asElement(params.type);
			String className = elem.getSimpleName().toString();
			String primitive = className.substring(0, className.length() - 3);
			DataHandlerUtils.addBufferInstructions(primitive, params.saveAccessExpr + ".get" + primitive + "()", (loadInst, value) -> params.setExpr.accept(loadInst, className + ".valueOf(" + value + ")"), builder);
		}
		
		@Override
		public Predicate<TypeMirror> getTypeValidator(ExtendedElements elemUtils, ExtendedTypes typeUtils)
		{
			TypeMirror thisType = this.getType(elemUtils, typeUtils);
			TypeMirror stringType = typeUtils.erasure(elemUtils.getTypeElement("net.minecraft.nbt.StringNBT").asType());
			return type -> (typeUtils.isAssignable(type, thisType) && !typeUtils.isSameType(type, thisType)) || typeUtils.isAssignable(type, stringType);
		}
	},
	CONCRETE("INBT")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			addCustomInstructions("Concrete", params, builder);
		}
		
		@Override
		public Predicate<TypeMirror> getTypeValidator(ExtendedElements elemUtils, ExtendedTypes typeUtils)
		{
			TypeMirror thisType = this.getType(elemUtils, typeUtils);
			return type ->
			{
				if (!typeUtils.isAssignable(type, thisType))
					return false;
				
				Element elem = typeUtils.asElement(type);
				if (elem == null)
					return false;
				
				return ElementFilter.fieldsIn(elem.getEnclosedElements()).stream().anyMatch(var -> var.getModifiers().contains(Modifier.STATIC) && var.getSimpleName().contentEquals("TYPE"));
			};
		}
	},
	ABSTRACT("INBT")
	{
		@Override
		public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
		{
			addCustomInstructions("Abstract", params, builder);
		}
	};
	
	private final String className;
	
	private NBTDataHandler(String nbtType)
	{
		this.className = "net.minecraft.nbt." + nbtType;
	}
	
	private static void addCustomInstructions(String mode, DataHandlerParameters params, IPacketBuilder builder)
	{
		ClassName typeName = params.tools.naming.buildClassName(params.tools.types.asElement(params.type));
		builder.encoder().add("write" + mode + "NBT(buf, " + params.saveAccessExpr + ");");
		params.setExpr.accept(builder.decoder(), "read" + mode + "NBT(buf, " + typeName.shortName() + "." + (mode.equals("Abstract") ? "class" : "TYPE") + ")");
		
		builder.addImport("net.minecraft.nbt.INBTType");
		builder.addImport("net.minecraft.nbt.INBT");
		builder.addImport("javax.annotation.Nonnull");
		builder.addImport("io.netty.buffer.ByteBufOutputStream");
		builder.addImport("io.netty.buffer.ByteBufInputStream");
		builder.addImport("java.io.IOException");
		builder.addImport("io.netty.handler.codec.EncoderException");
		builder.addImport("net.minecraft.nbt.NBTSizeTracker");
		builder.addImport("net.minecraft.crash.CrashReport");
		builder.addImport("net.minecraft.crash.CrashReportCategory");
		builder.addImport("net.minecraft.crash.ReportedException");
		builder.require("templates/TemplateConcreteNBTHandlingModule.jvtp");
		if (mode.equals("Abstract"))
		{
			builder.require("templates/TemplateAbstractNBTHandlingModule.jvtp");
			builder.addImport("net.minecraft.nbt.NBTTypes");
		}
	}

	@Override
	public String getTypeName()
	{
		return this.className;
	}
	
	@Override
	public String toString()
	{
		return super.toString() + "_NBT";
	}
}
