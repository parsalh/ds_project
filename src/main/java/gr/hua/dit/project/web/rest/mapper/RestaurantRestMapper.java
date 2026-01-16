package gr.hua.dit.project.web.rest.mapper;

import gr.hua.dit.project.core.model.MenuItem;
import gr.hua.dit.project.core.model.Restaurant;
import gr.hua.dit.project.web.rest.dto.OpenHourDTO;
import gr.hua.dit.project.web.rest.dto.MenuItemDTO;
import gr.hua.dit.project.web.rest.dto.RestaurantDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RestaurantRestMapper {

    public RestaurantDTO toDTO(Restaurant restaurant) {
        if (restaurant == null) return null;

        RestaurantDTO dto =  new RestaurantDTO();
        dto.setId(restaurant.getId());
        dto.setName(restaurant.getName());
        dto.setAddressInfo(restaurant.getAddressInfo());
        dto.setCuisines(restaurant.getCuisines());
        dto.setMinimumOrderAmount(restaurant.getMinimumOrderAmount());
        dto.setDeliveryFee(restaurant.getDeliveryFee());
        dto.setServiceType(restaurant.getServiceType());
        dto.setImageUrl(restaurant.getImageUrl());
        dto.setOpen(restaurant.isOpen());

        if (restaurant.getOwner() != null) {
            dto.setOwnerId(restaurant.getOwner().getId());
        }

        if (restaurant.getMenu() != null) {
            List<String> menuNames = restaurant.getMenu().stream()
                    .map(MenuItem::getName)
                    .collect(Collectors.toList());
            dto.setMenu(menuNames);
        } else {
            dto.setMenu(Collections.emptyList());
        }

        if (restaurant.getOpenHours() != null) {
            List<OpenHourDTO> openHourDTOS = restaurant.getOpenHours().stream()
                    .map(oh -> {
                        OpenHourDTO ohDTO = new OpenHourDTO();
                        ohDTO.setId(oh.getId());
                        ohDTO.setDayOfWeek(oh.getDayOfWeek());
                        ohDTO.setOpenTime(oh.getOpenTime());
                        ohDTO.setCloseTime(oh.getCloseTime());
                        return ohDTO;
                    })
                    .collect(Collectors.toList());
            dto.setOpenHours(openHourDTOS);
        }

        return dto;
    }

    public MenuItemDTO toDTO(MenuItem item) {
        if (item == null) {
            return null;
        }

        MenuItemDTO dto = new MenuItemDTO();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setPrice(item.getPrice());
        dto.setAvailable(item.getAvailable());
        dto.setDescription(item.getDescription());
        dto.setItemType(item.getType());
        dto.setImageUrl(item.getImageUrl());

        if (item.getRestaurant() != null) {
            dto.setRestaurantId(item.getRestaurant().getId());
        }

        return dto;
    }

}
