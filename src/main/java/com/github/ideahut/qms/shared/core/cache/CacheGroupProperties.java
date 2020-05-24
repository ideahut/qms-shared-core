package com.github.ideahut.qms.shared.core.cache;

import java.util.Optional;

public class CacheGroupProperties {

	public String name;
	
	public Optional<Integer> expiry = Optional.empty(); // Seconds, 0 = never expire
	
	public Optional<Boolean> nullable = Optional.empty();
	
	public Optional<Integer> limit = Optional.empty(); // 0 = unlimited
	
}
