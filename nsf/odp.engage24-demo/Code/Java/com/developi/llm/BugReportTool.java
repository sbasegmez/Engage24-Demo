package com.developi.llm;

import org.openntf.domino.Database;
import org.openntf.domino.Document;
import org.openntf.domino.utils.Factory;
import org.openntf.domino.utils.Factory.SessionType;

import dev.langchain4j.agent.tool.Tool;

/**
 * Tool code for the Engage 2024 Demo.
 * 
 * It has to define what the method is doing in a prompt format. 
 * This will be used during AiService creation. 
 * 
 * @author sbasegmez
 *
 */
public class BugReportTool {

	@Tool("""
			Create a bug report and return the problem id for future reference.
			We need three things to complete this step.
			- Product includes the product name and the version, separated by space.
			- Severity is either one of "Low", "Medium", "Critical" or "Unknown"
			- Problem Definition is contains detailed information about the problem, including steps to reproduce and platform. 
			""")
	public String createBugReport(String product, String severity, String problemDefinition) {
		System.out.println("Creating a bug report: " + problemDefinition);
		Database appDb = Factory.getSession(SessionType.CURRENT).getCurrentDatabase();
		
		Document doc = appDb.createDocument();
		doc.replaceItemValue("Form", "BugReport");
		doc.replaceItemValue("ProductNameVersion", product);
		doc.replaceItemValue("Severity", severity);
		doc.replaceItemValue("Problem", problemDefinition);
		
		doc.computeWithForm(false, false);
		
		if(doc.save()) {
			System.out.println("Saved!");
			return doc.getItemValueString("ProblemId");
		}
		
		System.out.println("NOT Saved!");

		throw new RuntimeException("We can't create the bug report now!");
	}
	
}
