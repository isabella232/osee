/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.core.internal.script;

import java.io.Reader;
import javax.script.ScriptContext;

/**
 * @author Roberto E. Escobar
 */
public interface OrcsScriptCompiler {

   public interface OrcsCompiledScript {

      Object eval(ScriptContext context) throws OrcsScriptException;

   }

   OrcsCompiledScript compileReader(Reader reader, String filename) throws OrcsScriptException;

}