package com.github.ideahut.qms.shared.core.audit;

import java.util.Optional;

public class AuditProperties {
	
	public Model model = new Model();
	
	public static class Model {		
		public Table table = new Table();
		public Column column = new Column();
		public Enable enable = new Enable();
		public Generate generate = new Generate();		
		
		public static class Table {
			public Optional<String> prefix = Optional.empty();
			public Optional<String> suffix = Optional.empty();
		}		
		
		public static class Column {
			public Optional<String> auditor = Optional.empty();
			public Optional<String> action = Optional.empty();
			public Optional<String> info = Optional.empty();
			public Optional<String> entry = Optional.empty();
		}		
		
		public static class Enable {
			public Optional<Boolean> rowid = Optional.empty();
			public Optional<Boolean> index = Optional.empty();
		}		
		
		public static class Generate {
			public Optional<Boolean> table = Optional.empty();
			public Optional<Integer> maxPrecision = Optional.empty();
			public Optional<Integer> maxScale = Optional.empty();
		}
	}
	
}
