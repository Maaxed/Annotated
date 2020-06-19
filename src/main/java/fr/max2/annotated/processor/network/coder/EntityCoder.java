package fr.max2.annotated.processor.network.coder;

import java.util.UUID;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.IHandlerProvider;
import fr.max2.annotated.processor.network.coder.handler.NamedDataHandler;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ClassRef;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PropertyMap;
import fr.max2.annotated.processor.utils.exceptions.CoderExcepetion;

public class EntityCoder
{
	public static final IHandlerProvider
		ENTITY_ID = NamedDataHandler.provider(ClassRef.ENTITY_BASE, (tools, uniqueName, paramType, properties) -> new IdCoder(tools, uniqueName, paramType, properties, tools.elements.getTypeElement(ClassRef.ENTITY_BASE).asType(), tools.types.getPrimitiveType(TypeKind.INT), "func_145782_y", "getEntityId", "func_73045_a", "getEntityByID")),
		PLAYER_ID = NamedDataHandler.provider(ClassRef.PLAYER_BASE, (tools, uniqueName, paramType, properties) -> new IdCoder(tools, uniqueName, paramType, properties, tools.elements.getTypeElement(ClassRef.PLAYER_BASE).asType(), tools.elements.getTypeElement(UUID.class.getCanonicalName()).asType(), "func_110124_au", "getUniqueID", "func_217371_b", "getPlayerByUuid"));
	
	private static class IdCoder extends DataCoder
	{
		private final String idGetter, entityGetter;
		private final DataCoder idCoder;
		private final TypeMirror expectedType;

		public IdCoder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties, TypeMirror expectedType, TypeMirror idType, String idGetterSRG, String idGetter, String entityGetterSRG, String entityGetter) throws CoderExcepetion
		{
			super(tools, uniqueName, paramType, properties);
			this.expectedType = expectedType;
			this.internalType = idType;
			this.idGetter = tools.naming.getMethodMapping(idGetterSRG, idGetter);
			this.entityGetter = tools.naming.getMethodMapping(entityGetterSRG, entityGetter);
			this.idCoder = tools.coders.getCoder(uniqueName + "Id", this.internalType, properties.getSubPropertiesOrEmpty("id"));
		}

		@Override
		public OutputExpressions addInstructions(IPacketBuilder builder, String saveAccessExpr, String internalAccessExpr, String externalAccessExpr)
		{
			OutputExpressions idOutput = builder.runCoderWithoutConversion(this.idCoder, saveAccessExpr);
			String entityGetter = "ctx.getSender()." + this.tools.naming.getMethodMapping("func_130014_f_", "getEntityWorld") + "()." + this.entityGetter + "(" + externalAccessExpr + ")";
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
