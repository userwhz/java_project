
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!"); // 在控制台输出 Hello, World!
    }
}

class Animal {
    public void  sayHello()//父类的方法
    {
        System.out.println("hello,everybody");
    }
}
class Dog extends Animal//继承animal
{ }

class test {
    public static void main(String[] args) {
        System.out.println("Main 1: 标准main方法");


    }

    // 另一个main方法，参数不同
    public static void main(String arg) {
        System.out.println("Main 2: 参数是String: " + arg);
    }

    // 第三个main方法，参数类型不同
    public static void main(int number) {
        System.out.println("Main 3: 参数是int: " + number);
    }
}