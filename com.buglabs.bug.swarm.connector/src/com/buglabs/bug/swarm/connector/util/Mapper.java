package com.buglabs.bug.swarm.connector.util;

import java.util.Collection;

import com.buglabs.bug.swarm.connector.util.Applier.Fn;

public class Mapper {

	public static <I, O> O map(Fn<I, O> function, Object input) {
		return Applier.mapSingle(input, function);
	}
	
	public static <I, O> Collection<O> map(Fn<I, O> function, Iterable<I> input) {
		return Applier.map(input, function);
	}
	
	public static <I, O> Collection<O> map(Fn<I, O> function, Object[] input) {
		return Applier.map(input, function);
	}
}
