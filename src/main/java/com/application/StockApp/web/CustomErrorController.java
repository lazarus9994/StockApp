package com.application.StockApp.web;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute("jakarta.servlet.error.status_code");
        Object message = request.getAttribute("jakarta.servlet.error.message");

        model.addAttribute("status", status != null ? status : "Unknown");
        model.addAttribute("error", "Unexpected error");
        model.addAttribute("message", message != null ? message : "Something went wrong.");

        return "error";
    }
}
