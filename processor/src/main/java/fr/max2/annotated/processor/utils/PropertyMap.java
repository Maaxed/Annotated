package fr.max2.annotated.processor.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import fr.max2.annotated.processor.utils.exceptions.InvalidPropertyException;

public class PropertyMap
{
	public static final PropertyMap EMPTY_PROPERTIES = new PropertyMap();
	
	private final Map<String, PropertyValue> properties = new HashMap<>();
	
	private PropertyMap()
	{ }
	
	public static PropertyMap fromArray(String[] properties) throws InvalidPropertyException
	{
		if (properties.length == 0)
			return EMPTY_PROPERTIES;
		
		PropertyMap topLevelMap = new PropertyMap();
		for (String property : properties)
		{
			int sep = property.indexOf('=');
			if (sep == -1)
				throw new InvalidPropertyException("The property '" + property + "' is invalid");
			
			String[] identifiers = property.substring(0, sep).split("\\.");
			String value = property.substring(sep + 1);
			
			PropertyMap map = topLevelMap;
			
			for (int i = 0; i < identifiers.length - 1; i++)
			{
				map = map.createSubProperties(identifiers[i]);
			}
			
			map.createValue(identifiers[identifiers.length - 1], value);
		}
		return topLevelMap;
	}
	
	private PropertyMap createSubProperties(String identifier) throws InvalidPropertyException
	{
		if (this.properties.containsKey(identifier))
		{
			PropertyValue prop = this.properties.get(identifier);
			if (!prop.isPropertyMap())
				throw new InvalidPropertyException("The property '" + identifier + "' has a value and sub values");
			
			return prop.asPropertyMap();
		}
		PropertyMap newMap = new PropertyMap();
		this.properties.put(identifier, new PropertyValue(identifier, newMap));
		return newMap;
	}
	
	private void createValue(String identifier, String value) throws InvalidPropertyException
	{
		if (this.properties.containsKey(identifier))
		{
			throw new InvalidPropertyException("The property '" + identifier + "' is defined several times");
		}
		this.properties.put(identifier, new PropertyValue(identifier, value));
	}

	public PropertyMap overrideWith(PropertyMap other) throws InvalidPropertyException
	{
		if (other.properties.isEmpty())
			return this;
		
		if (this.properties.isEmpty())
			return other;
		
		for (PropertyValue value : other.properties.values())
		{
			if (value.isPropertyMap() && this.properties.containsKey(value.identifier))
			{
				// Combine maps
				PropertyValue v2 = this.properties.get(value.identifier);
				
				if (!v2.isPropertyMap())
				{
					throw new InvalidPropertyException("The type of the property '" + value.identifier + "' doesn't match");
				}
				this.properties.put(value.identifier, new PropertyValue(value.identifier, v2.asPropertyMap().overrideWith(value.asPropertyMap())));
			}
			else
			{
				// Add value to map
				this.properties.put(value.identifier, value);
			}
		}
		
		return this;
	}
	
	private Optional<PropertyValue> getProperty(String identifier)
	{
		return Optional.ofNullable(this.properties.get(identifier)).map(PropertyValue::setUsed);
	}
	
	public Optional<String> getValue(String identifier) throws InvalidPropertyException
	{
		Optional<PropertyValue> prop = getProperty(identifier);
		return prop.isPresent() ? Optional.of(prop.get().asString()) : Optional.empty();
	}
	
	public Optional<PropertyMap> getSubProperties(String identifier) throws InvalidPropertyException
	{
		Optional<PropertyValue> prop = getProperty(identifier);
		return prop.isPresent() ? Optional.of(prop.get().asPropertyMap()) : Optional.empty();
	}
	
	public String getValueOrEmpty(String identifier) throws InvalidPropertyException
	{
		return getValue(identifier).orElse("");
	}
	
	public PropertyMap getSubPropertiesOrEmpty(String identifier) throws InvalidPropertyException
	{
		return getSubProperties(identifier).orElse(EMPTY_PROPERTIES);
	}

	public String requireValue(String identifier) throws InvalidPropertyException
	{
		return getValue(identifier).orElseThrow(() -> new InvalidPropertyException("The mandatory property '" + identifier + "' is missing"));
	}
	
	public PropertyMap requireSubProperties(String identifier) throws InvalidPropertyException
	{
		return getSubProperties(identifier).orElseThrow(() -> new InvalidPropertyException("The mandatory properties under '" + identifier + "' are missing"));
	}

	public void checkUnusedProperties() throws InvalidPropertyException
	{
		Optional<PropertyValue> p = this.properties.values().stream().filter(prop -> !prop.used).findAny();
		if (p.isPresent())
			throw new InvalidPropertyException("The property identifier '" + p.get().identifier + "' is invalid");
		
		for (PropertyValue property : this.properties.values())
		{
			property.checkUnusedProperties();
		}
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
			return this.value instanceof String;
		}
		
		private boolean isPropertyMap()
		{
			return this.value instanceof PropertyMap;
		}

		private String asString() throws InvalidPropertyException
		{
			if (!this.isString())
				throw new InvalidPropertyException("The value of the property '" + this.identifier + "' should be a string but is a permerty map");
			
			return (String)this.value;
		}
		
		private PropertyMap asPropertyMap() throws InvalidPropertyException
		{
			if (!isPropertyMap())
				throw new InvalidPropertyException("The value of the property '" + this.identifier + "' should be a property map but is a string");
			
			return (PropertyMap)this.value;
		}
		
		private PropertyValue setUsed()
		{
			this.used = true;
			return this;
		}

		private void checkUnusedProperties() throws InvalidPropertyException
		{
			if (this.value instanceof PropertyMap)
			{
				((PropertyMap)this.value).checkUnusedProperties();
			}
		}
	}
}
