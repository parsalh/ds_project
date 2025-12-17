package gr.hua.dit.project.core.service.impl;

import gr.hua.dit.project.core.model.MenuItem;
import gr.hua.dit.project.core.repository.MenuItemRepository;
import gr.hua.dit.project.core.service.MenuItemService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MenuItemServiceImpl implements MenuItemService {


    private final MenuItemRepository menuItemRepository;


    public MenuItemServiceImpl(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
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
    public MenuItem save(MenuItem menuItem) {
        return menuItemRepository.save(menuItem);
    }


    @Override
    public void deleteById(Long id) {
        menuItemRepository.deleteById(id);
    }
}