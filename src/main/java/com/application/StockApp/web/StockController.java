package com.application.StockApp.web;

import com.application.StockApp.stock.model.Stock;
import com.application.StockApp.records.model.StockRecord;
import com.application.StockApp.records.repository.StockRecordRepository;
import com.application.StockApp.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StockController {

    private final StockRepository stockRepository;
    private final StockRecordRepository recordRepository;

/*    @GetMapping("/stocks")
    public String index(Model model) {
        List<Stock> allStocks = stockRepository.findAll();
        model.addAttribute("stocks", allStocks);

        // примерни данни за "Top Gainer / Loser"
        if (!allStocks.isEmpty()) {
            model.addAttribute("topGainer", allStocks.get(0).getStockCode());
        }

        return "index";
    }*/

    @GetMapping("/stocks")
    public String stocks(Model model) {
        List<Stock> allStocks = stockRepository.findAll();
        model.addAttribute("stocks", allStocks);
        return "stocks";
    }


    @GetMapping("/stocks/{code}")
    public String stockDetails(@PathVariable("code") String code, Model model) {
        Stock stock = stockRepository.findByStockCodeIgnoreCase(code)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found: " + code));

        List<StockRecord> records = recordRepository.findAllByStock(stock);

        model.addAttribute("stock", stock);
        model.addAttribute("records", records);

        return "stock-details";
    }


}
