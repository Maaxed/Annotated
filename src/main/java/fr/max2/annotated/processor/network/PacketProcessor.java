package fr.max2.annotated.processor.network;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;

import fr.max2.annotated.api.processor.network.ClientPacket;
import fr.max2.annotated.api.processor.network.DelegateChannel;
import fr.max2.annotated.api.processor.network.GenerateChannel;
import fr.max2.annotated.api.processor.network.ServerPacket;
import fr.max2.annotated.processor.network.model.ChannelProvider;
import fr.max2.annotated.processor.network.model.EnumSide;
import fr.max2.annotated.processor.utils.ClassName;
import fr.max2.annotated.processor.utils.ClassRef;
import fr.max2.annotated.processor.utils.ProcessingTools;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class PacketProcessor extends AbstractProcessor
{
	private static final Set<String> SUPPORTED_ANNOTATIONS = Collections.unmodifiableSet(Stream.of(
			GenerateChannel.class, DelegateChannel.class,
			ClientPacket.class, ServerPacket.class
		).map(Class::getCanonicalName).collect(Collectors.toSet()));
	
	private ProcessingTools tools;
	private Set<ClassName> processedClasses = new HashSet<>();
	
	@Override
	public Set<String> getSupportedAnnotationTypes()
	{
        return SUPPORTED_ANNOTATIONS;
	}
	
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv)
	{
		super.init(processingEnv);
		this.tools = new ProcessingTools(this.processingEnv);
		this.processedClasses.clear();
	}
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		if (roundEnv.processingOver())
			return false;
		
		
		Collection<NetworkProcessingUnit> networks;
		try
		{
			networks = buildProcessingUnits(roundEnv);
		}
		catch (Exception e)
		{
			this.processingEnv.getMessager().printMessage(Kind.ERROR, "Unexpected exception while building of the processing units : " + e.getClass().getCanonicalName() + ": " + e.getMessage());
			return true;
		}
		
		for (NetworkProcessingUnit network : networks)
		{
			if (this.processedClasses.contains(network.enclosingClassName))
				continue; // Skip the class if it has already been processed in a previous round
			
			network.processNetwork();
			
			if (!network.hasErrors())
				this.processedClasses.add(network.enclosingClassName);
		}
		
		return true;
	}
	
	private Collection<NetworkProcessingUnit> buildProcessingUnits(RoundEnvironment roundEnv)
	{
		Map<TypeElement, NetworkProcessingUnit> networks = new HashMap<>();
		
		for(ChannelProvider provider : ChannelProvider.values())
		{
			for (TypeElement elem : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(provider.getAnnotationClass())))
			{
				networks.put(elem, new NetworkProcessingUnit(this.tools, elem, provider, findModAnnotationId(elem)));
			}
		}
		
		for (EnumSide side : EnumSide.values())
		{
			for (ExecutableElement method : ElementFilter.methodsIn(roundEnv.getElementsAnnotatedWith(side.getAnnotationClass())))
			{
				if (method.getAnnotation(side.opposite().getAnnotationClass()) != null)
				{
					this.tools.log(Kind.ERROR, "Packets can only be send to a single logical side", method, this.tools.elements.getAnnotationMirror(method, side.getAnnotationClass().getCanonicalName()));
					continue; // Skip this packet
				}
				
				TypeElement enclosingClass = this.tools.elements.asTypeElement(method.getEnclosingElement());
				TypeElement parent = enclosingClass;
				while (!networks.containsKey(enclosingClass) && parent != null)
				{
					enclosingClass = parent;
					parent = this.tools.elements.asTypeElement(parent.getEnclosingElement());
				}
				
				if (enclosingClass == null)
				{
					this.tools.log(Kind.ERROR, "Unable find the enclosing class of the method", method, this.tools.elements.getAnnotationMirror(method, side.getAnnotationClass().getCanonicalName()));
					continue; // Skip this packet
				}
				else if (!networks.containsKey(enclosingClass))
				{
					this.tools.log(Kind.ERROR, "Unable find the enclosing channel of the method, use " + GenerateChannel.class.getCanonicalName() + " or " + DelegateChannel.class.getCanonicalName() + " on the enclosing class to define the channel to use", method, this.tools.elements.getAnnotationMirror(method, side.getAnnotationClass().getCanonicalName()));
				}
				
				NetworkProcessingUnit networkUnit = networks.computeIfAbsent(enclosingClass, clazz -> new NetworkProcessingUnit(this.tools, clazz, null, findModAnnotationId(clazz)));
				networkUnit.addPacket(method, side);
			}
		}
		
		return networks.values();
	}
	
	public String findModAnnotationId(Element elem)
	{
		PackageElement pkg = this.tools.elements.getPackageOf(elem);
		String packageName = pkg.getQualifiedName().toString();
		
		while (pkg != null)
		{
			for (Element e : pkg.getEnclosedElements())
			{
				String id = extractModId(e);
				if (id != null)
					return id;
			}
			
			int separator = packageName.lastIndexOf('.');
			if (separator < 0)
				break;
			
			packageName = packageName.substring(0, separator);
			pkg = this.tools.elements.getPackageElement(packageName);
		}
		
		return null;
	}
	
	private String extractModId(Element elem)
	{
		return this.tools.elements.getAnnotationValue(elem, ClassRef.FORGE_MOD_ANNOTATION, "value")
						 .map(an -> an.getValue().toString())
						 .orElse(null);
	}
	//TODO [v2.1] Add code completion
}
