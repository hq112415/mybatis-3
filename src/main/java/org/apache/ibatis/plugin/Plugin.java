package org.apache.ibatis.plugin;

import org.apache.ibatis.reflection.ExceptionUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Clinton Begin
 */
public class Plugin implements InvocationHandler {

    private final Object target;
    private final Interceptor interceptor;
    private final Map<Class<?>, Set<Method>> signatureMap;

    private Plugin(Object target, Interceptor interceptor, Map<Class<?>, Set<Method>> signatureMap) {
        this.target = target;
        this.interceptor = interceptor;
        this.signatureMap = signatureMap;
    }

    /**
     * @param target      这里target是ParameterHandler、ResultSetHandler、StatementHandler、Executor
     * @param interceptor 这个是我们自定义的插件
     */
    public static Object wrap(Object target, Interceptor interceptor) {
        Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);
        Class<?> type = target.getClass();
        /**
         * 返回符合要求的接口，这个要求是什么呢？
         * 1. 是target以及其父类实现的接口；
         * 2. 这个接口必须是在Interceptor的signature注解中定义过的
         */
        Class<?>[] interfaces = getAllInterfaces(type, signatureMap);
        /**
         * 如果有符合要求的要实现的接口，那么返回一个代理类，否则就不代理，返回target本身
         */
        if (interfaces.length > 0) {
            return Proxy.newProxyInstance(
                    type.getClassLoader(),
                    interfaces,
                    new Plugin(target, interceptor, signatureMap)
            );
        }
        return target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            Set<Method> methods = signatureMap.get(method.getDeclaringClass());
            /**
             * 如果target要执行的那个方法(method)是Signature中定义过的，那么做拦截
             * --
             * 其实这里 (1)return interceptor.intercept(new Invocation(target, method, args));
             * 也是做了一个层层代理，你会发现它最终都是调用的也是 (2)method.invoke(target, args);
             * 但是(1)它多执行了一步intercept，这个地方做了个步骤就做了所谓的拦截，在拦截前后你可以做些自定义的动作
             * 其实这个interceptor也就在这个地方有些实际的用途了，其他地方你会发现interceptor只是来回的做传递或者获取它的一些属性而已
             * --
             * 其实这个地方可以改写成下面，原理是这样，但是这样就没有扩展性了，为了扩展性才给interceptor定义了intercept方法，
             * 还把(target, method, args)封住成一个Invocation对象
             *
             *  if (method != null && methods.contains(method)) {
             *      //do something before
             *        Object res = method.invoke(target, args);
             *      //do something after
             *        return res;
             *  } else {
             *        return method.invoke(target, args);
             *  }
             *
             */
            if (methods != null && methods.contains(method)) {
                return interceptor.intercept(new Invocation(target, method, args));
            }
            return method.invoke(target, args);
        } catch (Exception e) {
            throw ExceptionUtil.unwrapThrowable(e);
        }
    }

    /**
     * 返回interceptor这个插件定义的所有的所有Method
     * 一个interceptor可以定义多个Signature，一个Signature可以定义多个类方法
     */
    private static Map<Class<?>, Set<Method>> getSignatureMap(Interceptor interceptor) {
        /**
         * 1 必须注解 @Intercepts，且@Intercepts必须设置value属性
         */
        Intercepts interceptsAnnotation = interceptor.getClass().getAnnotation(Intercepts.class);
        // issue #251
        if (interceptsAnnotation == null) {
            throw new PluginException("No @Intercepts annotation was found in interceptor " + interceptor.getClass().getName());
        }
        /**
         * 2. 可以配置多个@Signature，每个@Signature必须设置value属性，value属性包括
         *     Class<?> type();
         *     String method();
         *     Class<?>[] args();
         *    根据下面的for循环可以看出，这三个属性的意思是，Class类型是type 参数是args 方法名是method的那个Method会被加入到signatureMap中
         *    key是type的那个Class,value是这个method组成的Set
         */
        Signature[] sigs = interceptsAnnotation.value();
        Map<Class<?>, Set<Method>> signatureMap = new HashMap<>();
        for (Signature sig : sigs) {
            Set<Method> methods = signatureMap.computeIfAbsent(sig.type(), k -> new HashSet<>());
            try {
                Method method = sig.type().getMethod(sig.method(), sig.args());
                methods.add(method);
            } catch (NoSuchMethodException e) {
                throw new PluginException("Could not find method on " + sig.type() + " named " + sig.method() + ". Cause: " + e, e);
            }
        }
        return signatureMap;
    }

    private static Class<?>[] getAllInterfaces(Class<?> type, Map<Class<?>, Set<Method>> signatureMap) {
        Set<Class<?>> interfaces = new HashSet<>();
        while (type != null) {
            for (Class<?> c : type.getInterfaces()) {
                if (signatureMap.containsKey(c)) {
                    interfaces.add(c);
                }
            }
            type = type.getSuperclass();
        }
        return interfaces.toArray(new Class<?>[interfaces.size()]);
    }

}
