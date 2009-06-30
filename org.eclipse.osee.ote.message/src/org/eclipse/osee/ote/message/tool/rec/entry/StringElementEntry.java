/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ote.message.tool.rec.entry;

import java.nio.ByteBuffer;
import org.eclipse.osee.ote.message.data.MemoryResource;
import org.eclipse.osee.ote.message.elements.StringElement;


public class StringElementEntry implements IElementEntry {

	private final StringElement element;
	private final byte[] nameAsBytes;

	public StringElementEntry(final StringElement element) {
		this.element = element;
		nameAsBytes = element.getName().getBytes();
	}

	public StringElement getElement() {
		return element;
	}

	public void write(ByteBuffer buffer, MemoryResource mem, int limit) {
		mem.setOffset(element.getMsgData().getMem().getOffset());
		buffer.put(nameAsBytes).put(COMMA).put(TICK).put(element.valueOf(mem).getBytes()).put(TICK).put(COMMA);
	}

}
