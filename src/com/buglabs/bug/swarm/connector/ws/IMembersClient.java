package com.buglabs.bug.swarm.connector.ws;

import java.io.IOException;
import java.util.List;

/**
 * Contract for the API defined at https://github.com/buglabs/bugswarm/wiki/Swarm-Members-API
 * 
 * @author kgilmer
 *
 */
public interface IMembersClient {

	/**
	 * A member can be defined as a consumer and(?)/or producer.
	 */
	public enum MemberType {
		ALL(""), CONSUMER("consumer"), PRODUCER("producer");
		
		private final String name;
		
		private MemberType(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	/**
	 * @param swarmId
	 * @return
	 * @throws IOException 
	 */
	public List<SwarmMemberModel> list(String swarmId, IMembersClient.MemberType type) throws IOException;
	
	/**
	 * @param swarmId
	 * @param type
	 * @param userId
	 * @param resource
	 * @return
	 * @throws IOException 
	 */
	public int add(String swarmId, IMembersClient.MemberType type, String userId, String resource) throws IOException;
	
	/**
	 * @param swarmId
	 * @return
	 * @throws IOException 
	 */
	public int remove(String swarmId) throws IOException;
	
	/**
	 * @param userId
	 * @param type
	 * @return
	 * @throws IOException 
	 */
	public List<SwarmModel> getSwarmsByMember(String userId, IMembersClient.MemberType type) throws IOException;
}
