package cz.auderis.corusco.examples.showcase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

final class H2TimeseriesRepository {

    static final int OBSERVATION_COUNT = 100_000;

    private static final String URL = "jdbc:h2:mem:corusco_showcase_timeseries;DB_CLOSE_DELAY=-1";
    private static volatile boolean seeded;

    private H2TimeseriesRepository() {
    }

    static List<TimeseriesObservation> loadObservations() {
        try (Connection connection = DriverManager.getConnection(URL)) {
            seedIfNeeded(connection);
            try (PreparedStatement statement = connection.prepareStatement("""
                    select seq, timestamp_millis, symbol, venue, region, channel, state,
                           bid, ask, last_price, volume, notional, latency_millis
                    from observations
                    order by seq
                    """);
                    ResultSet resultSet = statement.executeQuery()) {
                List<TimeseriesObservation> rows = new ArrayList<>(OBSERVATION_COUNT);
                while (resultSet.next()) {
                    rows.add(new TimeseriesObservation(
                            resultSet.getLong("seq"),
                            resultSet.getLong("timestamp_millis"),
                            resultSet.getString("symbol"),
                            resultSet.getString("venue"),
                            resultSet.getString("region"),
                            resultSet.getString("channel"),
                            ObservationState.valueOf(resultSet.getString("state")),
                            resultSet.getBigDecimal("bid"),
                            resultSet.getBigDecimal("ask"),
                            resultSet.getBigDecimal("last_price"),
                            resultSet.getLong("volume"),
                            resultSet.getBigDecimal("notional"),
                            resultSet.getDouble("latency_millis")
                    ));
                }
                return rows;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load H2 time-series observations", e);
        }
    }

    private static void seedIfNeeded(Connection connection) throws SQLException {
        if (seeded) {
            return;
        }
        synchronized (H2TimeseriesRepository.class) {
            if (seeded) {
                return;
            }
            createSchema(connection);
            insertRows(connection);
            seeded = true;
        }
    }

    private static void createSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    create table if not exists observations (
                        seq bigint primary key,
                        timestamp_millis bigint not null,
                        symbol varchar(16) not null,
                        venue varchar(16) not null,
                        region varchar(16) not null,
                        channel varchar(24) not null,
                        state varchar(16) not null,
                        bid decimal(14, 4) not null,
                        ask decimal(14, 4) not null,
                        last_price decimal(14, 4) not null,
                        volume bigint not null,
                        notional decimal(18, 2) not null,
                        latency_millis double not null
                    )
                    """);
            statement.execute("delete from observations");
        }
    }

    private static void insertRows(Connection connection) throws SQLException {
        String[] symbols = {"ALFA", "BRAVO", "CRUX", "DELTA", "ECHO", "FOXTROT", "GAMMA", "HELIX"};
        String[] venues = {"XNAS", "XNYS", "BATS", "IEXG"};
        String[] regions = {"North", "South", "East", "West"};
        String[] channels = {"Orders", "Trades", "Quotes", "Risk", "Settlement"};
        ObservationState[] states = ObservationState.values();
        long start = 1_766_000_000_000L;
        connection.setAutoCommit(false);
        try (PreparedStatement statement = connection.prepareStatement("""
                insert into observations(
                    seq, timestamp_millis, symbol, venue, region, channel, state,
                    bid, ask, last_price, volume, notional, latency_millis
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """)) {
            for (int i = 0; i < OBSERVATION_COUNT; i++) {
                BigDecimal base = BigDecimal.valueOf(75 + (i % 9_000) / 100.0).setScale(4, RoundingMode.HALF_UP);
                BigDecimal bid = base.subtract(BigDecimal.valueOf(0.02));
                BigDecimal ask = base.add(BigDecimal.valueOf(0.02));
                long volume = 100L + (i * 37L) % 50_000L;
                statement.setLong(1, i + 1L);
                statement.setLong(2, start + i * 1_000L);
                statement.setString(3, symbols[i % symbols.length]);
                statement.setString(4, venues[i % venues.length]);
                statement.setString(5, regions[(i / 17) % regions.length]);
                statement.setString(6, channels[(i / 31) % channels.length]);
                statement.setString(7, states[(i / 997) % states.length].name());
                statement.setBigDecimal(8, bid);
                statement.setBigDecimal(9, ask);
                statement.setBigDecimal(10, base);
                statement.setLong(11, volume);
                statement.setBigDecimal(12, base.multiply(BigDecimal.valueOf(volume)).setScale(2, RoundingMode.HALF_UP));
                statement.setDouble(13, 0.4 + (i % 700) / 10.0);
                statement.addBatch();
                if (i % 1_000 == 999) {
                    statement.executeBatch();
                }
            }
            statement.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
