package fr.max2.annotated.processor.network;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import fr.max2.annotated.api.processor.network.GenerateChannel;
import fr.max2.annotated.api.processor.network.ServerPacket;
import fr.max2.annotated.processor.utils.ClassRef;
import fr.max2.annotated.processor.utils.EnumSide;
import fr.max2.annotated.processor.utils.ProcessingTools;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class PacketProcessor extends AbstractProcessor
{
	private Set<String> supportedAnnotations;
	private ProcessingTools tools;
	
	@Override
	public Set<String> getSupportedAnnotationTypes()
	{
		if (supportedAnnotations == null)
		{
	        Set<String> set = new HashSet<>();
	        set.add(GenerateChannel.class.getCanonicalName());
	        set.add(ClientPacket.class.getCanonicalName());
	        set.add(ServerPacket.class.getCanonicalName());
			supportedAnnotations = Collections.unmodifiableSet(set);
		}
        return supportedAnnotations;
	}
	
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv)
	{
		super.init(processingEnv);
		this.tools = new ProcessingTools(this.processingEnv);
	}
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		if (roundEnv.processingOver()) return true;
		
		Collection<NetworkProcessingUnit> networks;
		try
		{
			networks = buildProcessingUnits(roundEnv);
		}
		catch (Exception e)
		{
			this.processingEnv.getMessager().printMessage(Kind.ERROR, "An exception occured during the processing units building phase: " + e.getMessage());
			return true;
		}
		
		networks.forEach(NetworkProcessingUnit::processNetwork);
		
		return true;
	}
	
	private Collection<NetworkProcessingUnit> buildProcessingUnits(RoundEnvironment roundEnv)
	{
		Map<TypeElement, NetworkProcessingUnit> networks = new HashMap<>();
		
		for (TypeElement elem : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(GenerateChannel.class)))
		{
			networks.put(elem, new NetworkProcessingUnit(this.tools, elem, findModAnnotationId(elem)));
		}
		
		for (EnumSide side : EnumSide.values())
		{
			for (ExecutableElement method : ElementFilter.methodsIn(roundEnv.getElementsAnnotatedWith(side.getAnnotationClass())))
			{
				if (method.getAnnotation(side.opposite().getAnnotationClass()) != null)
				{
					this.tools.log(Kind.ERROR, "Packets can only be send to a single logical side", method, this.tools.typeHelper.getAnnotationMirror(method, side.getAnnotationClass().getCanonicalName()));
					continue; // Skip this packet
				}
				
				TypeElement enclosingClass = this.tools.typeHelper.asTypeElement(method.getEnclosingElement());
				TypeElement parent = enclosingClass;
				while (!networks.containsKey(enclosingClass) && parent != null)
				{
					enclosingClass = parent;
					parent = this.tools.typeHelper.asTypeElement(parent.getEnclosingElement());
				}
				
				if (enclosingClass == null)
				{
					this.tools.log(Kind.ERROR, "Couldn't find the enclosing class of the method", method, this.tools.typeHelper.getAnnotationMirror(method, side.getAnnotationClass().getCanonicalName()));
					continue; // Skip this packet
				}
				
				NetworkProcessingUnit networkUnit = networks.computeIfAbsent(enclosingClass, clazz -> new NetworkProcessingUnit(this.tools, clazz, findModAnnotationId(clazz)));
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
		return this.tools.typeHelper.getAnnotationValue(elem, ClassRef.FORGE_MOD_ANNOTATION, "value")
						 .map(an -> an.getValue().toString())
						 .orElse(null);
	}
	//TODO [v2.0] Add code completion
}
