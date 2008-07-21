package net.jini;

import java.net.URL;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The main plug-in class to be used in the desktop.
 */
public class JiniPlugin implements BundleActivator {

   private static JiniPlugin plugin;
   private String[] serviceGroups;
   private BundleContext context;

   public static JiniPlugin getInstance() {
      return plugin;
   }

   public String[] getJiniVersion() {
      //      if (serviceGroups == null) {
      Bundle bundle = context.getBundle();
      try {
    	  
         if (bundle != null) {
            URL home = bundle.getEntry("/");
            String id = home.getFile();
            if (id.endsWith("/")) {
               id = id.substring(0, id.length() - 1);
            }
            id = id.substring(id.lastIndexOf("/") + 1, id.length());
            serviceGroups = new String[1];
            serviceGroups[0] = id;
         }
      } catch (Exception e) {
         System.err.println("Failed to extract jini version");
         e.printStackTrace();
      }
      //      }
      return serviceGroups;
   }

/* (non-Javadoc)
 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
 */
@Override
public void start(BundleContext arg0) throws Exception {
	plugin = this;
	
	serviceGroups = null;
}

/* (non-Javadoc)
 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
 */
@Override
public void stop(BundleContext arg0) throws Exception {
}
}
