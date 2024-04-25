package com.developi.utils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @title NotesName - Basic implementation for Java
 * 
 *        This initial version is a very basic implementation for Notes Names in Java, of course without Session-binded Name
 *        class.
 * 
 *        It parses very basic names. Supports common name, OUs, Organization and Country. Use with caution regarding the
 *        following issues:
 * 
 *        - It does not check some of the naming standards like allowing more than 4 OUs. - Some illegal names might return
 *        unexpected results, like "SomeName//us" or "CN=Some Name/O=Company=some" - Null or empty name will result an empty
 *        name with no components.
 * 
 * 
 * @author sbasegmez
 * @version 
 * 			version 1.2 - 2022/08/31
 * 			- Added Java 8 streams to static functions.
 * 			- Removed in-place update functions.
 * 
 * 			version 1.1 - 2014/08/17
 *          - Added static methods toCommon, toAbbreviated, toCanonical - Added static methods setNamesCommon,
 *          	setNamesAbbreviated, setNamesCanonical - Added getCommon() alias for getCommonName()
 * 
 *          released version 1.0 - 2014/06/08
 * 
 */
public class NotesName implements Serializable {

	private static final long serialVersionUID = -8812239942651141597L;

	// Any name will be written in a specific order.
	private final static String[] PARTS_ORDER = { "CN", "OU1", "OU2", "OU3", "OU4", "O", "C" };

	private Map<String, String> partMap = new HashMap<String, String>();

	/**
	 * Constructs a new name and build the partMap.
	 * 
	 * Null or empty name will generate an empty NotesName
	 * 
	 * @param name
	 *            : Hierarchical or Abbreviated name
	 */
	public NotesName(String name) {
		if (!isEmpty(name))
			extractName(name);
	}

	protected void extractName(String name) {

		LinkedList<String> parts = new LinkedList<String>(Arrays.asList(name.split("/")));

		// if canonical, labels are already provided.
		if (isCanonical(name)) {
			short ouCount = 1;

			for (String part : parts) {
				int signPos = part.indexOf("=");

				String label = part.substring(0, signPos);
				String val = part.substring(signPos + 1);

				if (label.equalsIgnoreCase("OU"))
					label += (ouCount++);

				partMap.put(label.toUpperCase(Locale.ENGLISH), val);
			}
		} else {
			// It's an abbreviated name. We have to play a game.

			// If the last part is two-letter, it must be a country...
			if (parts	.getLast()
						.length() == 2) {
				partMap.put("C", parts.getLast());
				parts.removeLast();
			}

			// First part should be a common name
			if (parts.size() >= 1) {
				partMap.put("CN", parts.getFirst());
				parts.removeFirst();
			}

			// Last part of the remaining should be an organization
			if (parts.size() >= 1) {
				partMap.put("O", parts.getLast());
				parts.removeLast();
			}

			// The remaining will be OU
			short ouCount = 1;

			for (String part : parts) {
				partMap.put("OU" + (ouCount++), part);
			}

		}

	}

	private String getPart(String part) {
		String result = partMap.get(part.toUpperCase(Locale.ENGLISH));

		// Empty string will be returned...
		if (null == result)
			result = "";

		return result;
	}

	/**
	 * @param canonical
	 *            If true, it puts item labels in front of values.
	 * @return Ordered string in abbreviated or canonical format.
	 */
	protected String getOrdered(boolean canonical) {
		StringBuffer sb = new StringBuffer();

		for (String label : PARTS_ORDER) {
			if (!isEmpty(partMap.get(label))) {
				if (sb.length() > 0)
					sb.append("/");
				if (canonical) {
					sb.append((label.startsWith("OU") ? "OU" : label) + "=");
				}
				sb.append(partMap.get(label));
			}
		}

		return sb.toString();
	}

	protected static boolean isCanonical(String name) {
		// The rule is simple. Every items in the '/' seperated part should have '=' sign.

		String[] parts = name.split("/");

		for (String part : parts) {
			if (!part.contains("=")) {
				return false;
			}
		}

		return true;

	}

	public String getAbbreviated() {
		return getOrdered(false);
	}

	public String getCanonical() {
		return getOrdered(true);
	}

	// Compatibility with Notes Name class
	public String getCommon() {
		return getCommonName();
	}

	public String getCommonName() {
		return getPart("CN");
	}

	public String getCountry() {
		return getPart("C");
	}

	public String getOrgUnit1() {
		return getPart("OU1");
	}

	public String getOrgUnit2() {
		return getPart("OU2");
	}

	public String getOrgUnit3() {
		return getPart("OU3");
	}

	public String getOrgUnit4() {
		return getPart("OU4");
	}

	public String getOrganization() {
		return getPart("O");
	}

	@Override
	public String toString() {
		return getAbbreviated() + " - " + partMap;
	}

	public static boolean isName(String valueStr) {
		return valueStr.matches("CN=.*\\/O=.*");
	}

	private static boolean isEmpty(String value) {
		return (null == value) || ("".equals(value));
	}

	// basic utils
	public static String toCommon(String name) {
		NotesName nn = new NotesName(name);
		return nn.getCommonName();
	}

	public static String toAbbreviated(String name) {
		NotesName nn = new NotesName(name);
		return nn.getAbbreviated();
	}

	public static String toCanonical(String name) {
		NotesName nn = new NotesName(name);
		return nn.getCanonical();
	}

	public static List<String> toCommon(List<String> names) {
		return names.stream()
					.map(NotesName::toCommon)
					.collect(Collectors.toList());
	}

	public static List<String> toAbbreviated(List<String> names) {
		return names.stream()
				.map(NotesName::toAbbreviated)
				.collect(Collectors.toList());
	}

	public static List<String> toCanonical(List<String> names) {
		return names.stream()
				.map(NotesName::toCanonical)
				.collect(Collectors.toList());
	}

	// test
	protected static void nameTest(String name) {
		try {
			System.out.println("Given Name : '" + name + "' (" + (isCanonical(name) ? "Canonical" : "Abbreviated") + ")");

			NotesName nn = new NotesName(name);
			boolean testResult = name.equals(isCanonical(name) ? nn.getCanonical() : nn.getAbbreviated());

			System.out.print(testResult ? "Passed the test:" : "FAILED!!! ");
			System.out.println("'" + nn.getAbbreviated() + "' - '" + nn.getCanonical() + "'");
			System.out.println(nn.partMap);

			System.out.println(nn.getOrgUnit4());

			System.out.println("************");

		} catch (Throwable t) {
			System.out.println("Exception with the name '" + name + "': ");
			t.printStackTrace();
		}
	}

	public static void main(String[] args) {

		nameTest("");
		nameTest("John Managerial");
		nameTest("John Managerial/developi");
		nameTest("John Managerial/developi/tr");
		nameTest("John Managerial/marketing/developi");
		nameTest("John Managerial/marketing/north/developi");
		nameTest("John Managerial/marketing/developi/tr");
		nameTest("John Managerial/marketing/north/developi/tr");
		nameTest("CN=John Managerial");
		nameTest("CN=John Managerial/O=Developi");
		nameTest("CN=John Managerial/OU=North/O=Developi");
		nameTest("CN=John Managerial/OU=Marketing/OU=North/O=Developi");
		nameTest("CN=John Managerial/OU=Marketing/OU=North/O=Developi/C=tr");

	}

}
