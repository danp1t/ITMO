package org.example;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Named("CoorBean")
@ViewScoped
public class CoorBean implements Serializable {
    private static final long serialVersionUID = 1L;
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



    public void validateX(FacesContext context, UIComponent component, String value) throws ValidatorException {
        if (value != null && !value.isEmpty()) {
            try {
            double x = Double.parseDouble(value);
            if (x < -3 || x > 5) {
                throw new ValidatorException(new FacesMessage("Число должно быть от -3 до 5"));
            }
        }
            catch (NumberFormatException e) {
                throw new ValidatorException(new FacesMessage("Введите число"));
            }
    }
    else {
            throw new ValidatorException(new FacesMessage("Введите число от -3 до 5"));
        }

    }

    public void validateR(FacesContext context, UIComponent component, String value) throws ValidatorException {
        if (value != null && !value.isEmpty()) {
            try {
                double r = Double.parseDouble(value);
                if (r < 1 || r > 4) {
                    throw new ValidatorException(new FacesMessage("Число должно быть от 1 до 4"));
                }
            }
            catch (NumberFormatException e) {
                throw new ValidatorException(new FacesMessage("Введите число"));
            }
        }
        else if (value == null || value.isEmpty()) {
            throw new ValidatorException(new FacesMessage("Введите число от 1 до 4"));
        }

    }
    public void submitData() {
        validateCoordinates();
        // Логика обработки данных
        // Например, сохранение в базе данных или выполнение других действий

    }

    public void validateCoordinates() {
        // Пример валидации
        if (coorX.isEmpty() || coorY.isEmpty() || coorR.isEmpty()) {
            errorMessage = "Все координаты должны быть заполнены.";
        } else {
            errorMessage = ""; // Сброс сообщения об ошибке, если все в порядке
        }
    }

}

