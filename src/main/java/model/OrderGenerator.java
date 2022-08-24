package model;

import java.util.ArrayList;
import java.util.List;

public class OrderGenerator {
    public static Order getOrder() {
        List<String> color = new ArrayList<>();
        return new Order(
                "Naruto",
                "Uzumaki",
                 "Konoha, 142 apt.",
                4,
                "+7 800 355 35 35",
                5,
                "2022-08-22",
                "Saske, come back to Konoha",
                color);
    }
}
