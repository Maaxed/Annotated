package fr.max2.annotated.processor.network.datahandler;

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.DataHandlerParameters;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.TypeHelper;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;

public enum RegistryEntryDataHandler implements INamedDataHandler
{
	INSTANCE;
	
	@Override
	public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
	{
		DeclaredType collectionType = TypeHelper.refineTo(params.type, params.finder.elemUtils.getTypeElement(this.getTypeName()).asType(), params.finder.typeUtils);
		if (collectionType == null) throw new IncompatibleTypeException("The type '" + params.type + "' is not a sub type of " + this.getTypeName());
		
		TypeMirror contentType = collectionType.getTypeArguments().get(0);
		if (contentType.getKind() != TypeKind.DECLARED)
		{
			throw new IncompatibleTypeException("The registry type is invalid");
		}
		Element typeElement = params.finder.typeUtils.asElement(contentType);
		Name typeName = typeElement.getSimpleName();
		
		String forgeRegistry = "RegistryManager.ACTIVE.getRegistry(" + typeName + ".class)"; //TODO [v2.0] use ForgeRegistries for common types
		
		builder.addImport(TypeHelper.asTypeElement(typeElement).getQualifiedName());
		builder.addImport("net.minecraftforge.registries.RegistryManager");

		builder.encoder().add("buf.writeRegistryIdUnsafe(" + forgeRegistry + ", " + params.saveAccessExpr + ");");
		params.setExpr.accept(builder.decoder(), "buf.readRegistryIdUnsafe(" + forgeRegistry + ")");
	}

	@Override
	public String getTypeName()
	{
		return "net.minecraftforge.registries.IForgeRegistryEntry";
	}
}
