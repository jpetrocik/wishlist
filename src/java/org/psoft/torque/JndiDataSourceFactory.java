package org.psoft.torque;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.TorqueException;
import org.apache.torque.dsfactory.AbstractDataSourceFactory;
import org.apache.torque.dsfactory.DataSourceFactory;

public class JndiDataSourceFactory extends AbstractDataSourceFactory
    implements DataSourceFactory
{

    /** The log. */
    private static Log log = LogFactory.getLog(JndiDataSourceFactory.class);

    /** The path to get the resource from. */
    private String path;
    /** The context to get the resource from. */
    private Context ctx;

    /** A locally cached copy of the DataSource */
    private DataSource ds = null;

    /** Time of last actual lookup action */
    private long lastLookup = 0;

    /** Time between two lookups */
    private long ttl = 0; // ms

    /**
     * @see org.apache.torque.dsfactory.DataSourceFactory#getDataSource
     */
    public DataSource getDataSource() throws TorqueException
    {
    	long time = System.currentTimeMillis();
    	
    	if (ds == null || time - lastLookup > ttl)
    	{
            try
            {
                ds = ((DataSource) ctx.lookup(path));
	        lastLookup = time;
            }
            catch (Exception e)
            {
                throw new TorqueException(e);
            }
    	}

   	return ds;
    }

    /**
     * @see org.apache.torque.dsfactory.DataSourceFactory#initialize
     */
    public void initialize(Configuration configuration) throws TorqueException
    {
        if (configuration == null)
        {
            throw new TorqueException(
                "Torque cannot be initialized without "
                    + "a valid configuration. Please check the log files "
                    + "for further details.");
        }
        initJNDI(configuration);
    }

    /**
     * Initializes JNDI.
     *
     * @param configuration where to read the settings from
     * @throws TorqueException if a property set fails
     */
    private void initJNDI(Configuration configuration) throws TorqueException
    {
        log.debug("Starting initJNDI");
        Hashtable env = null;
        Configuration c = configuration.subset("jndi");
        if (c == null)
        {
            throw new TorqueException(
                "JndiDataSourceFactory requires a jndi "
                    + "path property to lookup the DataSource in JNDI.");
        }
        try
        {
            Iterator i = c.getKeys();
            while (i.hasNext())
            {
                String key = (String) i.next();
                if (key.equals("path"))
                {
                    path = c.getString(key);
                    log.debug("JNDI path: " + path);
                }
                else if (key.equals("ttl"))
				{
					ttl = c.getLong(key, ttl);
							log.debug("Time between context lookups: " + ttl);
				}
                else
                {
                    if (env == null)
                    {
                        env = new Hashtable();
                    }
                    String value = c.getString(key);
                    env.put(key, value);
                    log.debug("Set jndi property: " + key + "=" + value);
                }
            }
            if (env == null)
            {
                ctx = new InitialContext();
            }
            else
            {
                ctx = new InitialContext(env);
            }
            log.debug("Created new InitialContext");
            debugCtx(ctx);
        }
        catch (Exception e)
        {
            log.error("", e);
            throw new TorqueException(e);
        }
    }

    /**
     *
     * @param ctx the context
     * @throws NamingException
     */
    private void debugCtx(Context ctx) throws NamingException
    {
        log.debug("InitialContext -------------------------------");
        Map env = ctx.getEnvironment();
        Iterator qw = env.keySet().iterator();
        log.debug("Environment properties:" + env.size());
        while (qw.hasNext())
        {
            Object prop = qw.next();
            log.debug("    " + prop + ": " + env.get(prop));
        }
        log.debug("----------------------------------------------");
    }

}