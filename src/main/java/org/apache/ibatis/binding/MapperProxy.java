package org.apache.ibatis.binding;

import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.session.SqlSession;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {

    private static final long serialVersionUID = -6424540398559729838L;
    private static final int ALLOWED_MODES = MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
            | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC;
    private static Constructor<Lookup> lookupConstructor;
    private final SqlSession sqlSession;
    private final Class<T> mapperInterface;
    private final Map<Method, MapperMethod> methodCache;

    public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethod> methodCache) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
        this.methodCache = methodCache;
    }

    static {
        try {
            lookupConstructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
        } catch (NoSuchMethodException e) {
            try {
                // Since Java 14+8
                lookupConstructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Class.class, int.class);
            } catch (NoSuchMethodException e2) {
                throw new IllegalStateException("No known constructor found in java.lang.invoke.MethodHandles.Lookup.", e2);
            }
        }
        lookupConstructor.setAccessible(true);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            //来自Object的方法，例如toString、hashCode等
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, args);
            }
            //默认方法
            else if (method.isDefault()) {
                return invokeDefaultMethod(proxy, method, args);
            }
        } catch (Throwable t) {
            throw ExceptionUtil.unwrapThrowable(t);
        }
        //这里才是自定义方法
        final MapperMethod mapperMethod = cachedMapperMethod(method);
        return mapperMethod.execute(sqlSession, args);
    }

    private MapperMethod cachedMapperMethod(Method method) {
        return methodCache.computeIfAbsent(method,
                k -> new MapperMethod(mapperInterface, method, sqlSession.getConfiguration())
        );
    }

    private Object invokeDefaultMethod(Object proxy, Method method, Object[] args)
            throws Throwable {
        final Class<?> declaringClass = method.getDeclaringClass();
        final Lookup lookup;
        if (lookupConstructor.getParameterCount() == 2) {
            lookup = lookupConstructor.newInstance(declaringClass, ALLOWED_MODES);
        } else {
            // SInce JDK 14+8
            lookup = lookupConstructor.newInstance(declaringClass, null, ALLOWED_MODES);
        }
        return lookup.unreflectSpecial(method, declaringClass).bindTo(proxy).invokeWithArguments(args);
    }
}
