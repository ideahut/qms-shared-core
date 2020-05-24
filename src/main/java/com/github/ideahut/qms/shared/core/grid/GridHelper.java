package com.github.ideahut.qms.shared.core.grid;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.ideahut.qms.shared.client.object.AdminRequest;
import com.github.ideahut.qms.shared.client.object.Grid;
import com.github.ideahut.qms.shared.client.object.KeyValue;
import com.github.ideahut.qms.shared.client.type.GridControlType;
import com.github.ideahut.qms.shared.client.type.GridOrderType;
import com.github.ideahut.qms.shared.client.type.GridValidationType;
import com.github.ideahut.qms.shared.client.type.HorizontalAlignType;
import com.github.ideahut.qms.shared.client.type.IdType;
import com.github.ideahut.qms.shared.core.annotation.Accessible;
import com.github.ideahut.qms.shared.core.annotation.Admin;
import com.github.ideahut.qms.shared.core.bean.GridBean;
import com.github.ideahut.qms.shared.core.bean.OptionsBean;
import com.github.ideahut.qms.shared.core.grid.annotation.GridAdmin;
import com.github.ideahut.qms.shared.core.grid.annotation.GridAttribute;
import com.github.ideahut.qms.shared.core.grid.annotation.GridColumn;
import com.github.ideahut.qms.shared.core.grid.annotation.GridFilter;
import com.github.ideahut.qms.shared.core.grid.annotation.GridHeader;
import com.github.ideahut.qms.shared.core.grid.annotation.GridOrder;
import com.github.ideahut.qms.shared.core.grid.annotation.GridTable;
import com.github.ideahut.qms.shared.core.grid.annotation.GridValidation;
import com.github.ideahut.qms.shared.core.mapper.DataMapper;
import com.github.ideahut.qms.shared.core.mapper.DataMapperImpl;
import com.github.ideahut.qms.shared.core.message.MessageHandler;
import com.github.ideahut.qms.shared.core.model.FieldInfo;
import com.github.ideahut.qms.shared.core.model.IdInfo;
import com.github.ideahut.qms.shared.core.model.ModelInfo;

public final class GridHelper {
	
	private GridHelper() {}
	
	private static final Comparator<Grid.Sort> GRID_SORT = new Comparator<Grid.Sort>() {
		@Override
		public int compare(Grid.Sort o1, Grid.Sort o2) {
			return o1.getSort().compareTo(o2.getSort());
		}
	};
	
	private static final DataMapper dataMapper = new DataMapperImpl();
	
	private static void populateGridColumn(
		GridColumn gridColumn,
		Grid.Column column,
		List<Grid.Table.Header> tableHeaders, 
		List<Grid.Filter.Item> filterItems, 
		List<Grid.Order.Item> orderItems
	) {
		if (gridColumn == null) {
			return;
		}
		GridOrder[] gridOrders = gridColumn.order();
		if (gridOrders.length != 0) {
			Grid.Order.Item orderItem = createGridOrderItem(gridOrders[0]);
			orderItem.setColumn(column.getName());
			if (orderItem.getLabel().isEmpty()) {
				orderItem.setLabel(column.getLabel());
			}
			orderItems.add(orderItem);
		}
		
		GridFilter[] gridFilters = gridColumn.filter();
		if (gridFilters.length != 0) {
			Grid.Filter.Item filterItem = createGridFilterItem(gridFilters[0]);
			filterItem.setColumn(column.getName());
			if (filterItem.getLabel().isEmpty()) {
				filterItem.setLabel(column.getLabel());
			}
			filterItems.add(filterItem);
		}
		
		GridHeader[] gridHeaders = gridColumn.header();
		if (gridHeaders.length != 0) {
			Grid.Table.Header tableHeader = createGridTableHeader(gridHeaders[0]);
			tableHeader.setColumn(column.getName());
			if (tableHeader.getLabel().isEmpty()) {
				tableHeader.setLabel(column.getLabel());
			}
			tableHeaders.add(tableHeader);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void populateGrid(Map<String, Grid> mapGrid, Map<Class<?>, ModelInfo> mapModelInfo, ModelInfo modelInfo) throws Exception {
		GridAttribute gridAttribute = modelInfo.getModelClass().getAnnotation(GridAttribute.class);
		if (gridAttribute == null) {
			return;
		}
		String name = gridAttribute.name().trim();
		if (name.isEmpty()) {
			throw new Exception("Grid name is required");
		}
		if (mapGrid.containsKey(name)) {
			throw new Exception("Duplicate grid name: " + name + ", class: " + modelInfo.getModelClass().getName());
		}
		String admin = gridAttribute.admin().trim();
		String path  = gridAttribute.path().trim();
		if (admin.isEmpty() && path.isEmpty()) {
			Admin annotAdmin = modelInfo.getModelClass().getAnnotation(Admin.class);
			if (annotAdmin == null) {
				throw new Exception("@" + Admin.class.getSimpleName() + " is not found in class: " + modelInfo.getModelClass().getName());
			}
			admin = annotAdmin.name().trim();
			if (admin.isEmpty()) {
				throw new Exception("@" + GridAttribute.class.getSimpleName() + " admin or path is required, class: " + modelInfo.getModelClass().getName());
			}
		}
		Set<String> fieldNames = new HashSet<String>(modelInfo.getFieldInfoNames());
		IdInfo idInfo = modelInfo.getIdInfo();
		
		Grid grid = new Grid();
		grid.setName(name);
		grid.setTitle(gridAttribute.title());
		grid.setAdmin(admin);
		grid.setPath(path);
		grid.setIdFields(new HashSet<String>(idInfo.getFields()));
		grid.setEmbeddedIdFields(IdType.embedded.equals(idInfo.getType()) ? new HashSet<String>(idInfo.getEmbeddedIdInfo().getFieldInfoNames()) : null);
		grid.setIdType(idInfo.getType());
		
		List<Grid.Table.Header> tableHeaders = new ArrayList<Grid.Table.Header>();
		List<Grid.Order.Item> orderItems = new ArrayList<Grid.Order.Item>();
		List<Grid.Filter.Item> filterItems = new ArrayList<Grid.Filter.Item>();
		List<Grid.Column> columnList = new ArrayList<Grid.Column>();
		
		// ID harus selalu ada
		if (IdType.embedded.equals(idInfo.getType())) {
			String idField = idInfo.getFields().iterator().next();
			ModelInfo embeddedIdInfo = idInfo.getEmbeddedIdInfo();
			for (String fieldName : embeddedIdInfo.getFieldInfoNames()) {
				FieldInfo fieldInfo = embeddedIdInfo.getFieldInfo(fieldName);
				GridColumn gridColumn = fieldInfo.getAnnotation(GridColumn.class);
				Grid.Column column;
				if (gridColumn != null) {
					column = createGridColumn(gridColumn, embeddedIdInfo.getModelClass().getName() + "::" + fieldName);						
				} else {
					column = new Grid.Column();					
					column.setAlign(HorizontalAlignType.left);
					column.setControlType(GridControlType.text);
					column.setEditable(Boolean.FALSE);
					column.setInsertable(Boolean.TRUE);
					column.setVisible(Boolean.TRUE);
					column.setLabel(fieldName);
					column.setSort(0);
				}
				if (column.getName().isEmpty()) {
					column.setName(idField + "." + fieldName);
				}
				column.setIsModel(Boolean.FALSE);						
				columnList.add(column);
				populateGridColumn(gridColumn, column, tableHeaders, filterItems, orderItems);
			}
			fieldNames.remove(idField);
		} else {
			for (String fieldName : idInfo.getFields()) {
				FieldInfo fieldInfo = modelInfo.getFieldInfo(fieldName);
				GridColumn gridColumn = fieldInfo.getAnnotation(GridColumn.class);
				Grid.Column column;
				if (gridColumn != null) {
					column = createGridColumn(gridColumn, modelInfo.getModelClass().getName() + "::" + fieldName);									
				} else {
					column = new Grid.Column();					
					column.setAlign(HorizontalAlignType.left);
					column.setControlType(GridControlType.text);
					column.setEditable(Boolean.FALSE);
					column.setInsertable(Boolean.TRUE);
					column.setVisible(Boolean.TRUE);
					column.setLabel(fieldName);
				}
				if (column.getName().isEmpty()) {
					column.setName(fieldName);
				}
				column.setIsModel(Boolean.FALSE);						
				columnList.add(column);
				populateGridColumn(gridColumn, column, tableHeaders, filterItems, orderItems);
				fieldNames.remove(fieldName);
			}
		}
		for (String fieldName : fieldNames) {
			FieldInfo fieldInfo = modelInfo.getFieldInfo(fieldName);
			GridColumn gridColumn = fieldInfo.getAnnotation(GridColumn.class);
			if (gridColumn == null) {
				continue;
			}
			
			Grid.Column column = createGridColumn(gridColumn, modelInfo.getModelClass().getName() + "::" + fieldName);
			if (column.getName().isEmpty()) {
				column.setName(fieldName);
			}
			
			ModelInfo fieldModelInfo = mapModelInfo.get(fieldInfo.getType());
			if (fieldModelInfo != null && fieldModelInfo.isEntity()) {
				IdInfo fieldIdInfo = fieldModelInfo.getIdInfo();
				column.setIsModel(Boolean.TRUE);
				column.setIdFields(fieldIdInfo.getFields());
				column.setIdType(fieldIdInfo.getType());
				if (IdType.embedded.equals(fieldIdInfo.getType())) {
					Map<String, String> embeddedIdFields = new LinkedHashMap<String, String>();
					ModelInfo embeddedIdInfo = fieldIdInfo.getEmbeddedIdInfo();
					for (String efname : embeddedIdInfo.getFieldInfoNames()) {
						String eflabel = efname;
						FieldInfo efinfo = embeddedIdInfo.getFieldInfo(efname);
						if (efinfo != null) {
							GridColumn efcolumn = efinfo.getAnnotation(GridColumn.class);
							if (efcolumn != null && !efcolumn.label().isEmpty()) {
								eflabel = efcolumn.label();
							}
						}
						embeddedIdFields.put(efname, eflabel);
					}
					column.setEmbeddedIdFields(embeddedIdFields);
				}				
			} else {
				column.setIsModel(Boolean.FALSE);
			}
			
			columnList.add(column);
			populateGridColumn(gridColumn, column, tableHeaders, filterItems, orderItems);
			
		}
		
		// Columns
		Collections.sort(columnList, GRID_SORT);
		Map<String, Grid.Column> columns = new LinkedHashMap<String, Grid.Column>();
		for (Grid.Column column : columnList) {
			columns.put(column.getName(), column);
		}
		grid.setColumns(columns);
		
		// Table
		GridTable[] gridTables = gridAttribute.table();
		Grid.Table table = new Grid.Table();
		if (gridTables.length != 0) {
			table.setMultiselect(gridTables[0].multiselect());
			table.setFooter(gridTables[0].footer());
		}
		Collections.sort(tableHeaders, GRID_SORT);
		table.setHeaders(tableHeaders);
		grid.setTable(table);
		
		// Filter
		Grid.Filter filter = new Grid.Filter();
		filter.setMatrixType(gridAttribute.filterMatrixType());
		filter.setMatrixNum(gridAttribute.filterMatrixNum());
		Collections.sort(filterItems, GRID_SORT);
		filter.setItems(filterItems);		
		grid.setFilter(filter);
		
		// Order
		Grid.Order order = new Grid.Order();
		order.setMatrixType(gridAttribute.orderMatrixType());
		order.setMatrixNum(gridAttribute.orderMatrixNum());
		Collections.sort(orderItems, GRID_SORT);
		order.setItems(orderItems);
		grid.setOrder(order);
		
		mapGrid.put(grid.getName(), grid); // put grid
		
		
		// CUSTOM GRID
		Class<? extends GridBean>[] gridCustomClasses = gridAttribute.customClasses();
		if (gridCustomClasses.length == 0) {
			String[] stringCustomClasses = gridAttribute.stringCustomClasses();
			for (String stringCustomClass : stringCustomClasses) {
				stringCustomClass = stringCustomClass.trim();
				if (stringCustomClass.isEmpty()) {
					continue;
				}
				gridCustomClasses[gridCustomClasses.length] = (Class<? extends GridBean>)Class.forName(stringCustomClass);
			}
		}
		for (Class<? extends GridBean> gridCustomClass : gridCustomClasses) {
			GridAttribute customGridAttribute = gridCustomClass.getAnnotation(GridAttribute.class);
			if (customGridAttribute == null) {
				throw new Exception("@" + GridAttribute.class.getSimpleName() + " is not found in custom class: " + gridCustomClass.getName() + ", model: " + modelInfo.getModelClass().getName());
			}
			String customName = customGridAttribute.name().trim();
			if (customName.isEmpty()) {
				throw new Exception("Custom Grid name is required, class: " + gridCustomClass.getName());
			}
			if (mapGrid.containsKey(customName)) {
				throw new Exception("Duplicate grid name: " + name + ", class: " + gridCustomClass.getName());
			}
			String customTitle = customGridAttribute.title().trim();
			if (customTitle.isEmpty()) {
				customTitle = grid.getTitle();
			}			
			Grid customGrid = new Grid();
			customGrid.setName(customName);
			customGrid.setTitle(customTitle);
			customGrid.setAdmin(admin);
			customGrid.setPath(path);
			customGrid.setColumns(new HashMap<String, Grid.Column>(columns));
			customGrid.setIdFields(idInfo.getFields());
			customGrid.setEmbeddedIdFields(IdType.embedded.equals(idInfo.getType()) ? new HashSet<String>(idInfo.getEmbeddedIdInfo().getFieldInfoNames()) : null);
			customGrid.setIdType(idInfo.getType());
			
			// Table
			GridTable[] customGridTables = customGridAttribute.table();
			Grid.Table customTable;
			if (customGridTables.length != 0) {
				customTable = new Grid.Table();
				customTable.setMultiselect(customGridTables[0].multiselect());
				GridHeader[] customGridHeaders = customGridTables[0].headers();
				if (customGridHeaders.length != 0) {
					List<Grid.Table.Header> customHeaderList = new ArrayList<Grid.Table.Header>();
					for (GridHeader customGridHeader : customGridHeaders) {
						Grid.Table.Header customTableHeader = createGridTableHeader(customGridHeader);
						String column = customTableHeader.getColumn();
						if (column.isEmpty()) {
							throw new Exception("@" + GridHeader.class.getName() + " column is required, class: " + gridCustomClass.getName());
						}
						if (!grid.getColumns().containsKey(column)) {
							throw new Exception("@" + GridHeader.class.getName() + " column is not valid, column: " + column + ", class: " + gridCustomClass.getName());
						}
						String label = customTableHeader.getLabel();
						if (label.isEmpty()) {
							customTableHeader.setLabel(grid.getColumns().get(column).getLabel());
						}
						customHeaderList.add(customTableHeader);
					}
					Collections.sort(customHeaderList, GRID_SORT);
					customTable.setHeaders(customHeaderList);
				} else {
					customTable.setHeaders(grid.getTable().getHeaders());
				}				
			} else {
				customTable = grid.getTable();
			}
			customGrid.setTable(customTable);
			
			// Filter
			Grid.Filter customFilter = new Grid.Filter();
			customFilter.setMatrixNum(customGridAttribute.filterMatrixNum());
			customFilter.setMatrixType(customGridAttribute.filterMatrixType());
			GridFilter[] customGridFilters = customGridAttribute.filters();
			if (customGridFilters.length != 0) {
				List<Grid.Filter.Item> customFilterItems = new ArrayList<Grid.Filter.Item>();
				for (GridFilter customGridFilter : customGridFilters) {
					Grid.Filter.Item customFilterItem = createGridFilterItem(customGridFilter);
					String column = customFilterItem.getColumn();
					if (column.isEmpty()) {
						throw new Exception("@" + GridFilter.class.getName() + " column is required, class: " + gridCustomClass.getName());
					}
					if (!grid.getColumns().containsKey(column)) {
						throw new Exception("@" + GridFilter.class.getName() + " column is not valid, class: " + gridCustomClass.getName());
					}
					String label = customFilterItem.getLabel();
					if (label.isEmpty()) {
						customFilterItem.setLabel(grid.getColumns().get(column).getLabel());
					}
					customFilterItems.add(customFilterItem);
				}
				Collections.sort(customFilterItems, GRID_SORT);
				customFilter.setItems(customFilterItems);
			}
			customGrid.setFilter(customFilter);
			
			// Order
			Grid.Order customOrder = new Grid.Order();
			customOrder.setMatrixNum(customGridAttribute.orderMatrixNum());
			customOrder.setMatrixType(customGridAttribute.orderMatrixType());
			GridOrder[] customGridOrders = customGridAttribute.orders();
			if (customGridOrders.length != 0) {
				List<Grid.Order.Item> customOrderItems = new ArrayList<Grid.Order.Item>();
				for (GridOrder customGridOrder : customGridOrders) {
					Grid.Order.Item customOrderItem = createGridOrderItem(customGridOrder);
					String column = customOrderItem.getColumn();
					if (column.isEmpty()) {
						throw new Exception("@" + GridOrder.class.getName() + " column is required, class: " + gridCustomClass.getName());
					}
					if (!grid.getColumns().containsKey(column)) {
						throw new Exception("@" + GridOrder.class.getName() + " column is not valid, class: " + gridCustomClass.getName());
					}
					String label = customOrderItem.getLabel();
					if (label.isEmpty()) {
						customOrderItem.setLabel(grid.getColumns().get(column).getLabel());
					}
					customOrderItems.add(customOrderItem);
				}
				Collections.sort(customOrderItems, GRID_SORT);
				customOrder.setItems(customOrderItems);
			}
			customGrid.setOrder(customOrder);
			
			mapGrid.put(customGrid.getName(), customGrid);
		}
	}
	
	public static Grid.Column createGridColumn(GridColumn gridColumn, String annotLoc) throws Exception {
		String annotName = "@" + GridColumn.class.getSimpleName();
		Grid.Column column = new Grid.Column();
		String label = gridColumn.label().trim();
		if (label.isEmpty()) {
			throw new Exception(annotName + " label is required");
		}
		GridControlType controlType = gridColumn.controlType();
		switch (controlType) {
		case grid:
			String metadata = gridColumn.metadata().trim();
			if (metadata.isEmpty()) {
				throw new Exception(annotName + " metadata is required for controlType: " + controlType.name() + ", loc: " + annotLoc);
			}
			column.setMetadata(metadata);
			break;
		case select:
			Accessible[] accessibles = gridColumn.options();
			if (accessibles.length == 0) {
				throw new Exception(annotName + " options is required for controlType: " + controlType.name() + ", loc: " + annotLoc);
			}
			Accessible accessible = accessibles[0];
			Class<?> target = accessible.target();
			String targetAsString = accessible.targetAsString().trim();
			String method = accessible.method().trim();
			String field = accessible.field().trim();
			boolean isStatic = accessible.isStatic();
			
			Class<?> optionsClass;
			if (!Void.class.isAssignableFrom(target)) {
				optionsClass = target;
			} else {
				if (targetAsString.isEmpty()) {
					throw new Exception(annotName + " target or targetAsString is required, loc: " + annotLoc);
				}
				optionsClass = Class.forName(targetAsString);
			}
			OptionsBean optionsBean;
			if (!method.isEmpty()) {
				Method optionsMethod = optionsClass.getDeclaredMethod(method);
				if (isStatic) {
					optionsBean = (OptionsBean)optionsMethod.invoke(null);
				} else {
					optionsBean = (OptionsBean)optionsMethod.invoke(optionsClass.newInstance());					
				}
			} else if (!field.isEmpty()) {
				Field optionsField = optionsClass.getDeclaredField(field);
				optionsField.setAccessible(true);
				if (isStatic) {
					optionsBean = (OptionsBean)optionsField.get(null);
				} else {
					optionsBean = (OptionsBean)optionsField.get(optionsClass.newInstance());
				}
			} else {
				optionsBean = (OptionsBean)optionsClass.newInstance();
			}
			column.setOptions(optionsBean.getKeyValueOptions());			
			break;
		case admin:
			GridAdmin[] gridAdmins = gridColumn.admin();
			if (gridAdmins.length == 0) {
				throw new Exception(annotName + " admin is required for controlType: " + controlType.name() + ", loc: " + annotLoc);
			}
			GridAdmin gridAdmin = gridAdmins[0];
			Grid.Admin admin = createGridAdmin(gridAdmin);
			column.setAdmin(admin);
			break;
		default:
			break;
		}
		column.setLabel(label);
		column.setControlType(controlType);
		column.setName(gridColumn.name().trim());
		column.setAlign(gridColumn.align());
		column.setEditable(gridColumn.editable());
		column.setInsertable(gridColumn.insertable());
		column.setVisible(gridColumn.visible());
		column.setReadOnly(gridColumn.readOnly());
		column.setRequired(gridColumn.required());
		column.setDefaultValue(gridColumn.defaultValue());
		column.setSort(gridColumn.sort());
		column.setViewColumn(gridColumn.viewColumn());
		
		GridAdmin[] recall = gridColumn.recall();
		if (recall.length != 0) {
			column.setRecall(recall[0].name());
		}
		
		GridValidation[] validations = gridColumn.validations();
		if (validations.length != 0) {
			List<Grid.Validation> list = new ArrayList<Grid.Validation>();
			for (GridValidation gridValidation : validations) {
				if (GridValidationType.none.equals(gridValidation.type())) {
					continue;
				}
				list.add(createGridValidation(gridValidation));				
			}
			column.setValidations(list);
		}
		
		return column;
	}
	
	public static Grid.Order.Item createGridOrderItem(GridOrder gridOrder) {
		Grid.Order.Item orderItem = new Grid.Order.Item();
		orderItem.setColumn(gridOrder.column().trim());
		orderItem.setLabel(gridOrder.label().trim());
		orderItem.setSort(gridOrder.sort());
		orderItem.setOrderType(gridOrder.orderType().name());		
		return orderItem;
	}
	
	public static Grid.Filter.Item createGridFilterItem(GridFilter gridFilter) {
		Grid.Filter.Item filterItem = new Grid.Filter.Item();
		filterItem.setColumn(gridFilter.column().trim());
		filterItem.setCondition(gridFilter.condition().getValue());
		filterItem.setLabel(gridFilter.label().trim());
		filterItem.setLogical(gridFilter.logical().name());
		filterItem.setSort(gridFilter.sort());
		return filterItem;
	}
	
	public static Grid.Table.Header createGridTableHeader(GridHeader gridHeader) {
		Grid.Table.Header tableHeader = new Grid.Table.Header();
		tableHeader.setTitleAlign(gridHeader.titleAlign());
		tableHeader.setContentAlign(gridHeader.contentAlign());
		tableHeader.setColumn(gridHeader.column().trim());
		tableHeader.setLabel(gridHeader.label().trim());
		tableHeader.setSort(gridHeader.sort());
		return tableHeader;
	}
	
	public static Grid.Admin createGridAdmin(GridAdmin gridAdmin) {
		AdminRequest request = new AdminRequest();
		request.setModel(gridAdmin.name());
		request.setLimit(gridAdmin.limit());		
		request.setField(Arrays.asList(gridAdmin.fields()));
		
		List<AdminRequest.Filter> filter = new ArrayList<AdminRequest.Filter>();
		for (GridFilter gridFilter : gridAdmin.filters()) {
			AdminRequest.Filter afilter = new AdminRequest.Filter();
			afilter.setCondition(gridFilter.condition());
			afilter.setField(gridFilter.column());
			afilter.setLogical(gridFilter.logical());
			afilter.setValue(gridFilter.value());
			filter.add(afilter);
		}
		request.setFilter(filter);
		
		List<String> order = new ArrayList<String>();
		for (GridOrder gridOrder : gridAdmin.orders()) {
			order.add((GridOrderType.desc.equals(gridOrder.orderType()) ? "-" : "") + gridOrder.column());		
		}
		request.setOrder(order);
		Grid.Admin admin = new Grid.Admin();
		admin.setPattern(gridAdmin.pattern());
		admin.setRequest(request);
		return admin;
	}
	
	public static Grid.Validation createGridValidation(GridValidation gridValidation) {
		Grid.Validation validation = new Grid.Validation();
		validation.setFormat(gridValidation.format());
		validation.setMaxValue(gridValidation.maxValue());
		validation.setMaxLength(gridValidation.maxLength());
		validation.setMinValue(gridValidation.minValue());
		validation.setMinLength(gridValidation.minLength());
		validation.setRangeValue(gridValidation.rangeValue());
		validation.setRangeLength(gridValidation.rangeLength());
		validation.setType(gridValidation.type());
		return validation;
	}
	
	public static Grid cloneGrid(Grid sourceGrid, MessageHandler messageHandler, String language) {
		byte[] data = dataMapper.writeJsonAsBytes(sourceGrid);
		Grid grid = dataMapper.readData(data, Grid.class);
		grid.setTitle(messageHandler.getMessage(grid.getTitle()));
		for (Grid.Table.Header header : grid.getTable().getHeaders()) {
			header.setLabel(messageHandler.getMessage(header.getLabel()));
		}
		for (Grid.Order.Item order : grid.getOrder().getItems()) {
			order.setLabel(messageHandler.getMessage(order.getLabel()));
		}
		for (Grid.Filter.Item filter : grid.getFilter().getItems()) {
			filter.setLabel(messageHandler.getMessage(filter.getLabel()));
		}
		for (Grid.Column column : grid.getColumns().values()) {
			column.setLabel(messageHandler.getMessage(column.getLabel()));
			if (column.getOptions() != null) {
				for (KeyValue<String, String> option : column.getOptions()) {
					option.setValue(messageHandler.getMessage(option.getValue()));
				}
			}
			if (column.getEmbeddedIdFields() != null) {
				for (String efkey : column.getEmbeddedIdFields().keySet()) {
					column.getEmbeddedIdFields().put(efkey, messageHandler.getMessage(column.getEmbeddedIdFields().get(efkey)));
				}
			}
		}
		return grid;
	}
	
}
