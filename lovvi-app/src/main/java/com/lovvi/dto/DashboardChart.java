package com.lovvi.dto;

import java.util.List;

public record DashboardChart(String id, String title, String type, List<DashboardPoint> points) {
}
