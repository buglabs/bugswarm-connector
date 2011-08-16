package com.buglabs.bug.swarm.connector.test;

import com.buglabs.services.ws.IWSResponse;
import com.buglabs.services.ws.PublicWSDefinition;
import com.buglabs.services.ws.PublicWSProvider;

public class MockPublicWSProvider implements PublicWSProvider {

	private final String name;
	private final String desc;

	public MockPublicWSProvider(String name, String desc) {
		this.name = name;
		this.desc = desc;

	}

	@Override
	public PublicWSDefinition discover(int operation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IWSResponse execute(int operation, String input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPublicName() {
		return name;
	}

	@Override
	public String getDescription() {
		return desc;
	}

}
