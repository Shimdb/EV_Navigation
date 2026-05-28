package com.example.demo.dto; // 본인의 패키지명 확인

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

public class EvStationDto {

    @Getter @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ApiResponse {
        private Body body;
    }

    @Getter @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        private Items items;
    }

    @Getter @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {
        private List<Item> item;
    }

    @Getter @Setter @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        @JsonProperty("statId")
        private String stationId; // 충전소 ID

        @JsonProperty("statNm")
        private String name; // 충전소 명칭

        @JsonProperty("lat")
        private double lat; // 위도

        @JsonProperty("lng")
        private double lng; // 경도

        @JsonProperty("busiId")
        private String operatorId; // 운영기관 ID
    }
}
