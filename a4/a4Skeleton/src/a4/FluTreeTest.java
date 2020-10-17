package a4;

import static org.junit.Assert.assertEquals;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

/** @author david gries */
public class FluTreeTest {

	private static Network n;
	private static Person[] people;
	private static Person personA;
	private static Person personB;
	private static Person personC;
	private static Person personD;
	private static Person personE;
	private static Person personF;
	private static Person personG;
	private static Person personH;
	private static Person personI;
	private static Person personJ;
	private static Person personK;
	private static Person personL;

	/** */
	@BeforeClass
	public static void setup() {
		n= new Network();
		people= new Person[] { new Person("A", 0, n),
				new Person("B", 0, n), new Person("C", 0, n),
				new Person("D", 0, n), new Person("E", 0, n), new Person("F", 0, n),
				new Person("G", 0, n), new Person("H", 0, n), new Person("I", 0, n),
				new Person("J", 0, n), new Person("K", 0, n), new Person("L", 0, n)
		};
		personA= people[0];
		personB= people[1];
		personC= people[2];
		personD= people[3];
		personE= people[4];
		personF= people[5];
		personG= people[6];
		personH= people[7];
		personI= people[8];
		personJ= people[9];
		personK= people[10];
		people[10]= personK;
		personL= people[11];
	}

	/** * */
	@Test
	public void testBuiltInGetters() {
		FluTree st= new FluTree(personB);
		assertEquals("B", toStringBrief(st));
	}

	// A.sh(B, C) = A
	// A.sh(D, F) = B
	// A.sh(D, I) = B
	// A.sh(H, I) = H
	// A.sh(D, C) = A
	// B.sh(B, C) = null
	// B.sh(I, E) = B

	/** Create a FluTree with structure A[B[D E F[G[H[I]]]] C] <br>
	 * This is the tree
	 *
	 * <pre>
	 *            A
	 *          /   \
	 *         B     C
	 *       / | \
	 *      D  E  F
	 *            |
	 *            G
	 *            |
	 *            H
	 *            |
	 *            I
	 * </pre>
	 */
	private FluTree makeTree1() {
		FluTree dt= new FluTree(personA); // A
		dt.insert(personB, personA); // A, B
		dt.insert(personC, personA); // A, C
		dt.insert(personD, personB); // B, D
		dt.insert(personE, personB); // B, E
		dt.insert(personF, personB); // B, F
		dt.insert(personG, personF); // F, G
		dt.insert(personH, personG); // G, H
		dt.insert(personI, personH); // H, I
		return new FluTree(dt);
	}

	/** test a call on makeTree1(). */
	@Test
	public void testMakeTree1() {
		FluTree dt= makeTree1();
		assertEquals("A[B[D E F[G[H[I]]]] C]", toStringBrief(dt));
	}

	/** */
	@Test
	public void test1Insert() {
		FluTree st= new FluTree(personB);

		// Test insert in the root
		FluTree dt2= st.insert(personC, personB);
		assertEquals("B[C]", toStringBrief(st)); // test tree
		assertEquals(personC, dt2.rootPerson());  // test return value
	}

	/** */
	@Test
	public void test2size() {
		FluTree st= new FluTree(personC);
		assertEquals(1, st.size());
	}

	/** */
	@Test
	public void test3contains() {
		FluTree st= new FluTree(personC);
		assertEquals(true, st.contains(personC));
	}

	/** */
	@Test
	public void test4depth() {
		FluTree st= new FluTree(personB);
		st.insert(personC, personB);
		st.insert(personD, personC);
		assertEquals(0, st.depth(personB));

	}

	/** */
	@Test
	public void test5WidthAtDepth() {
		FluTree st= new FluTree(personB);
		assertEquals(1, st.widthAtDepth(0));

	}

	@SuppressWarnings("javadoc")
	@Test
	public void test6FluRouteTo() {
		FluTree st= new FluTree(personB);
		List<Person> route= st.fluRouteTo(personB);
		assertEquals("[B]", getNames(route));
	}

	/** Return the names of Persons in sp, separated by ", " and delimited by [ ]. Precondition: No
	 * name is the empty string. */
	private String getNames(List<Person> sp) {
		String res= "[";
		for (Person p : sp) {
			if (res.length() > 1) res= res + ", ";
			res= res + p.name();
		}
		return res + "]";
	}

	/** */
	@Test
	public void test7commonAncestor() {
		FluTree st= new FluTree(personB);
		st.insert(personC, personB);
		Person p= st.commonAncestor(personC, personC);
		assertEquals(personC, p);

	}

	/** */
	@Test
	public void test8equals() {
		FluTree treeB1= new FluTree(personB);
		FluTree treeB2= new FluTree(personB);
		assertEquals(true, treeB1.equals(treeB2));
	}

	// ===================================
	// ==================================

	/** Return a representation of this tree. This representation is: <br>
	 * (1) the name of the Person at the root, followed by <br>
	 * (2) the representations of the children <br>
	 * . (in alphabetical order of the children's names). <br>
	 * . There are two cases concerning the children.
	 *
	 * . No children? Their representation is the empty string. <br>
	 * . Children? Their representation is the representation of each child, <br>
	 * . with a blank between adjacent ones and delimited by "[" and "]". <br>
	 * <br>
	 * Examples: One-node tree: "A" <br>
	 * root A with children B, C, D: "A[B C D]" <br>
	 * root A with children B, C, D and B has a child F: "A[B[F] C D]" */
	public static String toStringBrief(FluTree t) {
		String res= t.rootPerson().name();

		Object[] childs= t.copyOfChildren().toArray();
		if (childs.length == 0) return res;
		res= res + "[";
		selectionSort1(childs);

		for (int k= 0; k < childs.length; k= k + 1) {
			if (k > 0) res= res + " ";
			res= res + toStringBrief((FluTree) childs[k]);
		}
		return res + "]";
	}

	/** Sort b --put its elements in ascending order. <br>
	 * Sort on the name of the Person at the root of each FluTree.<br>
	 * Throw a cast-class exception if b's elements are not FluTree */
	public static void selectionSort1(Object[] b) {
		int j= 0;
		// {inv P: b[0..j-1] is sorted and b[0..j-1] <= b[j..]}
		// 0---------------j--------------- b.length
		// inv : b | sorted, <= | >= |
		// --------------------------------
		while (j != b.length) {
			// Put into p the index of smallest element in b[j..]
			int p= j;
			for (int i= j + 1; i != b.length; i++ ) {
				String bi= ((FluTree) b[i]).rootPerson().name();
				String bp= ((FluTree) b[p]).rootPerson().name();
				if (bi.compareTo(bp) < 0) {
					p= i;

				}
			}
			// Swap b[j] and b[p]
			Object t= b[j];
			b[j]= b[p];
			b[p]= t;
			j= j + 1;
		}
	}

}
