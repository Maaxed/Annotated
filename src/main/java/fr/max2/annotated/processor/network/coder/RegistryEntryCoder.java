package fr.max2.annotated.processor.network.coder;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import fr.max2.annotated.processor.network.coder.handler.NamedDataHandler;
import fr.max2.annotated.processor.network.model.IPacketBuilder;
import fr.max2.annotated.processor.utils.ClassRef;
import fr.max2.annotated.processor.utils.ProcessingTools;
import fr.max2.annotated.processor.utils.PropertyMap;
import fr.max2.annotated.processor.utils.exceptions.IncompatibleTypeException;

public class RegistryEntryCoder extends DataCoder
{
	private static final String ENTRY_TYPE = ClassRef.REGISTRY_ENTRY;
	public static final NamedDataHandler HANDLER = new NamedDataHandler(ENTRY_TYPE, RegistryEntryCoder::new);
	
	private final TypeElement typeElement;
	
	public RegistryEntryCoder(ProcessingTools tools, String uniqueName, TypeMirror paramType, PropertyMap properties) throws IncompatibleTypeException
	{
		super(tools, uniqueName, paramType, properties);
		
		DeclaredType entryType = tools.types.refineTo(paramType, tools.elements.getTypeElement(ENTRY_TYPE).asType());
		if (entryType == null) throw new IncompatibleTypeException("The type '" + paramType + "' is not a sub type of " + ENTRY_TYPE);
		
		TypeMirror contentType = entryType.getTypeArguments().get(0);
		if (contentType.getKind() != TypeKind.DECLARED)
		{
			throw new IncompatibleTypeException("The registry type '" + contentType + "' is invalid");
		}
		this.typeElement = tools.elements.asTypeElement(tools.types.asElement(contentType));
	}
	
	@Override
	public OutputExpressions addInstructions(IPacketBuilder builder, String saveAccessExpr, String internalAccessExpr, String externalAccessExpr)
	{
		String registryName = getForgeRegistry(this.typeElement);
		
		String forgeRegistry;
		if (registryName == null)
		{
			forgeRegistry = "RegistryManager.ACTIVE.getRegistry(" + this.typeElement.getSimpleName() + ".class)";
			builder.addImport(this.typeElement);
			builder.addImport("net.minecraftforge.registries.RegistryManager");
		}
		else
		{
			forgeRegistry = "ForgeRegistries." + registryName;
			builder.addImport("net.minecraftforge.registries.ForgeRegistries");
		}

		builder.encoder().add("buf.writeRegistryIdUnsafe(" + forgeRegistry + ", " + saveAccessExpr + ");");
		return new OutputExpressions("buf.readRegistryIdUnsafe(" + forgeRegistry + ")", internalAccessExpr, externalAccessExpr);
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
}
