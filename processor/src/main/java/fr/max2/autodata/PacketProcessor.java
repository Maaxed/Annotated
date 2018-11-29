package fr.max2.autodata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

import fr.max2.autodata.api.network.DataType;
import fr.max2.autodata.api.network.DataType.DataHandler;
import fr.max2.autodata.api.network.GeneratePacket;
import fr.max2.autodata.utils.ClassRef;
import fr.max2.autodata.utils.EnumSides;
import fr.max2.autodata.utils.ExceptionUtils;

@SupportedAnnotationTypes(ClassRef.PACKET_ANNOTATION)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class PacketProcessor extends AbstractProcessor
{
	private DataType.Finder finder;
	
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv)
	{
		super.init(processingEnv);
		this.finder = new DataType.Finder(this.processingEnv);
	}
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		for (Element elem : roundEnv.getElementsAnnotatedWith(GeneratePacket.class))
		{
			if (elem.getKind() == ElementKind.CLASS)
			{
				TypeElement type = (TypeElement)elem;
				
				Name className = type.getQualifiedName();
				EnumSides sides = sidesFromClass(type.asType());
				List<? extends Element> members = this.processingEnv.getElementUtils().getAllMembers(type);
				try
				{
					JavaFileObject file = this.processingEnv.getFiler().createSourceFile(className + "Message");
					Writer writer = file.openWriter();
					
					writePacket(writer, className.toString(), sides, members);
					
					writer.close();
				}
				catch (IOException e)
				{
					throw new UncheckedIOException(e);
				}
			}
		}
		return true;
	}
	
	public EnumSides sidesFromClass(TypeMirror type)
	{
		Elements elemUtils = this.processingEnv.getElementUtils();
		Types typeUtils = this.processingEnv.getTypeUtils();
		
		TypeMirror clientType = elemUtils.getTypeElement(ClassRef.CLIENT_PACKET).asType();
		TypeMirror serverType = elemUtils.getTypeElement(ClassRef.SERVER_PACKET).asType();
		
		boolean isClient = typeUtils.isAssignable(type, clientType);
		boolean isServer = typeUtils.isAssignable(type, serverType);
		
		if (isClient)
		{
			return isServer ? EnumSides.BOTH : EnumSides.CLIENT;
		}
		else if (isServer)
		{
			return EnumSides.SERVER;
		}
		else
		{
			throw new IllegalArgumentException();
		}
	}
	
	private void writePacket(Writer wr, String className, EnumSides sides, List<? extends Element> members) throws IOException
	{
		String ls = System.lineSeparator();
		
		int packageSeparator = className.lastIndexOf('.');
		
		List<Element> fields = members.stream().filter(m -> m.getKind() == ElementKind.FIELD).collect(Collectors.toList());
		List<DataHandler> dataHandlers = fields.stream().map(f -> this.finder.getDataType(f)).collect(Collectors.toList());
		
		Set<String> imports = new TreeSet<>();
		
		sides.addImports(imports);
		
		fields.stream().map(f -> f.asType()).filter(t -> t.getKind() == TypeKind.DECLARED).map(f -> f.toString()).filter(f -> !f.startsWith("java.lang")).forEach(imports::add);
		
		dataHandlers.forEach(handler -> handler.addImportsToSet(imports));
		
		Map<String, String> replacements = new HashMap<>();
		replacements.put("package", className.substring(0, packageSeparator));
		replacements.put("baseClass", className.substring(packageSeparator + 1));
		replacements.put("interfaces", sides.getInterfaces());
		replacements.put("allFields", fields.stream().map(f -> simpleName(f.asType()) + " " + f.getSimpleName()).collect(Collectors.joining(", ")));
		replacements.put("fieldsInit", fields.stream().map(f -> "this." + f.getSimpleName() + " = " + f.getSimpleName() + ";").collect(Collectors.joining(ls + "\t\t")));
		replacements.put("toBytes", dataHandlers.stream().flatMap(h -> Stream.of(h.saveDataInstructions())).collect(Collectors.joining(ls + "\t\t")));
		replacements.put("fromBytes", dataHandlers.stream().flatMap(h -> Stream.of(h.loadDataInstructions())).collect(Collectors.joining(ls + "\t\t")));
		replacements.put("imports", imports.stream().map(i -> "import " + i + ";" + ls).collect(Collectors.joining()));
		
		try (InputStream fileStream = this.getClass().getClassLoader().getResourceAsStream("templates/TemplatePacket.jvtp"))
		{
			new BufferedReader(new InputStreamReader(fileStream)).lines().map(line -> PacketProcessor.mapKeys(line, replacements) + ls).forEach(ExceptionUtils.toTryRun(wr::write));
		}
	}
	
	private static String mapKeys(String content, Map<String, String> replacements)
	{
		Pattern p = Pattern.compile("\\$\\{(.+?)\\}"); //	${key}
		Matcher m = p.matcher(content);
		
		StringBuffer sb = new StringBuffer();
		while (m.find())
		{
			String key = m.group(1);
			String rep = replacements.get(key);
		    m.appendReplacement(sb, rep == null ? key : rep);
		}
		m.appendTail(sb);
		
		return sb.toString();
	}
	
	private static CharSequence simpleName(TypeMirror type)
	{
		return type.getKind() == TypeKind.DECLARED ? ((TypeElement)((DeclaredType)type).asElement()).getSimpleName() : type.toString();
	}
	
}
