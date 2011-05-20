package com.buglabs.bug.swarm.connector.test;

import java.io.IOException;
import java.util.List;

import com.buglabs.module.IModuleControl;
import com.buglabs.module.IModuleProperty;

public class MockIModuleControl implements IModuleControl {

	private final String name;
	private final int slot;
	private final List properties;

	MockIModuleControl(String name, int slot, List properties) {
		this.name = name;
		this.slot = slot;
		this.properties = properties;

	}

	@Override
	public List getModuleProperties() {
		return properties;
	}

	@Override
	public String getModuleName() {
		return name;
	}

	@Override
	public int getSlotId() {
		return slot;
	}

	@Override
	public boolean setModuleProperty(IModuleProperty property) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int suspend() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int resume() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

}