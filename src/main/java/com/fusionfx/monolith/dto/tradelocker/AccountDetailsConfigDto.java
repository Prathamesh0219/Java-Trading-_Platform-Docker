package com.fusionfx.monolith.dto.tradelocker;

import lombok.Data;
import java.util.List;

@Data
public class AccountDetailsConfigDto {
    private D d; // Main data section
    private String s; // Status

    @Data
    public static class D {
        private Config accountDetailsConfig;
        private CustomerAccess customerAccess;
        private Config filledOrdersConfig;
        private Config ordersConfig;
        private Config ordersHistoryConfig;
        private Config positionsConfig;
        private List<RateLimits> rateLimits;
        private List<Limits> limits;
    }

    @Data
    public static class Config {
        private String id;
        private String title;
        private List<Column> columns;

        @Data
        public static class Column {
            private String description;
            private String id;
        }
    }

    @Data
    public static class CustomerAccess {
        private boolean filledOrders;
        private boolean marketDepth;
        private boolean orders;
        private boolean ordersHistory;
        private boolean positions;
        private boolean symbolInfo;
    }

    @Data
    public static class RateLimits {
        private String rateLimitType;
        private String measure;
        private int intervalNum;
        private int limit;
    }

    @Data
    public static class Limits {
        private String limitType;
        private int limit;
    }
}

