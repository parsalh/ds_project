package gr.hua.dit.project.web.rest.dto;

import gr.hua.dit.project.core.model.MenuItem;

import java.math.BigDecimal;
import java.util.*;

public class Cart {

    public static class CartItem{
        private final MenuItem menuItem;
        private int quantity;

        public CartItem(MenuItem menuItem, int quantity) {
            this.menuItem = menuItem;
            this.quantity = quantity;
        }

        public MenuItem getMenuItem() {
            return menuItem;
        }

        public int getQuantity() {
            return quantity;
        }

        public void incrementQuantity(){
            this.quantity++;
        }

        public void decrementQuantity(){
            this.quantity--;
        }

        public BigDecimal getPrice(){
            return menuItem.getPrice();
        }

        public BigDecimal getSubtotal(){
            return menuItem.getPrice().multiply(new BigDecimal(quantity));
        }
    }

    private List<CartItem> items = new ArrayList<>();

    public List<CartItem> getItems() {
        return items;
    }

    public void addItem(MenuItem menuItem){
        Optional<CartItem> existing = items.stream()
                .filter(i -> i.getMenuItem().getId().equals(menuItem.getId()))
                .findFirst();

        if (existing.isPresent()){
            existing.get().incrementQuantity();
        } else {
            items.add(new CartItem(menuItem,1));
        }
    }

    public void removeItem(Long itemId){
        items.stream()
                .filter(i -> i.getMenuItem().getId().equals(itemId))
                .findFirst()
                .ifPresent(item -> {
                    item.decrementQuantity();
                    if (item.getQuantity() <= 0){
                        items.remove(item);
                    }
                });
    }

    public int getQuantity(Long itemId){
        return items.stream()
                .filter(i -> i.getMenuItem().getId().equals(itemId))
                .mapToInt(CartItem::getQuantity)
                .findFirst()
                .orElse(0);
    }

    public BigDecimal getTotalPrice(){
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalQuantity(){
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public void clear(){
        items.clear();
    }

}
