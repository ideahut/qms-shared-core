package com.github.ideahut.qms.shared.core.message;

import java.util.List;
import java.util.Optional;

public class ResourceBundleMessageProperties {

	public String basename;
	
	public Language language = new Language();
	
	
	public static class Language {
		
		public List<String> available;
		
		public Optional<String> primary = Optional.empty();
		
	}
	
}
