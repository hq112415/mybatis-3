package org.apache.ibatis.executor.parameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A parameter handler sets the parameters of the {@code PreparedStatement}.
 *
 * @author Clinton Begin
 */
public interface ParameterHandler {

    Object getParameterObject();

    void setParameters(PreparedStatement ps) throws SQLException;
}
