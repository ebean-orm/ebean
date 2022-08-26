package io.ebeaninternal.server.bind.capture;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface BindCaptureEntry {

  void bind(PreparedStatement statement, Connection connection) throws SQLException;
}
