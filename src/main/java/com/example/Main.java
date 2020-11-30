package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
@SpringBootApplication
public class Main {
    private static HashMap<String, List<String>> relatedProducts = new HashMap<>();
    private static HashMap<String, List<String>> userViewHistory = new HashMap<>();

    @RequestMapping("/")
    String index() {
        return "Running";
    }

    public static void main(String[] args) throws Exception {
        Service.service(new Subscription[]{
                new Subscription("recommendation", "fetch-product", (body, sender) -> {
                    System.out.println("recommendation: recommend");
                    System.out.println(body);
                    String[] messageBody = body.split(",");
                    String userid = messageBody[1];
                    String productid = messageBody[2];

                    List<String> userHistory = Main.userViewHistory.getOrDefault(userid, new ArrayList<>());
                    List<String> productHistory = Main.relatedProducts.getOrDefault(productid, new ArrayList<>());

                    if (userHistory.size() > 0) {
                        String previouslyVisitedProduct = userHistory.get(userHistory.size() - 1);
                        productHistory.add(previouslyVisitedProduct);
                        Main.relatedProducts.put(productid, productHistory);
                    }
                    userHistory.add(productid);
                    Main.userViewHistory.put(userid, userHistory);
                    List<String> topThreeProducts = Main.getTopThreeProducts(productid);
                    sender.send("display", "SESSION_ID,recommendation," + String.join(",", topThreeProducts));
                    sender.send("fetch-product", topThreeProducts.get(0));
                    sender.send("fetch-product", topThreeProducts.get(1));
                    sender.send("fetch-product", topThreeProducts.get(2));

                })
        });

        SpringApplication.run(Main.class, args);
    }

    private static List<String> getTopThreeProducts(String product_id) {
        List<String> productHistory = Main.relatedProducts.getOrDefault(product_id, new ArrayList<>());
        if (productHistory.size() <= 3) {
            return productHistory;
        }
        return productHistory.subList(productHistory.size() - 3, productHistory.size());
    }

}
