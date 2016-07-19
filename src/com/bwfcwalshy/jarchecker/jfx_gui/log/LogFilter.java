package com.bwfcwalshy.jarchecker.jfx_gui.log;

import java.util.function.Predicate;
import java.util.logging.Level;

/**
 * A filter in the log
 */
public class LogFilter {

	private String description;
	private FilterType type;
	private Predicate<Level> filter;
	
	/**
	 * @param type The type of the filter
	 * @param filter The filter
	 * @param description The description of the filter
	 */
	public LogFilter(FilterType type, Predicate<Level> filter, String description) {
		this.type = type;
		this.filter = filter;
		this.description = description;
	}

	/**
	 * @return The type
	 */
	public FilterType getType() {
		return type;
	}
	
	/**
	 * @return The filter predicate
	 */
	public Predicate<Level> getFilter() {
		return filter;
	}
	
	/**
	 * @return The description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * @param filter The filter to add it to
	 * @return The resulting filter predicate
	 */
	public Predicate<Level> addAfter(Predicate<Level> filter) {
		if(type == FilterType.AND) {
			return filter.and(getFilter());
		}
		else {
			return filter.or(getFilter());
		}
	}
	
	/**
	 * The type of a filter
	 */
	public enum FilterType {
		/**
		 * A filter that is combined with a logical AND
		 */
		AND,
		/**
		 * A filter that is combined with a logical OR
		 */
		OR;
	}
}
