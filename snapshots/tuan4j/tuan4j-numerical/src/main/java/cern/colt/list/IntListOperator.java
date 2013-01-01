/**
 * ==================================
 * Copyright (c) 2010 Max-Planck Institute for Informatics
 * Database and Information Systems Department
 * http://www.mpi-inf.mpg.de/departments/d5/index.html
 * 
 * Copyright (c) 2010 Anh Tuan Tran
 *
 * URL: http://www.mpi-inf.mpg.de/~attran
 *
 * Email: attran (at) mpi (dash) inf (dot) mpg (dot) de
 * ==================================
 * 
 * This source code is provided with AS IF - it does not guarantee the
 * or compatibilities with older or newer version of third-parties. In any
 * cases, if you have problems regarding using libraries delivered with
 * the project, feel free to write to the above email. Also, we would like
 * to get feedbacks from all of you
 *
 */
package cern.colt.list;


import cern.colt.list.IntArrayList;

/**
 * <p>This class extends a href="http://acs.lbl.gov/software/colt/">Java Colt</a>
 * library. It provides a naive implementation for common set operations (intersection, 
 * unions,...dice similarity).</p>
 * 
 * @version 0.1
 * @author tuan
 *
 */
public class IntListOperator {

	/**
	 * Return the number of common values between two sets
	 * @param list1 the list of distinctive integers
	 * @param list2the list of distinctive integers
	 * @return the number of common integers
	 */
	public static int intersect(IntArrayList list1, IntArrayList list2) {
		if (list1 == null || list2 == null)
			return 0;
		list1.sort();
		list2.sort();
		int intersect = 0;
		int[] arr1 = list1.elements;
		for (int i : arr1) {
			if (list2.binarySearch(i) >= 0)
				++intersect;
		}
		return intersect;
	}

	/**
	 * Return the list of common values between two sets
	 * @param list1 the list of distinctive integers
	 * @param list2the list of distinctive integers
	 * @return the number of common integers
	 */
	public static IntArrayList intersectList(IntArrayList list1, IntArrayList list2) {
		IntArrayList resLst = null;
		if (list1 != null && list2 != null) {
			resLst = new IntArrayList();
			list1.sort();
			list2.sort();
			int[] arr1 = list1.elements;
			for (int i : arr1) {
				if (list2.binarySearch(i) >= 0)
					resLst.add(i);
			}	
		}					
		return resLst;
	}

	/**
	 * Return the number of common values between an arbitrary number of  sets
	 * @return the number of common integers, -1 if the argument is invalid
	 */
	public static int intersect(IntArrayList... list) {
		int intersect = 0;
		int n = list.length;
		if (n <= 1) return -1;
		for (IntArrayList listElem : list)
			listElem.sort();
		int[] arr1 = list[0].elements;
		boolean found;
		for (int i : arr1) {
			found = true;
			for (int j = 1; j < n; j++) 
				if (list[j].binarySearch(i) < 0) {
					found = false;
					break;
				}
			if (found) ++intersect;
		}
		return intersect;
	}

	/**
	 * Return the dice's similarity between an arbitrary number of  sets 
	 * @param the double value of dice's similarity
	 */
	public static double dice(IntArrayList... list) {
		int n = list.length;
		int sum = 0;
		for (IntArrayList listElem : list)
			sum += listElem.size();
		return (double)(n * intersect(list)) / (double)sum; 
	}

	/**
	 * Return the extended dice's similarity between an arbitrary number of sets
	 * with specified intersection size
	 * @param intersect the size of the sets' intersection
	 * @param list list of sets
	 * @return
	 */
	public static double dice(int intersect, IntArrayList... list) {
		int n = list.length;
		int sum = 0;
		for (IntArrayList listElem : list)
			sum += listElem.size();
		return (double)(n * intersect) / (double)sum; 
	}
}