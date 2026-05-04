package com.lovvi.dto;

import java.util.List;

public record DatabaseOverview(String databaseName, List<TableSummary> tables) {
}
