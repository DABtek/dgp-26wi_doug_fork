public class Kennel {

    public static void main(String[] args) { // definition
        int size = 27; //Assignment
        String name = "Fido"; //Assignment
        Dog myDog = new Dog(name, size); //Assignment and function call
        int x = size - 5; //Assignment
        if (x < 15) {  //Flow control
            myDog.bark(8); //Function call
        }
        System.out.println(name); //Function call
    }
}
