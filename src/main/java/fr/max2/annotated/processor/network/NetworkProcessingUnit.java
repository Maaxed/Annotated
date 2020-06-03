package fr.max2.annotated.processor.network;

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

import fr.max2.annotated.processor.network.model.ChannelProvider;
import fr.max2.annotated.processor.network.model.EnumSide;
import fr.max2.annotated.processor.network.model.SimpleImportClassBuilder;
import fr.max2.annotated.processor.utils.ClassName;
import fr.max2.annotated.processor.utils.ProcessingTools;

public class NetworkProcessingUnit
{
	private final ProcessingTools tools;
	private final TypeElement enclosingClass;
	private final ChannelProvider channelProvider;
	private final Optional<? extends AnnotationMirror> annotation;
	private final @Nullable String modId;
	private final List<PacketProcessingUnit> packets = new ArrayList<>();
	public final ClassName enclosingClassName;
	public final ClassName networkClassName;
	private boolean hasErrors = false;
	
	public NetworkProcessingUnit(ProcessingTools tools, TypeElement enclosingClass, @Nullable ChannelProvider channelProvider, String modId)
	{
		this.tools = tools;
		this.enclosingClass = enclosingClass;
		this.channelProvider = channelProvider;
		this.modId = modId;
		this.enclosingClassName = tools.naming.buildClassName(enclosingClass);
		this.networkClassName = new ClassName(this.enclosingClassName.packageName(), this.enclosingClassName.shortName().replace('.', '_') + "Network");
		this.annotation = channelProvider == null ? Optional.empty() : tools.elements.getAnnotationMirror(enclosingClass, channelProvider.getAnnotationClass().getCanonicalName());
	}
	
	public boolean hasErrors()
	{
		return this.hasErrors;
	}
	
	public void addPacket(ExecutableElement method, EnumSide side)
	{
		this.packets.add(new PacketProcessingUnit(this.tools, this, method, side));
	}
	
	public void processNetwork()
	{
		// Generate each packet class
		for (PacketProcessingUnit packet : this.packets)
		{
			packet.processPacket();
		}
		
		// Generate the network class
		try
        {
			if (this.writeNetwork())
				return;
	    }
		catch (Exception e)
		{
			this.tools.log(Kind.ERROR, "Unexpected exception while generating the '" + this.networkClassName.qualifiedName() + "' class: " + e.getClass().getCanonicalName() + ": " + e.getMessage(), this.enclosingClass, this.annotation);
		}
		this.hasErrors = true;
	}
	
	private boolean writeNetwork()
	{
		List<String> registerPackets = new ArrayList<>();
		
		for (int i = 0; i < this.packets.size(); i++)
		{
			PacketProcessingUnit packet = this.packets.get(i);
			if (!packet.hasErrors()) // Don't include packets with errors in the network
				registerPackets.add("\t\t" + packet.messageClassName.shortName() + ".registerTo(CHANNEL, index.getAndIncrement());");
		}
		
		SimpleImportClassBuilder<?> imports = new SimpleImportClassBuilder<>(this.tools, this.networkClassName.packageName());
		List<String> content = new ArrayList<>();
		if (this.channelProvider != null && !this.channelProvider.getNetworkClassContent(this.tools, imports, content::add, this.enclosingClass, this.enclosingClassName, this.networkClassName, this.modId, this.annotation))
			return false;
		
		String ls = System.lineSeparator();
		
		Map<String, String> replacements = new HashMap<>();
		replacements.put("package", this.networkClassName.packageName());
		replacements.put("imports", imports.imports.stream().map(i -> "import " + i + ";" + ls).collect(Collectors.joining()));
		replacements.put("className", this.networkClassName.shortName());
		replacements.put("registerPackets", registerPackets.stream().collect(Collectors.joining(ls)));
		replacements.put("classContent", content.stream().map(l -> '\t' + l).collect(Collectors.joining()));
		
		return this.tools.templates.writeFileWithLog(this.networkClassName.qualifiedName(), "templates/TemplateNetwork.jvtp", replacements, this.enclosingClass, this.annotation);
	}

	
}
