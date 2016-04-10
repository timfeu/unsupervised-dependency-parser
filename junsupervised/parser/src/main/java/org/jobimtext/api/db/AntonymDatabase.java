package org.jobimtext.api.db;

/*
 * #%L
 * JUnsupervisedParser
 * %%
 * Copyright (C) 2016 Tim Feuerbach
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class AntonymDatabase implements Destroyable {
    private DatabaseConnection connection;

    public AntonymDatabase() {
        connection = new DatabaseConnection();
    }

    public void connect(String url, String user, String password, String driver) throws SQLException, ClassNotFoundException {
        connection.openConnection(url, user, password, driver);
    }

    @Override
    public void destroy() {
        if (connection != null) connection.closeConnection();
    }

    public int getCount(String jo1, String jo2) throws SQLException {
        int count = 0;

        String sql = "SELECT `COUNT` FROM COHYPONYMS WHERE WORD1 = ? AND WORD2 = ?";
        PreparedStatement ps = connection.getConnection().prepareStatement(sql);
        ps.setString(1, jo1);
        ps.setString(2, jo2);

        ResultSet set = ps.executeQuery();

        if (set.next()) {
            count = set.getInt(1);
        }

        ps.close();

        return count;
    }

    public int getCount(String jo) throws SQLException {
        int count = 0;

        String sql = "SELECT SUM(`COUNT`) FROM COHYPONYMS WHERE WORD1 = ?";
        PreparedStatement ps = connection.getConnection().prepareStatement(sql);
        ps.setString(1, jo);

        ResultSet set = ps.executeQuery();

        if (set.next()) {
            count = set.getInt(1);
        }

        ps.close();

        return count;
    }
}
