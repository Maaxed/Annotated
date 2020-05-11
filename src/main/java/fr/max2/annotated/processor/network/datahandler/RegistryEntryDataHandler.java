package fr.max2.annotated.processor.network.datahandler;

import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.DataHandlerParameters;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;

public enum RegistryEntryDataHandler implements INamedDataHandler
{
	INSTANCE;
	
	@Override
	public void addInstructions(DataHandlerParameters params, IPacketBuilder builder)
	{
		DeclaredType collectionType = params.tools.types.refineTo(params.type, params.tools.elements.getTypeElement(this.getTypeName()).asType());
		if (collectionType == null) throw new IncompatibleTypeException("The type '" + params.type + "' is not a sub type of " + this.getTypeName());
		
		TypeMirror contentType = collectionType.getTypeArguments().get(0);
		if (contentType.getKind() != TypeKind.DECLARED)
		{
			throw new IncompatibleTypeException("The registry type is invalid");
		}
		TypeElement typeElement = params.tools.elements.asTypeElement(params.tools.types.asElement(contentType));
		Name typeName = typeElement.getSimpleName();
		
		String registryName = getForgeRegistry(typeElement);
		String forgeRegistry;
		
		if (registryName == null)
		{
			forgeRegistry = "RegistryManager.ACTIVE.getRegistry(" + typeName + ".class)";
			builder.addImport(typeElement);
			builder.addImport("net.minecraftforge.registries.RegistryManager");
		}
		else
		{
			forgeRegistry = "ForgeRegistries." + registryName;
			builder.addImport("net.minecraftforge.registries.ForgeRegistries");
		}

		builder.encoder().add("buf.writeRegistryIdUnsafe(" + forgeRegistry + ", " + params.saveAccessExpr + ");");
		params.setExpr.accept(builder.decoder(), "buf.readRegistryIdUnsafe(" + forgeRegistry + ")");
	}
	
	public static String getForgeRegistry(TypeElement typeElement)
	{
		switch (typeElement.getQualifiedName().toString())
		{
		case "net.minecraft.block.Block": return "BLOCKS";
		case "net.minecraft.fluid.Fluid": return "FLUIDS";
		case "net.minecraft.item.Item": return "ITEMS";
		case "net.minecraft.entity.EntityType": return "ENTITIES";
		case "net.minecraft.tileentity.TileEntityType": return "TILE_ENTITIES";
		
		case "net.minecraft.util.SoundEvent": return "SOUND_EVENTS";
		case "net.minecraft.particles.ParticleType": return "PARTICLE_TYPES";
		default: return null;
		}
	}

	@Override
	public String getTypeName()
	{
		return "net.minecraftforge.registries.IForgeRegistryEntry";
	}
}
