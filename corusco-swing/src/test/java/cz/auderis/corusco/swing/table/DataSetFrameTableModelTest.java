package cz.auderis.corusco.swing.table;

import cz.auderis.corusco.annotations.dataset.DataColumnRole;
import cz.auderis.corusco.annotations.dataset.MissingPolicy;
import cz.auderis.corusco.annotations.dataset.QualityPolicy;
import cz.auderis.corusco.core.dataset.DataColumnDescriptor;
import cz.auderis.corusco.core.dataset.DataColumnKey;
import cz.auderis.corusco.core.dataset.DataSetDescriptor;
import cz.auderis.corusco.core.dataset.DataSetKey;
import cz.auderis.corusco.core.dataset.DataStorageType;
import cz.auderis.corusco.core.dataset.UnitMetadata;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DataSetFrameTableModelTest {

    @Test
    void exposesDescriptorColumnsAndDelegatesValues() {
        DataSetDescriptor<Row> descriptor = descriptor();
        List<Row> rows = List.of(new Row(10L, "AAPL", 189.25));
        DataSetFrameTableModel<Row> model = DataSetFrameTableModel.of(
                descriptor,
                rows::size,
                (row, column) -> value(rows.get(row), column)
        );

        assertThat(model.getRowCount()).isOne();
        assertThat(model.getColumnCount()).isEqualTo(3);
        assertThat(model.getColumnName(0)).isEqualTo("quotes/timestamp");
        assertThat(model.getColumnClass(2)).isEqualTo(Double.class);
        assertThat(model.getValueAt(0, 1)).isEqualTo("AAPL");
        assertThat(model.getValueAt(0, 2)).isEqualTo(189.25);
        assertThat(model.column(2).unit()).isEqualTo(UnitMetadata.of("USD"));
        assertThat(model.descriptor()).isSameAs(descriptor);
        assertThatThrownBy(() -> model.getValueAt(1, 0))
                .isInstanceOf(IndexOutOfBoundsException.class)
                .hasMessageContaining("row=1");
    }

    private static Object value(Row row, DataColumnDescriptor<Row, ?> column) {
        return switch (column.sourceMemberName()) {
            case "timestamp" -> row.timestamp();
            case "symbol" -> row.symbol();
            case "price" -> row.price();
            default -> throw new IllegalArgumentException(column.sourceMemberName());
        };
    }

    private static DataSetDescriptor<Row> descriptor() {
        DataSetKey<Row> key = DataSetKey.of("quotes", Row.class);
        DataColumnDescriptor<Row, Long> timestamp = DataColumnDescriptor.of(
                DataColumnKey.of("quotes/timestamp", key, Long.class),
                "timestamp",
                DataColumnRole.TIME_AXIS,
                DataStorageType.LONG_ARRAY,
                UnitMetadata.of("millis"),
                MissingPolicy.NONE,
                QualityPolicy.NONE,
                Set.of()
        );
        DataColumnDescriptor<Row, String> symbol = DataColumnDescriptor.of(
                DataColumnKey.of("quotes/symbol", key, String.class),
                "symbol",
                DataColumnRole.DIMENSION,
                DataStorageType.OBJECT_ARRAY,
                null,
                MissingPolicy.NULL_VALUE,
                QualityPolicy.NONE,
                Set.of()
        );
        DataColumnDescriptor<Row, Double> price = DataColumnDescriptor.of(
                DataColumnKey.of("quotes/price", key, Double.class),
                "price",
                DataColumnRole.MEASURE,
                DataStorageType.DOUBLE_ARRAY,
                UnitMetadata.of("USD"),
                MissingPolicy.NAN,
                QualityPolicy.NONE,
                Set.of()
        );
        return new DataSetDescriptor<>(key, List.of(timestamp, symbol, price));
    }

    private record Row(long timestamp, String symbol, double price) {
    }
}
