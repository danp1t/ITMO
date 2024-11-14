package org.example;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

import java.util.Arrays;
import java.util.List;

@Named("CoorBean")
@RequestScoped
public class CoorBean {
    private String coorX = "";
    private String coorY = "" ;
    private String coorR = "";
    private String errorMessage;
    private List<String> coorYOptions = Arrays.asList("-3", "-2", "-1", "0", "1", "2", "3", "4", "5");

    // Getters и Setters

    public String getCoorX() {
        return coorX;
    }

    public void setCoorX(String coorX) {
        this.coorX = coorX;
    }

    public String getCoorY() {
        return coorY;
    }

    public void setCoorY(String coorY) {
        this.coorY = coorY;
    }

    public String getCoorR() {
        return coorR;
    }

    public void setCoorR(String coorR) {
        this.coorR = coorR;
    }

    public void selectCoorY(String value) {
        this.coorY = value;
        // Здесь вы можете добавить любую логику, которая должна выполняться при выборе значения
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public List<String> getCoorYOptions() {
        return coorYOptions;
    }

    public void submit() {
        // Ваша логика обработки данных формы
        if (validateInputs()) {
            // Обработка успешной отправки
            errorMessage = null; // Сброс ошибки
        } else {
            errorMessage = "Пожалуйста, исправьте ошибки."; // Установка сообщения об ошибке
        }
    }

    private boolean validateInputs() {
        // Ваша логика валидации
        return true; // или false в зависимости от проверки
    }
}
