package gr.hua.dit.project.core.service.impl;

import gr.hua.dit.project.core.model.MenuItem;
import gr.hua.dit.project.core.repository.MenuItemRepository;
import gr.hua.dit.project.core.repository.OrderItemRepository;
import gr.hua.dit.project.core.service.MenuItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final OrderItemRepository orderItemRepository;

    public MenuItemServiceImpl(MenuItemRepository menuItemRepository,
                               OrderItemRepository orderItemRepository) {
        this.menuItemRepository = menuItemRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public List<MenuItem> findAll() {
        return menuItemRepository.findAll();
    }

    @Override
    public Optional<MenuItem> findById(Long id) {
        return menuItemRepository.findById(id);
    }

    @Override
    public List<MenuItem> findByRestaurantId(Long restaurantId) {
        return menuItemRepository.findAllByRestaurantId(restaurantId);
    }

    @Override
    @Transactional
    public MenuItem save(MenuItem menuItem) {
        return menuItemRepository.save(menuItem);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (orderItemRepository.existsByMenuItemId(id)) {
            menuItemRepository.findById(id).ifPresent(item -> {
                item.setAvailable(false);
                menuItemRepository.save(item);
            });
        } else {
            menuItemRepository.deleteById(id);
        }
    }
}