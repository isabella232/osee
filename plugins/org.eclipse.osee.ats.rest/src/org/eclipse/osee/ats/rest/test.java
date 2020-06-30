/*
 * Created on Jun 29, 2020
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.rest;

import org.eclipse.osee.framework.core.JaxRsApi;

public class test {
   private static JaxRsApi jaxRsApi;

   public static void Main(String[] args) {
      String s = jaxRsApi.toJson("");
      System.out.println("s: " + s);
   }

}
