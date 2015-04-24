package jazmin.server.sip.io.pkts.packet.rtp;

/*
 * Copyright 2010 Bruno de Carvalho
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/**
 * @author <a href="http://bruno.biasedbit.com/">Bruno de Carvalho</a>
 */
public enum RtpVersion {

    // constants ------------------------------------------------------------------------------------------------------

    V2((byte) 0x80), V1((byte) 0x40), V0((byte) 0x00);

    // internal vars --------------------------------------------------------------------------------------------------

    private final byte b;

    // constructors ---------------------------------------------------------------------------------------------------

    private RtpVersion(final byte b) {
        this.b = b;
    }

    // public static methods ------------------------------------------------------------------------------------------

    public static RtpVersion fromByte(final byte b) throws IllegalArgumentException {
        final byte tmp = (byte) (b & 0xc0);
        // Starts from version 2, which is the most common.
        for (final RtpVersion version : values()) {
            if (version.getByte() == tmp) {
                return version;
            }
        }

        throw new IllegalArgumentException("Unknown version for byte: " + b);
    }

    // getters & setters ----------------------------------------------------------------------------------------------

    public byte getByte() {
        return this.b;
    }
}