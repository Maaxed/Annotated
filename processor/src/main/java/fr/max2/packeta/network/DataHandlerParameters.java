package fr.max2.packeta.network;

import java.security.InvalidParameterException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import fr.max2.packeta.api.network.CustomData;
import fr.max2.packeta.api.network.DataType;
import fr.max2.packeta.network.datahandler.ArrayDataHandler;
import fr.max2.packeta.network.datahandler.CollectionDataHandler;
import fr.max2.packeta.network.datahandler.IDataHandler;
import fr.max2.packeta.network.datahandler.PrimitiveDataHandler;
import fr.max2.packeta.network.datahandler.SimpleClassHandler;
import fr.max2.packeta.network.datahandler.SpecialDataHandler;
import fr.max2.packeta.utils.ValueInitStatus;

public class DataHandlerParameters
{
	public final String simpleName, saveAccessExpr, loadAccessExpr;
	public final UnaryOperator<String> setExpr;
	public final TypeMirror type;
	public final AnnotatedConstruct annotations;
	public final IDataHandler typeHandler;
	public final ValueInitStatus initStatus;
	public final Finder finder;
	public final String[] parameters;
	
	public DataHandlerParameters(String simpleName, String saveGetExpr, String loadGetExpr, UnaryOperator<String> setExpr, TypeMirror type, AnnotatedConstruct annotations, IDataHandler typeHandler, ValueInitStatus initStatus, Finder finder, String... parameters)
	{
		this.simpleName = simpleName;
		this.saveAccessExpr = saveGetExpr;
		this.loadAccessExpr = loadGetExpr;
		this.setExpr = setExpr;
		this.type = type;
		this.annotations = annotations;
		this.typeHandler = typeHandler;
		this.initStatus = initStatus;
		this.finder = finder;
		this.parameters = parameters;
	}
	
	public DataHandlerParameters(String simpleName, String saveGetExpr, String loadGetExpr, UnaryOperator<String> setExpr, TypeMirror type, AnnotatedConstruct annotations, CustomData data, ValueInitStatus hasDefaultValue, Finder finder)
	{
		this(simpleName, saveGetExpr, loadGetExpr, setExpr, type, annotations, dataTypeToHandler(data.type()), hasDefaultValue, finder, data.value());
	}

	/*public String firstSetInit()
	{
		return this.initStatus.isDeclared() ? this.setExpr : NamingUtils.simpleTypeName(this.type) + " " + this.setExpr;
	}*/
	
	public void addInstructions(Consumer<String> saveInstructions, Consumer<String> loadInstructions, Consumer<String> imports)
	{
		this.typeHandler.addInstructions(this, saveInstructions, loadInstructions, imports);
	}
	
	
	
	public static class Finder
	{
		private final List<Map.Entry<Predicate<TypeMirror>, IDataHandler>> typeMap;
		public final Elements elemUtils;
		public final Types typeUtils;
		
		public Finder(ProcessingEnvironment env)
		{
			this.elemUtils = env.getElementUtils();
			this.typeUtils = env.getTypeUtils();
			
			this.typeMap = TYPE_TO_HANDLER.values().stream().map(data -> new SimpleEntry<>(data.getTypeValidator(this.elemUtils, this.typeUtils), data)).collect(Collectors.toList());
		}
		
		public DataHandlerParameters getDataType(Element field)
		{//TODO special case for setExpr if undefined status
			String setExpr = "this." + field.getSimpleName() + " = ";
			return this.getDataType(field.getSimpleName().toString(), "this." + field.getSimpleName(), "this." + field.getSimpleName(), value -> setExpr + value + ";", field.asType(), field, ValueInitStatus.INITIALISED);
		}
		
		public DataHandlerParameters getDataType(String simpleName, String saveGetExpr, String loadGetExpr, UnaryOperator<String> setExpr, TypeMirror type, AnnotatedConstruct annotations, ValueInitStatus initStatus)
		{
			DataHandlerParameters params = this.getDataTypeOrNull(simpleName, saveGetExpr, loadGetExpr, setExpr, type, annotations, initStatus);
			if (params == null) throw new InvalidParameterException("Unknown default DataHandler for type '" + type + "'");
			return params;
		}
		
		public DataHandlerParameters getDataTypeOrNull(String simpleName, String saveGetExpr, String loadGetExpr, UnaryOperator<String> setExpr, TypeMirror type, AnnotatedConstruct annotations, ValueInitStatus initStatus)
		{
			CustomData customData = annotations.getAnnotation(CustomData.class);
			if (customData == null) customData = type.getAnnotation(CustomData.class);
			if (customData == null && type instanceof DeclaredType) customData = ((DeclaredType)type).asElement().getAnnotation(CustomData.class);
			
			if (customData != null)
			{
				return new DataHandlerParameters(simpleName, saveGetExpr, loadGetExpr, setExpr, type, annotations, customData, initStatus, this);
			}
			
			IDataHandler defaultHandler = this.getDefaultDataType(type);
			
			if (defaultHandler == SpecialDataHandler.CUSTOM) return null;
			
			return new DataHandlerParameters(simpleName, saveGetExpr, loadGetExpr, setExpr, type, annotations, defaultHandler, initStatus, this);
		}
		
		public IDataHandler getDefaultDataType(TypeMirror type)
		{
			for (Entry<Predicate<TypeMirror>, IDataHandler> entry : this.typeMap)
			{
				if (entry.getKey().test(type))
				{
					return entry.getValue();
				}
			}
			
			return SpecialDataHandler.CUSTOM;
		}
	}
	
	
	private static Map<DataType, IDataHandler> TYPE_TO_HANDLER = new EnumMap<>(DataType.class);
	
	static
	{
		TYPE_TO_HANDLER.put(DataType.BYTE, PrimitiveDataHandler.BYTE);
		TYPE_TO_HANDLER.put(DataType.SHORT, PrimitiveDataHandler.SHORT);
		TYPE_TO_HANDLER.put(DataType.INT, PrimitiveDataHandler.INT);
		TYPE_TO_HANDLER.put(DataType.LONG, PrimitiveDataHandler.LONG);
		TYPE_TO_HANDLER.put(DataType.FLOAT, PrimitiveDataHandler.FLOAT);
		TYPE_TO_HANDLER.put(DataType.DOUBLE, PrimitiveDataHandler.DOUBLE);
		TYPE_TO_HANDLER.put(DataType.BOOLEAN, PrimitiveDataHandler.BOOLEAN);
		TYPE_TO_HANDLER.put(DataType.CHAR, PrimitiveDataHandler.CHAR);
		
		TYPE_TO_HANDLER.put(DataType.ARRAY, ArrayDataHandler.INSTANCE);
		
		TYPE_TO_HANDLER.put(DataType.STRING, SimpleClassHandler.STRING);
		TYPE_TO_HANDLER.put(DataType.ENUM, SimpleClassHandler.ENUM);
		TYPE_TO_HANDLER.put(DataType.UUID, SimpleClassHandler.UUID);
		TYPE_TO_HANDLER.put(DataType.NBT_COMPOUND, SimpleClassHandler.NBT_COMPOUND);
		TYPE_TO_HANDLER.put(DataType.STACK, SimpleClassHandler.STACK);
		
		TYPE_TO_HANDLER.put(DataType.COLLECTION, CollectionDataHandler.INSTANCE);
		
		TYPE_TO_HANDLER.put(DataType.WILDCARD, SpecialDataHandler.WILDCRD);
		TYPE_TO_HANDLER.put(DataType.TYPE_VARIABLE, SpecialDataHandler.VARIABLE_TYPE);
		TYPE_TO_HANDLER.put(DataType.INTERSECTION, SpecialDataHandler.INTERSECTION);
		
		TYPE_TO_HANDLER.put(DataType.DEFAULT, SpecialDataHandler.DEFAULT);
		TYPE_TO_HANDLER.put(DataType.CUSTOM, SpecialDataHandler.CUSTOM);
	}
	
	private static IDataHandler dataTypeToHandler(DataType type)
	{
		return TYPE_TO_HANDLER.get(type);
	}
	
}
