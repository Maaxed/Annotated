package fr.max2.annotated.processor.network;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import fr.max2.annotated.api.processor.network.GenerateChannel;
import fr.max2.annotated.processor.utils.ClassName;
import fr.max2.annotated.processor.utils.EnumSide;
import fr.max2.annotated.processor.utils.NamingUtils;
import fr.max2.annotated.processor.utils.TypeHelper;
import fr.max2.annotated.processor.utils.exceptions.AnnotationStructureException;
import fr.max2.annotated.processor.utils.template.TemplateHelper;

public class NetworkProcessingUnit
{
	private PacketProcessor processor;
	private final TypeElement enclosingClass;
	public final ClassName enclosingClassName;
	public final ClassName networkClassName;
	private Optional<? extends AnnotationMirror> annotation;
	@Nullable
	private final String modId;
	private final List<PacketProcessingUnit> packets = new ArrayList<>();
	
	public NetworkProcessingUnit(PacketProcessor processor, TypeElement enclosingClass)
	{
		this.processor = processor;
		this.enclosingClass = enclosingClass;
		this.modId = processor.findModAnnotationId(enclosingClass);
		this.enclosingClassName = NamingUtils.buildClassName(processor.elementUtils(), enclosingClass);
		this.networkClassName = new ClassName(this.enclosingClassName.packageName(), this.enclosingClassName.shortName().replace('.', '_') + "Network");
		this.annotation = TypeHelper.getAnnotationMirror(processor.typeUtils(), enclosingClass, GenerateChannel.class.getCanonicalName());
	}
	
	public void addPacket(ExecutableElement method, EnumSide side)
	{
		this.packets.add(new PacketProcessingUnit(this.processor, this, method, side));
	}
	
	public void processNetwork()
	{
		this.processor.log(Kind.NOTE, "Processing packet class generation", this.enclosingClass, this.annotation);
		
		// Generate each packet class
		for (PacketProcessingUnit packet : this.packets)
		{
			packet.processPacket();
		}
		
		// Generate the network class
		try
        {
			this.writeNetwork();
	    }
	    catch (IOException e)
	    {
	    	this.processor.log(Kind.ERROR, "An exception has occured during the generation of the network class", this.enclosingClass, this.annotation);
	        throw new UncheckedIOException("An exception has occured during the generation of the network class from '" + this.enclosingClassName.qualifiedName() + "'", e);
	    }
		catch (Exception e)
		{
			this.processor.log(Kind.ERROR, "An unexpected exception has occured during the generation of the network class", this.enclosingClass, this.annotation);
	        throw new RuntimeException("An unexpected exception has occured during the generation of the network class from '" + this.enclosingClassName.qualifiedName() + "'", e);
		}
	}
	
	private void writeNetwork() throws IOException
	{
		GenerateChannel generatorData = enclosingClass.getAnnotation(GenerateChannel.class);
		
		String channelName = generatorData.channelName();
		if (!channelName.contains(":"))
		{
			if (this.modId == null)
			{
				processor.log(Kind.ERROR, "Couldn't find @Mod annotation in the package '" + this.enclosingClassName.packageName() + "'", this.enclosingClass, this.annotation);
				throw new AnnotationStructureException("Couldn't find @Mod annotation in the package '" + this.enclosingClassName.packageName() + "'");
			}
			if (channelName.isEmpty())
				channelName = this.enclosingClassName.shortName().toLowerCase();
			
			channelName = this.modId + ":" + channelName;
		}
		
		if (!channelName.toLowerCase().equals(channelName))
		{
			processor.log(Kind.WARNING, "The channel name '" + channelName + "' chould be in lower case, lower case is enforced", this.enclosingClass, this.annotation, "channelName");
			channelName = channelName.toLowerCase();	
		}

		List<String> registerPackets = new ArrayList<>();
		
		for (int i = 0; i < this.packets.size(); i++)
		{
			registerPackets.add("\t\t" + this.packets.get(i).messageClassName.shortName() + ".registerTo(CHANNEL, " + i + ");");
		}
		
		Map<String, String> replacements = new HashMap<>();
		replacements.put("package", this.networkClassName.packageName());
		replacements.put("className", this.networkClassName.shortName());
		replacements.put("channelName", channelName);
		replacements.put("protocolVersion", generatorData.protocolVersion());
		replacements.put("registerPackets", registerPackets.stream().collect(Collectors.joining(System.lineSeparator())));

		TemplateHelper.writeFileFromTemplateWithLog(this.processor, this.networkClassName.qualifiedName(), "templates/TemplateNetwork.jvtp", replacements, this.enclosingClass, this.annotation);
	}

	
}
