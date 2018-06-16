package io.ebeaninternal.server.cluster.binarymessage;

public interface MsgKeys {

	/**
	 * Used to identify client on connection initiation.
	 */
	int HELLO = 182;

	/**
	 * Used to confirm protocol on reading messages.
	 */
	int HEADER = 11;

	/**
	 * Used to confirm protocol on reading messages.
	 */
	int DATA = 12;
}
