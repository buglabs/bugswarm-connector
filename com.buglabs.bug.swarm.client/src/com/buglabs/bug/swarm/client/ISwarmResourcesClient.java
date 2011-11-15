package com.buglabs.bug.swarm.client;

import java.io.IOException;
import java.util.List;

import com.buglabs.bug.swarm.client.model.SwarmModel;
import com.buglabs.bug.swarm.client.model.SwarmResourceModel;

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
	
	List<SwarmResourceModel> list(String swarmId) throws IOException;

	/**
	 * @param swarmId
	 *            id of swarm
	 * @param type
	 *            consumer or producer
	 * @param userId
	 *            user id
	 * @param resource resource id
	 * @return HTTP response of operation
	 * @throws IOException on I/O error
	 */
	SwarmWSResponse add(String swarmId, ISwarmResourcesClient.MemberType type, String resourceId) throws IOException;

	/**
	 * @param swarmId
	 *            id of swarm
	 * @param type
	 *            consumer or producer
	 * @param userId
	 *            user id
	 * @param resourceId id of resource
	 * @return HTTP response of operation
	 * @throws IOException on I/O error
	 */
	SwarmWSResponse remove(String swarmId, ISwarmResourcesClient.MemberType type, String userId, String resourceId) throws IOException;

	/**
	 * @param resource id of resource
	 * @return list of SwarmModel or emtpy list
	 * @throws IOException on I/O error
	 */
	List<SwarmModel> getSwarmsByMember(String resourceiD) throws IOException;
}
