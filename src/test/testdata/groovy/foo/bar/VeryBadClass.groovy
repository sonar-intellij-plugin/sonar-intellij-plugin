package foo.bar

/**
 * Created with IntelliJ IDEA.
 * User: Oleg
 * Date: 10.03.13
 * Time: 20:18
 * To change this template use File | Settings | File Templates.
 */
class VeryBadClass {

    /* public void veryBadMethod() {
         this.println();
         this.println();
         this.println();
         this.println();
     }*/

    def b1 = new BigDecimal(0.1)               // BigDecimalInstantiation violation
    def b2 = new java.math.BigDecimal(23.45d)  // BigDecimalInstantiation violation

    int myMethod(int count) {
        try {
            doSomething()
        } finally {
            assert count > 0        // AssertWithinFinallyBlock violation
        }
    }

    public void veryBadMethod() {
        this.println();
        this.println();
        this.println();
        this.println();
    }
}
