package com.zs.reflection;

import java.lang.reflect.Method;

public class JavaConcurrencyApplication2 {

    public static void main(String[] args) {

        try {
            Method method = OneClass.class.getMethod("methodTest", null);
            method.invoke(OneClass.class.newInstance(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class OneClass {

        public OneClass() {
        }

        public String methodTest() {
            System.out.println("print: methodTest");
            return "methodTest";
        }
    }
}
