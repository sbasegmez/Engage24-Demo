package com.developi.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author sbasegmez
 *
 */
public class ListUtils {

	
	/**
	 * Returns a set of unique values of the given list. 
	 * 
	 * @param list
	 * @return
	 */
	public static <T> List<T> uniqueList(List<T> list) {
		if(list == null) return null;
		
		return list.stream().distinct().collect(Collectors.toList());
	}

	/**
	 * Returns a sorted set of unique values of the given list 
	 * 
	 * @param list
	 * @return
	 */
	public static <T> List<T> uniqueSortedList(List<T> list) {
		if(list == null) return null;
		
		return list.stream().distinct().sorted().collect(Collectors.toList());
	}

	/**
	 * Check if the list contains the member. For objects, it uses standard .equals() method. 
	 * For strings, it compares but ignores the case.
	 * 
	 * @param list
	 * @param member
	 * @return
	 */	
	public static boolean contains(List<? extends Object> list, Object member) {
		return find(list, member) >=0;
	}

	/**
	 * Returns the index number of the member from the list. For objects, it uses standard .equals() method. 
	 * For strings, it compares but ignores the case.
	 * 
	 * @param list
	 * @param member
	 * @return -1 if the list is empty, member is null or the list does not contain the member
	 */	
	public static int find(List<? extends Object> list, Object member) {
		if(list.isEmpty() || member==null) return -1;
		
		for(int i=0; i<list.size(); i++) {
			Object obj = list.get(i);
			
			if(obj instanceof CharSequence && member instanceof CharSequence) {
				if(StringUtils.equalsIgnoreCase((CharSequence)obj, (CharSequence)member)) return i;
			} else {
				if(member.equals(obj)) return i;
			}
		}
		
		return -1;
	}
	
	/**
	 * Compares two lists. Caution: Lists can hold same values multiple times.
	 * 
	 * @param list1
	 * @param list2
	 * @return -1 any of lists are empty/null.
	 * 			0 no common elements
	 * 			x (x>0) if there are x elements in common. 
	 * 
	 */
	public static int compareLists(List<? extends Object> list1, List<? extends Object> list2) {
		if(null==list1 || null==list2) {
			return -1;
		}
		
		int count=0;
	
		for(Object o: list1) {
			if(list2.contains(o)) count++;
		}
		
		return count;
		
	}

	/**
	 * 
	 * Convert incoming values object to a String List <br>
	 * 
	 * <li>if given values is null, returns an empty ArrayList. 
	 * <li>Non-string elements will be stringified into a single member list. 
	 * 
	 * <br><br>
	 * 
	 * @param values with empty generics, coming from getItemValue().
	 * @return Vector object with strings
	 */
	public static List<String> toStringList(final Object values) {
		List<String> list=new ArrayList<String>();
		
		if(values==null) return list;
		
		if(values instanceof Collection<?>) {
			for(Object o: (Collection<?>)values) {
				if(o!=null) list.add(o.toString());
			}
		} else if(StringUtils.isNotEmpty(values.toString())) {
			//Assume it's somewhat to be stringified...
			list.add(values.toString());
		}
					
		return list;
	}

	/**
	 * Check if the list is empty or not.
	 * 
	 * Returns true if the list is null or empty. Also checks for single element lists and return true if it contains an empty string.
	 * 
	 * @param list
	 * @return
	 */
	public static boolean isEmpty(final Collection<?> list) {
		if(list == null || list.isEmpty()) {
			return true;
		}

		if(list instanceof List) {
			List<?> list2 = (List<?>) list;
			
			if(list2.size() == 1 && list2.get(0) instanceof CharSequence) {
				return StringUtils.isEmpty((CharSequence)list2.get(0));
			}
		}
		
		return false;
	}
	
	/**
	 * Check if the list is empty or not.
	 * 
	 * Returns false if the list is null or empty. Also checks for single element lists and return false if it contains an empty string.
	 * 
	 * @param list
	 * @return
	 */
	public static boolean isNotEmpty(final Collection<?> list) {
		return ! isEmpty(list);
	}
	
}
