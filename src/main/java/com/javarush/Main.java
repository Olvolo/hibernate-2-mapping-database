package com.javarush;

import com.javarush.dao.*;
import com.javarush.domain.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class Main {
    private final SessionFactory sessionFactory;
    private final ActorDAO actorDAO;
    private final AddressDAO addressDAO;
    private final CategoryDAO categoryDAO;
    private final CityDAO cityDAO;
    private final CountryDAO countryDAO;
    private final CustomerDAO customerDAO;
    private final FilmDAO filmDAO;
    private final FilmTextDAO filmTextDAO;
    private final InventoryDAO inventoryDAO;
    private final LanguageDAO languageDAO;
    private final PaymentDAO paymentDAO;
    private final RentalDAO rentalDAO;
    private final StaffDAO staffDAO;
    private final StoreDAO storeDAO;

    public Main() {
        Properties properties = getProperties();
        sessionFactory = new Configuration().
                addAnnotatedClass(Actor.class).
                addAnnotatedClass(Address.class).
                addAnnotatedClass(Category.class).
                addAnnotatedClass(City.class).
                addAnnotatedClass(Country.class).
                addAnnotatedClass(Customer.class).
                addAnnotatedClass(Features.class).
                addAnnotatedClass(Film.class).
                addAnnotatedClass(FilmText.class).
                addAnnotatedClass(Inventory.class).
                addAnnotatedClass(Language.class).
                addAnnotatedClass(Payment.class).
                addAnnotatedClass(Rating.class).
                addAnnotatedClass(Rental.class).
                addAnnotatedClass(Staff.class).
                addAnnotatedClass(Store.class).
                addProperties(properties).
                buildSessionFactory();

        actorDAO = new ActorDAO(sessionFactory);
        addressDAO = new AddressDAO(sessionFactory);
        categoryDAO = new CategoryDAO(sessionFactory);
        cityDAO = new CityDAO(sessionFactory);
        countryDAO = new CountryDAO(sessionFactory);
        customerDAO = new CustomerDAO(sessionFactory);
        filmDAO = new FilmDAO(sessionFactory);
        filmTextDAO = new FilmTextDAO(sessionFactory);
        inventoryDAO = new InventoryDAO(sessionFactory);
        languageDAO = new LanguageDAO(sessionFactory);
        paymentDAO = new PaymentDAO(sessionFactory);
        rentalDAO = new RentalDAO(sessionFactory);
        staffDAO = new StaffDAO(sessionFactory);
        storeDAO = new StoreDAO(sessionFactory);
    }
    private static Properties getProperties() {
        Properties properties = new Properties();
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
        properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/movie");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "root");
        properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
        properties.put(Environment.HBM2DDL_AUTO, "validate");
        return properties;
    }
    public static void main(String[] args) {
        Main main = new Main();
        Customer customer = main.createCustomer();
        main.customerReturnInventoryToStore(customer);
        main.customerRentInventoryToStore(customer);
        main.newFilmWasMade();
    }
    private void customerRentInventoryToStore(Customer customer) {
        try(Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();

            Film film = filmDAO.getAvailableFilmForRent();
            Store store = storeDAO.getItems(0,1).get(0);

            Inventory inventory = new Inventory();
            inventory.setStore(store);
            inventory.setFilm(film);
            inventoryDAO.save(inventory);

            Staff staff = store.getStaff();

            Rental rental = new Rental();
            rental.setRentalDAte(LocalDateTime.now());
            rental.setCustomer(customer);
            rental.setInventory(inventory);
            rental.setStaff(staff);
            rentalDAO.save(rental);

            Payment payment = new Payment();
            payment.setRental(rental);
            payment.setPaymentDate(LocalDateTime.now());
            payment.setCustomer(customer);
            payment.setAmount(BigDecimal.valueOf(55.55));
            payment.setStaff(staff);
            paymentDAO.save(payment);

            session.getTransaction().commit();
        }
    }
    private void newFilmWasMade() {
        try(Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();

            Language language = languageDAO.getItems(0,20).stream().unordered().findAny().get();

            List<Category>categories = categoryDAO.getItems(0, 4);

            List<Actor>actors = actorDAO.getItems(0,10);

            Film film = new Film();
            film.setActors(new HashSet<>(actors));
            film.setRating(Rating.NC17);
            film.setSpecialFeatures(Set.of(Features.TRAILERS, Features.COMMENTARIES));
            film.setLength((short)153);
            film.setReplacementCost(BigDecimal.TEN);
            film.setLanguage(language);
            film.setDescription("Crazy man plus crazy woman - this is enough");
            film.setTitle("Mad Max");
            film.setRentalDuration((byte)48);
            film.setOriginalLanguage(language);
            film.setCategories(new HashSet<>(categories));
            film.setYear(Year.now());
            film.setRentalRate(BigDecimal.valueOf(4.99));
            filmDAO.save(film);

            FilmText filmText = new FilmText();
            filmText.setFilm(film);
            filmText.setId(film.getId());
            filmText.setDescription("Crazy man plus crazy woman - this is enough");
            filmText.setTitle("Mad Max");
            filmTextDAO.save(filmText);
        }
    }

    private void customerReturnInventoryToStore(Customer customer) {
        try (Session session = sessionFactory.getCurrentSession()){
            session.beginTransaction();
            Rental rental = rentalDAO.getAnyUnreturnedRental();
            rental.setReturnDate(LocalDateTime.now());
            rentalDAO.save(rental);
            session.getTransaction().commit();
        }
    }
    private Customer createCustomer() {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();

            Store store = storeDAO.getItems(0, 1).get(0);
            City city = cityDAO.getByName("Ahmadnagar");

            Address address = new Address();
            address.setAddress("Vila Velha Loop, 922");
            address.setPhone("510-737-228-015");
            address.setCity(city);
            address.setDistrict("Maharashtra");
            addressDAO.save(address);

            Customer customer = new Customer();
            customer.setActive(true);
            customer.setEmail("SHERRI.RHODES@sakilacustomer.org");
            customer.setAddress(address);
            customer.setStore(store);
            customer.setFirstName("SHERRI");
            customer.setLastName("RHODES");
            customerDAO.save(customer);

            session.getTransaction().commit();
            return customer;
        }
    }
    public StaffDAO getStaffDAO() {
        return staffDAO;
    }
    public CountryDAO getCountryDAO() {
        return countryDAO;
    }
}