package org.apache.ibatis.plugin;

import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Clinton Begin
 */
public class InterceptorChain {

    private final List<Interceptor> interceptors = new ArrayList<>();

    /**
     * 从Configuration的解析中来看，这里的target只有 下面四个
     * 1. ParameterHandler {@link Configuration#newParameterHandler(org.apache.ibatis.mapping.MappedStatement, java.lang.Object, org.apache.ibatis.mapping.BoundSql)}
     * 2. ResultSetHandler {@link Configuration#newResultSetHandler(org.apache.ibatis.executor.Executor, org.apache.ibatis.mapping.MappedStatement, org.apache.ibatis.session.RowBounds, org.apache.ibatis.executor.parameter.ParameterHandler, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.mapping.BoundSql)}
     * 3. StatementHandler {@link Configuration#newStatementHandler(org.apache.ibatis.executor.Executor, org.apache.ibatis.mapping.MappedStatement, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.mapping.BoundSql)}
     * 4. Executor {@link Configuration#newExecutor(org.apache.ibatis.transaction.Transaction, org.apache.ibatis.session.ExecutorType)}
     */
    public Object pluginAll(Object target) {
        for (Interceptor interceptor : interceptors) {
            target = interceptor.plugin(target);
        }
        return target;
    }

    public void addInterceptor(Interceptor interceptor) {
        interceptors.add(interceptor);
    }

    public List<Interceptor> getInterceptors() {
        return Collections.unmodifiableList(interceptors);
    }

}
