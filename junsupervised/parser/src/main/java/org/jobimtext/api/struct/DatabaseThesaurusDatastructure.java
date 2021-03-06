package org.jobimtext.api.struct;

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

/*******************************************************************************
 * Copyright 2012
 * Technische Universitaet Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.jobimtext.api.configuration.DatabaseThesaurusConfiguration;
import org.jobimtext.api.db.DatabaseConnection;
import org.jobimtext.api.db.DatabaseResource;
import org.jobimtext.api.db.DatabaseThesaurus;

/**
 * 
 * @author Martin Riedl (riedl@cs.tu-darmstadt.de)
 * 
 */
public class DatabaseThesaurusDatastructure extends DatabaseResource implements
		IThesaurusDatastructure<String, String> {

	DatabaseThesaurus dbThesaurus = new DatabaseThesaurus();

	public DatabaseThesaurusDatastructure(File dbConfigurationFile) {
		dbThesaurus.setDbConfigurationFile(dbConfigurationFile);
		// this.setDbConfigurationFile(dbConfigurationFile);
	}

	public DatabaseThesaurusDatastructure(DatabaseThesaurusConfiguration conf) {
		// super.setDbConf(conf);
		dbThesaurus.setDbConf(conf);
	}

	/*
	 * public DatabaseThesaurusDatastructure(SqlThesaurusConfiguration dbConf,
	 * TunnelConfiguration tunnelConf) { super.setDbConf(dbConf);
	 * super.setTunnelConf(tunnelConf); }
	 */

	public DatabaseThesaurusDatastructure(String dbConfigurationFile) {
		this(new File(dbConfigurationFile));
	}

	@Override
	public DatabaseThesaurusConfiguration getDatabaseConfiguration() {

		return dbThesaurus.getDatabaseConfiguration();
	}

	public DatabaseThesaurusDatastructure(File dbConfigurationFile,
			File tunnelConfigurationFile) {
		dbThesaurus.setDbConfigurationFile(dbConfigurationFile);
		dbThesaurus.setTunnelConfigurationFile(tunnelConfigurationFile);
	}

	public DatabaseThesaurusDatastructure() {

	}

    @Override
    public Exception getError() {
        if (dbThesaurus.getError() != null) {
            return dbThesaurus.getError();
        }
        return super.getError();
    }

    public Exception getConnectionError() {
        return getError();
    }

	@Override
	public boolean connect() {
		boolean value = dbThesaurus.connect();
		return value;
	}

	@Override
	public void destroy() {
		super.destroy();
		dbThesaurus.destroy();
	}

	public List<Order2> getSimilarTerms(String key) {
		try {
			ResultSet set = dbThesaurus.getSimilarTerms(key);
			return fillExpansions(set);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Order2> getSimilarTerms(String key, int numberOfEntries) {
		try {
			ResultSet set = dbThesaurus.getSimilarTerms(key, numberOfEntries);
			return fillExpansions(set);
		} catch (SQLException e) {
            throw new RuntimeException(e);
		}
	}

	public List<Order2> getSimilarTerms(String key, double threshold) {
		try {
			ResultSet set = dbThesaurus.getSimilarTerms(key, threshold);
			return fillExpansions(set);
		} catch (SQLException e) {
            throw new RuntimeException(e);
		}
	}

	@Override
	public DatabaseConnection getDatabaseConnection() {
		return dbThesaurus.getDatabaseConnection();
	}

	private List<Order2> fillExpansions(ResultSet set) throws SQLException {
		List<Order2> list = new ArrayList<Order2>(1000);
		while (set.next()) {
			list.add(new Order2(set.getString(1), set.getDouble(2)));
		}
		set.getStatement().close();
        set.close();
		return list;
	}

	public Long getTermCount(String key) {
		return dbThesaurus.getTermCount(key);
	}

	public Long getContextsCount(String value) {
		return dbThesaurus.getContextsCount(value);
	}

    public Long getTermContextsCount(String key, String value) {
		return dbThesaurus.getTermContextsCount(key, value);
	}

	public Double getTermContextsScore(String key, String val) {
		return dbThesaurus.getTermContextsScore(key, val);
	}

    public Map<String, Double> getBatchTermContextsScore(String expandedJo, String context) {return dbThesaurus.getBatchTermContextsScore(expandedJo, context);}

    @Override
    public Double getAverageContextsScore() {return dbThesaurus.getAverageContextsScore();  }

    @Override
    public List<Order2> getContextTermsScores(String feature) {
        try {
            return fillExpansions(dbThesaurus.getContextTermsScores(feature));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
	public List<Order1> getTermContextsScores(String key) {
		ResultSet set = dbThesaurus.getTermContextsScores(key);
		List<Order1> list = fillKeyValuesScores(set);
		return list;
	}

	private List<Order1> fillKeyValuesScores(ResultSet set) {

		List<Order1> list = new ArrayList<Order1>();
		try {

			while (set.next()) {

				list.add(new Order1(set.getString(1), set.getDouble(2)));
			}
			set.getStatement().close();
            set.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	@Override
	public List<Order1> getTermContextsScores(String key, int numberOfEntries) {

		ResultSet set = dbThesaurus.getTermContextsScores(key, numberOfEntries);
		return fillKeyValuesScores(set);
	}

	@Override
	public List<Order1> getTermContextsScores(String key, double threshold) {
		ResultSet set = dbThesaurus.getTermContextsScores(key, threshold);
		return fillKeyValuesScores(set);
	}

	@Override
	public List<Order2> getSimilarContexts(String values, int max) {
		ResultSet set = dbThesaurus.getSimilarContexts(values, max);
		return fillSimilarValues(set);
	}

	public List<Order2> fillSimilarValues(ResultSet set) {

		List<Order2> list = new ArrayList<Order2>();
		try {
			while (set.next()) {
				list.add(new Order2(set.getString(1), set.getDouble(2)));
			}
			set.getStatement().close();
			set.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return list;
	}

	@Override
	public List<Order2> getSimilarContexts(String values) {
		ResultSet set = dbThesaurus.getSimilarContexts(values);
		return fillSimilarValues(set);
	}

	@Override
	public List<Order2> getSimilarContexts(String key, double threshold) {
		ResultSet set = dbThesaurus.getSimilarContexts(key, threshold);
		return fillSimilarValues(set);
	}

	@Override
	public List<Sense> getSenses(String key) {
		List<Sense> senseList = new ArrayList<Sense>();
		ResultSet set = dbThesaurus.getSenses(key);
		try {
			while (set.next()) {
				Sense s = new Sense();
				s.setCui(set.getString(1));
				String k = set.getString(2);
				String[] senses = k.split(", ");
				s.setIsas(new ArrayList<String>());
				s.setSenses(new ArrayList<String>());
				for (String sen : senses) {
					s.getSenses().add(sen);
				}
				String[] isas = set.getString(3).split(" ");

				for (String isa : isas) {
					if (isa.trim().length() > 0)
						s.getIsas().add(isa);
				}

				senseList.add(s);
			}
            set.getStatement().close();
            set.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return senseList;
	}

	@Override
	public List<Sense> getIsas(String key) {
		return getSenses(key);
	}

	@Override
	public List<Sense> getSenseCUIs(String key) {
		return getSenses(key);
	}

	@Override
	public Double getSimilarTermScore(String t1, String t2) {
		return dbThesaurus.getSimilarTermScore(t1, t2);
	}

}
