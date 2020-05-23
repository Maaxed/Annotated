package fr.max2.annotated.processor.network.coder;

import java.util.UUID;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.IDataHandler;
import fr.max2.annotated.processor.network.coder.handler.NamedDataHandler;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ClassRef;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PropertyMap;

public class EntityCoder
{
	public static final IDataHandler
		ENTITY_ID = new NamedDataHandler(ClassRef.ENTITY_BASE, (tools, uniqueName, paramType, properties) -> new IdCoder(tools, uniqueName, paramType, properties, tools.elements.getTypeElement(ClassRef.ENTITY_BASE).asType(), tools.types.getPrimitiveType(TypeKind.INT), "getEntityId", "getEntityByID")),
		PLAYER_ID = new NamedDataHandler(ClassRef.PLAYER_BASE, (tools, uniqueName, paramType, properties) -> new IdCoder(tools, uniqueName, paramType, properties, tools.elements.getTypeElement(ClassRef.PLAYER_BASE).asType(), tools.elements.getTypeElement(UUID.class.getCanonicalName()).asType(), "getUniqueID", "getPlayerByUuid"));
	
	private static class IdCoder extends DataCoder
	{
		private final String idGetter, entityGetter;
		private final DataCoder idCoder;
		private final TypeMirror expectedType;

		public IdCoder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties, TypeMirror expectedType, TypeMirror idType, String idGetter, String entityGetter)
		{
			super(tools, uniqueName, paramType, properties);
			this.expectedType = expectedType;
			this.internalType = idType;
			this.idGetter = idGetter;
			this.entityGetter = entityGetter;
			this.idCoder = tools.handlers.getDataType(uniqueName + "ID", this.internalType, properties.getSubPropertiesOrEmpty("id"));
		}

		@Override
		public OutputExpressions addInstructions(IPacketBuilder builder, String saveAccessExpr, String internalAccessExpr, String externalAccessExpr)
		{
			OutputExpressions idOutput = builder.runCoderWithoutConversion(this.idCoder, saveAccessExpr);
			String entityGetter = "ctx.getSender().getEntityWorld()." + this.entityGetter + "(" + externalAccessExpr + ")";
			if (!this.tools.types.isAssignable(this.expectedType, this.paramType))
			{
				String uncheckedName = this.uniqueName + "Unchecked";
				this.tools.types.provideTypeImports(this.expectedType, builder);
				builder.externalizer().add(this.tools.naming.computeFullName(this.expectedType) + " " + uncheckedName + " = " + entityGetter + ";");
				entityGetter = uncheckedName + " instanceof " + this.tools.naming.computeFullName(this.tools.types.erasure(this.paramType)) + " ? (" + this.tools.naming.computeFullName(this.paramType) + ")" + uncheckedName + " : null";
			}
			return new OutputExpressions(idOutput.decoded, internalAccessExpr + "." + this.idGetter + "()", entityGetter);
		}
		
	}
}
