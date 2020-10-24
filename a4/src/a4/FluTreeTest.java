package a4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.LinkedList;
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

    private FluTree singlePersonTree() {
        return new FluTree(personA);
    }

    private FluTree listTree() {
        FluTree dt= new FluTree(personA);
        dt.insert(personB, personA);
        dt.insert(personC, personB);
        dt.insert(personD, personC);
        dt.insert(personE, personD);
        dt.insert(personF, personE);

        return new FluTree(dt);

    }

    private FluTree randomTree() {
        List<Person> available= new LinkedList<>();
        List<Person> inTree= new LinkedList<>();
        for (int i= 0; i < people.length; i++ ) {
            available.add(people[i]);
        }
        int nextParent= (int) (Math.random() * 9);
        Person nextP= available.remove(nextParent);
        FluTree rt= new FluTree(nextP);
        inTree.add(nextP);
        double add= Math.random();
        while (add <= 0.8 && !available.isEmpty()) {
            nextParent= (int) (Math.random() * inTree.size());
            int nextChild= (int) (Math.random() * available.size());
            Person nextC= available.remove(nextChild);
            nextP= inTree.get(nextParent);
            rt.insert(nextC, nextP);
            inTree.add(nextC);
            add= Math.random();

        }
        return new FluTree(rt);
    }

    @Test
    public void testRandomTree() {
        FluTree dt= randomTree();
        System.out.println(toStringBrief(dt));
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
        assertThrows(IllegalArgumentException.class, () -> { st.insert(null, personB); });
        assertThrows(IllegalArgumentException.class, () -> { st.insert(personC, null); });
        assertThrows(IllegalArgumentException.class, () -> { st.insert(personB, personB); });
        assertThrows(IllegalArgumentException.class, () -> { st.insert(personD, personC); });

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
        FluTree t1= makeTree1();
        System.out.println(t1.size());
        assertEquals(9, t1.size());
        FluTree t2= listTree();
        assertEquals(6, t2.size());
    }

    /** */
    @Test
    public void test3contains() {
        FluTree st= new FluTree(personC);
        assertEquals(true, st.contains(personC));
        FluTree dt= makeTree1();
        assertEquals(true, dt.contains(personI));
        for (FluTree c : dt.copyOfChildren()) {
            assertEquals(personB.equals(c.rootPerson()), c.contains(personH));
        }

    }

    /** */
    @Test
    public void test4depth() {
        FluTree st= new FluTree(personB);
        st.insert(personC, personB);
        st.insert(personD, personC);
        assertEquals(0, st.depth(personB));
        assertEquals(2, st.depth(personD));

        FluTree t1= singlePersonTree();
        assertEquals(0, t1.depth(personA));
        assertEquals(-1, t1.depth(personB));

        FluTree t2= listTree();
        assertEquals(0, t2.depth(personA));
        assertEquals(3, t2.depth(personD));
        assertEquals(5, t2.depth(personF));
        assertEquals(-1, t2.depth(personI));

        FluTree t3= makeTree1();
        assertEquals(0, t3.depth(personA));
        assertEquals(1, t3.depth(personC));
        assertEquals(2, t3.depth(personE));
        assertEquals(4, t3.depth(personH));
        assertEquals(5, t3.depth(personI));
        assertEquals(-1, t3.depth(personK));

    }

    /** */
    @Test
    public void test5WidthAtDepth() {
        FluTree st= new FluTree(personB);
        assertEquals(1, st.widthAtDepth(0));
        FluTree dt= makeTree1();
        assertEquals(1, dt.widthAtDepth(0));
        assertEquals(2, dt.widthAtDepth(1));
        assertEquals(3, dt.widthAtDepth(2));
        assertEquals(0, dt.widthAtDepth(7));
    }

    @SuppressWarnings("javadoc")
    @Test
    public void test6FluRouteTo() {
        FluTree st= new FluTree(personB);
        List<Person> route1= st.fluRouteTo(personB);
        assertEquals("[B]", getNames(route1));
        FluTree dt= makeTree1();
        List<Person> route2= dt.fluRouteTo(personB);
        assertEquals("[A, B]", getNames(route2));
        List<Person> route3= dt.node(personC).fluRouteTo(personB);
        assertEquals(null, route3);
        List<Person> route4= dt.fluRouteTo(personI);
        assertEquals("[A, B, F, G, H, I]", getNames(route4));
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
        assertEquals(null,st.commonAncestor(null, null));
        assertEquals(null,st.commonAncestor(null, personC));
        assertEquals(null,st.commonAncestor(personC, null));
        assertEquals(null,st.commonAncestor(personD, personC));
        assertEquals(null,st.commonAncestor(personC, personD));
        assertEquals(null,st.commonAncestor(personE, personD));
        
        FluTree t1=makeTree1();
        assertEquals(personA,t1.commonAncestor(personA, personG));
        assertEquals(personA,t1.commonAncestor(personB, personC));
        assertEquals(personB,t1.commonAncestor(personD, personG));
        assertEquals(personA,t1.commonAncestor(personC, personH));
        assertEquals(personI,t1.commonAncestor(personI, personI));
        assertEquals(personF,t1.commonAncestor(personF, personH));
       
        FluTree t2=listTree();
        assertEquals(personA,t2.commonAncestor(personA, personE));
        assertEquals(personA,t2.commonAncestor(personE, personA));
        assertEquals(personD,t2.commonAncestor(personD, personE));
        
        FluTree t3=singlePersonTree();
        assertEquals(personA,t3.commonAncestor(personA, personA));
        assertEquals(null,t3.commonAncestor(personB, personA));
        assertEquals(null,t3.commonAncestor(personA, personB));

    }

    /** */
    @Test
    public void test8equals() {
        FluTree treeB1= new FluTree(personB);
        FluTree treeB2= new FluTree(personB);
        FluTree treeA1= new FluTree(personA);
        assertEquals(true, treeB1.equals(treeB2));
        assertEquals(false, treeB1.equals(treeA1));
        treeB1.insert(personC, personB);
        assertEquals(false, treeB1.equals(treeB2));
        treeB2.insert(personC, personB);
        assertEquals(true, treeB1.equals(treeB2));
        treeB1.insert(personJ, personB);
        treeB2.insert(personL, personC);
        assertEquals(false, treeB1.equals(treeB2));
        treeB2.insert(personJ, personB);
        treeB1.insert(personL, personC);
//        treeB1.insert(personE, personA);
        treeB2.insert(personE, personC);
        assertEquals(false, treeB1.equals(treeB2));
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
