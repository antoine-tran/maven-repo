package basics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.DataFormatException;

/**
 * Class Fact Represents a fact
 * 
 * @author Fabian M. Suchanek
 */
public class Fact {
	/** Argument 1 */
	public String arg1;
	/** First argument if it is a fct (NULL else) */
	public Fact arg1fact;
	/** Argument 2 */
	public String arg2;
	/** Relation */
	public String relation;
	/** 1=check first arg, 2=check second arg, 0=no typecheck needed */
	public int toBeChecked = 0;
	/** Holds the technique used to extract this fact */
	public String technique = null;
	/** TRUE for positive facts */
	public boolean polarity = true;

	public Fact(String arg1, String relation, String arg2, String technique) {
		super();
		this.arg1 = arg1;
		this.arg2 = arg2;
		this.relation = relation;
		this.technique = technique;
	}

	public Fact(Fact arg1fact, String relation, String arg2) {
		super();
		this.arg1fact = arg1fact;
		this.arg1 = "#" + arg1fact.hashCode();
		this.arg2 = arg2;
		this.relation = relation;
	}

	public Fact(Fact copy) {
		this.arg1 = copy.arg1;
		this.arg2 = copy.arg2;
		this.arg1fact = copy.arg1fact;
		this.relation = copy.relation;
		this.technique = copy.technique;
		this.toBeChecked = copy.toBeChecked;
		this.polarity = copy.polarity;
	}

	/** Creates a fact from a template of the form "ARG1 RELATION ARG2" */
	public Fact(String template, String technique) {
		if (template.startsWith("~")) {
			template = template.substring(1);
			polarity = false;
		}
		String[] split = template.trim().split(" ");
		arg1 = split[0];
		relation = split[1];
		arg2 = split[2];
		this.technique = technique;
	}

	@Override
	public String toString() {
		return (polarity ? "" : "~") + relation + "(" + (arg1fact==null?arg1:arg1fact)+ ", " + arg2
				+ ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((arg1 == null) ? 0 : arg1.hashCode());
		result = prime * result
				+ ((arg1fact == null) ? 0 : arg1fact.hashCode());
		result = prime * result + ((arg2 == null) ? 0 : arg2.hashCode());
		result = prime * result + (polarity ? 1231 : 1237);
		result = prime * result
				+ ((relation == null) ? 0 : relation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Fact other = (Fact) obj;
		if (arg1 == null) {
			if (other.arg1 != null)
				return false;
		} else if (!arg1.equals(other.arg1))
			return false;
		if (arg1fact == null) {
			if (other.arg1fact != null)
				return false;
		} else if (!arg1fact.equals(other.arg1fact))
			return false;
		if (arg2 == null) {
			if (other.arg2 != null)
				return false;
		} else if (!arg2.equals(other.arg2))
			return false;
		if (polarity != other.polarity)
			return false;
		if (relation == null) {
			if (other.relation != null)
				return false;
		} else if (!relation.equals(other.relation))
			return false;
		return true;
	}

	/** Instantiates the $x in the fact */
	public Fact instantiateSingle(String[] variableValues)
			throws DataFormatException, NoSuchMethodException {
		if (!arg1.contains("$") && !arg2.contains("$")
				&& !relation.contains("$") && arg1fact == null)
			return (this);
		Fact result = new Fact(this);
		if (result.arg1fact != null) {
			result.arg1fact = result.arg1fact.instantiateSingle(variableValues);
		}
		for (int j = 0; j < variableValues.length; j++) {
			result.arg1 = result.arg1.replace("$" + j, variableValues[j]);
			result.arg2 = result.arg2.replace("$" + j, variableValues[j]);
			result.relation = result.relation.replace("$" + j,
					variableValues[j]);
		}
		boolean[] requiresTypecheck = new boolean[1];
		result.arg1 = TermExtractor.convertSingle(result.arg1,
				requiresTypecheck);
		if (requiresTypecheck[0]
				&& !Basics.domain(result.relation).equals(Basics.ENTITY))
			result.toBeChecked = 1;
		requiresTypecheck[0] = false;
		result.arg2 = TermExtractor.convertSingle(result.arg2,
				requiresTypecheck);
		if (requiresTypecheck[0]
				&& !Basics.range(result.relation).equals(Basics.ENTITY))
			result.toBeChecked = 2;
		return (result);
	}

	/**
	 * Instantiates the $x in the fact. If one $x can be expanded into multiple
	 * items, returns one fact for each item
	 */
	public List<Fact> instantiateList(String[] variableValues)
			throws DataFormatException, NoSuchMethodException {
		List<Fact> resultFacts = new ArrayList<Fact>();

		if (!arg1.contains("$") && !arg2.contains("$")
				&& !relation.contains("$") && arg1fact == null) {
			return (Arrays.asList(this));
		}
		Fact resultTemplate = new Fact(this);
		if (resultTemplate.arg1fact != null) {
			resultTemplate.arg1fact = resultTemplate.arg1fact.instantiateSingle(variableValues);
			resultTemplate.arg1 = "#" + resultTemplate.arg1fact.hashCode();
		}
		for (int j = 0; j < variableValues.length; j++) {
			resultTemplate.arg1 = resultTemplate.arg1.replace("$" + j,
					variableValues[j]);
			resultTemplate.arg2 = resultTemplate.arg2.replace("$" + j,
					variableValues[j]);
			resultTemplate.relation = resultTemplate.relation.replace("$" + j,
					variableValues[j]);
		}
		boolean[] requiresTypecheck1 = new boolean[1];
		for (String arg1 : TermExtractor.convertList(resultTemplate.arg1,
				requiresTypecheck1)) {
			boolean[] requiresTypecheck2 = new boolean[1];
			for (String arg2 : TermExtractor.convertList(resultTemplate.arg2,
					requiresTypecheck2)) {
				Fact resultFact = new Fact(resultTemplate);
				resultFact.arg1 = arg1;
				resultFact.arg2 = arg2;
				if (requiresTypecheck1[0]
						&& !Basics.domain(resultFact.relation).equals(
								Basics.ENTITY))
					resultFact.toBeChecked = 1;
				if (requiresTypecheck2[0]
						&& !Basics.range(resultFact.relation).equals(
								Basics.ENTITY))
					resultFact.toBeChecked = 2;
				resultFacts.add(resultFact);
			}
		}
		return resultFacts;
	}

	/** Sets argument 1 or 2 */
	public void setArg(int a, String s) {
		if (a == 1)
			arg1 = s;
		else
			arg2 = s;
	}

	/** Gets argument 1 or 2 */
	public String getArg(int a) {
		return (a == 1 ? arg1 : arg2);
	}

	/** Gets the domain of argument 1 or 2 */
	public String getDomainOfArg(int a) {
		return (a == 1 ? Basics.domain(relation) : Basics.range(relation));
	}
}
