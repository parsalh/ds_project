package gr.hua.dit.project.core.model;


import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(
        name = "open_hour",
        indexes = {
                @Index(name = "idx_openhour_restaurant", columnList = "restaurant_id"),
                @Index(name = "idx_openhour_day", columnList ="day_of_week")
        }
)

public class OpenHour {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    public OpenHour() {}

    public OpenHour(
                    Restaurant restaurant,
                    DayOfWeek dayOfWeek,
                    LocalTime openTime,
                    LocalTime closeTime) {

        this.restaurant = restaurant;
        this.dayOfWeek = dayOfWeek;
        this.openTime = openTime;
        this.closeTime = closeTime;

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getOpenTime() {
        return openTime;
    }

    public void setOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }

    @Override
    public String toString() {
        return "OpenHour{" +
                "restaurant=" + restaurant +
                ", dayOfWeek=" + dayOfWeek +
                ", openTime=" + openTime +
                ", closeTime=" + closeTime +
                '}';
    }
}
