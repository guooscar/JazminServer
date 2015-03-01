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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author Franck WOLFF
 */
public class LocaleInstanciator extends AbstractInstanciator<Locale> {

    private static final long serialVersionUID = 1L;
    
    private static final List<String> orderedFields;
    static {
        List<String> of = new ArrayList<String>(3);
        of.add("country");
        of.add("language");
        of.add("variant");
        orderedFields = Collections.unmodifiableList(of);
        
    }

    public LocaleInstanciator() {
        super();
    }
    
    @Override
    public Locale newInstance() {
        String language = noNull((String)get("language"));
        String country = noNull((String)get("country"));
        String variant = noNull((String)get("variant"));

        return new Locale(language, country, variant);
    }
    
    @Override
    public List<String> getOrderedFieldNames() {
        return orderedFields;
    }

    private static String noNull(String s) {
        return s != null ? s : "";
    }
}
