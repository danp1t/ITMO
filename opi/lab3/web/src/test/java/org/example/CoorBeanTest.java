package org.example;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class CoorBeanTest {

    private CoorBean coorBean;
    private FacesContext facesContext;
    private UIComponent uiComponent;

    @Before
    public void setUp() {
        coorBean = new CoorBean();
        facesContext = null; 
        uiComponent = null; 
    }

    @Test
    public void testValidateX_Valid() {
        coorBean.validateX(facesContext, uiComponent, "2.5"); 
    }

    @Test(expected = ValidatorException.class)
    public void testValidateX_InvalidFormat() {
        coorBean.validateX(facesContext, uiComponent, "abc");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateX_OutOfRange() {
        coorBean.validateX(facesContext, uiComponent, "10");
    }

    @Test
    public void testValidateR_Valid() {
        coorBean.validateR(facesContext, uiComponent, "2");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateR_TooSmall() {
        coorBean.validateR(facesContext, uiComponent, "0.5");
    }

    @Test
    public void testCheckArea_Quarter1() {
        assertFalse(coorBean.checkArea(1, 1, 3)); 
    }

    @Test
    public void testCheckArea_Quarter2_Inside() {
        assertTrue(coorBean.checkArea(-1, 1, 3)); 
    }

    @Test
    public void testCheckArea_Quarter3_Inside() {
        assertTrue(coorBean.checkArea(-1, -1, 2)); 
    }

    @Test
    public void testCheckArea_Quarter4_Inside() {
        assertTrue(coorBean.checkArea(1, -1, 3)); 
    }

    @Test
    public void testSubmitData_Valid() {
        coorBean.setCoorX("1");
        coorBean.setCoorY("2");
        coorBean.setCoorR("3");

        coorBean.submitData();

        assertEquals(1, coorBean.getResults().size());
        ResultBean result = coorBean.getResults().get(0);
        assertEquals(1.0, result.getX(), 0.001);
        assertEquals(2.0, result.getY(), 0.001);
    }

    @Test
    public void testSubmitData_Invalid() {
        coorBean.setCoorX(""); 
        coorBean.setCoorY("2");
        coorBean.setCoorR("3");

        coorBean.submitData();

        assertTrue(coorBean.getResults().isEmpty());
        assertNotNull(coorBean.getErrorMessage());
    }

    @Test
    public void testCoorYOptions() {
        List<String> options = coorBean.getCoorYOptions();
        assertFalse(options.isEmpty());
        assertEquals("-3", options.get(0));
        assertEquals("5", options.get(options.size() - 1));
    }
}