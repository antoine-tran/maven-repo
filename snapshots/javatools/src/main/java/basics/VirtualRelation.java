package basics;

import javatools.parsers.DateParser;
import javatools.parsers.Name;
import javatools.parsers.NumberParser;

/**
 * Class VirtualRelation
 * 
 * Holds all virtual relations 
 * @author Fabian M. Suchanek
 */
public class VirtualRelation {
	/** TRUE if the relation holds*/
	public boolean holds(String x, String y) {
		return (false);
	}

	public static VirtualRelation AfterRelation = new VirtualRelation() {
		@Override
		public boolean holds(String x, String y) {
			try {
				return (DateParser.isEarlier(DateParser.asInts(y.split("-")),
						DateParser.asInts(x.split("-"))));
			} catch (Exception e) {
				return (false);
			}
		}
	};

	public static VirtualRelation BeforeRelation = new VirtualRelation() {
		@Override
		public boolean holds(String x, String y) {
			try {
				return (DateParser.isEarlier(DateParser.asInts(x.split("-")),
						DateParser.asInts(y.split("-"))));
			} catch (Exception e) {
				return (false);
			}
		}
	};

	public static VirtualRelation DifferentRelation = new VirtualRelation() {
		@Override
		public boolean holds(String x, String y) {
			return !x.equals(y);
		}
	};

	public static VirtualRelation DisjointRelation = new VirtualRelation() {
		@Override
		public boolean holds(String x, String y) {
			if (DateParser.isDate(x) && DateParser.isDate(y)) {
				return (DateParser.disjoint(x, y));
			}
			if (NumberParser.isFloat(x) && NumberParser.isFloat(y)) {
				return (NumberParser.different(x, y));
			}
			if (x.contains(",") && Name.isUSState(y)) {
				return (!x.replace('_', ' ').split(",[ _]++")[1].trim()
						.equalsIgnoreCase(y.replace('_', ' ')));
			}
			if (y.contains(",") && Name.isUSState(x)) {
				return (!y.replace('_', ' ').split(",[ _]++")[1].trim()
						.equalsIgnoreCase(x.replace('_', ' ')));
			}
			return (!x.equals(y));
		}
	};

	public static VirtualRelation GreaterThanRelation = new VirtualRelation() {
		@Override
		public boolean holds(String x, String y) {
			try {
				return (NumberParser.getDouble(x) > NumberParser.getDouble(y));
			} catch (NumberFormatException e) {
				return (false);
			}
		}
	};

	public static VirtualRelation LifeTimeRelation = new VirtualRelation() {
		@Override
		public boolean holds(String x, String y) {
			try {
				int yearborn = Integer.parseInt(DateParser.getDate(x)[0]
						.replace('#', '9'));
				int yeardied = Integer.parseInt(DateParser.getDate(y)[0]
						.replace('#', '0'));
				return (yeardied > yearborn && yeardied - yearborn < 100);
			} catch (Exception e) {
			}
			return (false);
		}
	};

	public static VirtualRelation SameYearRelation = new VirtualRelation() {
		@Override
		public boolean holds(String x, String y) {
			try {
				return (DateParser.getDate(x))[0]
						.equals(DateParser.getDate(y)[0]);
			} catch (Exception e) {
				return (false);
			}
		}
	};

	public static VirtualRelation SmallerThanRelation = new VirtualRelation() {
		@Override
		public boolean holds(String x, String y) {
			try {
				return (NumberParser.getDouble(x) < NumberParser.getDouble(y));
			} catch (Exception e) {
				return (false);
			}
		}
	};

}
