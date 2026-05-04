package com.lovvi.dto;

import java.util.List;
import java.util.Map;

public record TableDetails(String tableName, List<ColumnInfo> columns, List<Map<String, Object>> rows) {
}
