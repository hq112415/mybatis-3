package my.test2;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Properties;

@Intercepts(value = {
        @Signature(type = MyPlugin.MyPluginSignature1.class, method = "sayHello", args = String.class),
        //ParameterHandler
        @Signature(type = ParameterHandler.class, method = "getParameterObject", args = {}),
        @Signature(type = ParameterHandler.class, method = "setParameters", args = {PreparedStatement.class}),
        //StatementHandler
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class MyPlugin implements Interceptor {

    private Properties properties = null;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        System.out.println("--before--");
        System.out.println(invocation.getTarget().getClass().getName());
        System.out.println(invocation.getMethod().getName());
        System.out.println(invocation.getArgs().getClass().getName());

        Object res = invocation.proceed();

        System.out.println("--after. res = " + res);

        return res;
    }

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * signature 1
     * 这个signature因为没有实现的ParameterHandler、ResultSetHandler、StatementHandler、Executor等任何一个接口，其实没啥用
     */
    class MyPluginSignature1 {
        public String sayHello(String something) {
            System.out.println("---- hello1 ----");
            return something;
        }
    }
}


