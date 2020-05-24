package com.github.ideahut.qms.shared.core.cache;

import java.util.Optional;

public class RedisProperties {

	public String host; 
    
	public Integer port;
	
	public Optional<String> password = Optional.empty(); 
    
	public Optional<Integer> database = Optional.empty();	
	
}
