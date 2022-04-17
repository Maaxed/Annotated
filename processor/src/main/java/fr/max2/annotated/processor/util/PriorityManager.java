package fr.max2.annotated.processor.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PriorityManager<T>
{
	private final Map<T, PriorityRule> rules = new HashMap<>();
	
	public PriorityRule prioritize(T value)
	{
		return this.rules.computeIfAbsent(value, v -> new PriorityRule());
	}
	
	public List<T> getHighests(Iterable<T> values)
	{
		Set<PriorityRule> visited = new HashSet<>();
		
		for (T value : values)
		{
			PriorityRule rule = this.rules.get(value);
			if (rule != null && !visited.contains(rule))
			{
				rule.dfsDown(visited);
			}
		}
		
		List<T> results = new ArrayList<>();
		
		for (T value : values)
		{
			PriorityRule rule = this.rules.get(value);
			if (rule == null || !visited.contains(rule))
			{
				results.add(value);
			}
		}
		
		return results;
	}
	
	public class PriorityRule implements Comparable<PriorityRule>
	{
		private final Set<PriorityRule> lowerRules = new HashSet<>();
		
		public PriorityRule over(T other)
		{
			PriorityRule otherRule = prioritize(other);
			// Check for existing rules
			if (otherRule.lowerRules.contains(this) || this.lowerRules.contains(otherRule))
			{
				throw new IllegalArgumentException("A rule already applies to the two objects");
			}
			
			// Check for cycles
			if (this.isHigherThan(otherRule))
			{
				throw new IllegalArgumentException("The rule causes a priority cycle");
			}
			
			this.lowerRules.add(otherRule);
			return this;
		}

		@Override
		public int compareTo(PriorityRule other)
		{
			Set<PriorityRule> visited = new HashSet<>();
			if (this.isHigherThan(other, visited))
			{
				return 1;
			}
			else if (other.isHigherThan(this, visited))
			{
				return -1;
			}
			return 0;
		}
		
		/**
		 * Indicates if this rule is strictly higher than the given rule
		 * @param other the rule to compare to
		 * @return true if this rule is strictly higher, false otherwise
		 */
		private boolean isHigherThan(PriorityRule other)
		{
			return isHigherThan(other, new HashSet<>());
		}
		
		private boolean isHigherThan(PriorityRule other, Set<PriorityRule> visited)
		{
			visited.add(this);
			
			for (PriorityRule lowerRule : this.lowerRules)
			{
				if (lowerRule == other || (! visited.contains(lowerRule) && lowerRule.isHigherThan(other, visited)))
				{
					return true;
				}
			}
			return false;
		}
		
		private void dfsDown(Set<PriorityRule> visited)
		{
			for (PriorityRule lowerRule : this.lowerRules)
			{
				if (!visited.contains(lowerRule))
				{
					visited.add(lowerRule);
					lowerRule.dfsDown(visited);
				}
			}
		}
	}
}
