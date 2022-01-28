package com.example.weather_rest_clone.repository.impl;

import com.example.weather_rest_clone.exception.CustomInternalServerException;
import com.example.weather_rest_clone.model.entity.User;
import com.example.weather_rest_clone.repository.UserRepository;
import com.example.weather_rest_clone.service.util.HibernateUtil;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRepositoryImpl.class);

    @Override
    public User findByUsernameWithRoles(String username) {
        Transaction transaction = null;

        try (var session = HibernateUtil.openNewSession()) {
            transaction = session.beginTransaction();
            var user = session.createQuery("select ur from User ur left join fetch ur.roles where ur.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
            transaction.commit();

            return user;

        } catch (NoResultException nre) {
            return null;

        } catch (Exception e) {
            HibernateUtil.rollbackTransaction(transaction);

            LOGGER.error("Find User get error: username = {}", username, e);
            throw new CustomInternalServerException();
        }
    }

    @Override
    public void updateJwtTokenForUser(@NonNull User user, @NonNull String jwtToken) {
        Transaction transaction = null;

        try (var session = HibernateUtil.openNewSession()) {
            transaction = session.beginTransaction();
            user.setJwtToken(jwtToken);
            session.update(user);
            transaction.commit();

        } catch (Exception e) {
            HibernateUtil.rollbackTransaction(transaction);

            LOGGER.error("Update JwtToken for user get error: userId = {}", user.getId(), e);
            throw new CustomInternalServerException();
        }
    }
}
