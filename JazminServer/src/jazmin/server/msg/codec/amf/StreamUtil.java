/**
 * *****************************************************************************
 * 							Copyright (c) 2014 yama.
 * This is not a free software,all rights reserved by yama(guooscar@gmail.com).
 * ANY use of this software MUST be subject to the consent of yama.
 *
 * *****************************************************************************
 */
/*
  GRANITE DATA SERVICES
  Copyright (C) 2007 ADEQUATE SYSTEMS SARL

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation; either version 3 of the License, or (at your
  option) any later version.
 
  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
  for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package jazmin.server.msg.codec.amf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Franck WOLFF
 */
public class StreamUtil {

	public static ByteArrayInputStream getResourceAsStream(String path) throws IOException {		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
		if (is == null)
			throw new FileNotFoundException("Resource not found: " + path);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
		try {
			int b = -1;
			while ((b = is.read()) != -1)
				baos.write(b);
		} finally {
			is.close();
		}
		return new ByteArrayInputStream(baos.toByteArray());
	}
}
