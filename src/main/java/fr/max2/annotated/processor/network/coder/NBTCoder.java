package fr.max2.annotated.processor.network.coder;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import fr.max2.annotated.processor.network.coder.handler.IDataHandler;
import fr.max2.annotated.processor.network.coder.handler.NamedDataHandler;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ClassName;
import fr.max2.annotated.processor.utils.ClassRef;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PropertyMap;

public class NBTCoder
{
	public static final IDataHandler
		PRIMITIVE = new NamedDataHandler(ClassRef.NBT_NUMBER, (tools, uniqueName, paramType, properties) -> new DataCoder(tools, uniqueName, paramType, properties)
		{
			@Override
			public OutputExpressions addInstructions(IPacketBuilder builder, String saveAccessExpr, String internalAccessExpr, String externalAccessExpr)
			{
				Element elem = this.tools.types.asElement(paramType);
				String className = elem.getSimpleName().toString();
				String primitive = className.substring(0, className.length() - 3);
				String decodedOutput = DataCoderUtils.addBufferInstructions(primitive, saveAccessExpr + ".get" + primitive + "()", builder);
				return new OutputExpressions(className + ".valueOf(" + decodedOutput + ")", internalAccessExpr, externalAccessExpr);
			}
		})
		{
			@Override
			public boolean canProcess(TypeMirror type)
			{
				TypeMirror stringType = this.tools.types.erasure(this.tools.elements.getTypeElement(ClassRef.NBT_STRING).asType());
				return (this.tools.types.isAssignable(type, this.type) && !this.tools.types.isSameType(type, this.type)) || this.tools.types.isAssignable(type, stringType);
			}
		},
		CONCRETE = new NamedDataHandler(ClassRef.NBT_BASE, (tools, uniqueName, paramType, properties) -> new Coder(tools, uniqueName, paramType, properties, "Concrete"))
		{
			@Override
			public boolean canProcess(TypeMirror type)
			{
				if (!this.tools.types.isAssignable(type, this.type))
					return false;
				
				Element elem = this.tools.types.asElement(type);
				if (elem == null)
					return false;
				
				return ElementFilter.fieldsIn(elem.getEnclosedElements()).stream().anyMatch(var -> var.getModifiers().contains(Modifier.STATIC) && var.getSimpleName().contentEquals("TYPE"));
			}
		},
		ABSTRACT = new NamedDataHandler(ClassRef.NBT_BASE, (tools, uniqueName, paramType, properties) -> new Coder(tools, uniqueName, paramType, properties, "Abstract"));
	
	private static class Coder extends DataCoder
	{
		private final String mode;
		
		public Coder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties, String mode)
		{
			super(tools, uniqueName, paramType, properties);
			this.mode = mode;
		}

		@Override
		public OutputExpressions addInstructions(IPacketBuilder builder, String saveAccessExpr, String internalAccessExpr, String externalAccessExpr)
		{
			ClassName typeName = tools.naming.buildClassName(tools.types.asElement(paramType));
			
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
			if (this.mode.equals("Abstract"))
			{
				builder.addImport("net.minecraft.nbt.NBTTypes");
				builder.require("templates/TemplateAbstractNBTHandlingModule.jvtp");
			}

			builder.encoder().add("write" + this.mode + "NBT(buf, " + saveAccessExpr + ");");
			return new OutputExpressions("read" + this.mode + "NBT(buf, " + typeName.shortName() + "." + (this.mode.equals("Abstract") ? "class" : "TYPE") + ")", internalAccessExpr, externalAccessExpr);
		}
	}
}
