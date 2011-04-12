package com.buglabs.bug.swarm.connector.ws;

import java.util.List;

/**
 * A Swarm WS Client implementation using json.simple and simplerestclient.
 * @author kgilmer
 *
 */
public class SwarmWSClient implements ISwarmWSClient {

	@Override
	public String create(String name, boolean isPublic, String description) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(boolean isPublic, String description) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int delete() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<SwarmModel> list() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SwarmModel get(String swarmId) {
		// TODO Auto-generated method stub
		return null;
	}


}
