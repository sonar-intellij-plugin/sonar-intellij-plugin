package org.mayevskiy.intellij.sonar.javaassist;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import org.mayevskiy.intellij.sonar.SonarLocalInspectionTool;

import java.lang.reflect.Method;

/**
 * Author: Oleg Mayevskiy
 * Date: 31.03.13
 * Time: 13:52
 */
public class Test {
    public static void main(String[] args) throws IllegalAccessException, InstantiationException {

//        Class clazz = getaClass("111");
//        HelloWorld obj = (HelloWorld) clazz.newInstance();
        SonarLocalInspectionTool doit1 = doit("111");
        SonarLocalInspectionTool doit2 = doit("2222");
        System.out.println(doit1.getDisplayName());
        System.out.println(doit2.getDisplayName());

        System.out.println(doit1.getDisplayName());
        System.out.println(doit2.getDisplayName());

        System.out.println(doit1.getDisplayName());
        System.out.println(doit2.getDisplayName());

        System.out.println(doit("111").getClass());
        System.out.println(doit("1112").getClass());


//        System.out.println(clazz.getName());
    }

//    private static Class getaClass(String p) {
//        Class clazz = null;
//        try {
//            ClassPool cp = ClassPool.getDefault();
//            CtClass ctClass = cp.makeClass("Class" + p);
//            ctClass.setSuperclass(cp.get("org.mayevskiy.intellij.sonar.javaassist.HelloWorld"));
//
//            CtMethod method1 = ctClass.addMethod(CtMethod.make());
////            eagetMethod() DeclaredMethod("sayHello");
//            method1.insertBefore("{ System.out.println(\"Code injected " + p + " before method\"); }");
//            method1.insertAfter("{ System.out.println(\"Code injected " + p + " after method\"); }");
////            method1.im
//            clazz = ctClass.toClass();
//
//        } catch (NotFoundException e) {
//            throw new RuntimeException(e);
//        } catch (CannotCompileException e) {
//            throw new RuntimeException(e);
//        }
//        return clazz;
//    }

    private static SonarLocalInspectionTool doit(final String p) throws IllegalAccessException, InstantiationException {
        ProxyFactory f = new ProxyFactory();
        f.setSuperclass(SonarLocalInspectionTool.class);
        f.setFilter(new MethodFilter() {
            @Override
            public boolean isHandled(Method method) {
                return method.getName().equals("getDisplayName");
            }
        });
        Class c = f.createClass();

        MethodHandler mi = new MethodHandler() {
            String myDisplayName = "Das ist eine sehr schlechte Sache!";

            @Override
            public Object invoke(Object o, Method method, Method method2, Object[] objects) throws Throwable {
                return myDisplayName;
            }
        };
        SonarLocalInspectionTool foo = (SonarLocalInspectionTool) c.newInstance();
        ((ProxyObject) foo).setHandler(mi);
        return foo;
    }
}
