package org.example;

import org.junit.Before;
import org.junit.Test;
import jakarta.persistence.*;
import java.lang.reflect.Field;
import java.io.*;

import static org.junit.Assert.*;

public class ResultBeanTest {

    private ResultBean resultBean;

    @Before
    public void setUp() {
        resultBean = new ResultBean(1.5, 2.5, 3.0, true);
    }

    @Test
    public void testConstructorWithArgs() {
        assertEquals(1.5, resultBean.getX(), 0.001);
        assertEquals(2.5, resultBean.getY(), 0.001);
        assertEquals(3.0, resultBean.getR(), 0.001);
        assertTrue(resultBean.isStatus());
    }

    @Test
    public void testDefaultConstructor() {
        ResultBean emptyBean = new ResultBean();
        assertNotNull(emptyBean);
    }

    @Test
    public void testIdGetterSetter() {
        resultBean.setId(10L);
        assertEquals(Long.valueOf(10), resultBean.getId());
    }

    @Test
    public void testXGetterSetter() {
        resultBean.setX(5.5);
        assertEquals(5.5, resultBean.getX(), 0.001);
    }

    @Test
    public void testStatusGetterSetter() {
        resultBean.setStatus(false);
        assertFalse(resultBean.isStatus());
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(resultBean);
        out.close();

        ObjectInputStream in = new ObjectInputStream(
            new ByteArrayInputStream(bos.toByteArray()));
        ResultBean deserialized = (ResultBean) in.readObject();

        assertEquals(resultBean.getX(), deserialized.getX(), 0.001);
        assertEquals(resultBean.getY(), deserialized.getY(), 0.001);
        assertEquals(resultBean.getR(), deserialized.getR(), 0.001);
        assertEquals(resultBean.isStatus(), deserialized.isStatus());
    }

    @Test
    public void testEntityAnnotation() {
        assertTrue(resultBean.getClass().isAnnotationPresent(Entity.class));
    }

    @Test
    public void testTableAnnotation() {
        Table table = resultBean.getClass().getAnnotation(Table.class);
        assertNotNull(table);
        assertEquals("results", table.name());
    }

    @Test
    public void testIdAnnotation() throws NoSuchFieldException {
        Field idField = resultBean.getClass().getDeclaredField("id");
        assertTrue(idField.isAnnotationPresent(Id.class));
        assertTrue(idField.isAnnotationPresent(GeneratedValue.class));
    }   
}