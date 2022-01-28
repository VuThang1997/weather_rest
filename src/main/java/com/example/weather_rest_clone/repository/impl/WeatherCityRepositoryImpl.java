package com.example.weather_rest_clone.repository.impl;

import com.example.weather_rest_clone.exception.CustomInternalServerException;
import com.example.weather_rest_clone.model.entity.WeatherCity;
import com.example.weather_rest_clone.model.pojo.paging.PaginationInfo;
import com.example.weather_rest_clone.model.pojo.paging.PaginationResult;
import com.example.weather_rest_clone.model.pojo.paging.PaginationSetting;
import com.example.weather_rest_clone.repository.WeatherCityRepository;
import com.example.weather_rest_clone.service.util.HibernateUtil;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import java.time.LocalDate;
import java.util.List;

@Repository
public class WeatherCityRepositoryImpl implements WeatherCityRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherCityRepositoryImpl.class);

    @Override
    public boolean checkWeatherCityExist(@NonNull String standardizedCityName, @NonNull LocalDate retrieveDate) {
        try (var session = HibernateUtil.openNewSession()) {
            var transaction = session.beginTransaction();
            Integer queryResult =
                    session.createQuery("select 1 from WeatherCity wc where wc.cityName = :cityName and wc.retrieveDate = :retrieveDate", Integer.class)
                            .setParameter("cityName", standardizedCityName)
                            .setParameter("retrieveDate", retrieveDate)
                            .getSingleResult();
            transaction.commit();

            return queryResult != null && queryResult.equals(1);

        } catch (NoResultException e) {
            return false;

        } catch (Exception e) {
            LOGGER.error("Check WeatherCity exist get error: cityName = {}, retrieveDate = {}", standardizedCityName, retrieveDate, e);
            throw new CustomInternalServerException();
        }
    }

    @Override
    public WeatherCity findByCityNameAndRetrieveDate(@NonNull String standardizedCityName, @NonNull LocalDate retrieveDate) {
        Transaction transaction = null;

        try (var session = HibernateUtil.openNewSession()) {
            transaction = session.beginTransaction();
            WeatherCity weatherCity =
                    session.createQuery("select wc from WeatherCity wc where wc.cityName = :cityName and wc.retrieveDate = :retrieveDate", WeatherCity.class)
                            .setParameter("cityName", standardizedCityName)
                            .setParameter("retrieveDate", retrieveDate)
                            .getSingleResult();
            transaction.commit();

            return weatherCity;

        } catch (NoResultException nre) {
            return null;

        } catch (Exception e) {
            HibernateUtil.rollbackTransaction(transaction);

            LOGGER.error("Find WeatherCity get error: cityName = {}, retrieveDate = {}", standardizedCityName, retrieveDate, e);
            throw new CustomInternalServerException();
        }
    }

    @Override
    public PaginationResult<WeatherCity> findByPeriod(@NonNull LocalDate startDate, @NonNull LocalDate endDate, @NonNull PaginationSetting setting) {
        int pageSize = setting.getPageSize();
        int pageIndex = setting.getPageIndex();
        Transaction transaction = null;

        try (var session = HibernateUtil.openNewSession()) {
            transaction = session.beginTransaction();
            Query<WeatherCity> query = session.createQuery("from WeatherCity wc where wc.retrieveDate between :startDate and :endDate order by wc.retrieveDate asc", WeatherCity.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            query.setFirstResult(setting.getFirstResultIndex());
            query.setMaxResults(pageSize);
            List<WeatherCity> weatherCities = query.list();

            Query<Long> countQuery = session.createQuery(
                    "select count(wc.id) from WeatherCity wc where wc.retrieveDate between :startDate and :endDate", Long.class);
            countQuery.setParameter("startDate", startDate);
            countQuery.setParameter("endDate", endDate);
            long totalCount = countQuery.uniqueResult();
            transaction.commit();

            int totalPage = (int) Math.ceil((float) totalCount / pageSize);

            var paginationInfo = new PaginationInfo(pageIndex, pageSize, (int) totalCount, totalPage);
            return new PaginationResult<>(paginationInfo, weatherCities);

        } catch (Exception e) {
            HibernateUtil.rollbackTransaction(transaction);

            LOGGER.error("Find WeatherCity by period get error: startDate = {}, endDate = {}", startDate, endDate, e);
            throw new CustomInternalServerException();
        }
    }

    @Override
    public int saveNewWeatherCity(@NonNull WeatherCity weatherCity) {
        Transaction transaction = null;

        try (var session = HibernateUtil.openNewSession()) {
            transaction = session.beginTransaction();
            int weatherCityId = (Integer) session.save(weatherCity);
            transaction.commit();

            return weatherCityId;

        } catch (Exception e) {
            HibernateUtil.rollbackTransaction(transaction);

            LOGGER.error("saveNewWeatherCity get error: cityName = {}, retrieveDate = {}", weatherCity.getCityName(), weatherCity.getRetrieveDate(), e);
            throw new CustomInternalServerException();
        }
    }

    @Override
    public void deleteWeatherCity(@NonNull String standardizedCityName, @NonNull LocalDate retrieveDate) {
        Transaction transaction = null;

        try (var session = HibernateUtil.openNewSession()) {
            transaction = session.beginTransaction();

            session.createQuery("delete from WeatherCity wc where wc.cityName = :cityName and wc.retrieveDate = :retrieveDate")
                    .setParameter("cityName", standardizedCityName)
                    .setParameter("retrieveDate", retrieveDate)
                    .executeUpdate();

            transaction.commit();

        } catch (Exception e) {
            HibernateUtil.rollbackTransaction(transaction);

            LOGGER.error("Delete WeatherCity get error: cityName = {}, retrieveDate = {}", standardizedCityName, retrieveDate, e);
            throw new CustomInternalServerException();
        }
    }

    @Override
    public void updateExistingWeatherCity(@NonNull WeatherCity weatherCity, @NonNull String weatherDataJson) {
        Transaction transaction = null;

        try (var session = HibernateUtil.openNewSession()) {
            transaction = session.beginTransaction();
            weatherCity.setWeatherDataJson(weatherDataJson);
            session.update(weatherCity);
            transaction.commit();

        } catch (Exception e) {
            HibernateUtil.rollbackTransaction(transaction);

            LOGGER.error("Update WeatherCity get error: cityName = {}, retrieveDate = {}", weatherCity.getCityName(), weatherCity.getRetrieveDate(), e);
            throw new CustomInternalServerException();
        }
    }
}
