package org.psoft.torque;

import java.io.InputStream;

import javax.servlet.ServletException;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;
import org.apache.struts.config.ModuleConfig;
import org.apache.torque.Torque;

/**
 * 
 * @author john
 */
public class TorquePlugin implements PlugIn {
	private static Log log = LogFactory.getLog(TorquePlugin.class);

	private String config;

	/** Creates a new instance of ToquePlugin */
	public TorquePlugin() {
	}

	public void destroy() {
	}

	public void init(ActionServlet actionServlet, ModuleConfig moduleConfig)
			throws ServletException {
		try {
			InputStream in = getClass().getClassLoader().getResourceAsStream(
					config);

			if (in == null) {
				throw new ServletException(
						"Unable to open torque configuration file " + config);
			}

			PropertiesConfiguration config = new PropertiesConfiguration();
			config.load(in);
			Torque.init(config);

		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException("Error to initialize Torque", e);
		}
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public static void main(String args[]) throws Exception{
		TorquePlugin plugin = new TorquePlugin();
		plugin.setConfig("torque.properties");
		plugin.init(null,null);
	}
}
