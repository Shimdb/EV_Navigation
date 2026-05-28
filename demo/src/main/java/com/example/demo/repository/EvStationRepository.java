package com.example.demo.repository; // 본인의 패키지명 확인

import com.example.demo.dto.EvStationDto;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
public class EvStationRepository {

    private final JdbcTemplate jdbcTemplate;

    public EvStationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void bulkInsert(List<EvStationDto.Item> stations) {
        // POINT 함수로 공간 데이터(내비게이션용)까지 한 번에 저장합니다.
        String sql = "INSERT INTO ChargingStation (station_id, name, lat, lng, operator_id, location) " +
                "VALUES (?, ?, ?, ?, ?, ST_GeomFromText(CONCAT('POINT(', ?, ' ', ?, ')'), 4326)) " +
                "ON DUPLICATE KEY UPDATE " +
                "name = VALUES(name), operator_id = VALUES(operator_id)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                EvStationDto.Item item = stations.get(i);
                ps.setString(1, item.getStationId());
                ps.setString(2, item.getName());
                ps.setDouble(3, item.getLat());
                ps.setDouble(4, item.getLng());
                ps.setString(5, item.getOperatorId());
                ps.setDouble(6, item.getLng()); // POINT X 좌표 (경도)
                ps.setDouble(7, item.getLat()); // POINT Y 좌표 (위도)
            }

            @Override
            public int getBatchSize() {
                return stations.size();
            }
        });
    }
}