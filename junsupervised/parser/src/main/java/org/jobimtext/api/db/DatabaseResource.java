/*******************************************************************************
 * Copyright 2012
 * FG Language Technologie
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

import java.io.File;
import java.sql.SQLException;
import java.util.Map;

import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.resource.Resource_ImplBase;
import org.jobimtext.api.configuration.DatabaseThesaurusConfiguration;



/**
 * 
 * @author Martin Riedl This class creates a UIMAFIt Shared Resource that
 *         initializes a connection which also uses a vpn connection if
 *         specified in the configuration file
 * 
 */
public class DatabaseResource extends Resource_ImplBase implements Destroyable {
	public static final String PARAM_DB_CONFIGURATION_FILE = "PARAM_DB_CONFIGURATION_FILE";
	public static final String PARAM_TUNNEL_CONFIGURATION_FILE = "PARAM_TUNNEL_CONFIGURATION_FILE";

	//@ConfigurationParameter(name = PARAM_DB_CONFIGURATION_FILE, mandatory = false)
	private File dbConfigurationFile = null;
	//@ConfigurationParameter(name = PARAM_TUNNEL_CONFIGURATION_FILE, mandatory = false)
	private File tunnelConfigurationFile = null;

	private DatabaseThesaurusConfiguration dbConf = null;

    private Exception error = null;

	public DatabaseThesaurusConfiguration getDbConf() {
		return dbConf;
	}

	public void setDbConf(DatabaseThesaurusConfiguration dbConf) {
		this.dbConf = dbConf;
	}

	

	
	//private TunnelConfiguration tunnelConf = null;
	private DatabaseConnection databaseConnection=null;
	//private PortForwarding tunnel;

	
	
	public DatabaseResource() {

	}

	public File getDbConfigurationFile() {
		return dbConfigurationFile;
	}

	public void setDbConfigurationFile(File dbConfigurationFile) {
		this.dbConfigurationFile = dbConfigurationFile;
	}

	public File getTunnelConfigurationFile() {
		return tunnelConfigurationFile;
	}

	public void setTunnelConfigurationFile(File tunnelConfigurationFile) {
		this.tunnelConfigurationFile = tunnelConfigurationFile;
	}

	public DatabaseThesaurusConfiguration getDatabaseConfiguration() {
		return dbConf;
	}

	public DatabaseConnection getDatabaseConnection() {
		return databaseConnection;
	}

	public void setDatabaseConnection(DatabaseConnection databaseConnection) {
		this.databaseConnection = databaseConnection;
	}

    public Exception getError() {
        return error;
    }

    @Override
	public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams) throws ResourceInitializationException {
		boolean returnCode = super.initialize(aSpecifier, aAdditionalParams);

		returnCode &= connect();

		return returnCode;

	}

	public boolean connect() {
		databaseConnection = new DatabaseConnection();
		
		if (dbConf == null) {
			try {
				dbConf = DatabaseThesaurusConfiguration.getFromXmlFile(dbConfigurationFile);
			} catch (Exception e) {
				error = e;
			}
		}
		/*if (tunnelConfigurationFile != null) {
			if (tunnelConf == null) {
				tunnelConf = (TunnelConfiguration) x.fromXML(tunnelConfigurationFile);
			}
			if (tunnelConf.isActivate()) {
				tunnel = new PortForwarding(tunnelConf.getSshUser(), tunnelConf.getSshHost(), tunnelConf.getSshPort(), tunnelConf.getSshDirIdRsa());
				tunnel.openTunnel(tunnelConf.getHost(), tunnelConf.getrPort(), tunnelConf.getlPort());

			}
		}*/
		try {
			databaseConnection.openConnection(dbConf.getDbUrl(), dbConf.getDbUser(), dbConf.getDbPassword(), dbConf.getJdbcString());
		} catch (ClassNotFoundException e) {
            error = e;
			return false;
		} catch (SQLException e) {
            error = e;
			return false;
		}
		return true;
	}

	@Override
	public void destroy() {
		System.out.println("[DESTROY Database Resource]");
		if(databaseConnection!=null)
		databaseConnection.closeConnection();
		/*if (tunnelConf != null && tunnelConf.isActivate()) {
			tunnel.closeTunnel();
		}*/
		super.destroy();
	}

	@Override
	protected void finalize() throws Throwable {
		destroy();
		super.finalize();
	}

}
