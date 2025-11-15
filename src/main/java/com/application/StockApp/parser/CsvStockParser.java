package com.application.StockApp.parser;

import com.application.StockApp.records.model.StockRecord;
import com.application.StockApp.stock.model.Stock;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.application.StockApp.utility.ParseUtils.*;

@Component
@RequiredArgsConstructor
public class CsvStockParser {

    public List<StockRecord> parseCsv(String filePath, Stock stock)
            throws IOException, CsvValidationException {

        List<StockRecord> records = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {

            String[] next;
            boolean header = true;

            while ((next = reader.readNext()) != null) {
                if (header) { header = false; continue; }
                if (next.length == 0 || next[0].isBlank()) continue;

                LocalDate date = LocalDate.parse(next[0]);

                records.add(StockRecord.builder()
                        .stock(stock)
                        .date(date)
                        .open(parseDecimal(next[1]))
                        .high(parseDecimal(next[2]))
                        .low(parseDecimal(next[3]))
                        .close(parseDecimal(next[4]))
                        .adjustedClose(parseDecimal(next[5]))
                        .changePercent(parseDecimal(next[6].replace("%","")))
                        .volume(BigDecimal.valueOf(parseLongSafe(next[7])))
                        .build()
                );
            }
        }

        return records;
    }
}
