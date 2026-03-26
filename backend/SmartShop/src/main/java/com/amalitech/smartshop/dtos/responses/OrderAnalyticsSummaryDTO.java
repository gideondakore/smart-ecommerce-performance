package com.amalitech.smartshop.dtos.responses;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderAnalyticsSummaryDTO {
    private List<RevenueReportDTO> revenueReport;
    private List<BestSellerDTO> bestSellers;
}
