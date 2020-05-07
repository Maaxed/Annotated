package fr.max2.annotated.processor.network;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;

import fr.max2.annotated.api.processor.network.ClientPacket;
import fr.max2.annotated.api.processor.network.GenerateChannel;
import fr.max2.annotated.api.processor.network.ServerPacket;
import fr.max2.annotated.processor.utils.ClassRef;
import fr.max2.annotated.processor.utils.EnumSide;
import fr.max2.annotated.processor.utils.TypeHelper;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class PacketProcessor extends AbstractProcessor
{
	private Set<String> supportedAnnotations;
	private DataHandlerParameters.Finder finder;
	
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
		this.finder = new DataHandlerParameters.Finder(this.processingEnv);
	}
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		if (roundEnv.processingOver()) return true;
		
		Messager logs = this.processingEnv.getMessager();
		Collection<NetworkProcessingUnit> networks;
		
		try
		{
			networks = buildProcessingUnits(roundEnv);
		}
		catch (Exception e)
		{
			logs.printMessage(Kind.ERROR, "An exception occured during the processing units building phase");
			throw e;
		}
		
		networks.forEach(NetworkProcessingUnit::processNetwork);
		
		logs.printMessage(Kind.NOTE, "End of the " + this.getClass().getCanonicalName() + " annotation processos !");
		
		return true;
	}
	
	private Collection<NetworkProcessingUnit> buildProcessingUnits(RoundEnvironment roundEnv)
	{
		Map<TypeElement, NetworkProcessingUnit> networks = new HashMap<>();
		Messager logs = this.processingEnv.getMessager();
		
		logs.printMessage(Kind.NOTE, "Building processing units");
		
		for (TypeElement elem : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(GenerateChannel.class)))
		{
			networks.put(elem, new NetworkProcessingUnit(this, elem));
		}
		
		for (EnumSide side : EnumSide.values())
		{
			for (ExecutableElement method : ElementFilter.methodsIn(roundEnv.getElementsAnnotatedWith(side.getAnnotationClass())))
			{
				if (method.getAnnotation(side.opposite().getAnnotationClass()) != null)
				{
					logs.printMessage(Kind.ERROR, "Packets can only be send to a single logical side", method, TypeHelper.getAnnotationMirror(this.processingEnv.getTypeUtils(), method, side.getAnnotationClass().getCanonicalName()).get());
					continue; // Skip this packet
				}
				
				TypeElement enclosingClass = TypeHelper.asTypeElement(method.getEnclosingElement());
				TypeElement parent = enclosingClass;
				while (!networks.containsKey(enclosingClass) && parent != null)
				{
					enclosingClass = parent;
					parent = TypeHelper.asTypeElement(parent.getEnclosingElement());
				}
				
				if (enclosingClass == null)
				{
					logs.printMessage(Kind.ERROR, "Couldn't find the enclosing class of the method", method, TypeHelper.getAnnotationMirror(this.processingEnv.getTypeUtils(), method, side.getAnnotationClass().getCanonicalName()).get());
					continue; // Skip this packet
				}
				
				NetworkProcessingUnit networkUnit = networks.computeIfAbsent(enclosingClass, clazz -> new NetworkProcessingUnit(this, clazz));
				networkUnit.addPacket(method, side);
			}
		}
		
		return networks.values();
	}
	
	public String findModAnnotationId(Element elem)
	{
		Elements elemUtils =  this.processingEnv.getElementUtils();
		PackageElement pkg = elemUtils.getPackageOf(elem);
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
			pkg = elemUtils.getPackageElement(packageName);
		}
		
		return null;
	}
	
	private String extractModId(Element elem)
	{
		return TypeHelper.getAnnotationValue(this.processingEnv.getTypeUtils(), elem, ClassRef.FORGE_MOD_ANNOTATION, "value").map(an -> an.getValue().toString()).orElse(null);
	}
	
	public void log(Diagnostic.Kind kind, CharSequence msg, Element e)
	{
		this.processingEnv.getMessager().printMessage(kind, msg, e);
	}
	
	public void log(Diagnostic.Kind kind, CharSequence msg, Element e, Optional<? extends AnnotationMirror> a)
	{
		if (a.isPresent())
			this.processingEnv.getMessager().printMessage(kind, msg, e, a.get());
		else
			this.log(kind, msg, e);
	}
	
	public void log(Diagnostic.Kind kind, CharSequence msg, Element e, Optional<? extends AnnotationMirror> a, String property)
	{
		if (a.isPresent())
		{
			Optional<? extends AnnotationValue> value = TypeHelper.getAnnotationValue(a, property);
			if (value.isPresent())
			{
				this.processingEnv.getMessager().printMessage(kind, msg, e, a.get(), value.get());
			}
			else
			{
				this.processingEnv.getMessager().printMessage(kind, msg, e, a.get());
			}
		}
		else
		{
			this.log(kind, msg, e);
		}
	}
	
	public DataHandlerParameters.Finder getFinder()
	{
		return finder;
	}
	
	public Filer filer()
	{
		return this.processingEnv.getFiler();
	}
	
	public Elements elementUtils()
	{
		return this.processingEnv.getElementUtils();
	}
	
	public Types typeUtils()
	{
		return this.processingEnv.getTypeUtils();
	}
	
}
