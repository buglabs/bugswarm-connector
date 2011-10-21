package com.buglabs.bug.swarm.connector.ws;

import java.io.IOException;
import java.util.List;

import com.buglabs.bug.swarm.connector.model.SwarmModel;
import com.buglabs.bug.swarm.connector.model.SwarmResourceModel;

/**
 * Contract for the API defined at
 * https://github.com/buglabs/bugswarm/wiki/Swarm-Members-API.
 * 
 * @author kgilmer
 * 
 */
public interface ISwarmResourcesClient {

	/**
	 * A member can be defined as a consumer and(?)/or producer.
	 */
	public enum MemberType {
		/**
		 * There are two types of members, 'consumer' and 'producer'.
		 */
		CONSUMER("consumer"), PRODUCER("producer");

		/**
		 * Name of member.
		 */
		private final String name;

		/**
		 * @param name
		 *            of member
		 */
		private MemberType(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * @param swarmId
	 *            id of swarm
	 * @param type
	 *            consumer or producer
	 * @return List of SwarmMemberModel
	 * @throws IOException
	 *             on connection error
	 */
	List<SwarmResourceModel> list(String swarmId, ISwarmResourcesClient.MemberType type) throws IOException;

	/**
	 * @param swarmId
	 *            id of swarm
	 * @param type
	 *            consumer or producer
	 * @param userId
	 *            user id
	 * @param resource
	 *            XMPP resource
	 * @return HTTP response of operation
	 * @throws IOException
	 */
	SwarmWSResponse add(String swarmId, ISwarmResourcesClient.MemberType type, String resource) throws IOException;

	/**
	 * @param swarmId
	 *            id of swarm
	 * @param type
	 *            consumer or producer
	 * @param userId
	 *            user id
	 * @param resource
	 *            XMPP resource
	 * @return HTTP response of operation
	 * @throws IOException
	 */
	SwarmWSResponse remove(String swarmId, ISwarmResourcesClient.MemberType type, String userId, String resource) throws IOException;

	/**
	 * @param resource
	 *            XMPP resource
	 * @return list of SwarmModel or emtpy list
	 * @throws IOException
	 */
	List<SwarmModel> getSwarmsByMember(String resource) throws IOException;
}
