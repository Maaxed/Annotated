package fr.max2.annotated.processor.utils;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PropertyMap
{
	public static final PropertyMap EMPTY_PROPERTIES = new PropertyMap();
	
	private final Map<String, PropertyValue> properties = new HashMap<>();
	
	private PropertyMap()
	{
		this(new String[0]);
	}
	
	public PropertyMap(String[] properties)
	{
		for (String property : properties)
		{
			int sep = property.indexOf('=');
			if (sep == -1)
				throw new InvalidParameterException("The property '" + property + "' is invalid");
			
			String[] identifiers = property.substring(0, sep).split("\\.");
			String value = property.substring(sep + 1);
			
			PropertyMap map = this;
			
			for (int i = 0; i < identifiers.length - 1; i++)
			{
				map = map.createSubProperties(identifiers[i]);
			}
			
			map.createValue(identifiers[identifiers.length - 1], value);
		}
	}
	
	private PropertyMap createSubProperties(String identifier)
	{
		if (this.properties.containsKey(identifier))
		{
			PropertyValue prop = this.properties.get(identifier);
			if (!prop.isPropertyMap())
				throw new InvalidParameterException("The property '" + identifier + "' has a value and sub values");
			
			return prop.asPropertyMap();
		}
		PropertyMap newMap = new PropertyMap();
		this.properties.put(identifier, new PropertyValue(identifier, newMap));
		return newMap;
	}
	
	private void createValue(String identifier, String value)
	{
		if (this.properties.containsKey(identifier))
		{
			throw new InvalidParameterException("The property '" + identifier + "' is defined several times");
		}
		this.properties.put(identifier, new PropertyValue(identifier, value));
	}
	
	private Optional<PropertyValue> getProperty(String identifier)
	{
		return Optional.ofNullable(this.properties.get(identifier)).map(PropertyValue::setUsed);
	}
	
	public Optional<String> getValue(String identifier)
	{
		return getProperty(identifier).map(PropertyValue::asString);
	}
	
	public Optional<PropertyMap> getSubProperties(String identifier)
	{
		return getProperty(identifier).map(PropertyValue::asPropertyMap);
	}
	
	public String getValueOrEmpty(String identifier)
	{
		return getValue(identifier).orElse("");
	}
	
	public PropertyMap getSubPropertiesOrEmpty(String identifier)
	{
		return getSubProperties(identifier).orElse(EMPTY_PROPERTIES);
	}

	public String requireValue(String identifier)
	{
		return getValue(identifier).orElseThrow(() -> new InvalidParameterException("The mandatory property '" + identifier + "' is missing"));
	}
	
	public PropertyMap requireSubProperties(String identifier)
	{
		return getSubProperties(identifier).orElseThrow(() -> new InvalidParameterException("The mandatory properties under '" + identifier + "' are missing"));
	}

	public void checkUnusedProperties()
	{
		this.properties.values().stream()
		.filter(prop -> !prop.used)
		.findAny().ifPresent(prop ->
		{
			throw new InvalidParameterException("The property identifier '" + prop.identifier + "' is invalid");
		});
		
		this.properties.values().forEach(PropertyValue::checkUnusedProperties);
	}
	
	private static class PropertyValue
	{
		private final String identifier;
		private final Object value;
		private boolean used = false;
		
		private PropertyValue(String identifier, Object value)
		{
			this.identifier = identifier;
			this.value = value;
		}
		
		private boolean isString()
		{
			return value instanceof String;
		}
		
		private boolean isPropertyMap()
		{
			return value instanceof PropertyMap;
		}

		private String asString()
		{
			if (!this.isString())
				throw new InvalidParameterException("The value of the property property '" + this.identifier + "' is should be a string");
			
			return (String)value;
		}
		
		private PropertyMap asPropertyMap()
		{
			if (!isPropertyMap())
				throw new InvalidParameterException("The value of the property property '" + this.identifier + "' is should be a property map");
			
			return (PropertyMap)value;
		}
		
		private PropertyValue setUsed()
		{
			this.used = true;
			return this;
		}

		private void checkUnusedProperties()
		{
			if (value instanceof PropertyMap)
			{
				((PropertyMap)value).checkUnusedProperties();
			}
		}
	}
}
