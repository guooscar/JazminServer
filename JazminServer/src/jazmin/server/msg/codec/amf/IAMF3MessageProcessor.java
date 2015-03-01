/**
 * *****************************************************************************
 * 							Copyright (c) 2014 yama.
 * This is not a free software,all rights reserved by yama(guooscar@gmail.com).
 * ANY use of this software MUST be subject to the consent of yama.
 *
 * *****************************************************************************
 */
/*
  Exadel AMF-serializer
  Copyright (C) 2008 Exadel, Inc.

  AMF-serializer is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation

  IAMF3MessageProcessor.java
  Last modified by: $Author$
  $Revision$   $Date$
*/

package jazmin.server.msg.codec.amf;

import flex.messaging.messages.Message;

/**
 * Interface for classes which provides processing of AMF3Message.
 * 
 * @author apleskatsevich
 */
public interface IAMF3MessageProcessor {

    /**
     * Process amf3 message.
     * 
     * @param amf3Message Message to process
     * 
     * @return Result of processing
     */
    Message process(Message amf3Message);
    
}
