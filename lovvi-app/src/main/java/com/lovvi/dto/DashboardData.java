package com.lovvi.dto;

import java.util.List;

public record DashboardData(List<DashboardMetric> metrics, List<DashboardChart> charts) {
}
