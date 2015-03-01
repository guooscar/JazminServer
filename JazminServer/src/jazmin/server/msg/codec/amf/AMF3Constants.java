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

/**
 * @author Franck WOLFF
 */
public interface AMF3Constants {

    public static final byte AMF3_UNDEFINED = 0x00;
    public static final byte AMF3_NULL = 0x01;
    public static final byte AMF3_BOOLEAN_FALSE = 0x02;
    public static final byte AMF3_BOOLEAN_TRUE = 0x03;
    public static final byte AMF3_INTEGER = 0x04;
    public static final byte AMF3_NUMBER = 0x05;
    public static final byte AMF3_STRING = 0x06;
    public static final byte AMF3_XML = 0x07;
    public static final byte AMF3_DATE = 0x08;
    public static final byte AMF3_ARRAY = 0x09;
    public static final byte AMF3_OBJECT = 0x0A;
    public static final byte AMF3_XMLSTRING = 0x0B;
    public static final byte AMF3_BYTEARRAY = 0x0C;
    
    public static final int AMF3_INTEGER_MAX = Integer.MAX_VALUE >> 3;
    public static final int AMF3_INTEGER_MIN = Integer.MIN_VALUE >> 3;
}
